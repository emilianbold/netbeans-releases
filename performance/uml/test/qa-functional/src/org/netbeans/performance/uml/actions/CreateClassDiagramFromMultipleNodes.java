/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.performance.uml.actions;

import java.awt.event.InputEvent;


import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;



import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class CreateClassDiagramFromMultipleNodes extends PerformanceTestCase {

    private static String testProjectName = "jEdit-Model";
    private NbDialogOperator create_diag;

    /** Creates a new instance of CreateClassDiagramFromMultipleNodes */
    public CreateClassDiagramFromMultipleNodes(String testName) {
        super(testName);
        expectedTime = 5000;
        WAIT_AFTER_OPEN = 4000;
    }

    public CreateClassDiagramFromMultipleNodes(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 5000;
        WAIT_AFTER_OPEN = 4000;
    }

    @Override
    public void initialize() {
        log(":: initialize");
        ProjectSupport.openProject(CommonUtilities.getProjectsDir() + testProjectName);
    }

    public void prepare() {
        log(":: prepare");

        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node diag1 = new Node(pNode, "Model|org|gjt|sp|jedit|gui|AbbrevEditor");
        Node diag2 = new Node(pNode, "Model|org|gjt|sp|jedit|gui|IOProgressMonitor");
        JTreeOperator projectTree = new ProjectsTabOperator().tree();

        TreePath path1 = diag1.getTreePath();
        TreePath path2 = diag2.getTreePath();

        projectTree.clickOnPath(path1, 1, InputEvent.BUTTON1_MASK);
        new EventTool().waitNoEvent(500);
        projectTree.clickOnPath(path2, 1, InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK);
        new EventTool().waitNoEvent(2000);
        projectTree.clickOnPath(path2, 1, InputEvent.BUTTON3_MASK);

        log(projectTree.getSelectionCount() + " elements selected");

        JPopupMenuOperator selectMenu = new JPopupMenuOperator();

        selectMenu.pushMenu("Create Diagram From Selected Elements");

        create_diag = new NbDialogOperator("Create New Diagram");
        create_diag.move(0, 0);
        new EventTool().waitNoEvent(1000);
        JListOperator diag_type = new JListOperator(create_diag, 1);
        diag_type.selectItem("Class Diagram");
        JComboBoxOperator spaceCombo = new JComboBoxOperator(create_diag);
        spaceCombo.selectItem("jEdit-Model");
    }

    public ComponentOperator open() {
        log("::open");

        JButtonOperator finishButton = new JButtonOperator(create_diag, "Finish");
        finishButton.push();

        return null;
    }

    @Override
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
    }

    @Override
    public void close() {
        log("::close");
        new CloseAllDocumentsAction().performAPI();
    }

//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new CreateClassDiagramFromMultipleNodes("measureTime"));
//    }
}
