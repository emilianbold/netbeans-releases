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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;

// XXX testChangesFromAntPropertyChanges
// XXX testFileRenames
// XXX testExternalSourceRoots

/**
 * Test functionality of GlobFileBuiltQuery.
 * @author Jesse Glick
 */
public class GlobFileBuiltQueryTest extends NbTestCase {
    
    static {
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
        }, GlobFileBuiltQueryTest.class.getClassLoader());
    }
    
    public GlobFileBuiltQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private AntProjectHelper h;
    private FileBuiltQueryImplementation fbqi;
    private FileObject foo, bar, fooTest, nonsense;
    private FileBuiltQuery.Status fooStatus, barStatus, fooTestStatus;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        h = ProjectGenerator.createProject(scratch, "test");
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir", "src");
        ep.setProperty("test.src.dir", "test/src");
        ep.setProperty("build.classes.dir", "build/classes");
        ep.setProperty("test.build.classes.dir", "build/test/classes");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(scratch));
        foo = TestUtil.createFileFromContent(null, scratch, "src/pkg/Foo.java");
        bar = TestUtil.createFileFromContent(null, scratch, "src/pkg/Bar.java");
        fooTest = TestUtil.createFileFromContent(null, scratch, "test/src/pkg/FooTest.java");
        nonsense = TestUtil.createFileFromContent(null, scratch, "misc-src/whatever/Nonsense.java");
        fbqi = h.createGlobFileBuiltQuery(h.getStandardPropertyEvaluator(), new String[] {
            "${src.dir}/*.java",
            "${test.src.dir}/*.java",
        }, new String[] {
            "${build.classes.dir}/*.class",
            "${test.build.classes.dir}/*.class",
        });
        fooStatus = fbqi.getStatus(foo);
        barStatus = fbqi.getStatus(bar);
        fooTestStatus = fbqi.getStatus(fooTest);
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
        FileObject fooClass = TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, scratch, "src/pkg/Foo.java");
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("Foo.class rebuilt", fooStatus.isBuilt());
        fooClass.delete();
        assertFalse("Foo.class deleted", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, scratch, "build/test/classes/pkg/FooTest.class");
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        assertFalse("Bar.java still not built", barStatus.isBuilt());
        TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java built again", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        assertFalse("Foo.java modified", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        FileObject buildDir = scratch.getFileObject("build");
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
    private static final long QUICK_WAIT = 1000;
    
    public void testChangeFiring() throws Exception {
        L fooL = new L();
        fooStatus.addChangeListener(fooL);
        L barL = new L();
        barStatus.addChangeListener(barL);
        L fooTestL = new L();
        fooTestStatus.addChangeListener(fooTestL);
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        assertFalse("no more changes in Foo.java", fooL.expect(QUICK_WAIT));
        fooClass.delete();
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.java no longer built", fooStatus.isBuilt());
        assertFalse("no changes yet in FooTest.java", fooTestL.expect(QUICK_WAIT));
        FileObject fooTestClass = TestUtil.createFileFromContent(null, scratch, "build/test/classes/pkg/FooTest.class");
        assertTrue("change in FooTest.java", fooTestL.expect(WAIT));
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        FileObject buildDir = scratch.getFileObject("build");
        assertNotNull("build dir exists", buildDir);
        buildDir.delete();
        assertFalse("no change in Foo.java (still not built)", fooL.expect(QUICK_WAIT));
        assertFalse("Foo.java not built (build dir gone)", fooStatus.isBuilt());
        assertTrue("got change in FooTest.java (build dir gone)", fooTestL.expect(WAIT));
        assertFalse("FooTest.java not built (build dir gone)", fooTestStatus.isBuilt());
        assertFalse("never got changes in Bar.java (never built)", barL.expect(QUICK_WAIT));
        TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("change in Foo.class", fooL.expect(WAIT));
        assertTrue("Foo.class created", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, scratch, "src/pkg/Foo.java");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, scratch, "build/classes/pkg/Foo.class");
        assertTrue("touched Foo.class", fooL.expect(WAIT));
        assertTrue("Foo.class touched", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        assertTrue("Foo.java modified in memory", fooL.expect(WAIT));
        assertFalse("Foo.java modified in memory", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        assertTrue("Foo.java unmodified in memory", fooL.expect(WAIT));
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        File buildF = new File(getWorkDir(), "build");
        assertTrue("build dir exists", buildF.isDirectory());
        TestUtil.deleteRec(buildF);
        assertFalse(buildF.getAbsolutePath() + " is gone", buildF.exists());
        scratch.getFileSystem().refresh(false);
        assertTrue("build dir deleted", fooL.expect(WAIT));
        assertFalse("Foo.class gone (no build dir)", fooStatus.isBuilt());
        File pkg = new File(buildF, "classes/pkg".replace('/', File.separatorChar));
        File fooClassF = new File(pkg, "Foo.class");
        //System.err.println("--> going to make " + fooClassF);
        assertTrue("created " + pkg, pkg.mkdirs());
        assertFalse("no such file yet: " + fooClassF, fooClassF.exists());
        OutputStream os = new FileOutputStream(fooClassF);
        os.close();
        scratch.getFileSystem().refresh(false);
        assertTrue(fooClassF.getAbsolutePath() + " created on disk", fooL.expect(WAIT));
        assertTrue("Foo.class back", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, scratch, "src/pkg/Foo.java");
        assertTrue("change in Foo.java", fooL.expect(WAIT));
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        os = new FileOutputStream(new File(pkg, "Foo.class"));
        os.close();
        scratch.getFileSystem().refresh(false);
        assertTrue("Foo.class recreated on disk", fooL.expect(WAIT));
        assertTrue("Foo.class touched", fooStatus.isBuilt());
    }
    
    /**
     * Helper class to check that changes are fired. Handles asynchronous changes
     * (since Filesystems threading obeys no known specification).
     */
    private static final class L implements ChangeListener {
        
        private boolean fired;
        
        /**
         * Create a listener, initially with no changes in it.
         */
        public L() {}
        
        public synchronized void stateChanged(ChangeEvent e) {
            fired = true;
            notify();
        }
        
        /**
         * Check whether a change has occurred by now (do not block).
         * Also resets the flag so the next call will expect a new change.
         * @return true if a change has occurred
         */
        public synchronized boolean expect() {
            boolean f = fired;
            fired = false;
            return f;
        }
        
        /**
         * Check whether a change has occurred by now or occurs within some time.
         * Also resets the flag so the next call will expect a new change.
         * @param timeout a maximum amount of time to wait, in milliseconds
         * @return true if a change has occurred
         */
        public synchronized boolean expect(long timeout) throws InterruptedException {
            if (!fired) {
                wait(timeout);
            }
            return expect();
        }
        
    }
    
}
