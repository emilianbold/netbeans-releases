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

package org.netbeans.modules.j2ee.earproject.ui;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * Test functionality of {@link ModuleNode} and maybe more of EAR's
 * {@link LogicalViewProvider logical view provider}.
 *
 * @author Martin Krauskopf
 */
public class ModuleNodeTest extends NbTestCase {
    
    private static final int CHILDREN_UPDATE_TIME_OUT = 20000;
    
    private String serverInstanceID;
    
    public ModuleNodeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        serverInstanceID = TestUtil.registerSunAppServer(this);
    }
    
    public void testRemoveFromJarContent() throws Exception {
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = "1.4";
        String jarName = "testEA-ejb";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeLevel,
                serverInstanceID, null, null, jarName, null, null, null);
        
        Project earProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        
        LogicalViewProvider lvp = (LogicalViewProvider) earProject.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        LogicalViewNode j2eeModules = (LogicalViewNode) root.getChildren().findChild(LogicalViewNode.J2EE_MODULES_NAME);
        assertSame("have ejb module's node", 1, j2eeModules.getChildren().getNodes(true).length);
        
        ModuleNode moduleNode = (ModuleNode) j2eeModules.getChildren().findChild(ModuleNode.MODULE_NODE_NAME);
        assertNotNull("have modules node", moduleNode);
        moduleNode.removeFromJarContent();
        assertNumberOfNodes("ejb module removed", j2eeModules, 0);
    }
    
    // See also issue #70943
    public void testRemoveFromJarContentWithDeletedProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        String j2eeLevel = "1.4";
        String jarName = "testEA-ejb";
        
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeLevel,
                serverInstanceID, null, null, jarName, null, null, null);
        
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        Project earProject = ProjectManager.getDefault().findProject(prjDirFO);
        
        LogicalViewProvider lvp = (LogicalViewProvider) earProject.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        LogicalViewNode j2eeModules = (LogicalViewNode) root.getChildren().findChild(LogicalViewNode.J2EE_MODULES_NAME);
        assertSame("have ejb module's node", 1, j2eeModules.getChildren().getNodes(true).length);
        
        ModuleNode moduleNode = (ModuleNode) j2eeModules.getChildren().findChild(ModuleNode.MODULE_NODE_NAME);
        assertNotNull("have modules node", moduleNode);
        
        // Simulata one of scenarios in #70943
        FileObject ejbJarFO = prjDirFO.getFileObject("testEA-ejb");
        ejbJarFO.delete();
        moduleNode.removeFromJarContent();
        j2eeModules.getChildren().getNodes(true);
        
        assertNumberOfNodes("ejb module removed", j2eeModules, 0);
    }
    
    private void assertNumberOfNodes(final String message, final LogicalViewNode j2eeModules,
            int expectedNumber) throws InterruptedException {
        int waitTime = 0;
        boolean failed = false;
        while (!failed && j2eeModules.getChildren().getNodes(true).length != 0) {
            failed = ++waitTime > CHILDREN_UPDATE_TIME_OUT/50;
            Thread.sleep(50);
        }
        assertFalse(message, failed);
    }
    
}
