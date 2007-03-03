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

package org.netbeans.modules.apisupport.project.ui;

import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.UnitTestLibrariesNode.RemoveDependencyAction;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author Tomas Musil
 */
public class UnitTestLibrariesNodeTest extends TestBase {
    private static final String UNIT = TestModuleDependency.UNIT;
    private static final String DEP_CNB = "org.openide.filesystems";
    private static final String JUNIT_CNB = "org.netbeans.modules.junit";
    private static final String NBJUNIT_CNB = "org.netbeans.modules.nbjunit";
    private static int nc = 0;             //says if junit or nbjunit is present
    
    public UnitTestLibrariesNodeTest(String testName) {
        super(testName);
    }
    
    //this tests if node draws subnodes    
    public void testLibrariesNodeDrawingDeps() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        //initial check
        NbModuleProject p = generateStandaloneModule("module");
        if((p.getModuleList().getEntry(JUNIT_CNB)) != null) {
            nc++;
        }
        if((p.getModuleList().getEntry(NBJUNIT_CNB)) != null) {
            nc++;
        }

        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libs = root.getChildren().findChild(UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME);
        assertNotNull("have the Libraries node", libs);
        libs.getChildren().getNodes();
        assertEquals("nc node", nc, libs.getChildren().getNodes(true).length);
        
        //add tests dependecy
        ProjectXMLManager pxm = new ProjectXMLManager(p);
        addTestDependency(p);
        ModuleList ml = p.getModuleList();
        Set unitDeps = pxm.getTestDependencies(ml).get(TestModuleDependency.UNIT);
        assertNotNull("Have unit deps now", unitDeps);
        assertEquals("one dep now", 1,  unitDeps.size());
        assertEquals("nc+1 nodes now", nc+1, libs.getChildren().getNodes().length);
        
        //remove test dependency
        pxm.removeTestDependency(UNIT, DEP_CNB);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("nc nodes now", nc, libs.getChildren().getNodes().length);
    }
    
    //test action on node
    public void testActions() throws Exception{
        Lookup.getDefault().lookup(ModuleInfo.class);
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libs = root.getChildren().findChild(UnitTestLibrariesNode.UNIT_TEST_LIBRARIES_NAME);
        assertNotNull("have the Libraries node", libs);
        //test removedep action
        addTestDependency(p);
        String depName = p.getModuleList().getEntry(DEP_CNB).getLocalizedName();
        Node depNode = libs.getChildren().findChild(depName);
        assertNotNull("have a node with dependency", depNode);
        Action[] act = depNode.getActions(false);
        assertEquals("have three actions", 3, act.length);
        RemoveDependencyAction removeAct = (RemoveDependencyAction) act[2];
        assertEquals("nc+1 nodes now", nc+1, libs.getChildren().getNodes().length);
        removeAct.performAction(new Node[] {depNode});
        assertEquals("nc nodes now, dep removed", nc, libs.getChildren().getNodes().length);
    }
    
    //TODO add more tests, try to invoke all actions on nodes, etc
    
    private void addTestDependency(NbModuleProject project) throws Exception{
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        ModuleList ml = project.getModuleList();
        ModuleEntry me = ml.getEntry(DEP_CNB);
        assertNotNull("me exist", me);
        TestModuleDependency tmd = new TestModuleDependency(me, true, true, true);
        pxm.addTestDependency(UNIT, tmd);
        ProjectManager.getDefault().saveProject(project);
    }
    
    
}
