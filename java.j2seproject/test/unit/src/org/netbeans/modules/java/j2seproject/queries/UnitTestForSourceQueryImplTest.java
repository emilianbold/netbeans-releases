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

package org.netbeans.modules.java.j2seproject.queries;

import java.net.URL;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 * Tests for UnitTestForSourceQueryImpl
 *
 * @author David Konecny
 */
public class UnitTestForSourceQueryImplTest extends NbTestCase {
    
    public UnitTestForSourceQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private FileObject sources;
    private FileObject tests;
    private AntProjectHelper helper;

    Project pp;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.java.project.UnitTestForSourceQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null);
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);        
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    public void testFindUnitTest() throws Exception {
        URL u = UnitTestForSourceQuery.findUnitTest(projdir);
        assertNull(u);
        
        u = UnitTestForSourceQuery.findUnitTest(sources);
        assertNotNull(u);
        URL result = URLMapper.findURL(tests, URLMapper.EXTERNAL);
        assertNotNull(result);
        assertEquals(result, u);
        
        u = UnitTestForSourceQuery.findSource(tests);
        assertNotNull(u);
        result = URLMapper.findURL(sources, URLMapper.EXTERNAL);
        assertNotNull(result);
        assertEquals(result, u);
        
        //Test the case when the tests folder does not exist
        result = tests.getURL();
        tests.delete();
        u = UnitTestForSourceQuery.findUnitTest (sources);
        assertEquals (result, u);
    }

    public void testFindUnitTestMultiRoots () throws Exception {
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper,projdir,"src.other.dir","other");
        URL[] urls = UnitTestForSourceQuery.findSources(tests);
        assertNotNull(urls);
        assertEquals(2,urls.length);
        assertEquals(sources.getURL(), urls[0]);
        assertEquals(newRoot.getURL(), urls[1]);
    }

}
