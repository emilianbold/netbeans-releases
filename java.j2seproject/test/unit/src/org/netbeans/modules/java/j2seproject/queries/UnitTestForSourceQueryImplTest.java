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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 * Tests for UnitTestForSourceQueryImpl
 *
 * @author David Konecny
 */
public class UnitTestForSourceQueryImplTest extends NbTestCase {
    
    public UnitTestForSourceQueryImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(UnitTestForSourceQueryImplTest.class);
        return suite;
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private FileObject sources;
    private FileObject tests;
    
    Project pp;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.java.project.UnitTestForSourceQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        }, UnitTestForSourceQueryImpl.class.getClassLoader());
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        AntProjectHelper helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.java.j2seproject");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("src.dir", "src");
        props.setProperty("test.src.dir", "test");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        sources = projdir.createFolder("src");
        tests = projdir.createFolder("test");
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
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
    }
    
}
