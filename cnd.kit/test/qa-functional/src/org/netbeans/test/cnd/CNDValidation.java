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
package org.netbeans.test.cnd;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewCNDProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/** CND commit validation suite.
 *
 * @author ap153252
 */
public class CNDValidation extends JellyTestCase {

    static final String [] tests = {
                "testCreateSampleProject",
                "testClassView",
                "testBuildProject"
    };

    /** Creates a new instance of CNDValidation */
    public CNDValidation(String name) {
        super(name);
    }

    /** Defines order of test cases.
     * @return NbTestSuite instance
     */
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new CNDValidation("testCreateSampleProject"));
//        suite.addTest(new CNDValidation("testClassView"));
//        suite.addTest(new CNDValidation("testBuildProject"));
//        return suite;
//    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CNDValidation.class)
                .addTest(tests)
                .clusters(".*")
                .enableModules(".*")
                .gui(true)
                );
    }

    /** Setup before every test case. */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** Clean up after every test case. */
    public void tearDown() {
    }

    private static final String SAMPLE_PROJECT_NAME = "Welcome";

    /** Test new project
     * - open new project wizard
     * - select Samples|C/C++ Development|C/C++ category
     * - select Welcome project
     * - wait until wizard and Opening projects dialogs are closed
     * - close possible error dialogs when compiler is not found
     * - check project node appears in project view
     */
    public void testCreateSampleProject() {
        NewProjectWizardOperator.invoke().cancel(); //MacOS issue workaround
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Samples"
        String samplesLabel = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
        String develLabel = Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Samples/Native");
        //String ccLabel = Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Samples/Native/Applications");
        //npwo.selectCategory(samplesLabel + "|" + develLabel + "|" + ccLabel);
        npwo.selectCategory(samplesLabel + "|" + develLabel);
        npwo.selectProject(SAMPLE_PROJECT_NAME);
        npwo.next();
        NewCNDProjectNameLocationStepOperator npnlso = new NewCNDProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.btFinish().pushNoBlock();
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        do {
            try {
                // wait for error dialog if compiler is not set
                NbDialogOperator errorOper = new NbDialogOperator("No Compilers Found");
                errorOper.close();
            } catch (TimeoutExpiredException e) {
                // no more error dialog => we can continue
                break;
            }
        } while (true);
        npnlso.waitClosed();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
        try {
            // wait at most 120 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(openingProjectsTitle);
            openingOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
            openingOper.waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        // wait project appear in projects view
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
    }

    /** Test Class View
     * - open Window|Class View
     * - check Welcome|main node is available
     */
    public void testClassView() {
//        TopComponentOperator projectView = new TopComponentOperator("Projects");
//        new Node(new JTreeOperator(projectView), SAMPLE_PROJECT_NAME+"|Header Files|welcome.h").performPopupActionNoBlock("Open");
        new Action("Window|Classes", null).perform(); // NOI18N
        TopComponentOperator classView = new TopComponentOperator("Classes"); // NOI18N
        new Node(new JTreeOperator(classView), SAMPLE_PROJECT_NAME+"|main");
    }

    /** Test build project
     * - call Clean and Build on project node
     * - if compiler is not set, close 'Resolve Missing...' dialog
     * - otherwise wait for clean and build finished
     */
    public void testBuildProject() {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
        // "Clean and Build"
        String buildItem = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.Bundle", "LBL_RebuildAction_Name");
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new ActionNoBlock(null, buildItem).perform(projectNode);
        try {
            // wait for possible dialog when compiler are not set
            NbDialogOperator resolveOper = new NbDialogOperator("Resolve Missing Native Build Tools");
            // close and finish the test
            resolveOper.close();
            return;
        } catch (TimeoutExpiredException e) {
            // ignore when it doesn't appear
        }
        // wait message "Clean successful"
        stt.waitText("Clean successful", true); // NOI18N
        // wait message "Build successful."
        stt.waitText(Bundle.getString("org.netbeans.modules.cnd.builds.Bundle", "MSG_BuildFinishedOK"), true);
        stt.stop();
    }
}
