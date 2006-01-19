/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Petr Zajac
 */
public class CompilationDependencyTest extends TestBase {
    
    private final static String ANT_PROJECT_SUPPORT = "org.netbeans.modules.project.ant";
    private final static String WINDOWS = "org.openide.windows";
    private final static Set ASSUMED_CNBS;
    
    static {
        
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        
        Set assumedCNBs = new HashSet(2);
        assumedCNBs.add(ANT_PROJECT_SUPPORT);
        assumedCNBs.add(WINDOWS);
        ASSUMED_CNBS = Collections.unmodifiableSet(assumedCNBs);
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
    

    
    private NbModuleProject testingProject ;
    
    public void testInvalidSpecVersion() throws Exception {
        testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
       
        FileObject buildScript = findBuildXml();
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"jar"}, null);
        et.waitFinished();
        assertEquals("Error during ant ...",0,et.result());
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        NbPlatform platform = testingProject.getPlatform(true);
        ModuleEntry modules[] = platform.getModules();
        ModuleEntry module = null;
        for (int mIt = 0 ; mIt < modules.length ; mIt++) {
            module = modules[mIt];
            if (module.getCodeNameBase().equals(WINDOWS)) {
                break;
            }
        }
        ModuleDependency newDep = new ModuleDependency(
                module,
                module.getReleaseVersion(),
                "1000", // nonsence
                true,
                false);
        testingPXM.addDependency(newDep);
        ProjectManager.getDefault().saveProject(testingProject);
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","jar"}, null);
        et.waitFinished();
        
        // it must fail but I don't know why it passed
        assertFalse("Error during ant ...",0  == et.result());
        assertFalse("Successfully compiled when is invalid specification version",
                   testingProject.getModuleJarLocation().exists());
    }
    
    public void testCompileAgaistPublicPackage() throws Exception {
        testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
        FileObject buildScript = findBuildXml();
        assertNotNull(buildScript);
        Properties antProps = new Properties();
        
         ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        NbPlatform platform = testingProject.getPlatform(true);
        ModuleEntry modules[] = platform.getModules();
        ModuleEntry module = null;
        for (int mIt = 0 ; mIt < modules.length ; mIt++) {
            module = modules[mIt];
            if (module.getCodeNameBase().equals(WINDOWS)) {
                break;
            }
        }
        testingPXM.removeDependency(WINDOWS);
        
        ModuleDependency newDep = new ModuleDependency(module);
        testingPXM.addDependency(newDep);
        ProjectManager.getDefault().saveProject(testingProject);
        FileObject javaFo = testingProject.getSourceDirectory().getFileObject("org/example/testing").createData("JavaFile.java");
        FileLock lock = javaFo.lock();
        PrintStream ps = new PrintStream(javaFo.getOutputStream(lock));
        ps.println("package org.example.testing;");
        ps.println("import org.netbeans.modules.openide.windows.*;");
        ps.println("public class JavaFile {}");
        
        ps.close();
        lock.releaseLock();
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, antProps);
        et.waitFinished();
        
        assertFalse("project was successfully compiled against non public package",
                testingProject.getModuleJarLocation().exists());

        testingPXM = new ProjectXMLManager(testingProject.getHelper());
        testingPXM.removeDependency(WINDOWS);
        newDep = new ModuleDependency(module,module.getReleaseVersion(),module.getSpecificationVersion(),true,true);
        testingPXM.addDependency(newDep); 
        ProjectManager.getDefault().saveProject(testingProject);
        
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, antProps);
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
        NbPlatform platform = proj.getPlatform(true);
        ModuleEntry modules[] = platform.getModules(); 
        ProjectXMLManager testingPXM = new ProjectXMLManager(proj.getHelper());
        ModuleEntry module = null;
        for (int mIt = 0 ; mIt < modules.length ; mIt++) {
            module = modules[mIt];
            if (module.getCodeNameBase().equals(WINDOWS)) {
                break;
            }
        }
        testingPXM.removeDependency(WINDOWS);
        ModuleDependency newDep = new ModuleDependency(module);
        testingPXM.addDependency(newDep);
        // remove WINDOWS from platform
        //
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties"); 
        ep.setProperty(SuiteProperties.DISABLED_MODULES_PROPERTY,module.getCodeNameBase());   
        suite.getHelper().putProperties("nbproject/platform.properties", ep); 
        ProjectManager.getDefault().saveProject(proj);       
        ProjectManager.getDefault().saveProject(suite);       
        //// build project
        FileObject buildScript = proj.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        et.waitFinished();
        assertFalse("project was successfully compiled against removed module from platform",proj.getModuleJarLocation().exists());
  
        
    }
    
    private FileObject findBuildXml() {
        return testingProject.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
}

