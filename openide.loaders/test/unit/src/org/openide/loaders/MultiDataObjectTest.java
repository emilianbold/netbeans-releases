/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.extensions.*;
import junit.textui.TestRunner;

import org.openide.filesystems.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.IOException;
import org.openide.nodes.Node;

/**
 * @author  Vitezslav Stejskal
 */
public class MultiDataObjectTest extends NbTestCase {

    /** Creates new DataObjectTest */
    public MultiDataObjectTest (String name) {
        super (name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new SetUpTearDownOnlyOnce ();
    }

    /** This is a test of calling files (), secondaryEntries (), etc. from 
     * constructor of MultiDataObject. It always worked, but because of one strange
     * bug in DataObjectPool.waitNotify could stop for 500ms. Thus we are not 
     * just testing whether it works, but also whether it is fast. Wild but true.
     */
    public void testCallSecondaryEntriesInConstructor () throws Exception {
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/file.simple",
        });

        class D extends MultiDataObject {
            public D (FileObject pf, MultiFileLoader loader) throws IOException {
                super(pf, loader);

                long time = System.currentTimeMillis ();
                
                java.util.Iterator it = secondaryEntries ().iterator ();
                while (it.hasNext ()) {
                    Object o = it.next ();
                }
                
                long delta = System.currentTimeMillis () - time;
                if (delta > 200) {
                    fail ("It seems that calling secondaryEntries () from data objects constructor is really time consuming. It has taken " + delta + " ms");
                }
            }
            
            protected Node createNodeDelegate() {
                return Node.EMPTY;
            }
        }
    
        class L extends MultiFileLoader {
            public L () {
                super(D.class.getName());
            }
            protected String displayName() {
                return "L";
            }
            
            protected FileObject findPrimaryFile (FileObject obj) {
                return obj.hasExt ("simple") ? obj : null;
            }
            
            protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
                return new D(pf, this);
            }
            
            
            protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject x, FileObject obj) {
                throw new IllegalStateException ();
            }
            
            protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject x, FileObject obj) {
                return new org.openide.loaders.FileEntry (x, obj);
            }
        }
        
        
        
        FileObject fo = lfs.findResource("folder/file.simple");
        assertNotNull(fo);
        DataLoader l = new L ();
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            DataObject o = DataObject.find(fo);
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    } // end of testCallSecondaryEntriesInConstructor
    
    /** test for bugfix #14222, #15391, files copied by MDO.copy operation
     * should be listed as MDO entries imediately after the copy is finished
     */
    public void testEntriesConsistencyAfterCopy () throws Exception {
        String fsstruct [] = new String [] {
            "source/file.primary",
            "source/file.secondary",
            "target/",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        
        FileObject fsrc = lfs.findResource ("source/file.primary");
        FileObject fsrc_s = lfs.findResource ("source/file.secondary");
        DataObject dsrc = DataObject.find (fsrc);

        assertTrue ("Can't create source DataObject.", dsrc != null);
        assertEquals("Correct data loader", DataLoader.getLoader(MultiFileLoaderHid.class), dsrc.getLoader());
        checkEntries ((MultiDataObject)dsrc, fsrc, new FileObject [] { fsrc_s });

        FileObject folder = lfs.findResource ("target");
        DataFolder trg_folder = DataFolder.findFolder (folder);
        
        assertTrue ("Can't create target DataFolder.", dsrc != null);
        
        DataObject dtrg = dsrc.copy (trg_folder);
        FileObject ftrg = lfs.findResource ("target/file.primary");
        FileObject ftrg_s = lfs.findResource ("target/file.secondary");

        checkEntries ((MultiDataObject)dtrg, ftrg, new FileObject [] { ftrg_s });
    }
    
    private void checkEntries (MultiDataObject mdo, FileObject primary, FileObject secondary []) {
        assertEquals ("Primary entry doesn't match.", mdo.getPrimaryEntry ().getFile (), primary);

        for (int i = 0; i < secondary.length; i++) {
            if (null == mdo.findSecondaryEntry (secondary [i])) {
                fail ("There is no entry for file " + secondary [i]);
            }
        }
    }
    
    private static class SetUpTearDownOnlyOnce extends NbTestSetup {
        private final MultiFileLoaderHid loader = new MultiFileLoaderHid ();

        public SetUpTearDownOnlyOnce () {
            super (new NbTestSuite (MultiDataObjectTest.class));
        }

        /**
         * Sets up the fixture. Override to set up additional fixture
         * state. Executed just once for all tests in outer class. It's the
         * behavior of TestSetup decorator.
         */
        protected void setUp() throws Exception {
            // add loader to loader pool before all tests are executed
            AddLoaderManuallyHid.addRemoveLoader (loader, true);
        }
        /**
         * Tears down the fixture. Override to tear down the additional
         * fixture state. Executed just once for all tests in outer class. It's the
         * behavior of TestSetup decorator.
         */
        protected void tearDown() throws Exception {
            // remove loader from loader pool after all tests are executed
            AddLoaderManuallyHid.addRemoveLoader (loader, false);
        }
    }
}
