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

import org.openide.filesystems.*;
import java.beans.*;
import org.netbeans.junit.*;

/** Test the complexity of deleting N files in a folder without recognized
 * DataOjects (which may as well be tested on deleting secondary files
 * of a recognized DataObject).
 *
 * The test may need slight tweaking of the timing constants for reliability.
 * 
 * @author  Petr Nejedly
 */
public class DataFolderSlowDeletionTest extends LoggingTestCaseHid {
    private FileObject folder;
    // just holders
    private FileObject[] children;
    private DataFolder df;
    private DataObject do0;
    

    /** Creates new DataFolderSlowDeletionTest */
    public DataFolderSlowDeletionTest (String name) {
        super (name);
    }
    
    /**
     * @return a  speedSuite configured as to allow 2x linear slowdown between
     * 10-fold increase of the paratemer
     */
    public static NbTestSuite suite () {
        return NbTestSuite.linearSpeedSuite(DataFolderSlowDeletionTest.class, 2, 3);
    }
    
    /**
     * Prepares a filesystem with a prepopulated folder of N files, where N
     * is extracted from the test name.
     * @throws java.lang.Exception 
     */
    protected void setUp() throws Exception {
        clearWorkDir();
        TestUtilHid.destroyLocalFileSystem(getName());
        
        int count = getTestNumber ();
        String[] resources = new String[count];       
        for (int i=0; i<resources.length; i++) resources[i] = "folder/file" + i + ".txt";
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
        folder = fs.findResource("folder");
        
        // convert to masterfs
        folder = FileUtil.toFileObject(FileUtil.toFile(folder));
        
        
        children = folder.getChildren();
        df = DataFolder.findFolder (folder);
        do0 = DataObject.find(children[0]);
    }
    
    
    
    /**
     * 
     * @throws java.lang.Exception 
     */
    private void performSlowDeletionTest () throws Exception {
        folder.delete();
    }
    
    /**
     * Preheat the infrastructure so the lower end is measured already JITed
     * @throws java.lang.Exception 
     */
    public void testSlowDeletionPrime1000() throws Exception {
        performSlowDeletionTest();
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    public void testSlowDeletion1000() throws Exception {
        performSlowDeletionTest();
    }

    public void testSlowDeletion3000() throws Exception {
        performSlowDeletionTest();
    }
    
}
