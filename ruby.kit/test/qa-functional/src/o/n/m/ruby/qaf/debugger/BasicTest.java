/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package o.n.m.ruby.qaf.debugger;

import java.awt.Component;
import javax.swing.JButton;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewRubyProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
//import org.netbeans.junit.ide.ProjectSupport;

/**
 * Basic ruby debugger test.
 *
 * @author Tomas Musil, Jiri Skrivanek, Lukas Jungmann
 */
public class BasicTest extends JellyTestCase {

    /** Need to be defined because of JUnit */
    public BasicTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BasicTest.class).enableModules(".*").clusters(".*")); //NOI18N
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BasicTest("testCreateRubyProject"));
    }

    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // name of sample projects
    private static final String SAMPLE_RUBY_PROJECT_NAME = "SampleRubyApplication";  //NOI18N

    /** Test Ruby Application
     * - open new project wizard
     * - choose Ruby|Ruby Application
     * - click Next
     * - type name and location and finish the wizard
     * - wait until project is in Projects view
     * - wait classpath scanning finished
     * - wait until main.rb is opened in editor
     * - insert some test data into editor
     */
    public void testCreateRubyProject() {
        // create new web application project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Ruby"
        String rubyLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "Templates/Project/Ruby");
        npwo.selectCategory(rubyLabel);
        // "Ruby Application"
        String rubyApplicationLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle", "TXT_NewJavaApp");
        npwo.selectProject(rubyApplicationLabel);
        npwo.next();
        NewRubyProjectNameLocationStepOperator npnlso = new NewRubyProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_RUBY_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_RUBY_PROJECT_NAME);
        // wait classpath scanning finished
//        ProjectSupport.waitScanFinished();
        // wait for main.rb opened in editor
        EditorOperator eo = new EditorOperator("main.rb");  // NOI18N
        eo.replace("puts \"Hello World\"", "require 'date'\ndate1 = Date.today\ndate2 = Date.today\nputs date2");
        ProjectSupport.waitScanFinished();
    }

    /** Test start of ruby debugger
     * - set breakpoint to line with date1 declaration
     * - start debugger
     * - wait debugger is stopped at breakpoint
     */
    public void testDebuggerStart() throws Exception {
        EditorOperator eo = new EditorOperator("main.rb");  // NOI18N
        Util.setBreakpoint(eo, "date1 ="); // NOI18N
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_RUBY_PROJECT_NAME);
        new DebugProjectAction().performMenu(rootNode);
        Util.waitStopped(eo);
    }

    /** Test step into, step over and step out.
     * - perform step over
     * - wait it stops at next line
     * - check date1 is shown in Local Variables view
     * - check stack has two rows in Call Stack view
     * - perform step into
     * - wait date.rb is opened and debugger stopped in it
     * - perform step out and check debugger is finished
     */
    public void testStepInOutOver() throws Exception {
        EditorOperator eo = new EditorOperator("main.rb");
        int lineNumber = eo.getLineNumber();
        new StepOverAction().perform();
        lineNumber = Util.waitStopped(eo, lineNumber + 1);
        assertTrue("Debugger not at \"date2 = Date.today\"", eo.getText(lineNumber).indexOf("date2 =") > -1);

        TopComponentOperator localVariablesTCO = new TopComponentOperator("Variables");//NOI18N
        assertEquals("date1", new JTableOperator(localVariablesTCO).getModel().getValueAt(1, 0).toString());

        new StepIntoAction().perform();
        // wait for date.rb opened in editor
        EditorOperator eoDate = new EditorOperator("date.rb");
        lineNumber = Util.waitStopped(eoDate);
        assertTrue("Debugger not at \"today in date.rb\"", eoDate.getText(lineNumber).indexOf("today") > -1);

        TopComponentOperator callStackTCO = new TopComponentOperator("Call Stack");//NOI18N
        assertEquals("Call Stack row count wrong.", 2, new JTableOperator(callStackTCO).getRowCount());

        new StepOutAction().perform();
        lineNumber = Util.waitStopped(eo);
        assertTrue("Debugger not at \"puts in main.rb\"", eo.getText(lineNumber).indexOf("puts") > -1);
        new StepOverAction().perform();

        //String debugToolbarLabel = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "Toolbars/Debug"); // debugger.jpda.ui is a part of java, so it is not good to use it here
        String debugToolbarLabel = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Toolbars/Debug");
        ContainerOperator debugToolbarOper = MainWindowOperator.getDefault().getToolbar(debugToolbarLabel);
        // wait until Finish Debugger Session button in Debug toolbar dismiss
        JButtonOperator finishDebugSessionButton = new JButtonOperator(debugToolbarOper, new ComponentChooser() {

            //Finish Debugger Session
            private String finishTTip = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name");

            public boolean checkComponent(Component c) {
                if (c instanceof JButton) {
                    JButton jb = (JButton) c;
                    return jb.getToolTipText().contains(finishTTip);
                }
                return false;
            }

            public String getDescription() {
                return "Finish Debugger Session debug toolbar button"; //NOI18N
            }
        });
        if (finishDebugSessionButton.isShowing()) {
            finishDebugSessionButton.waitComponentShowing(false);
        }
    }

    /** Test kill outstanding debugger sessions
     */
    public void testKillOutstandingSessions() throws Exception {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        System.out.println("Found " + sessions.length + " outstanding debugger sessions.");
        for (Session session : sessions) {
            System.out.println("WARNING: killing dbg session: " + session);
            session.kill();
        }
    }
}
