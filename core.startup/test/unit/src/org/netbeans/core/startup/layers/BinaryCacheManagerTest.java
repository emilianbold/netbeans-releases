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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
/** Test layer cache manager.
 * @author Jesse Glick
 * @see "#20628"
 */
public class BinaryCacheManagerTest extends CacheManagerTestBaseHid 
implements CacheManagerTestBaseHid.ManagerFactory {
    
    public BinaryCacheManagerTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        if (System.getProperty("nbjunit.workdir") == null) {
            // Hope java.io.tmpdir is set...
            System.setProperty("nbjunit.workdir", System.getProperty("java.io.tmpdir"));
        }
        System.setProperty("org.openide.util.Lookup", "-");
        System.setProperty("org.netbeans.core.projects.cache", "0");
        TestRunner.run(new NbTestSuite(BinaryCacheManagerTest.class));
    }

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    //
    // Manager factory methods
    //
    public LayerCacheManager createManager() throws Exception {
        return new BinaryCacheManager(getWorkDir());
    }

    public boolean supportsTimestamps () {
        return true;
    }
    
    
    //
    // new test methods
    //
    
    public void testFastReplacement() throws Exception {
        clearWorkDir();
        LayerCacheManager m = new BinaryCacheManager(getWorkDir());
        assertFalse(m.cacheExists());
        // layer2.xml should override layer1.xml where necessary:
        List urls = new ArrayList(Arrays.asList(new URL[] {
            BinaryCacheManagerTest.class.getResource("data/layer2.xml"),
            BinaryCacheManagerTest.class.getResource("data/layer1.xml"),
        }));
        FileSystem f = m.store(urls);
        assertTrue(m.cacheExists());
        FixedFileSystem base = new FixedFileSystem("ffs", "FFS");
        base.add("baz/thongy", new FixedFileSystem.Instance(false, null, null, null, (URL)null));
        final MFS mfs = new MFS(new FileSystem[] {base, f});
        FileObject baz = mfs.findResource("baz");
        assertNotNull(baz);
        assertEquals(2, baz.getChildren().length);
        FileObject thingy = mfs.findResource("baz/thingy");
        assertNotNull(thingy);
        L l = new L();
        baz.addFileChangeListener(l);
        //L l2 = new L();mfs.addFileChangeListener(l2);
        urls.remove(0);
        f = m.store(urls);
        final FileSystem[] fss = new FileSystem[] {base, f};
        mfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() {
                mfs._setDelegates(fss);
            }
        });
        assertEquals(2, baz.getChildren().length);
        assertTrue(thingy.isValid());
        assertEquals(0, l.ac);
        assertEquals(0, l.c);
        assertEquals(0, l.dc);
        assertEquals(0, l.d);
        assertEquals(0, l.fc);
        assertEquals(0, l.r);
        urls.remove(0);
        f = m.store(urls);
        final FileSystem[] fss2 = new FileSystem[] {base, f};
        mfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() {
                mfs._setDelegates(fss2);
            }
        });
        assertEquals(1, baz.getChildren().length);
        assertFalse(thingy.isValid());
        assertEquals(0, l.ac);
        assertEquals(0, l.c);
        assertEquals(0, l.dc);
        assertEquals(1, l.d);
        assertEquals(0, l.fc);
        assertEquals(0, l.r);
    }
    
    // Make setDelegates public:
    private static final class MFS extends MultiFileSystem {
        public MFS(FileSystem[] fss) {
            super(fss);
        }
        public void _setDelegates(FileSystem[] fss) {
            setDelegates(fss);
        }
    }
        
    private static final class L implements FileChangeListener {
        public int ac = 0, c = 0, dc = 0, d = 0, fc = 0, r = 0;
        public void fileAttributeChanged(FileAttributeEvent fe) {
            System.err.println("ac: " + fe.getFile().getPath());
            ac++;
        }
        public void fileChanged(FileEvent fe) {
            System.err.println("c: " + fe.getFile().getPath());
            c++;
        }
        public void fileDataCreated(FileEvent fe) {
            System.err.println("dc: " + fe.getFile().getPath());
            dc++;
        }
        public void fileDeleted(FileEvent fe) {
            System.err.println("d: " + fe.getFile().getPath());
            d++;
        }
        public void fileFolderCreated(FileEvent fe) {
            System.err.println("fc: " + fe.getFile().getPath());
            fc++;
        }
        public void fileRenamed(FileRenameEvent fe) {
            System.err.println("r: " + fe.getFile().getPath());
            r++;
        }
    }
    
}
