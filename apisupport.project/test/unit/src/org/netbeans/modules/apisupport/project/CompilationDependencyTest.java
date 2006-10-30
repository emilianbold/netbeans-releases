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

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Petr Zajac
 */
public class CompilationDependencyTest extends TestBase {
    
    private final static String WINDOWS = "org.openide.windows";
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public CompilationDependencyTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
        
    }
    
    protected void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }
    
    public void testInvalidSpecVersion() throws Exception {
        NbModuleProject testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
        
        FileObject buildScript = findBuildXml(testingProject);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"jar"}, null);
        et.waitFinished();
        assertEquals("Error during ant ...",0,et.result());
        SpecificationVersion invalid = new SpecificationVersion("1000");
        Util.addDependency(testingProject, WINDOWS, null, invalid, true);
        ProjectManager.getDefault().saveProject(testingProject);
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","jar"}, null);
        et.waitFinished();
        
        // it must fail but I don't know why it passed
        assertFalse("Error during ant ...", 0 == et.result());
        assertFalse("Successfully compiled when is invalid specification version",
                testingProject.getModuleJarLocation().exists());
    }
    
    public void testCompileAgainstPublicPackage() throws Exception {
        NbModuleProject testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
        FileObject buildScript = findBuildXml(testingProject);
        assertNotNull(buildScript);
        
        Util.addDependency(testingProject, WINDOWS);
        ProjectManager.getDefault().saveProject(testingProject);
        
        FileObject javaFo = testingProject.getSourceDirectory().getFileObject("org/example/testing").createData("JavaFile.java");
        FileLock lock = javaFo.lock();
        PrintStream ps = new PrintStream(javaFo.getOutputStream(lock));
        ps.println("package org.example.testing;");
        ps.println("import org.netbeans.modules.openide.windows.*;");
        ps.println("public class JavaFile {}");
        ps.close();
        lock.releaseLock();
        
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        et.waitFinished();
        
        assertFalse("project was successfully compiled against non public package",
                testingProject.getModuleJarLocation().exists());
        
        ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        testingPXM.removeDependency(WINDOWS);
        ModuleEntry module = testingProject.getModuleList().getEntry(WINDOWS);
        ModuleDependency newDep = new ModuleDependency(module,module.getReleaseVersion(),module.getSpecificationVersion(),true,true);
        testingPXM.addDependency(newDep);
        ProjectManager.getDefault().saveProject(testingProject);
        
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        Reader reader = et.getInputOutput().getIn();
        BufferedReader breader = new BufferedReader(reader);
        et.waitFinished();
        String line = null;
        while ((line = breader.readLine()) != null ) {
            log(line);
            System.out.println(line);
        }
        assertTrue("compilation failed for implementation dependency",
                testingProject.getModuleJarLocation().exists());
    }
    
    public void testCompileAgainstRemovedModule68716() throws Exception {
        SuiteProject suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        SuiteProjectTest.openSuite(suite);
        Util.addDependency(proj, WINDOWS);
        
        // remove WINDOWS from platform
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.DISABLED_MODULES_PROPERTY, WINDOWS);
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(proj);
        ProjectManager.getDefault().saveProject(suite);
        
        // build project
        FileObject buildScript = proj.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        et.waitFinished();
        assertFalse("project was successfully compiled against removed module from platform",proj.getModuleJarLocation().exists());
    }
    
    private static FileObject findBuildXml(final NbModuleProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
}

