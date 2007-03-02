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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Test build-impl.xml functionality.
 * Large portion of this class was copied from JavaAntLoggerTest.
 * @author Jesse Glick, David Konecny
 */
public final class BuildImplTest extends NbTestCase {
    
    public BuildImplTest(String name) {
        super(name);
    }
    
    private File junitJar;
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        output.clear();
        outputType.clear();
        String junitJarProp = System.getProperty("test.junit.jar");
        assertNotNull("must set test.junit.jar", junitJarProp);
        junitJar = new File(junitJarProp);
        assertTrue("file " + junitJar + " exists", junitJar.isFile());
        MockServices.setServices(IOP.class, IFL.class);
    }
    
    private AntProjectHelper setupProject(String subFolder, int numberOfSourceFiles, boolean generateTests) throws Exception {
        File proj = getWorkDir();
        if (subFolder != null) {
            proj = new File(getWorkDir(), subFolder);
        }
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, subFolder != null ? subFolder + getName() : getName(), (String)null, (String)null);
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        FileObject root = aph.getProjectDirectory();
        for (int i=0; i<numberOfSourceFiles; i++) {
            generateJava(root, "src/pkg/Source" + i + ".java", false);
            if (generateTests) {
                generateJava(root, "test/pkg/Source" + i + "Test.java", true);
            }
        }
        return aph;
    }
    
    private AntProjectHelper setupProject(int numberOfSourceFiles, boolean generateTests) throws Exception {
        clearWorkDir();
        return setupProject(null, numberOfSourceFiles, generateTests);
    }

    private void generateJava(FileObject root, String path, boolean test) throws Exception {
        String name = path.replaceFirst("^.+/", "").replaceFirst("\\..+$", "");
        if (test) {
            writeFile(root, path,
                "package pkg;\n" +
                "import junit.framework.TestCase;\n" +
                "public class " + name + " extends TestCase {\n" +
                "public " + name + "() { }\n"+
                "public void testDoSomething() { System.out.println(\"" + name + " test executed\"); }\n" +
                "}\n");
        } else {
            writeFile(root, path,
                "package pkg;\n" +
                "public class " + name + " {\n" +
                "public boolean doSomething() { return true; }\n" +
                "public static void main(String[] args) { System.err.println(\"" + name + " main class executed\"); }\n" +
                "}\n");
        }
    }

    private FileObject writeFile(FileObject root, String path, String body) throws Exception {
        FileObject fo = FileUtil.createData(root, path);
        OutputStream os = fo.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
        pw.print(body);
        pw.flush();
        os.close();
        return fo;
    }

    private Properties getProperties() {
        Properties p = new Properties();
        p.setProperty("libs.junit.classpath", junitJar.getAbsolutePath());
        return p;
    }
    
    public void testDefaultTargets() throws Exception {
        AntProjectHelper aph = setupProject(1, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, null, p));
        assertTrue("Default target must test project", output.contains("test:"));
        assertTrue("Default target must jar project", output.contains("jar:"));
        assertTrue("Default target must build javadoc", output.contains("javadoc:"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/test/classes/pkg/Source0Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull("dist/testDefaultTargets.jar must exist", fo.getFileObject("dist/testDefaultTargets.jar"));
        assertNotNull("dist/javadoc/index.html must exist", fo.getFileObject("dist/javadoc/index.html"));
    }
    
    public void testCompile() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testCompileSingle() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile-single:"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertEquals("Only one class should be compiled", 1, fo.getFileObject("build/classes/pkg").getChildren().length);
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testIncludesExcludes() throws Exception {
        AntProjectHelper aph = setupProject(12, true);
        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(J2SEProjectProperties.INCLUDES, "**/*1*");
        ep.setProperty(J2SEProjectProperties.EXCLUDES, "**/*0*");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveAllProjects();
        FileObject dir = aph.getProjectDirectory();
        FileUtil.createData(dir, "src/data0.xml");
        FileUtil.createData(dir, "src/data1.xml");
        FileUtil.createData(dir, "src/data2.xml");
        FileUtil.createData(dir, "src/data10.xml");
        FileUtil.createData(dir, "src/data11.xml");
        generateJava(dir, "test/pkg/Utils1.java", true);
        FileObject buildXml = dir.getFileObject("build.xml");
        assertNotNull(buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"test"}, p));
        assertNull(dir.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source1.class"));
        assertNull(dir.getFileObject("build/classes/pkg/Source2.class"));
        assertNull(dir.getFileObject("build/classes/pkg/Source10.class"));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source11.class"));
        assertNull(dir.getFileObject("build/classes/data0.xml"));
        assertNotNull(dir.getFileObject("build/classes/data1.xml"));
        assertNull(dir.getFileObject("build/classes/data2.xml"));
        assertNull(dir.getFileObject("build/classes/data10.xml"));
        assertNotNull(dir.getFileObject("build/classes/data11.xml"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source1Test.class"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source2Test.class"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source10Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source11Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Utils1.class"));
        assertFalse(output.contains("Source0Test test executed"));
        assertTrue(output.contains("Source1Test test executed"));
        assertFalse(output.contains("Source2Test test executed"));
        assertFalse(output.contains("Source10Test test executed"));
        assertTrue(output.contains("Source11Test test executed"));
        assertFalse(output.contains("Utils1 test executed"));
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"javadoc"}, p));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source0.html"));
        assertNotNull(dir.getFileObject("dist/javadoc/pkg/Source1.html"));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source2.html"));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source10.html"));
        assertNotNull(dir.getFileObject("dist/javadoc/pkg/Source11.html"));
        p.setProperty("javac.includes", "pkg/Source4.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source4.class"));
        p.setProperty("javac.includes", "pkg/Source4Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source4Test.class"));
        p.setProperty("javac.includes", "pkg/Source7Test.java");
        p.setProperty("test.includes", "pkg/Source7Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"test-single"}, p));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source7Test.class"));
        assertTrue(output.contains("Source7Test test executed"));
        writeFile(dir, "src/RefersToExcluded1a.java", "class RefersToExcluded1a {{new pkg.Source11();}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
        writeFile(dir, "src/RefersToExcluded1b.java", "class RefersToExcluded1b {{new pkg.Source10();}}");
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
    }
    
    private void touch(FileObject f, FileObject ref) throws Exception {
        File ff = FileUtil.toFile(f);
        long older = ff.lastModified();
        if (ref != null) {
            older = Math.max(older, FileUtil.toFile(ref).lastModified());
        } 
        for (long pause = 1; pause < 9999; pause *= 2) {
            Thread.sleep(pause);
            ff.setLastModified(System.currentTimeMillis());
            if (ff.lastModified() > older) {
                return;
            }
        }
        fail("Did not manage to touch " + ff);
    }
    
    /** @see "issue #36033" */
    public void testCompileWithDependencyAnalysis() throws Exception {
        AntProjectHelper aph = setupProject(0, false);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        FileObject d = aph.getProjectDirectory();
        FileObject x = writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y1();}}");
        FileObject y = writeFile(d, "src/p/Y.java", "package p; public class Y {static void y1() {}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        writeFile(d, "src/p/Y.java", "package p; public class Y {static void y2() {}}");
        touch(y, d.getFileObject("build/classes/p/Y.class"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y2();}}");
        touch(x, null);
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        FileObject yt = writeFile(d, "test/p/YTest.java", "package p; public class YTest extends junit.framework.TestCase {public void testY() {Y.y2();}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
        writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y1();}}");
        touch(x, d.getFileObject("build/classes/p/X.class"));
        writeFile(d, "src/p/Y.java", "package p; public class Y {static void y1() {}}");
        touch(y, d.getFileObject("build/classes/p/Y.class"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
        writeFile(d, "test/p/YTest.java", "package p; public class YTest extends junit.framework.TestCase {public void testY() {Y.y1();}}");
        touch(yt, null);
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
    }
    
    public void testRun() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("main.class", "pkg.Source1");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"run"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("run target was not executed", output.contains("run:"));
        assertTrue("main class was not executed", output.contains("Source1 main class executed"));
       
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testRunSingle() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("main.class", "pkg.Source0");
        p.setProperty("javac.includes", "pkg/Source2.java");
        p.setProperty("run.class", "pkg.Source2");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"run-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile-single:"));
        assertTrue("run target was not executed", output.contains("run-single:"));
        assertTrue("main class was not executed", output.contains("Source2 main class executed"));
       
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertEquals("Only one class should be compiled", 1, fo.getFileObject("build/classes/pkg").getChildren().length);
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testJar() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        Attributes mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertNull("Main-class was not set", mf.getValue("Main-class"));

        // set a manifest
        
        writeFile(aph.getProjectDirectory(), "manifest/manifest.mf",
            "Manifest-Version: 1.0\n" +
            "Something: s.o.m.e\n\n");
        p.setProperty("manifest.file", "manifest/manifest.mf");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertEquals("Specified manifest was not used", "s.o.m.e", mf.getValue("Something"));
        assertNull("Main-class was not set", mf.getValue("Main-class"));

        // set a mainclass
        
        p.setProperty("main.class", "some.clazz.Main");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertEquals("Specified manifest was not used", "s.o.m.e", mf.getValue("Something"));
        assertEquals("Main-class was not set", "some.clazz.Main", mf.getValue("Main-class"));
    }
    
    public void testJavadoc() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"javadoc"}, p));
        assertTrue("javadoc target was not executed", output.contains("javadoc:"));
       
        FileObject fo = aph.getProjectDirectory();
        assertNull("build folder should not be created", fo.getFileObject("build"));
        assertNull("dist/testJavadoc.jar should not exist", fo.getFileObject("dist/testJavadoc.jar"));
        assertNotNull("dist/javadoc/index.html must exist", fo.getFileObject("dist/javadoc/index.html"));
    }

    public void testTest() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"test"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("compile-test target was not executed", output.contains("compile-test:"));
        assertTrue("test target was not executed", output.contains("test:"));
        assertTrue("test 0 was not executed", output.contains("Source0Test test executed"));
        assertTrue("test 1 was not executed", output.contains("Source1Test test executed"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/test/classes/pkg/Source0Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull("build/test/classes/pkg/Source1Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source1Test.class"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testCompileSingleTest() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile-test-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile:"));
        assertTrue("compile-single target was not executed", output.contains("compile-test-single:"));
        
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNotNull("build/test/classes/pkg/Source2Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source2Test.class"));
        assertEquals("Only one test class should be compiled", 1, fo.getFileObject("build/test/classes/pkg").getChildren().length);
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testRunSingleTest() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2Test.java");
        p.setProperty("test.includes", "pkg/Source2Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"test-single"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("run target was not executed", output.contains("compile-test-single:"));
        assertTrue("run target was not executed", output.contains("test-single:"));
        assertTrue("test was not executed", output.contains("Source2Test test executed"));
       
        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNotNull("build/test/classes/pkg/Source2Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source2Test.class"));
        assertEquals("Only one test class should be compiled", 1, fo.getFileObject("build/test/classes/pkg").getChildren().length);
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }
    
    public void testSubprojects() throws Exception {
        clearWorkDir();
        AntProjectHelper aph1 = setupProject("p1", 1, false);
        AntProjectHelper aph2 = setupProject("p2", 1, false);
        Project proj1 = ProjectManager.getDefault().findProject(aph1.getProjectDirectory());
        Project proj2 = ProjectManager.getDefault().findProject(aph2.getProjectDirectory());
        ReferenceHelper refHelper = ((J2SEProject)proj1).getReferenceHelper();
        AntArtifactProvider aap = proj2.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact[] aa = aap.getBuildArtifacts();
        assertTrue("Project should have an artifact", aa.length > 0);
        assertTrue("Reference was not added?", refHelper.addReference(aa[0]));
        ProjectManager.getDefault().saveAllProjects();
        FileObject fo = aph1.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));

        FileObject buildXml = aph1.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("no.dependencies", "true");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("jar target was not executed", output.contains("jar:"));
        output.remove("jar:");
        assertFalse("subproject's jar should not be executed", output.contains("jar:"));
        fo = aph1.getProjectDirectory();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));
        
        p.setProperty("no.dependencies", "false");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("jar target was not executed", output.contains("jar:"));
        output.remove("jar:");
        assertTrue("subproject's jar target was not executed", output.contains("jar:"));
        fo = aph1.getProjectDirectory();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));

        p.setProperty("no.dependencies", "true");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"clean"}, p));
        assertTrue("clean target was not executed", output.contains("clean:"));
        output.remove("clean:");
        assertFalse("subproject's clean should not be executed", output.contains("clean:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        fo.refresh();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));
        
        p.setProperty("no.dependencies", "false");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"clean"}, p));
        assertTrue("clean target was not executed", output.contains("clean:"));
        output.remove("clean:");
        assertTrue("subproject's clean target was not executed", output.contains("clean:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNull("build folder must be removed", fo.getFileObject("build"));
        assertNull("dist folder must be removed", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        fo.refresh();
        assertNull("build folder must be removed", fo.getFileObject("build"));
        assertNull("dist folder must be removed", fo.getFileObject("dist"));
    }
    
    
    private Attributes getJarManifest(FileObject fo) throws Exception {
        File f = FileUtil.toFile(fo);
        JarFile jf = new JarFile(f);
        Attributes attrs = (Attributes)jf.getManifest().getMainAttributes().clone();
        jf.close();
        return attrs;
    }
    
    private void assertBuildSuccess(ExecutorTask task) {
        if (task.result() != 0) {
            dumpOutput();
            fail("target failed");
        }
    }

    private void assertBuildFailure(ExecutorTask task) {
        if (task.result() == 0) {
            dumpOutput();
            fail("target failed");
        }
    }

    private void dumpOutput() {
        List<String> output = new ArrayList<String>(this.output);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (String line : output) {
            System.out.println(line);
        }
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @SuppressWarnings("deprecation")
    public static final class IOP extends IOProvider implements InputOutput {
        
        public IOP() {}

        public InputOutput getIO(String name, boolean newIO) {
            return this;
        }

        public OutputWriter getStdOut() {
            throw new UnsupportedOperationException();
        }

        public OutputWriter getOut() {
            return new OW(false);
        }

        public OutputWriter getErr() {
            return new OW(true);
        }

        public Reader getIn() {
            return new StringReader("");
        }

        public Reader flushReader() {
            return getIn();
        }

        public void closeInputOutput() {}

        public boolean isClosed() {
            return false;
        }

        public boolean isErrSeparated() {
            return false;
        }

        public boolean isFocusTaken() {
            return false;
        }

        public void select() {}

        public void setErrSeparated(boolean value) {}

        public void setErrVisible(boolean value) {}

        public void setFocusTaken(boolean value) {}

        public void setInputVisible(boolean value) {}

        public void setOutputVisible(boolean value) {}
        
    }
    
    private static final List<String> output = new ArrayList<String>();
    private static final List<String> outputType = new ArrayList<String>();
    
    private static final String TYPE_ERR = "err";
    private static final String TYPE_OK = "ok";
    
    private static final class OW extends OutputWriter {
        
        private final boolean err;
        
        public OW(boolean err) {
            super(new StringWriter());
            this.err = err;
        }

        public void println(String s, OutputListener l) throws IOException {
            message(s, l != null);
        }

        public void println(String x) {
            message(x, false);
        }
        
        private void message(String msg, boolean hyperlinked) {
            output.add(msg);
            String type = err ? TYPE_ERR : TYPE_OK;
            outputType.add(type);
        }
        
        public void reset() throws IOException {}

    }

    /** Copied from AntLoggerTest. */
    public static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
