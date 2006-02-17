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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;

/**
 * Tests {@link SuiteUtils}
 *
 * @author Martin Krauskopf
 */
public class SuiteUtilsTest extends TestBase {
    
    public SuiteUtilsTest(String name) {
        super(name);
    }
    
    public void testAddModule() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = generateStandaloneModule("module1");
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 is standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        SuiteUtils.addModule(suite1, module1);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 1, spp.getSubprojects().size());
        suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        NbModuleProject module2 = generateStandaloneModule("module2");
        NbModuleProject module3 = generateStandaloneModule("module3");
        SuiteUtils.addModule(suite1, module2);
        SuiteUtils.addModule(suite1, module3);
        
        assertEquals("three module suite components", 3, spp.getSubprojects().size());
    }
    
    public void testRemoveModuleFromSuite() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 1, spp.getSubprojects().size());
        
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 is suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        assertNull("user.properites.file property doesn't exist", module1.evaluator().getProperty("user.properties.file"));
        SuiteUtils.removeModuleFromSuite(module1);
        assertEquals("user.properties.file resolved for standalone module",
                getWorkDirPath() + File.separatorChar + "build.properties",
                module1.evaluator().getProperty("user.properties.file"));
        spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("doesn't have suite component", 0, spp.getSubprojects().size());
        suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 became standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
    }
    
    public void testRemoveModuleFromSuiteWithDependencies() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
        NbModuleProject module2 = TestBase.generateSuiteComponent(suite1, "module2");
        
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("two suite components", 2, spp.getSubprojects().size());
        
        Util.addDependency(module2, module1);
        ProjectManager.getDefault().saveProject(module2);
        ProjectXMLManager pxm2 = new ProjectXMLManager(module2);
        assertEquals("one dependency", 1, pxm2.getDirectDependencies().size());
        
        SuiteUtils.removeModuleFromSuiteWithDependencies(module1);
        spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one suite component", 1, spp.getSubprojects().size());
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 became standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        pxm2 = new ProjectXMLManager(module2);
        assertEquals("dependency was removed", 0, pxm2.getDirectDependencies().size());
    }
    
    /** Simulates scenario when deadlock occurs when playing with 64582. */
    public void testPreventDeadLockWhenAddThenRemoveModule_64582() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SuiteUtils.addModule(suite1, module1);
        SuiteUtils.removeModuleFromSuite(module1);
    }
    
    public void testAddTwoModulesWithTheSameCNB_62819() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1a = generateStandaloneModule("module1");
        File otherDir = new File(getWorkDir(), "otherDir");
        otherDir.mkdir();
        NbModuleProject module1b = TestBase.generateStandaloneModule(otherDir, "module1");
        
        SuiteUtils.addModule(suite1, module1a);
        SuiteUtils.addModule(suite1, module1b);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("cannot add two suite components with the same cnb", 1, spp.getSubprojects().size());
        
        SuiteProvider suiteProvider = (SuiteProvider) module1a.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1a became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        suiteProvider = (SuiteProvider) module1b.getLookup().lookup(SuiteProvider.class);
        assertNull("module1b remains standalone - has not valid SuiteProvider", suiteProvider.getSuiteDirectory());
    }
    
    public void testGeneratingOfUniqAntProperty_62819() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        NbModuleProject module1 = generateStandaloneModule("module1");
        NbModuleProject module2 = generateStandaloneModule("module2");
        
        SuiteUtils.addModule(suite1, module1);
        FileObject propsFO = suite1.getProjectDirectory().getFileObject("nbproject/project.properties");
        EditableProperties props = Util.loadProperties(propsFO);
        assertEquals("modules property", "${project.org.example.module1}", props.getProperty("modules"));
        assertEquals("module1 property", "../module1", props.getProperty("project.org.example.module1"));
        
        // user is free to do this, although in more sensible way
        assertEquals("module1 project removed (sanity check)", "../module1", props.remove("project.org.example.module1"));
        props.setProperty("modules", "${project.org.example.module2}");
        props.setProperty("project.org.example.module2", "../module1");
        Util.storeProperties(propsFO, props);
        
        SuiteUtils.addModule(suite1, module2);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 2, spp.getSubprojects().size());
        
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        suiteProvider = (SuiteProvider) module2.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module2 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
    }
    
    public void testIsSuite() throws Exception {
        SuiteProject suite = generateSuite("suite");
        generateSuiteComponent(suite, "suiteComponent");
        generateStandaloneModuleDirectory(getWorkDir(), "module");
        File suiteF = new File(getWorkDir(), "suite");
        assertTrue(suite + " is a suite", SuiteUtils.isSuite(suiteF));
        assertFalse(suite + " is not a suite", SuiteUtils.isSuite(new File(suiteF, "suiteComponent")));
        assertFalse(suite + " is not a suite", SuiteUtils.isSuite(new File(getWorkDir(), "module")));
    }
    
}

