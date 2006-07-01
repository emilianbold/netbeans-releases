/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
/** Test layer cache managers generally.
 * @author Jesse Glick
 */
public abstract class CacheManagerTestBaseHid extends NbTestCase {
    private long initTime = System.currentTimeMillis ();
    
    /**
     * Called from layer, do not rename!
     */
    public static Object method(FileObject fo, String attr) {
        //System.err.println("CMTBH.m: fo=" + fo.getClass().getName() + "<" + fo.getPath() + ">; attr=" + attr + "; x=" + fo.getAttribute("x"));
        return String.valueOf(fo.getAttribute("x")) + "/" + attr;
    }
    
    public static Object map1(Map map) {
        return String.valueOf(map.get("x"));
    }
    public static Object map2(Map map, String attr) {
        return String.valueOf(map.get("x")) + "/" + attr;
    }
    
    protected CacheManagerTestBaseHid(String name) {
        super(name);
    }
    
    protected static interface ManagerFactory {
        LayerCacheManager createManager() throws Exception;
        boolean supportsTimestamps ();
    }
    
    public void testCacheManager() throws Exception {
        ManagerFactory mf = (ManagerFactory)this;
        
        clearWorkDir();
        LayerCacheManager m = mf.createManager();
        assertFalse(m.cacheExists());
        // layer2.xml should override layer1.xml where necessary:
        List urls = Arrays.asList(new URL[] {
            CacheManagerTestBaseHid.class.getResource("data/layer2.xml"),
            CacheManagerTestBaseHid.class.getResource("data/layer1.xml"),
        });
        FileSystem f;
        if (m.supportsLoad()) {
            f = m.createEmptyFileSystem();
            assertEquals(Collections.EMPTY_LIST, Arrays.asList(f.getRoot().getChildren()));
            m.store(f, urls);
        } else {
            f = m.store(urls);
        }
        // Initial run.
        checkStruct(f);
        if (mf.supportsTimestamps ()) {
            checkLastModified (f, "foo/test2", "data/test2a");
            checkLastModified (f, "bar/test5", "data/layer2.xml");
            checkLastModified (f, "baz/thingy", "data/layer1.xml");
            checkLastModified (f, "foo/test1", "data/layer1.xml");
            checkLastModified (f, "bug39210/inline.txt", "data/layer1.xml");
        }
        if (m.cacheExists()) {
            // Now check the persistence.
            m = mf.createManager();
            f = m.createLoadedFileSystem();
            checkStruct(f);
            if (m.supportsLoad()) {
                // Also check load operation.
                f = m.createEmptyFileSystem();
                assertEquals(Collections.EMPTY_LIST, Arrays.asList(f.getRoot().getChildren()));
                m.load(f);
                checkStruct(f);
            }
        }
    }
    
    private void checkLastModified (FileSystem f, String file, String resource) throws Exception {
        FileObject obj = f.findResource (file);
        assertNotNull (file + " found", obj);
        
        long time = obj.lastModified ().getTime ();
        URL url = CacheManagerTestBaseHid.class.getResource(resource);
        long resourceTime = url.openConnection ().getLastModified ();
        
        if (initTime < resourceTime) {
            fail ("The time of the resource " + file + " (" + resourceTime + ") is likely older than iniciation of this class (" + initTime + ")");
        }

        assertEquals ("Time of " + file + " is the same as URL time of " + resource, resourceTime, time);
        
        
    }
    
    private void checkStruct(FileSystem f) throws Exception {
        assertEquals("Root has 5 children", 5, f.getRoot().getChildren().length);
        assertEquals(1, f.findResource("bar").getChildren().length);
        assertEquals("", slurp(f, "bar/test5"));
        assertEquals(5, f.findResource("foo").getChildren().length);
        // XXX not clear if this is in fact supposed to be empty instead:
        //assertEquals("lala", slurp(f, "foo/test1"));
        assertEquals("two", attr(f, "foo/test1", "y"));
        assertEquals("rara!", slurp(f, "foo/test2"));
        assertEquals("hi!", slurp(f, "foo/test3"));
        assertEquals("three", attr(f, "foo/test3", "x"));
        assertEquals("one too", attr(f, "foo/test3", "y"));
        assertEquals("", slurp(f, "foo/test4"));
        // #29356: methodvalue should pass in MultiFileObject, not the original FileObject:
        FixedFileSystem ffs = new FixedFileSystem("ffs", "FFS");
        FixedFileSystem.Instance i = new FixedFileSystem.Instance(false, null, null, null, (URL)null);
        i.writeAttribute("x", "val");
        ffs.add("foo/29356", i);
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {f, ffs});
        assertEquals("val", attr(ffs, "foo/29356", "x"));
        assertEquals("val", attr(mfs, "foo/29356", "x"));
        assertEquals("val/a", attr(mfs, "foo/29356", "a"));
        assertEquals("val", attr(mfs, "foo/29356", "map1"));
        assertEquals("val/map2", attr(mfs, "foo/29356", "map2"));
    }
    
    private static String slurp(FileSystem f, String path) throws IOException {
        FileObject fo = f.findResource(path);
        if (fo == null) return null;
        InputStream is = fo.getInputStream();
        StringBuffer text = new StringBuffer((int)fo.getSize());
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            text.append(new String(buf, 0, read, "US-ASCII"));
        }
        return text.toString();
    }
    
    private static Object attr(FileSystem f, String path, String a) throws IOException {
        FileObject fo = f.findResource(path);
        if (fo == null) return null;
        return fo.getAttribute(a);
    }
    
}
