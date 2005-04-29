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

package org.openide.filesystems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;

public class FileSystemTestHid extends TestBaseHid {
    private static String[] resources = new String [] {
        "atleastone"
    };
        
    public FileSystemTestHid(String testName) {
        super(testName);
    }
    private static int abacus = 0;
    
    
    protected String[] getResources (String testName) {
        return resources;
    }    
    public void testAddFileStatusListener () {
        if (!(this.testedFS instanceof TestUtilHid.StatusFileSystem))
            return;
        FileStatusListener[]  fsListeners =  new FileStatusListener [10];
        abacus = 0;

        for (int i = 0; i < fsListeners.length; i++) {
            fsListeners[i] = createFileStatusListener ();
            this.testedFS.addFileStatusListener(fsListeners[i]);
        }
        this.testedFS.fireFileStatusChanged(new FileStatusEvent (this.testedFS, true, true) );
        fsAssert("failure: not all FileStstausListeners invoked: " + abacus,  abacus == fsListeners.length);

        abacus = 0;        
        this.testedFS.removeFileStatusListener(fsListeners[0]);
        this.testedFS.fireFileStatusChanged(new FileStatusEvent (this.testedFS, true, true) );        
        fsAssert("failure: not all FileStstausListeners invoked",  abacus == (fsListeners.length -1));
    }

    
    public void testAddVetoableChangeListener () {
        VetoableChangeListener[]  vListeners =  new VetoableChangeListener[10];
        abacus = 0;

        for (int i = 0; i < vListeners.length; i++) {
            vListeners[i] = createVetoableChangeListener ();
            this.testedFS.addVetoableChangeListener(vListeners[i]);            
        }
        try {
            this.testedFS.fireVetoableChange("test", "old", "new");
        } catch (PropertyVetoException pex) {
            fsFail("unexpected veto exception");
        }
        fsAssert("failure: not all VetoableChangeListeners invoked",  abacus == vListeners.length);
        
        abacus = 0;        
        this.testedFS.removeVetoableChangeListener(vListeners[0]);
        try {
            this.testedFS.fireVetoableChange("test", "old", "new");        
        } catch (PropertyVetoException pex) {
            fsFail("unexpected veto exception");            
        }            
        fsAssert("failure: not all VetoableChangeListeners invoked",  abacus == (vListeners.length -1));
    }
    
    private FileStatusListener createFileStatusListener () {
        return new FileStatusListener () {
            public void annotationChanged (FileStatusEvent ev) {
                abacus++;
            }
        };

    }

    private VetoableChangeListener createVetoableChangeListener () {
        return new VetoableChangeListener  () {
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                    abacus++;            
            }
        };

    }
    
    public void testFsfileFolderCreated() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            root.createFolder("testtset");
            fileFolderCreatedAssert ("unexpecetd event count",1);
        }
    }
    
    public void testFsfileDataCreated() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            fileDataCreatedAssert ("unexpecetd event count",1);            
        }
        
    }

    public void testFsfileRenamed() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.rename(fLock,"obscure","uni");                               
            } finally {
                fLock.releaseLock();               
            }

            fileRenamedAssert("unexpecetd event count",1);                                    
        }
        
    }

    public void testFsfileDeleted() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (fs);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.delete(fLock);                               
            } finally {
                fLock.releaseLock();               
            }

            fileDeletedAssert("unexpecetd event count",1);                                    
        }
        
    }
    
    /** Test of isValid method, of class org.openide.filesystems.FileSystem. */
    public void testIsValid() {        
        Repository r = new Repository(new LocalFileSystem ());
        // 
        fsAssert("file system, which is not assigned to the repository, should be invalid", 
        !testedFS.isValid());
        
        // assign to empty repository -> become valid
        r.addFileSystem(testedFS);
        if (!testedFS.getSystemName().equals(""))
            fsAssert("assign to empty repository -> become valid" , testedFS.isValid());
        
        // remove from repo -> become invalid
        r.removeFileSystem(testedFS);
        fsAssert("remove from repo -> become invalid", !testedFS.isValid());
    }
    
}
