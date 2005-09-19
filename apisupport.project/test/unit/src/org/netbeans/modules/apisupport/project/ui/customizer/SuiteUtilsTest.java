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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;

/**
 * Tests {@link SuiteUtils}
 *
 * @author Martin Krauskopf
 */
public class SuiteUtilsTest extends TestBase {
    
    public SuiteUtilsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    public void testAddModule() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        NbModuleProject module1 = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 is standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        SuiteUtils.addModule(suite1, module1);
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 1, spp.getSubprojects().size());
        suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 became suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        NbModuleProject module2 = TestBase.generateStandaloneModule(getWorkDir(), "module2");
        NbModuleProject module3 = TestBase.generateStandaloneModule(getWorkDir(), "module3");
        SuiteUtils.addModule(suite1, module2);
        SuiteUtils.addModule(suite1, module3);
        
        assertEquals("three module suite components", 3, spp.getSubprojects().size());
    }
    
    public void testRemoveModuleFromSuite() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite1, "module1");
        SubprojectProvider spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("one module suite component", 1, spp.getSubprojects().size());
        
        SuiteProvider suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNotNull("module1 is suite component - has valid SuiteProvider", suiteProvider.getSuiteDirectory());
        
        SuiteUtils.removeModuleFromSuite(module1);
        spp = SuitePropertiesTest.getSubProjectProvider(suite1);
        assertEquals("doesn't have suite component", 0, spp.getSubprojects().size());
        suiteProvider = (SuiteProvider) module1.getLookup().lookup(SuiteProvider.class);
        assertNull("module1 became standalone module - doesn't have valid SuiteProvider", suiteProvider.getSuiteDirectory());
    }
    
    /** Simulates scenario when deadlock occurs when playing with 64582. */
    public void testPreventDeadLockWhenAddThenRemoveModule_64582() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        NbModuleProject module1 = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        SuiteUtils.addModule(suite1, module1);
        SuiteUtils.removeModuleFromSuite(module1);
    }
    
}

