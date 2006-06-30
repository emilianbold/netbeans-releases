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

package org.netbeans.modules.apisupport.project.queries;

import java.net.URI;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Test for UnitTestForSourceQuery
 * @author Tomas Zezula
 */
public class UnitTestForSourceQueryImplTest extends TestBase {

    public UnitTestForSourceQueryImplTest(String testName) {
        super(testName);
    }   

    public void testFindUnitTest() throws Exception {
        URL[] testRoots = UnitTestForSourceQuery.findUnitTests(nbroot);
        assertEquals("Test root for non project folder should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        FileObject srcRoot = nbroot.getFileObject("apisupport/project");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for project should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbroot.getFileObject("apisupport/project/test/unit/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for tests should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbroot.getFileObject("apisupport/project/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root defined", 1, testRoots.length);
        assertTrue("Test root exists", new File(URI.create(testRoots[0].toExternalForm())).exists());
        assertEquals("Test root", URLMapper.findFileObject(testRoots[0]), nbroot.getFileObject("apisupport/project/test/unit/src"));
        assertEquals("One test for this project", 1, UnitTestForSourceQuery.findUnitTests(nbroot.getFileObject("openide/windows/src")).length);
    }

    public void testFindSource() {
        URL[] srcRoots = UnitTestForSourceQuery.findSources(nbroot);
        assertEquals("Source root for non project folder should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        FileObject testRoot = nbroot.getFileObject("apisupport/project");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for project should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        testRoot = nbroot.getFileObject("apisupport/project/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for sources should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        assertEquals("No sources for this project's sources", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(nbroot.getFileObject("openide/windows/src"))));
        testRoot = nbroot.getFileObject("apisupport/project/test/unit/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root defined", 1, srcRoots.length);
        assertTrue("Source root exists", new File(URI.create(srcRoots[0].toExternalForm())).exists());
        assertEquals("Source root", URLMapper.findFileObject(srcRoots[0]), nbroot.getFileObject("apisupport/project/src"));
    }        
    
}
