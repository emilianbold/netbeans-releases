/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;

/**
 *
 * @author  Marian Petras
 */
public final class SearchIteratorTest extends NbTestCase {

    /** */
    private FileObject dataDir;
    /** */
    FileObject projectRoot;

    /**
     */
    public SearchIteratorTest(String name) {
        super(name);
    }

    /**
     */
    public static void main(String args[]) {
        TestRunner.run(new NbTestSuite(SearchIteratorTest.class));
    }

    /**
     */
    protected void setUp() throws Exception {
        dataDir = FileUtil.toFileObject(getDataDir());
        assert dataDir != null;
        
        projectRoot = dataDir.getFileObject("projects/Project1");       //NOI18N
        assert projectRoot != null;
        
        FileObject testDir;

        testDir = projectRoot;
        ensureTildeCopyExists(testDir, "build", "xml");             //NOI18N

        testDir = projectRoot.getFileObject("src/foo/bar/baz");     //NOI18N
        ensureTildeCopyExists(testDir, "SampleClass", "java");      //NOI18N
    }
    
    /**
     */
    private void ensureTildeCopyExists(FileObject folder,
                                       String name,
                                       String ext) throws IOException {
        String tildeExt = ext + '~';
        
        FileObject orig = folder.getFileObject(name, ext);
        assert orig != null;
        FileObject copy = folder.getFileObject(name, tildeExt);
        if (copy == null) {
            orig.copy(folder, name, tildeExt);
        }
    }
    
    /**
     */
    public void testPlainSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testSharabilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibSharSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    public void testNonRecursiveSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    false,          //not recursive
                                    false,
                                    false,
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    private void generateSearchableFileNames(
            FileObject folder,
            boolean recursive,
            boolean checkVisibility,
            boolean checkSharability,
            PrintStream refPrintStream) {
                
        FileObjectFilter[] filters;

        int filtersCount = 0;
        if (checkVisibility) {
            filtersCount++;
        }
        if (checkSharability) {
            filtersCount++;
        }

        if (filtersCount == 0) {
            filters = null;
        } else {
            filters = new FileObjectFilter[filtersCount];

            int i = 0;
            if (checkVisibility) {
                filters[i++] = SearchInfoFactory.VISIBILITY_FILTER;
            }
            if (checkSharability) {
                filters[i++] = SearchInfoFactory.SHARABILITY_FILTER;
            }
        }

        SearchInfo searchInfo = SearchInfoFactory.createSearchInfo(
                folder,
                recursive,
                filters);
        
        assertTrue("project root not searchable", searchInfo.canSearch());
        
        List foundFilesPaths = new ArrayList(16);
        for (Iterator i = searchInfo.objectsToSearch(); i.hasNext(); ) {
            FileObject primaryFile = ((DataObject) i.next()).getPrimaryFile();
            String relativePath = FileUtil.getRelativePath(projectRoot,
                                                           primaryFile);
            foundFilesPaths.add(relativePath);
        }
        
        Collections.sort(foundFilesPaths);
        
        for (Iterator i = foundFilesPaths.iterator(); i.hasNext(); ) {
            refPrintStream.println((String) i.next());
        }
    }

    /**
     */
    protected void tearDown() throws Exception {
        
    }

}
