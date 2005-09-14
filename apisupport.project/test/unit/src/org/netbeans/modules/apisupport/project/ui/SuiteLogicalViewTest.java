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

package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;

/**
 * Test functionality of {@link SuiteLogicalView}.
 *
 * @author Martin Krauskopf
 */
public class SuiteLogicalViewTest extends TestBase {
    
    public SuiteLogicalViewTest(String name) {
        super(name);
    }
    
    public void testModulesNode() throws Exception {
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        TestBase.generateSuiteComponent(suite1, "module1a");
        SuiteLogicalView.ModulesNode modulesNode = new SuiteLogicalView.ModulesNode(suite1);
        assertEquals("one children", 1, modulesNode.getChildren().getNodes().length);
        
        TestBase.generateSuiteComponent(suite1, "module1b");
        Thread.sleep(SuiteLogicalView.MODULES_NODE_SCHEDULE * 2);
        assertEquals("two children", 2, modulesNode.getChildren().getNodes().length);
    }
    
}
