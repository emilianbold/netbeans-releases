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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of SimpleAntArtifact.
 * @author Jesse Glick
 */
public class SimpleAntArtifactTest extends NbTestCase {
    
    public SimpleAntArtifactTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject sisterprojdir;
    private ProjectManager pm;
    private AntProjectHelper sisterh;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            new AntBasedProjectFactorySingleton(),
            AntBasedTestUtil.testAntBasedProjectType(),
        }));
        pm = ProjectManager.getDefault();
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "build/proj2.jar");
        props.setProperty("build.jar.absolute", getWorkDir().getAbsolutePath()+"/build/proj3.jar");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        sisterprojdir = null;
        sisterh = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    /**
     * Check that {@link SimpleAntArtifact} works as documented.
     */
    public void testSimpleAntArtifact() throws Exception {
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        assertEquals("correct artifact location", URI.create("build/proj2.jar"), art.getArtifactLocations()[0]);
        assertEquals("no artifact file yet", 0, art.getArtifactFiles().length);
        FileObject artfile = FileUtil.createData(sisterprojdir, "build/proj2.jar");
        assertEquals("now have an artifact file", artfile, art.getArtifactFiles()[0]);
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
        assertEquals("no script file yet", null, art.getScriptFile());
        FileObject scriptfile = FileUtil.createData(sisterprojdir, "build.xml");
        assertEquals("now have a script file", scriptfile, art.getScriptFile());
        assertEquals("correct project", pm.findProject(sisterprojdir), art.getProject());
        
        art = sisterh.createSimpleAntArtifact("jar", "build.jar.absolute", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct artifact location", (new File(getWorkDir().getAbsolutePath()+"/build/proj3.jar")).toURI(), art.getArtifactLocations()[0]);
    }
    
}
