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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.openide.filesystems.FileSystem;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** Creation of data object is said to be slow due to
 * poor implementation of BrokenDataShadow validate functionality.
 * @author Jaroslav Tulach
 */
public class DataFolderExpandIsSlowTest extends NbTestCase {
    /** folder to work with */
    private DataFolder folder;

    private FileObject root;
    /** keep some files */
    private DataObject[] arr;
    
    public DataFolderExpandIsSlowTest (String name) {
        super(name);
    }
    
    public static NbTestSuite suite () {
        return NbTestSuite.linearSpeedSuite(DataFolderExpandIsSlowTest.class, 5, 3);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "files/",
        });
        
        
        root = FileUtil.toFileObject(FileUtil.toFile(lfs.getRoot()));
        
        
        folder = DataFolder.findFolder(root.getFileObject("files"));
        assertNotNull(folder);
        
        int count = getTestNumber ();
        
        for (int i = 0; i < count; i++) {
            FileUtil.createData(folder.getPrimaryFile(), "empty" + i + ".txt");
        }
    }
    
    protected void tearDown() throws Exception {
        WeakReference<Object> ref = new WeakReference<Object>(root);
        this.root = null;
        this.folder = null;
        
        List<WeakReference<DataObject>> refs = new ArrayList<WeakReference<DataObject>>();
        for (DataObject dataObject : arr) {
            refs.add(new WeakReference<DataObject>(dataObject));
        }
        this.arr = null;
        try {
            assertGC("Make sure the filesystem is gone", ref);
            
            for (WeakReference<DataObject> weakReference : refs) {
                assertGC("All data objects needs to be gone", weakReference);
            }

        } catch (AssertionFailedError ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, null, ex);
        }
    }
    
    private void doArr(boolean fast) {
        DataObjectPool.fastCache(fast);
        arr = folder.getChildren();
        assertEquals("All computed as expected", getTestNumber(), arr.length);
    }
    
    
    public void testNew99 () { doArr(false); }
    public void testNew245 () { doArr(false); }
    public void testNew987 () { doArr(false); }
//    public void testNew9987 () { doArr(false); }
    public void testOld99 () { doArr(true); }
    public void testOld245 () { doArr(true); }
    public void testOld987 () { doArr(true); }
//    public void testOld9987 () { doArr(false); }
}
