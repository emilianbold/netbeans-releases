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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of SharabilityQueryImpl.
 * @author Jesse Glick
 */
public class SharabilityQueryImplTest extends NbTestCase {
    
    public SharabilityQueryImplTest(String name) {
        super(name);
    }
    
    /** Location of top of testing dir (contains projdir and external). */
    private File scratchF;
    /** Tested impl. */
    private SharabilityQueryImplementation sqi;
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
        });
        FileObject scratch = TestUtil.makeScratchDir(this);
        scratchF = FileUtil.toFile(scratch);
        FileObject projdir = scratch.createFolder("projdir");
        AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
        assertEquals("right project directory", projdir, h.getProjectDirectory());
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.dir", "build");
        props.setProperty("build2.dir", "build/2");
        props.setProperty("dist.dir", "dist");
        props.setProperty("src.dir", "src");
        File externalF = new File(scratchF, "external");
        props.setProperty("src2.dir", new File(externalF, "src").getAbsolutePath());
        props.setProperty("build3.dir", new File(externalF, "build").getAbsolutePath());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(projdir));
        sqi = h.createSharabilityQuery(h.getStandardPropertyEvaluator(), new String[] {"${src.dir}", "${src2.dir}"},
                                       new String[] {"${build.dir}", "${build2.dir}", "${build3.dir}", "${dist.dir}"});
    }
    
    private File file(String path) {
        return new File(scratchF, path.replace('/', File.separatorChar));
    }
    
    public void testBasicIncludesExcludes() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.MIXED, sqi.getSharability(file("projdir")));
        assertEquals("build.xml is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/build.xml")));
        assertEquals("src/ is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/src")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/src/org/foo")));
        assertEquals("src/org/foo/Foo.java is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/Foo.java")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.MIXED, sqi.getSharability(file("projdir/nbproject")));
        assertEquals("nbproject/project.xml is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/nbproject/project.xml")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private")));
        assertEquals("nbproject/private/private.properties is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/private.properties")));
        assertEquals("build/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build")));
        assertEquals("build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build/classes/org/foo/Foo.class")));
        assertEquals("dist/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/dist")));
    }
    
    public void testOverlaps() throws Exception {
        assertEquals("build/2/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2")));
        assertEquals("build/2/whatever is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/whatever")));
        // overlaps in includePaths tested in basicIncludesExcludes: src is inside projdir
    }
    
    public void testExternalFiles() throws Exception {
        assertEquals("external/src is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("external/src")));
        assertEquals("external/src/org/foo/Foo.java is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("external/src/org/foo/Foo.java")));
        assertEquals("external/build is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("external/build")));
        assertEquals("external/build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("external/build/classes/org/foo/Foo.class")));
    }
    
    public void testUnknownFiles() throws Exception {
        assertEquals("some other dir is unknown", SharabilityQuery.UNKNOWN, sqi.getSharability(file("something")));
        assertEquals("some other file is unknown", SharabilityQuery.UNKNOWN, sqi.getSharability(file("something/else")));
        assertEquals("external itself is unknown", SharabilityQuery.UNKNOWN, sqi.getSharability(file("external")));
    }
    
    public void testDirNamesEndingInSlash() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.MIXED, sqi.getSharability(file("projdir/")));
        assertEquals("src/ is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/src/")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.MIXED, sqi.getSharability(file("projdir/nbproject/")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/")));
        assertEquals("build/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build/")));
        assertEquals("dist/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/dist/")));
        assertEquals("build/2/ is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/")));
        assertEquals("some other dir is unknown", SharabilityQuery.UNKNOWN, sqi.getSharability(file("something/")));
        assertEquals("external itself is unknown", SharabilityQuery.UNKNOWN, sqi.getSharability(file("external/")));
        assertEquals("external/src is sharable", SharabilityQuery.SHARABLE, sqi.getSharability(file("external/src/")));
        assertEquals("external/build is not sharable", SharabilityQuery.NOT_SHARABLE, sqi.getSharability(file("external/build/")));
    }
    
    public void testSubprojectFiles() throws Exception {
        assertEquals("nbproject/private from a subproject is sharable as far as this impl is concerned", SharabilityQuery.SHARABLE, sqi.getSharability(file("projdir/subproj/nbproject/private")));
    }
    
    // XXX testChangedProperties
    // XXX testExternalSourceDirs
    
}
