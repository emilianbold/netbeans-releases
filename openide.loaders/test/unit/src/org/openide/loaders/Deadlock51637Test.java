/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.*;
import junit.framework.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;


/** Simulates the deadlock between copy/move operation and the creation
 * of node.
 *
 * @author Jaroslav Tulach
 */
public class Deadlock51637Test extends org.netbeans.junit.NbTestCase 
implements org.openide.filesystems.FileChangeListener {
    org.openide.filesystems.FileObject toolbars;
    org.openide.filesystems.FileSystem fs;
    org.openide.loaders.DataFolder toolbarsFolder;
    org.openide.loaders.DataFolder anotherFolder;
    org.openide.loaders.DataObject obj;
    
    org.openide.nodes.Node node;
    Exception assigned;
    boolean called;
    
    public Deadlock51637Test (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (Deadlock51637Test.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        fs = org.openide.filesystems.Repository.getDefault ().getDefaultFileSystem ();
        org.openide.filesystems.FileObject root = fs.getRoot ();
        toolbars = org.openide.filesystems.FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = org.openide.loaders.DataFolder.findFolder (toolbars);
        org.openide.filesystems.FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        org.openide.filesystems.FileObject fo = org.openide.filesystems.FileUtil.createData (root, "Ahoj.txt");
        obj = org.openide.loaders.DataObject.find (fo);
        fo = org.openide.filesystems.FileUtil.createFolder (root, "Another");
        anotherFolder = org.openide.loaders.DataFolder.findFolder (fo);
        
        fs.addFileChangeListener (this);
    }

    protected void tearDown() throws java.lang.Exception {
        fs.removeFileChangeListener (this);
        
        assertTrue ("The doCreateNode must be called", called);
        
        org.openide.filesystems.FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
    }
    
    private void doCreateNode () {
        if (node != null) {
            assertNotNull ("Node is not null, but it was not assigned", assigned);
            
            AssertionFailedError a = new AssertionFailedError ("Node cannot be null");
            a.initCause (assigned);
            throw a;
        }
        // just one event is enough
        fs.removeFileChangeListener (this);
        
        called = true;
        
        boolean ok;
        try {
            final Exception now = new Exception ("Calling to rp");
            ok = org.openide.util.RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    node = obj.getNodeDelegate ();
                    
                    assigned = new Exception ("Created in RP");
                    assigned.initCause (now);
                }
            }).waitFinished (100000);
        } catch (InterruptedException ex) {
            AssertionFailedError a = new AssertionFailedError (ex.getMessage ());
            a.initCause (ex);
            throw a;
        }
        
        if (node == null) {
            fail ("Node is still null and the waitFinished was " + ok);
        }
    }
    

    public void testMove () throws Exception {
        obj.move (anotherFolder);
    }

    public void testCopy () throws Exception {
        obj.copy (anotherFolder);
    }
    
    public void testRename () throws Exception {
        obj.rename ("NewName.txt");
    }
    
    public void testCreateShadow () throws Exception {
        obj.createShadow (anotherFolder);
    }
    
    public void testTemplate () throws Exception {
        obj.createFromTemplate (anotherFolder);
    }

    public void testTemplate2 () throws Exception {
        obj.createFromTemplate (anotherFolder, "AhojVole.txt");
    }

    //
    // Listener triggers creation of the node
    //

    public void fileRenamed (FileRenameEvent fe) {
        doCreateNode ();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }

    public void fileFolderCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDeleted (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDataCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileChanged (FileEvent fe) {
        doCreateNode ();
    }
    
}
