/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author Tomas Musil
 */
public class UnitTestLibrariesNodeTest extends TestBase {
    private static final String DEP_CNB = "org.openide.filesystems";
    private static int nc = 0;             //says if junit or nbjunit is present
    
    public UnitTestLibrariesNodeTest(String testName) {
        super(testName);
    }

//    XXX: failing test, fix or delete
    //this tests if node draws subnodes    
//    public void testLibrariesNodeDrawingDeps() throws Exception {
//        Lookup.getDefault().lookup(ModuleInfo.class);
//        //initial check
//        NbModuleProject p = generateStandaloneModule("module");
//
//        Node libs = new UnitTestLibrariesNode(TestModuleDependency.UNIT, p);
//        assertNotNull("have the Libraries node", libs);
//        assertEquals("nc node", nc, libs.getChildren().getNodes(true).length);
//
//        //add tests dependecy
//        ProjectXMLManager pxm = new ProjectXMLManager(p);
//        addTestDependency(p);
//        ModuleList ml = p.getModuleList();
//        Set unitDeps = pxm.getTestDependencies(ml).get(TestModuleDependency.UNIT);
//        assertNotNull("Have unit deps now", unitDeps);
//        assertEquals("one dep now", 1,  unitDeps.size());
//        assertEquals("nc+1 nodes now", nc+1, libs.getChildren().getNodes().length);
//
//        //remove test dependency
//        pxm.removeTestDependency(TestModuleDependency.UNIT, DEP_CNB);
//        ProjectManager.getDefault().saveProject(p);
//        assertEquals("nc nodes now", nc, libs.getChildren().getNodes().length);
//    }
    
    //test action on node
    public void testActions() throws Exception{
        Lookup.getDefault().lookup(ModuleInfo.class);
        NbModuleProject p = generateStandaloneModule("module");
        Node libs = new UnitTestLibrariesNode(TestModuleDependency.UNIT, p);
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
        pxm.addTestDependency(TestModuleDependency.UNIT, tmd);
        ProjectManager.getDefault().saveProject(project);
    }
    
}
