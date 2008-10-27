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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbTestSuite;

/** org.netbeans.jellytools.actions.DebugProjectAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugProjectActionTest extends JellyTestCase {
    
    public static final String[] tests = new String[]
    {"testPerformPopup", "testPerformMenu", "testPerformShortcut"};
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DebugProjectActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new DebugProjectActionTest("testPerformPopup"));
        suite.addTest(new DebugProjectActionTest("testPerformMenu"));
        suite.addTest(new DebugProjectActionTest("testPerformShortcut"));
        return suite;
         */
        return createModuleTest(DebugProjectActionTest.class, tests);
    }
    
    private static MainWindowOperator.StatusTextTracer statusTextTracer;

    /** Method called before all test cases. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");  // NOI18N
        openDataProjects("SampleProject");
        if(statusTextTracer == null) {
            // increase timeout to 60 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
        try {
            // "SampleWebProject (debug)"
            String outputTarget = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "TITLE_output_target", 
                    new Object[] {"SampleProject", null, "debug"});  // NOI18N
            // "Building SampleProject (debug)..."
            String buildingMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_running_ant",
                    new Object[] {outputTarget});
            // "Finished building SampleProject (debug)"
            String finishedMessage = Bundle.getString(
                    "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", 
                    new Object[] {outputTarget});
            // wait status text "Building SampleProject (debug)..."
            statusTextTracer.waitText(buildingMessage);
            // wait status text "Finished building SampleProject (debug)."
            statusTextTracer.waitText(finishedMessage);
            // wait status text "User program finished"
            String finishedLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
            statusTextTracer.waitText(finishedLabel); // NOI18N
        } catch (JemmyException e) {
            log("debugOutput.txt", new OutputTabOperator("SampleProject").getText()); // NOI18N
            throw e;
        } finally {
            statusTextTracer.stop();
        }
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test performPopup. */
    public void testPerformPopup() {
        new DebugProjectAction().performPopup(ProjectsTabOperator.invoke().getProjectRootNode("SampleProject")); // NOI18N
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        // Set as Main Project
        String setAsMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
        new Action(null, setAsMainProjectItem).perform(new ProjectsTabOperator().getProjectRootNode("SampleProject")); // NOI18N
        new DebugProjectAction().performMenu();
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new DebugProjectAction().performShortcut();
    }
    
}
