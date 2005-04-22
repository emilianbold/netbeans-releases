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

import java.lang.ref.WeakReference;
import java.util.*;
import junit.textui.TestRunner;
import org.openide.filesystems.FileSystem;
import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** Delete of an folder is said to be slow due to
 * poor implementation of DataShadow validate functionality.
 * @author Jaroslav Tulach
 */
public class DataShadowSlowness39981Test extends NbTestCase implements OperationListener {
    /** List of DataObject */
    private List shadows, brokenShadows;
    /** folder to work with */
    private DataFolder folder;
    /** fs we work on */
    private FileSystem lfs;
    /** start time of the test */
    private long time;
    /** number of created objects */
    private int createdObjects;
    
    public DataShadowSlowness39981Test (String name) {
        super(name);
    }
    
    public static NbTestSuite suite () {
        return NbTestSuite.speedSuite (DataShadowSlowness39981Test.class, 10, 5);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "shadows/",
            "brokenshadows",
            "folder/original.txt",
            "folder/orig.txt",
            "modify/"
        });
        Repository.getDefault ().addFileSystem (lfs);
        
        int count = getTestNumber ();
        
		shadows = createShadows (
            DataObject.find (lfs.findResource("folder/original.txt")), 
            DataFolder.findFolder (lfs.findResource ("shadows")),
            count
        );
        
		brokenShadows = /*Collections.EMPTY_LIST; */createShadows (
            DataObject.find (lfs.findResource("folder/orig.txt")), 
            DataFolder.findFolder (lfs.findResource ("shadows")),
            count
        );
        
        DataObject.find (lfs.findResource("folder/orig.txt")).delete ();
        
        ListIterator it = brokenShadows.listIterator ();
        while (it.hasNext ()) {
            DataObject obj = (DataObject)it.next ();
            assertFalse ("Is not valid", obj.isValid ());
            assertTrue ("Used to be shadow", obj instanceof DataShadow);
            DataObject newObj = DataObject.find (obj.getPrimaryFile ());
            assertTrue ("They are different", newObj != obj);
            assertFalse ("It is not shadow, as it is broken", newObj instanceof DataShadow);
            
            it.set (newObj);
        }
        
        FileObject files = lfs.findResource ("modify");
        for (int i = 0; i < 200; i++) {
            FileUtil.createData (files, "empty" + i + ".txt");
        }
        
        assertEquals ("Children created", 200, files.getChildren ().length);
        
        folder = DataFolder.findFolder (files);
        time = System.currentTimeMillis ();
    }
    
    private static List createShadows (DataObject original, DataFolder target, int count) throws java.io.IOException {
        ArrayList list = new ArrayList (count);
        for (int i = 0; i < count; i++) {
            DataShadow shad = DataShadow.create(target, original.getName()+i, original, "shadow");
            list.add (shad);
        }
        return list;
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault ().removeFileSystem (lfs);
    }
    
    private void createChildren () {
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        pool.addOperationListener (this);
        
        DataObject[] arr = folder.getChildren ();
        
        pool.removeOperationListener (this);
        
        if (arr.length > createdObjects) {
            fail ("The children of the folder should not be created before the getChildren method is called. Children: " + arr.length + " created: " + createdObjects);
        }
    }
    
    public void test0 () throws Exception {
        createChildren ();
    }
    
    public void test10 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test99 () throws java.io.IOException {
        createChildren ();
    }

    public void test245 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test552 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test987 () throws Exception {
        createChildren ();
    }
    
    
    public void operationCopy (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationCreateFromTemplate (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationCreateShadow (org.openide.loaders.OperationEvent.Copy ev) {
    }
    
    public void operationDelete (OperationEvent ev) {
    }
    
    public void operationMove (org.openide.loaders.OperationEvent.Move ev) {
    }
    
    public void operationPostCreate (OperationEvent ev) {
        createdObjects++;
    }
    
    public void operationRename (org.openide.loaders.OperationEvent.Rename ev) {
    }
    
}
