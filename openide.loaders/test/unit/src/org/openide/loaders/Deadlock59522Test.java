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


/** MDR listens on FileChanges and wants to acquire its lock then. 
 * Also it does rename under holding its lock in other thread.
 *
 * @author Jaroslav Tulach
 */
public class Deadlock59522Test extends org.netbeans.junit.NbTestCase 
implements org.openide.filesystems.FileChangeListener {
    org.openide.filesystems.FileObject toolbars;
    org.openide.filesystems.FileSystem fs;
    org.openide.loaders.DataFolder toolbarsFolder;
    org.openide.loaders.DataFolder anotherFolder;
    org.openide.loaders.DataObject obj;
    org.openide.loaders.DataObject anotherObj;
    
    
    Exception assigned;
    boolean called;
    boolean ok;
    
    private Object BIG_MDR_LOCK = new Object();
    
    public Deadlock59522Test (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (Deadlock59522Test.class);
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
        fo = org.openide.filesystems.FileUtil.createData (root, "Another.txt");
        anotherObj = org.openide.loaders.DataObject.find (fo);
        
        fs.addFileChangeListener (this);
    }

    protected void tearDown() throws java.lang.Exception {
        fs.removeFileChangeListener (this);
        
        assertTrue ("The doRenameAObjectWhileHoldingMDRLock must be called", called);
        
        org.openide.filesystems.FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        if (assigned != null) {
            throw assigned;
        }
    }
    private static int cnt = 0;
    private void startRename() throws Exception {
        synchronized (BIG_MDR_LOCK) {
            org.openide.util.RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    synchronized (BIG_MDR_LOCK) {
                        try {
                            called = true;
                            BIG_MDR_LOCK.notify();
                            BIG_MDR_LOCK.wait(); // for notification
                            // in some thread try to rename some object while holding mdr lock
                            anotherObj.rename ("mynewname" + cnt++);
                            ok = true;
                        } catch (Exception ex) {
                            assigned = ex;
                        } finally {
                            // end this all
                            BIG_MDR_LOCK.notifyAll();
                        }
                    }
                }
            });
            BIG_MDR_LOCK.wait();
        }
    }
    
    private void lockMdr() {
        // no more callbacks
        fs.removeFileChangeListener(this);
        
        synchronized (BIG_MDR_LOCK) {
            BIG_MDR_LOCK.notify(); // notified from herer
            try {
                BIG_MDR_LOCK.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                fail ("No InterruptedExceptions");
            }
            assertTrue ("Rename finished ok", ok);
        }
    }
    

    public void testMove () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.move (anotherFolder);
        }
    }

    public void testCopy () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.copy (anotherFolder);
        }
    }
    
    public void testRename () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.rename ("NewName.txt");
        }
    }
    
    public void testCreateShadow () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createShadow (anotherFolder);
        }
    }
    
    public void testTemplate () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createFromTemplate (anotherFolder);
        }
    }

    public void testTemplate2 () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createFromTemplate (anotherFolder, "AhojVole.txt");
        }
    }

    //
    // Listener triggers creation of the node
    //

    public void fileRenamed (FileRenameEvent fe) {
        lockMdr ();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }

    public void fileFolderCreated (FileEvent fe) {
        lockMdr ();
    }

    public void fileDeleted (FileEvent fe) {
        lockMdr ();
    }

    public void fileDataCreated (FileEvent fe) {
        lockMdr ();
    }

    public void fileChanged (FileEvent fe) {
        lockMdr ();
    }
    
}
