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
package org.netbeans.jellytools.actions;

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
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DebugProjectActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new DebugProjectActionTest("testPerformPopup"));
        suite.addTest(new DebugProjectActionTest("testPerformMenu"));
        suite.addTest(new DebugProjectActionTest("testPerformShortcut"));
        return suite;
    }
    
    private static MainWindowOperator.StatusTextTracer statusTextTracer;

    /** Method called before all test cases. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
        if(statusTextTracer == null) {
            // increase timeout to 60 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    /** method called after each testcase
     */
    protected void tearDown() {
        try {
            // wait status text "Building SampleProject (debug)..."
            statusTextTracer.waitText("debug", true); // NOI18N
            // wait status text "Finished building SampleProject (debug)."
            statusTextTracer.waitText("debug", true); // NOI18N
            // wait status text "User program finished"
            String finishedLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
            statusTextTracer.waitText(finishedLabel, true); // NOI18N
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
        // Set Main Project
        String setMainItem = Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetMainProjectAction_Name");
        new Action(null, setMainItem).perform(new ProjectsTabOperator().getProjectRootNode("SampleProject")); // NOI18N
        new DebugProjectAction().performMenu();
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new DebugProjectAction().performShortcut();
    }
    
}
