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

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Tests for SourceLevelQueryImpl
 *
 * @author David Konecny
 */
public class SourceLevelQueryImplTest extends NbTestCase {
    
    public SourceLevelQueryImplTest(java.lang.String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private ProjectManager pm;
    private Project pp;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.java.project.ProjectSourceLevelQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        }, SourceLevelQueryImpl.class.getClassLoader());
        scratch = TestUtil.makeScratchDir(this);
        Repository.getDefault().addFileSystem(scratch.getFileSystem()); // so FileUtil.fromFile works
        projdir = scratch.createFolder("proj");
        AntProjectHelper helper = ProjectGenerator.createProject(projdir, "org.netbeans.modules.java.j2seproject", "proj");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("javac.source", "${def}");
        props.setProperty("def", "1.2.3.4");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        sources = projdir.createFolder("src");
        tests = projdir.createFolder("test");
    }

    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(scratch.getFileSystem());
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    public void testGetSourceLevel() throws Exception {
        FileObject file = scratch.createData("some.java");
        String sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Non-project Java file does not have any source level", null, sl);
        file = sources.createData("a.java");
        sl = SourceLevelQuery.getSourceLevel(file);
        assertEquals("Project's Java file must have project's source", "1.2.3.4", sl);
    }
    
}
