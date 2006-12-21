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

import java.lang.ref.WeakReference;
import java.util.*;
import org.openide.filesystems.FileSystem;
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
    /** created children */
    private DataObject[] arr;
    
    public DataShadowSlowness39981Test (String name) {
        super(name);
    }
    
    public static NbTestSuite suite () {
        return NbTestSuite.speedSuite (DataShadowSlowness39981Test.class, 20, 5);
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
        
        ArrayList weaks = new ArrayList();
        addWeakRefs(Arrays.asList(arr), weaks);
        addWeakRefs(brokenShadows, weaks);
        addWeakRefs(shadows, weaks);
        addWeakRefs(Collections.singleton(lfs), weaks);
        addWeakRefs(Collections.singleton(folder), weaks);
        
        arr = null;
        brokenShadows = null;
        shadows = null;
        lfs = null;
        folder = null;
        
        
        WeakReference[] refArr = (WeakReference[])weaks.toArray(new WeakReference[0]);
        for (int i = 0; i < refArr.length; i++) {
            assertGC(i + " - gc(" + refArr[i].get() + "): ", refArr[i]);
        }
        
        
    }
    
    private static void addWeakRefs(Collection objects, List weakRefsToAdd) {
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            weakRefsToAdd.add(new WeakReference(obj));
        }
    }
    
    private void createChildren () {
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        pool.addOperationListener (this);
        
        arr = folder.getChildren ();
        
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
    
//    public void test987 () throws Exception {
//        createChildren ();
//    }
    
    
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
