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
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

// XXX testChangesFromAntPropertyChanges

/**
 * Test functionality of GlobFileBuiltQuery.
 * @author Jesse Glick
 */
public class GlobFileBuiltQueryTest extends NbTestCase {
    
    static {
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
        });
    }
    
    public GlobFileBuiltQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject prj;
    private FileObject extsrc;
    private FileObject extbuild;
    private AntProjectHelper h;
    private FileBuiltQueryImplementation fbqi;
    private FileObject foo, bar, fooTest, baz, nonsense;
    private FileBuiltQuery.Status fooStatus, barStatus, fooTestStatus, bazStatus;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        prj = scratch.createFolder("prj");
        h = ProjectGenerator.createProject(prj, "test");
        extsrc = scratch.createFolder("extsrc");
        extbuild = scratch.createFolder("extbuild");
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir", "src");
        ep.setProperty("test.src.dir", "test/src");
        ep.setProperty("ext.src.dir", "../extsrc");
        ep.setProperty("build.classes.dir", "build/classes");
        ep.setProperty("test.build.classes.dir", "build/test/classes");
        ep.setProperty("ext.build.classes.dir", "../extbuild/classes");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(prj));
        foo = TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        bar = TestUtil.createFileFromContent(null, prj, "src/pkg/Bar.java");
        fooTest = TestUtil.createFileFromContent(null, prj, "test/src/pkg/FooTest.java");
        baz = TestUtil.createFileFromContent(null, extsrc, "pkg2/Baz.java");
        nonsense = TestUtil.createFileFromContent(null, prj, "misc-src/whatever/Nonsense.java");
        fbqi = h.createGlobFileBuiltQuery(h.getStandardPropertyEvaluator(), new String[] {
            "${src.dir}/*.java",
            "${test.src.dir}/*.java",
            "${ext.src.dir}/*.java",
        }, new String[] {
            "${build.classes.dir}/*.class",
            "${test.build.classes.dir}/*.class",
            "${ext.build.classes.dir}/*.class",
        });
        fooStatus = fbqi.getStatus(foo);
        barStatus = fbqi.getStatus(bar);
        fooTestStatus = fbqi.getStatus(fooTest);
        bazStatus = fbqi.getStatus(baz);
    }
    
    /** Enough time (millisec) for file timestamps to be different. */
    private static final long PAUSE = 1500;
    
    public void testBasicFunctionality() throws Exception {
        assertNotNull("have status for Foo.java", fooStatus);
        assertNotNull("have status for Bar.java", barStatus);
        assertNotNull("have status for FooTest.java", fooTestStatus);
        assertNull("non-matching file ignored", fbqi.getStatus(nonsense));
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        assertFalse("Bar.java not built", barStatus.isBuilt());
        assertFalse("FooTest.java not built", fooTestStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.class rebuilt", fooStatus.isBuilt());
        fooClass.delete();
        assertFalse("Foo.class deleted", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/test/classes/pkg/FooTest.class");
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        assertFalse("Bar.java still not built", barStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java built again", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        assertFalse("Foo.java modified", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        FileObject buildDir = prj.getFileObject("build");
        assertNotNull("build dir exists", buildDir);
        buildDir.delete();
        assertFalse("Foo.java not built (build dir gone)", fooStatus.isBuilt());
        assertFalse("Bar.java still not built", barStatus.isBuilt());
        assertFalse("FooTest.java not built (build dir gone)", fooTestStatus.isBuilt());
        // Just to check that you can delete a source file safely:
        bar.delete();
        barStatus.isBuilt();
    }
    
    /** Maximum amount of time (in milliseconds) to wait for expected changes. */
    private static final long WAIT = 10000;
    /** Maximum amount of time (in milliseconds) to wait for unexpected changes. */
    private static final long QUICK_WAIT = 500;
    
    public void testChangeFiring() throws Exception {
        AntBasedTestUtil.TestCL fooL = new AntBasedTestUtil.TestCL();
        fooStatus.addChangeListener(fooL);
        AntBasedTestUtil.TestCL barL = new AntBasedTestUtil.TestCL();
        barStatus.addChangeListener(barL);
        AntBasedTestUtil.TestCL fooTestL = new AntBasedTestUtil.TestCL();
        fooTestStatus.addChangeListener(fooTestL);
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        assertFalse("no more changes in Foo.java", fooL.expect(QUICK_WAIT));
        fooClass.delete();
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.java no longer built", fooStatus.isBuilt());
        assertFalse("no changes yet in FooTest.java", fooTestL.expect(QUICK_WAIT));
        assertFalse("FooTest.java not yet built", fooTestStatus.isBuilt());
        FileObject fooTestClass = TestUtil.createFileFromContent(null, prj, "build/test/classes/pkg/FooTest.class");
        assertTrue("change in FooTest.java", fooTestL.expect(WAIT));
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        FileObject buildDir = prj.getFileObject("build");
        assertNotNull("build dir exists", buildDir);
        buildDir.delete();
        assertFalse("no change in Foo.java (still not built)", fooL.expect(QUICK_WAIT));
        assertFalse("Foo.java not built (build dir gone)", fooStatus.isBuilt());
        assertTrue("got change in FooTest.java (build dir gone)", fooTestL.expect(WAIT));
        assertFalse("FooTest.java not built (build dir gone)", fooTestStatus.isBuilt());
        assertFalse("never got changes in Bar.java (never built)", barL.expect(QUICK_WAIT));
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("change in Foo.class", fooL.expect(WAIT));
        assertTrue("Foo.class created", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("touched Foo.class", fooL.expect(WAIT));
        assertTrue("Foo.class touched", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        assertTrue("Foo.java modified in memory", fooL.expect(WAIT));
        assertFalse("Foo.java modified in memory", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        assertTrue("Foo.java unmodified in memory", fooL.expect(WAIT));
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        File buildF = new File(FileUtil.toFile(prj), "build");
        assertTrue("build dir exists", buildF.isDirectory());
        TestUtil.deleteRec(buildF);
        assertFalse(buildF.getAbsolutePath() + " is gone", buildF.exists());
        prj.getFileSystem().refresh(false);
        assertTrue("build dir deleted", fooL.expect(WAIT));
        assertFalse("Foo.class gone (no build dir)", fooStatus.isBuilt());
        File pkg = new File(buildF, "classes/pkg".replace('/', File.separatorChar));
        File fooClassF = new File(pkg, "Foo.class");
        //System.err.println("--> going to make " + fooClassF);
        assertTrue("created " + pkg, pkg.mkdirs());
        assertFalse("no such file yet: " + fooClassF, fooClassF.exists());
        OutputStream os = new FileOutputStream(fooClassF);
        os.close();
        prj.getFileSystem().refresh(false);
        assertTrue(fooClassF.getAbsolutePath() + " created on disk", fooL.expect(WAIT));
        assertTrue("Foo.class back", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        os = new FileOutputStream(fooClassF);
        os.write(69); // force Mac OS X to update timestamp
        os.close();
        prj.getFileSystem().refresh(false);
        assertTrue("Foo.class recreated on disk", fooL.expect(WAIT));
        assertTrue("Foo.class touched", fooStatus.isBuilt());
    }
    
    public void testExternalSourceRoots() throws Exception {
        // Cf. #43609.
        assertNotNull("have status for Baz.java", bazStatus);
        AntBasedTestUtil.TestCL bazL = new AntBasedTestUtil.TestCL();
        bazStatus.addChangeListener(bazL);
        assertFalse("Baz.java not built", bazStatus.isBuilt());
        FileObject bazClass = TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        assertTrue("got change", bazL.expect(WAIT));
        assertTrue("Baz.java now built", bazStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, extsrc, "pkg2/Baz.java");
        assertTrue("got change", bazL.expect(WAIT));
        assertFalse("Baz.class out of date", bazStatus.isBuilt());
        TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        assertTrue("got change", bazL.expect(WAIT));
        assertTrue("Baz.class rebuilt", bazStatus.isBuilt());
        bazClass.delete();
        assertTrue("got change", bazL.expect(WAIT));
        assertFalse("Baz.class deleted", bazStatus.isBuilt());
        TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        assertTrue("got change", bazL.expect(WAIT));
        assertTrue("Baz.java built again", bazStatus.isBuilt());
        DataObject.find(baz).setModified(true);
        assertTrue("got change", bazL.expect(WAIT));
        assertFalse("Baz.java modified", bazStatus.isBuilt());
        DataObject.find(baz).setModified(false);
        assertTrue("got change", bazL.expect(WAIT));
        assertTrue("Baz.java unmodified again", bazStatus.isBuilt());
        extbuild.delete();
        assertTrue("got change", bazL.expect(WAIT));
        assertFalse("Baz.java not built (build dir gone)", bazStatus.isBuilt());
    }
    
    public void testFileRenames() throws Exception {
        // Cf. #45694.
        assertNotNull("have status for Foo.java", fooStatus);
        AntBasedTestUtil.TestCL fooL = new AntBasedTestUtil.TestCL();
        fooStatus.addChangeListener(fooL);
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("got change", fooL.expect(WAIT));
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        FileLock lock = foo.lock();
        try {
            foo.rename(lock, "Foo2", "java");
        } finally {
            lock.releaseLock();
        }
        assertTrue("got change", fooL.expect(WAIT));
        assertFalse("Foo2.java no longer built", fooStatus.isBuilt());
        fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo2.class");
        assertTrue("got change", fooL.expect(WAIT));
        assertTrue("Now Foo2.java is built", fooStatus.isBuilt());
    }
    
}
