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

package org.openidex.search;

import java.io.IOException;
import java.util.Iterator;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;

/**
 *
 * @author  Marian Petras
 */
public class CompoundSearchInfoTest extends NbTestCase {
    
    public CompoundSearchInfoTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        MockServices.setServices(new Class[] {DummyDataLoader.class});
    }

    public void testNullArgument() {
        try {
            new CompoundSearchInfo(null);
            fail("constructor of CompoundSearchInfo should throw" +
                    " an IllegalArgumentException when null is passed");
        } catch (IllegalArgumentException ex) {
            //correct
        } catch (Exception ex) {
            fail("constructor of CompoundSearchInfo should throw" +
                    " an IllegalArgumentException when null is passed" +
                    " - different type of exception was thrown: "
                    + ex.getClass().getName());
        }
    }
    
    public void testEmptyList() {
        SearchInfo searchInfo = new CompoundSearchInfo(new SearchInfo[0]);
        assertFalse(searchInfo.canSearch());
        assertFalse(searchInfo.objectsToSearch().hasNext());
    }
    
    public void testOneItemList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();
        
        FileObject dir = fsRoot.createFolder("dir");
        dir.createData("a", DummyDataLoader.dummyExt);
        dir.createData("b", DummyDataLoader.dummyExt);
        dir.createData("c", DummyDataLoader.dummyExt);
        DataFolder folder = DataFolder.findFolder(dir);
        
        
        SearchInfo refSearchInfo;
        SearchInfo testSearchInfo;
        boolean refCanSearch;
        boolean testCanSearch;
        Iterator refIt;
        Iterator testIt;
        int testIterationsCount;
        
        
        refSearchInfo = new SimpleSearchInfo(folder, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        assertTrue(testSearchInfo.canSearch());
        
        refIt = refSearchInfo.objectsToSearch();
        testIt = testSearchInfo.objectsToSearch();
        for (testIterationsCount = 0;;testIterationsCount++) {
            boolean refHasNext = refIt.hasNext();
            boolean testHasNext = testIt.hasNext();
            assertEquals(refHasNext, testHasNext);

            if (!refHasNext) {
                break;
            }

            Object refObj = refIt.next();
            Object testObj = testIt.next();
            assertSame(refObj, testObj);
        }
        assertEquals(3, testIterationsCount);
        
        
        refSearchInfo = new SimpleSearchInfo(folder, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        refCanSearch = refSearchInfo.canSearch();
        testCanSearch = testSearchInfo.canSearch();
        assertEquals(refCanSearch, testCanSearch);
    }
    
    public void testMultipleItemsList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();
        
        FileObject dir1 = fsRoot.createFolder("dir1");
        dir1.createData("1a", DummyDataLoader.dummyExt);
        dir1.createData("1b", DummyDataLoader.dummyExt);
        dir1.createData("1c", DummyDataLoader.dummyExt);
        DataFolder folder1 = DataFolder.findFolder(dir1);
        
        FileObject dir2 = fsRoot.createFolder("dir2");
        dir2.createData("2a", DummyDataLoader.dummyExt);
        dir2.createData("2b", DummyDataLoader.dummyExt);
        DataFolder folder2 = DataFolder.findFolder(dir2);
        
        
        SearchInfo refSearchInfo1, refSearchInfo2;
        SearchInfo testSearchInfo;
        Iterator refIt;
        Iterator testIt;
        int testIterationsCount;
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        
        testIterationsCount = 0;
        testIt = testSearchInfo.objectsToSearch();
        refIt = refSearchInfo1.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testIt.hasNext());
            assertSame(refIt.next(), testIt.next());
            testIterationsCount++;
        }
        assertTrue(testIt.hasNext());
        refIt = refSearchInfo2.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testIt.hasNext());
            assertSame(refIt.next(), testIt.next());
            testIterationsCount++;
        }
        assertFalse(testIt.hasNext());
        assertEquals(5, testIterationsCount);
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        
        testIterationsCount = 0;
        testIt = testSearchInfo.objectsToSearch();
        refIt = refSearchInfo1.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testIt.hasNext());
            assertSame(refIt.next(), testIt.next());
            testIterationsCount++;
        }
        assertFalse(testIt.hasNext());
        assertEquals(3, testIterationsCount);
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        
        testIterationsCount = 0;
        testIt = testSearchInfo.objectsToSearch();
        refIt = refSearchInfo2.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testIt.hasNext());
            assertSame(refIt.next(), testIt.next());
            testIterationsCount++;
        }
        assertFalse(testIt.hasNext());
        assertEquals(2, testIterationsCount);
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertFalse(testSearchInfo.canSearch());
    }
    
}
