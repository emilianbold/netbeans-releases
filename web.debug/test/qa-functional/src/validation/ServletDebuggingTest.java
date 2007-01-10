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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package validation;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.SourcesOperator;
import org.netbeans.jellytools.modules.debugger.actions.ApplyCodeChangesAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/** Test of web application debugging. Manual test specification is here:
 * http://qa.netbeans.org/modules/webapps/promo-f/jspdebug/jspdebug-testspec.html
 * <br>
 * !!! Be careful when using internal swing html browser. It posts http requests
 * three times. That's why is probably better to finish debugging each time you
 * went through page.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ServletDebuggingTest extends JellyTestCase {
    // status bar tracer used to wait for state
    private MainWindowOperator.StatusTextTracer stt;
    
    public ServletDebuggingTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ServletDebuggingTest("testSetBreakpoint"));
        suite.addTest(new ServletDebuggingTest("testStepInto"));
        suite.addTest(new ServletDebuggingTest("testStepOut"));
        suite.addTest(new ServletDebuggingTest("testStepOver"));
        suite.addTest(new ServletDebuggingTest("testApplyCodeChanges"));
        suite.addTest(new ServletDebuggingTest("testStopServer"));
        return suite;
    }
    
    /** Print test name and initialize status bar tracer. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        // find servlet node in Projects view
        servletNode = new Node(new SourcePackagesNode(SAMPLE_WEB_PROJECT_NAME),
                               "org.netbeans.test.servlets|DivideServlet.java"); //NOI18N
    }
    
    /** Stops status bar tracer. */
    public void tearDown() {
        stt.stop();
    }
    
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "MainTestApplication";  //NOI18N
    // line number of breakpoint
    private static int line;
    // servlet node in Projects view
    private Node servletNode;

    /** Set breakpoint.
     * - open Source Packages|org.netbeans.test.servlets|DivideServlet.java
     * - select <h1> in editor
     * - toggle breakpoint at selected line
     */
    public void testSetBreakpoint() throws Exception {
        new OpenAction().performAPI(servletNode);
        // find file in Editor
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        line = Utils.setBreakpoint(eo, "<h1>"); // NOI18N
    }

    /** Step into in Servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - set sources from TestFreeformLibrary to be used for debugging
     * - call Run|Step Into from main menu
     * - wait until debugger stops at next line
     * - call Run|Step Into from main menu again
     * - wait until debugger stops at line in Divider.java
     * - find and close editor tab with Divider.java
     * - finish debugger
     */
    public void testStepInto() {
        new ActionNoBlock(null, new DebugAction().getPopupPath()).perform(servletNode);
        String setURITitle = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "TTL_setServletExecutionUri");
        new NbDialogOperator(setURITitle).ok();
        stt.waitText("DivideServlet.java:"+line); //NOI18N
        // set sources from TestFreeformLibrary to be used for debugging
        SourcesOperator so = SourcesOperator.invoke();
        so.useSource("TestFreeformLibrary"+File.separator+"src1", true); // NOI18N
        so.useSource("TestFreeformLibrary"+File.separator+"src2", true); // NOI18N
        so.close();
        new StepIntoAction().perform();
        stt.waitText("DivideServlet.java:"+(line+2)); //NOI18N
        new StepIntoAction().perform();
        stt.waitText("Divider.java:"); //NOI18N
        new EditorOperator("Divider.java").close(); //NOI18N
        Utils.finishDebugger();
    }

    /** Step out from servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - call Run|Step Out from main menu
     * - wait until debugger stops in doGet method
     * - finish debugger
     */
    public void testStepOut() {
        new DebugAction().perform(servletNode);
        stt.waitText("DivideServlet.java:"+line); //NOI18N
        stt.clear();
        new StepOutAction().perform();
        // it stops at doGet method
        stt.waitText("DivideServlet.java:"); //NOI18N
        Utils.finishDebugger();
    }

    /** Step over servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - call Run|Step Over from main menu
     * - wait until debugger stops at next line
     * - call Run|Step Over from main menu again
     * - wait until debugger stops at next line
     * - finish debugger
     */
    public void testStepOver() {
        new DebugAction().perform(servletNode);
        stt.waitText("DivideServlet.java:"+line); //NOI18N
        new StepOverAction().perform();
        stt.waitText("DivideServlet.java:"+(line+2)); //NOI18N
        new StepOverAction().perform();
        stt.waitText("DivideServlet.java:"+(line+4)); //NOI18N
        Utils.finishDebugger();
    }

    /** Apply code changes in servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - replace "Servlet DIVIDE" by "Servlet DIVIDE Changed" in DivideServlet.java
     * - call Run|Apply Code Changes from main menu
     * - wait until debugger stops somewhere in DivideServlet.java
     * - finish debugger
     * - find and close browser with title "Servlet DIVIDE Changed"
     */
    public void testApplyCodeChanges() {
        new DebugAction().perform(servletNode);
        stt.waitText("DivideServlet.java:"+line); //NOI18N
        stt.clear();
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        eo.replace("Servlet DIVIDE", "Servlet DIVIDE Changed"); //NOI18N
        new ApplyCodeChangesAction().perform();
        stt.waitText("DivideServlet.java:"); //NOI18N
        Utils.finishDebugger();
        new TopComponentOperator("Servlet DIVIDE Changed").close();// NOI18N
    }
    
    /** Stop server just for clean-up.
     * - stop server and wait until it finishes
     */
    public void testStopServer() {
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        serverNode.stop();
    }
}
