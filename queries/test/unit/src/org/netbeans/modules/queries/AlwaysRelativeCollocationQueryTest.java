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

package org.netbeans.modules.queries;

import java.io.IOException;
import junit.framework.*;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 *
 * @author Tomas Zezula
 */
public class AlwaysRelativeCollocationQueryTest extends NbTestCase {
    
    public AlwaysRelativeCollocationQueryTest(String testName) {
        super(testName);
    } 


    public void testFindRoot() throws IOException {
        AlwaysRelativeCollocationQuery cq = new AlwaysRelativeCollocationQuery ();
        File testRoot = this.getWorkDir();
        File root1 = new File (testRoot,"root1");
        root1.mkdirs();
        File root2 = new File (testRoot, "root2");
        root2.mkdirs();
        File folder1 = new File (new File (root1,"folder1_1"), "folder1_2");
        folder1.mkdirs();
        File folder2 = new File (new File (root1,"folder2_1"), "folder2_2");
        folder2.mkdirs();
        File folderExt = new File (new File (root2,"folderExt_1"), "folderExt_2");
        folderExt.mkdirs();        
        
        File[] roots = new File[] {
            root1
        };        
        cq.setFileSystemRoots (roots);
        assertEquals("Wrong root of the folder1", root1, cq.findRoot(folder1));
        assertEquals("Wrong root of the folder2", root1, cq.findRoot(folder2));
        
        roots = new File[] {
            root1,
            root2
        };
        cq.setFileSystemRoots (roots);
        assertEquals("Wrong root of the folder1", root1, cq.findRoot(folder1));
        assertEquals("Wrong root of the folder2", root1, cq.findRoot(folder2));
        assertEquals("Wrong root of the folderExt", root2, cq.findRoot(folderExt));
    }

    public void testAreCollocated() throws IOException {
        AlwaysRelativeCollocationQuery cq = new AlwaysRelativeCollocationQuery ();
        File testRoot = this.getWorkDir();
        File root1 = new File (testRoot,"root1");
        root1.mkdirs();
        File root2 = new File (testRoot, "root2");
        root2.mkdirs();
        File folder1 = new File (new File (root1,"folder1_1"), "folder1_2");
        folder1.mkdirs();
        File folder2 = new File (new File (root1,"folder2_1"), "folder2_2");
        folder2.mkdirs();
        File folderExt = new File (new File (root2,"folderExt_1"), "folderExt_2");
        folderExt.mkdirs();        
        
        File[] roots = new File[] {
            root1
        };       
        cq.setFileSystemRoots (roots);
        assertTrue ("The folder1 should be collocated with the folder2", cq.areCollocated(folder1,folder2));
                
        roots = new File[] {
            root1,
            root2
        };
        cq.setFileSystemRoots (roots);
        assertTrue ("The folder1 should be collocated with the folder2", cq.areCollocated(folder1,folder2));
        assertFalse ("The folder1 should not be collocated with the folderExt", cq.areCollocated(folder1,folderExt));
        assertFalse ("The folder2 should not be collocated with the folderExt", cq.areCollocated(folder2,folderExt));
    }
    
}
