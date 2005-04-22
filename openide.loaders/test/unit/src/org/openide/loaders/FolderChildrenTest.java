/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.lang.ref.WeakReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.*;

import org.netbeans.junit.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;


public class FolderChildrenTest extends NbTestCase {
    public FolderChildrenTest() {
        super("");
    }
    
    public FolderChildrenTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(FolderChildrenTest.class));
    }
    
    private static void setSystemProp(String key, String value) {
        java.util.Properties prop = System.getProperties();
        if (prop.get(key) != null) return;
        prop.put(key, value);
    }
    
    private void setupSystemProperties() throws IOException {
//        clearWorkDir();
        setSystemProp("netbeans.security.nocheck","true");
    }
    
    public void testSimulateADeadlockThatWillBeFixedByIssue49459 () throws Exception {
        setupSystemProperties();
        
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileObject a = FileUtil.createData (fs.getRoot (), "XYZ49459/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileObject bb = fs.findResource("/XYZ49459");
        assertNotNull (bb);
        
        class Run implements Runnable {
            private boolean read;
            private DataFolder folder;
            
            public Node[] children;
            
            public Run (DataFolder folder) {
                this.folder = folder;
            }
            
            public void run () {
                if (!read) {
                    read = true;
                    Children.MUTEX.readAccess (this);
                    return;
                }
        
                
                // this will deadlock without fix #49459
                children = folder.getNodeDelegate ().getChildren ().getNodes (true);
                
            }
        }
        
        Run r = new Run (DataFolder.findFolder (bb));
        Children.MUTEX.writeAccess (r);
        
        assertNotNull ("Children filled", r.children);
        assertEquals ("But are empty as cannot wait under getNodes", 0, r.children.length);
        
        // try once more without the locks
        r.children = null;
        r.run ();
        assertNotNull ("But running without mutexs works better - children filled", r.children);
        assertEquals ("One child", 1, r.children.length);
        DataObject obj = (DataObject)r.children[0].getCookie (DataObject.class);
        assertNotNull ("There is data object", obj);
        assertEquals ("It belongs to our file", a, obj.getPrimaryFile ());
    }
    
    public void testAdditionOfNewFileDoesNotInfluenceAlreadyExistingLoaders () 
    throws Exception {
        setupSystemProperties();
        
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileUtil.createData (fs.getRoot (), "AA/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileUtil.createData (fs.getRoot (), "AA/org-openide-loaders-FolderChildrenTest$N2.instance");
        
        FileObject bb = fs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (bb);
        Node node = folder.getNodeDelegate();
        
        Node[] arr = node.getChildren ().getNodes (true);
        assertEquals ("There is a nodes for both", 2, arr.length);
        assertNotNull ("First one is our node", arr[0].getCookie (N1.class));
        
        FileObject n = bb.createData ("A.txt");
        Node[] newarr = node.getChildren ().getNodes (true);
        assertEquals ("There is new node", 3, newarr.length);
        
        n.delete ();
        
        Node[] last = node.getChildren ().getNodes (true);
        assertEquals ("Again they are two", 2, arr.length);
        
        assertTrue ("First one is the same", last[0] == arr[0]);
        assertTrue ("Second one is the same", last[1] == arr[1]);
        
    }
    
    public void testChangeableDataFilter() throws Exception {
        setupSystemProperties();
        
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileUtil.createData (fs.getRoot (), "BB/A.txt");
        FileUtil.createData (fs.getRoot (), "BB/B.txt");
        FileUtil.createData (fs.getRoot (), "BB/AA.txt");
        FileUtil.createData (fs.getRoot (), "BB/BA.txt");
        
        
        FileObject bb = fs.findResource("/BB");
        
        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);
        
        Children ch = folder.createNodeChildren( filter );        
        Node[] arr = ch.getNodes (true);
        
        assertNodes( arr, new String[] { "A.txt", "AA.txt" } );
        filter.fire();
        arr = ch.getNodes (true);        
        assertNodes( arr, new String[] { "B.txt", "BA.txt" } );
        
    }
    
    public void testChildrenCanGC () {
        Filter filter = new Filter ();
        
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileObject bb = fs.findResource("/BB");
        DataFolder folder = DataFolder.findFolder (bb);
           
        Children ch = folder.createNodeChildren( filter );        
        Node[] arr = ch.getNodes (true);
        
        WeakReference ref = new WeakReference (ch);
        ch = null;
        arr = null;
        
        assertGC ("Children can disappear even we hold the filter", ref);
    }

    
    public static class N1 extends org.openide.nodes.AbstractNode 
    implements Node.Cookie {
        public N1 () {
            this (true);
        }
        
        private N1 (boolean doGc) {
            super (org.openide.nodes.Children.LEAF);
            
            if (doGc) {
                for (int i = 0; i < 5; i++) {
                    System.gc ();
                }
            }
        }
        
        public Node cloneNode () {
            return new N1 (false);
        }
        
        public Node.Cookie getCookie (Class c) {
            if (c == getClass ()) {
                return this;
            }
            return null;
        }
    }
    
    public static final class N2 extends N1 {
    }

    
    private void assertNodes( Node[] nodes, String names[] ) {
        
        assertEquals( "Wrong number of nodes.", names.length, nodes.length );
        
        for( int i = 0; i < nodes.length; i++ ) {            
            assertEquals( "Wrong name at index " + i + ".", names[i], nodes[i].getName() );
        }
        
    }
    
    private static class Filter implements ChangeableDataFilter  {

        private boolean selectA = true;
                    
        ArrayList listeners = new ArrayList();
        
        public boolean acceptDataObject (DataObject obj) {
            String fileName = obj.getPrimaryFile().getName();
            boolean select = fileName.startsWith( "A" );            
            select = selectA ? select : !select;
            return select;
        }
        
        public void addChangeListener( ChangeListener listener ) {
            listeners.add( listener );
        }
        
        public void removeChangeListener( ChangeListener listener ) {
            listeners.remove( listener );
        }
        
        public void fire( ) {
        
            selectA = !selectA;
            
            ChangeEvent che = new ChangeEvent( this );
            
            for( Iterator it = listeners.iterator(); it.hasNext(); ) {
                ChangeListener chl = (ChangeListener)it.next();
                chl.stateChanged( che );
            }
        }
        
    }
    
    public void testChildrenListenToFilesystemByABadea () throws Exception {
        doChildrenListenToFilesystem (false);
    }
    public void testChildrenListenToFileByABadea () throws Exception {
        doChildrenListenToFilesystem (true);
    }
        
    private void doChildrenListenToFilesystem (boolean useFileObject) throws Exception {
        
        final Object waitObj = new Object();
        
        class MyFileChangeListener implements FileChangeListener {
            boolean created;
            
            public void fileFolderCreated(FileEvent fe) {}
            public void fileChanged(FileEvent fe) {}
            public void fileDeleted(FileEvent fe) {}
            public void fileRenamed(FileRenameEvent fe) {}
            public void fileAttributeChanged(FileAttributeEvent fe) {}
            public void fileDataCreated(FileEvent e) {
                synchronized (waitObj) {
                    created = true;
                    waitObj.notify();
                }
            }
        }
        
        final String FILE_NAME = "C.txt";
        
        MyFileChangeListener fcl = new MyFileChangeListener();
        
        
        
        setupSystemProperties();
        clearWorkDir();
        
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        Repository.getDefault().addFileSystem(fs);
        final FileObject workDir = FileUtil.createFolder (fs.getRoot(), "workFolder");
        final FileObject sibling = FileUtil.createFolder (fs.getRoot (), "unimportantSibling");
        
        workDir.addFileChangeListener(fcl);
        
        DataFolder workDirDo = DataFolder.findFolder(workDir);
        FolderChildren fc = new FolderChildren(workDirDo);
        
        // init the FolderChildren
        fc.getNodes();
        
        File newFile;
        
        if (useFileObject) {
            FileObject newFo = FileUtil.createData (workDir, FILE_NAME);
            newFile = FileUtil.toFile(newFo);
        } else {
            newFile = new File(FileUtil.toFile(workDir), FILE_NAME);
            new FileOutputStream(newFile).close();
        }
        
        // first or second run (second run is after caling workDir.refresh())
        boolean firstRun = true;
        
        synchronized (waitObj) {
            
            for(;;) {
                // wait for create notification
                if (!fcl.created)
                    waitObj.wait(5000);

                if (!fcl.created) {
                    System.out.println("Not received file create notification, can't test.");
                    if (firstRun) {
                        // didn't get a notification, we should get one by calling refresh()
                        firstRun = false;
                        workDir.refresh();
                        continue;
                    }
                    else {
                        // didn't get a notification even after second run
                        // FolderChildren probably didn't get a notification neither
                        // so it doesn't know anything about the new file => nothing to test
                        return;
                    }
                } else {
                    break;
                }
            }
            
            // wait for FolderChildren to receive and process the create notification
            int cnt = 10;
            while (cnt-- > 0 && fc.getNodes ().length < 1) {
                try {
                    Thread.sleep(300);
                }
                catch (InterruptedException e) {}
            }
            
            assertEquals("FolderChildren doesn't contain " + newFile, 1, fc.getNodes().length);
        }
    }
    
}
