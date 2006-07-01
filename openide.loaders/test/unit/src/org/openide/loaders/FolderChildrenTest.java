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


public class FolderChildrenTest extends LoggingTestCaseHid {
    public FolderChildrenTest() {
        super("");
    }

    public FolderChildrenTest(java.lang.String testName) {
        super(testName);
    }
    
    private static void setSystemProp(String key, String value) {
        java.util.Properties prop = System.getProperties();
        if (prop.get(key) != null) return;
        prop.put(key, value);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        setSystemProp("netbeans.security.nocheck","true");
        
        FileObject[] arr = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
    }
    
    public void testSimulateADeadlockThatWillBeFixedByIssue49459 () throws Exception {
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
        assertEquals ("Again they are two", 2, last.length);
        
        assertEquals ("First one is the same", last[0], arr[0]);
        assertEquals ("Second one is the same", last[1], arr[1]);
        
    }
    
    public void testChangeableDataFilter() throws Exception {
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
    
    public void testChildrenCanGC () throws Exception {
        Filter filter = new Filter ();
        
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileObject bb = FileUtil.createFolder(fs.getRoot(), "/BB");
	bb.createData("Ahoj.txt");
	bb.createData("Hi.txt");
        DataFolder folder = DataFolder.findFolder (bb);
           
        Children ch = folder.createNodeChildren( filter );        
        Node[] arr = ch.getNodes (true);
	assertEquals("Accepts only Ahoj", 1, arr.length);
        
        WeakReference ref = new WeakReference (ch);
        ch = null;
        arr = null;
        
        assertGC ("Children can disappear even we hold the filter", ref);
    }
    
    public void testSeemsLikeTheAbilityToRefreshIsBroken() throws Exception {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        FileObject bb = FileUtil.createFolder(fs.getRoot(), "/BB");
	bb.createData("Ahoj.txt");
	bb.createData("Hi.txt");
	
        DataFolder folder = DataFolder.findFolder (bb);
	
	Node n = folder.getNodeDelegate();
	Node[] arr = n.getChildren().getNodes(true);
	assertEquals("Both are visible", 2, arr.length);
	
	WeakReference ref = new WeakReference(arr[0]);
	arr = null;
	assertGC("Nodes can disappear", ref);
	
	
	bb.createData("Third.3rd");
	
	arr = n.getChildren().getNodes(true);
	assertEquals("All are visbile ", 3, arr.length);
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