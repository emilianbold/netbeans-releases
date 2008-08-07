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
package org.netbeans.test.profiler;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OptionsViewAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.ide.WatchProjects;

/** Validation test of profiler.
 *
 * @author Alexandr Scherbatiy, Jiri Skrivanek
 */
public class ProfilerValidation extends JellyTestCase {
    
    private static final String SAMPLE_PROJECT_NAME = "AnagramGame";

    protected static final String  PROFILER_ACTIONS_BUNDLE = "org.netbeans.modules.profiler.actions.Bundle";
    protected static final String  PROFILER_UI_PANELS_BUNDLE = "org.netbeans.modules.profiler.ui.panels.Bundle";
    
    protected static final String  BUTTON_RUN = "Run"; // not used in tests yet
    protected static final String  TEXT_OUTPUT = "Established local connection with the tool"; // not used in tests yet

    
    static String[] tests = new String[]{
            "testProfilerMenus",
            "testProfilerProperties",
//            "testCreateProject",
//            "testProfiler"
    };
    /** Default constructor.
     * @param name test case name
     */
    public ProfilerValidation(String name){
        super(name);
    }
    
    /** Defaine order of test cases.
     * @return NbTestSuite instance
     */
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new ProfilerValidation("testProfilerMenus"));
//        suite.addTest(new ProfilerValidation("testProfilerProperties"));
//        //suite.addTest(new ProfilerValidation("testCreateProject"));
//        //suite.addTest(new ProfilerValidation("testProfiler"));
//        return suite;
//    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ProfilerValidation.class)
                .addTest(tests)
                .clusters(".*")
                .enableModules(".*")
                .gui(true)
                );
    }
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //TestRunner.run(new ProfilerValidation("testProfiler"));
    }

    /** Setup before every test case. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Test Profiler Menus. */
    public void testProfilerMenus(){
        //Profile|Profile Project
        new ActionNoBlock("Profile|" + Bundle.getStringTrimmed(PROFILER_ACTIONS_BUNDLE,
                                        "LBL_ProfileMainProjectAction"), null).isEnabled();
        //Profile|Attach Profiler...
        new ActionNoBlock("Profile|" + Bundle.getStringTrimmed(PROFILER_ACTIONS_BUNDLE,
                                        "LBL_AttachMainProjectAction"), null).isEnabled();
        //Profile|Take Snapshot of Collected Results
        new ActionNoBlock("Profile|" + Bundle.getStringTrimmed(PROFILER_ACTIONS_BUNDLE,
                                        "LBL_TakeSnapshotAction"), null).isEnabled();
        //Profile|Stop Profiling Session
        new ActionNoBlock("Profile|" + Bundle.getStringTrimmed(PROFILER_ACTIONS_BUNDLE,
                                        "LBL_StopAction"), null).isEnabled();
        
    }
    
    /** Test Profiler Properties. */
    public void testProfilerProperties(){
        new OptionsViewAction().performMenu();
        
        OptionsOperator options = new OptionsOperator();
        options.selectCategory("Miscellaneous");        
        
        JTabbedPaneOperator tabbedPane = new JTabbedPaneOperator(options);
        tabbedPane.selectPage("Profiler");

        JLabelOperator javaPlatform = new JLabelOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_JavaPlatformLabelText")); //"Profiler Java Platform"

        JLabelOperator communicationPort = new JLabelOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_CommPortLabelText") );//"Communication Port"

        JLabelOperator openThreads = new JLabelOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_ThreadsViewLabelText") );//"Open Threads View"
        JCheckBoxOperator cpu    = new JCheckBoxOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_CpuChckBoxText") );//"CPU"
        JCheckBoxOperator memory = new JCheckBoxOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_MemoryChckBoxText") );//"Memory"

        JComboBoxOperator openNewSnapshot= new JComboBoxOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_OpenSnapshotRadioText") );//"Open New Snapshot"

        JCheckBoxOperator enableHeapAnalisys = new JCheckBoxOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_EnableAnalysisCheckbox") ); //"Enable Rule-Based Heap Analysis"

        JButtonOperator reset = new JButtonOperator(options, Bundle.getStringTrimmed(PROFILER_UI_PANELS_BUNDLE,
                                                                "ProfilerOptionsPanel_ResetButtonName") ); //"Reset"
        
        new JButtonOperator(options, "OK").push();
        
    }    
    
    /** Create project to be tested. */
    public void testCreateProject() {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Samples|Java");
        npwo.selectProject("Anagram Game");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.btFinish().pushNoBlock();
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        npnlso.waitClosed();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
        waitProgressDialog(openingProjectsTitle, 120000);
        // wait project appear in projects view
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
        // wait classpath scanning finished
        WatchProjects.waitScanFinished();
        projectNode.buildProject();
        MainWindowOperator.getDefault().waitStatusText("Finished Building");
        
    }
    
    /** Test profiler
     * - run profiler calibration Profile|Advanced Commands|Run Profiler Calibration
     * - wait for calibration results and confirm information dialog
     * - call Profile|Profile Main Project
     * - confirm changes in project when profiled for the first time
     * - click Run in Profile AnagramGame dialog
     * - wait for Profiler view
     * - wait until text "Established local connection with the tool" appears in output window
     * - wait until "Profile|Take Snapshot of Collected Results" is enabled
     * - call Profile|Take Snapshot of Collected Results
     * - maximaze results view
     * - save collected results
     * - call "Profile|Stop Profiling Session"
     */
    public void testProfiler() throws Exception {
        new ActionNoBlock("Profile|Advanced Commands|Run Profiler Calibration", null).perform();
        new NbDialogOperator("Select Java Platform to calibrate").ok();
        // increase timeout for calibration
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 120000); // NOI18N
        new NbDialogOperator("Information").ok();
        new ActionNoBlock("Profile|Profile Main Project", null).perform();
        new NbDialogOperator("Question").ok();
        NbDialogOperator profileOper = new NbDialogOperator("Profile "+SAMPLE_PROJECT_NAME);
        new JButtonOperator(profileOper, "Run").push();
        profileOper.waitClosed();
        waitProgressDialog("Progress", 5000);
        new TopComponentOperator("Profiler");        
        new OutputTabOperator(SAMPLE_PROJECT_NAME).waitText("Established local connection with the tool");
        Action takeSnapshotAction = new Action("Profile|Take Snapshot of Collected Results", null);
        new Waiter(new Waitable() {
            public Object actionProduced(Object takeSnapshotAction) {
                return ((Action)takeSnapshotAction).isEnabled() ? Boolean.TRUE : null;
            }
            public String getDescription() {
                return("Wait menu item enabled."); // NOI18N
            }
        }).waitAction(takeSnapshotAction);
        takeSnapshotAction.perform();
        TopComponentOperator collectedResults = new TopComponentOperator("CPU");
        //collectedResults.maximize();
        collectedResults.saveDocument();
        new Action("Profile|Stop Profiling Session", null).perform();
    }
    
    
    public void waitProgressDialog(String title, int milliseconds){
        try {
            // wait at most 120 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(title);
            openingOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", milliseconds);
            openingOper.waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }        
    }
}