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

package org.netbeans.api.project.ant;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/* XXX tests needed:
 * - testFindArtifactFromNonexistentFile
 * like testFindArtifactFromFile but file does not yet exist on disk
 * - testFindArtifactByTarget
 * - testFindArtifactsByType
 */

/**
 * Test functionality of AntArtifactQuery, StandardAntArtifactQueryImpl, etc.
 * @author Jesse Glick
 */
public class AntArtifactQueryTest extends NbTestCase {
    
    public AntArtifactQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sisterprojdir;
    private FileObject dummyprojdir;
    private ProjectManager pm;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
            TestUtil.testProjectFactory(),
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        ProjectGenerator.createProject(projdir, "test");
        pm = ProjectManager.getDefault();
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        AntProjectHelper sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        dummyprojdir = scratch.createFolder("dummy");
        dummyprojdir.createFolder("testproject");
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sisterprojdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    public void testFindArtifactFromFile() throws Exception {
        FileObject proj2JarFO = FileUtil.createData(sisterprojdir, "dist/proj2.jar");
        File proj2Jar = FileUtil.toFile(proj2JarFO);
        assertNotNull("have dist/proj2.jar on disk", proj2Jar);
        AntArtifact art = AntArtifactQuery.findArtifactFromFile(proj2Jar);
        assertNotNull("found an artifact matching " + proj2Jar, art);
        assertEquals("correct project", pm.findProject(sisterprojdir), art.getProject());
        assertEquals("correct artifact file", proj2JarFO, art.getArtifactFiles()[0]);
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
    }
    
    public void testFindArtifactsByType() throws Exception {
        Project p = pm.findProject(projdir);
        assertNotNull("have a project in " + projdir, p);
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(p, "jar");
        assertEquals("one JAR artifact", 1, arts.length);
        assertEquals("correct project", p, arts[0].getProject());
        assertEquals("correct target name", "dojar", arts[0].getTargetName());
        p = pm.findProject(dummyprojdir);
        assertNotNull("have a dummy project in " + dummyprojdir, p);
        arts = AntArtifactQuery.findArtifactsByType(p, "jar");
        assertEquals("no JAR artifacts", 0, arts.length);
    }
    
}
