/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.startup.layers;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
/** Test layer cache managers generally.
 * @author Jesse Glick
 */
public abstract class CacheManagerTestBaseHid extends NbTestCase implements ImageObserver {
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
    public static Object mapImage(Map map) {
        return map.get("image");
    }
    public static Object mapDisplayName(Map map) {
        return map.get("displayName");
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
        // layer2.xml should override layer1.xml where necessary:
        List<URL> urls = Arrays.asList(
            CacheManagerTestBaseHid.class.getResource("data/layer2.xml"),
            CacheManagerTestBaseHid.class.getResource("data/layer1.xml"));
        FileSystem f = BinaryCacheManagerTest.store(m, urls);
        // Initial run.
        checkStruct(f);
        if (mf.supportsTimestamps ()) {
            checkLastModified (f, "foo/test2", "data/test2a");
            checkLastModified (f, "bar/test5", "data/layer2.xml");
            checkLastModified (f, "baz/thingy", "data/layer1.xml");
            checkLastModified (f, "foo/test1", "data/layer1.xml");
            checkLastModified (f, "bug39210/inline.txt", "data/layer1.xml");
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
        FileSystem ffs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(ffs.getRoot(), "foo/29356").setAttribute("x", "val");
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {f, ffs});
        assertEquals("val", attr(ffs, "foo/29356", "x"));
        assertEquals("val", attr(mfs, "foo/29356", "x"));
        assertEquals("val/a", attr(mfs, "foo/29356", "a"));
        assertEquals("val", attr(mfs, "foo/29356", "map1"));
        assertEquals("val/map2", attr(mfs, "foo/29356", "map2"));
        assertEquals("Ahoj", attr(mfs, "foo/29356", "mapDisplayName"));

        FileObject annot = f.findResource("foo/29356");
        String annotName = SystemFileSystem.annotateName(annot);
        assertEquals("Ahoj", annotName);

        Image img = SystemFileSystem.annotateIcon(annot, BeanInfo.ICON_COLOR_16x16);
        assertNotNull("Icon provided", img);
        assertEquals("height", 16, img.getHeight(this));
        assertEquals("width", 16, img.getHeight(this));
        Image img32 = SystemFileSystem.annotateIcon(annot, BeanInfo.ICON_COLOR_32x32);
        assertNotNull("Icon 32 provided", img32);
        assertEquals("height", 32, img32.getHeight(this));
        assertEquals("width", 32, img32.getHeight(this));
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return true;
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
