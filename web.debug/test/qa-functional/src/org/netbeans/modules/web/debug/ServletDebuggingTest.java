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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.web.debug;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ApplyCodeChangesAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Exceptions;

/** Test of web application debugging. Manual test specification is here:
 * http://qa.netbeans.org/modules/webapps/promo-f/jspdebug/jspdebug-testspec.html
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ServletDebuggingTest extends J2eeTestCase {
    // status bar tracer used to wait for state

    private MainWindowOperator.StatusTextTracer stt;

    public ServletDebuggingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH, NbModuleSuite.createConfiguration(ServletDebuggingTest.class),
                "testSetBreakpoint",
                "testStepInto",
                "testStepOut",
                "testStepOver",
                "testApplyCodeChanges",
                "testStopServer").enableModules(".*").clusters(".*"));
    }

    /** Print test name and initialize status bar tracer. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        // find servlet node in Projects view
        try {
            openProjects(new File(getDataDir(), SAMPLE_WEB_PROJECT_NAME).getAbsolutePath());
            ProjectSupport.waitScanFinished();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        servletNode = new Node(new SourcePackagesNode(SAMPLE_WEB_PROJECT_NAME),
                "org.netbeans.test.servlets|DivideServlet.java"); //NOI18N
    }

    /** Stops status bar tracer. */
    @Override
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
     * - call Debug "DivideServlet.java" popup on servlets node
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
        // "Debug "DivideServlet.java""
        String debugFileItem =
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
                "LBL_DebugSingleAction_Name",
                new Object[]{new Integer(1), servletNode.getText()});
        new ActionNoBlock(null, debugFileItem).perform(servletNode);
        String setURITitle = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "TTL_setServletExecutionUri");
        Utils.confirmClientSideDebuggingMeassage(SAMPLE_WEB_PROJECT_NAME);
        new NbDialogOperator(setURITitle).ok();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        //OutputTabOperator outputTab = new OutputTabOperator("MainTestApplication (debug)");
        //outputTab.waitText("BUILD SUCCESSFUL");
        //stt.waitText("DivideServlet.java:" + line); //NOI18N
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet");
        waitEnabled(new StepIntoAction());
        new StepIntoAction().perform();
        MainWindowOperator.getDefault().pressKey(KeyEvent.VK_ENTER);
        //stt.waitText("DivideServlet.java:"+(line+2)); //NOI18N
        new StepIntoAction().perform();
        //stt.waitText("DivideServlet.java:"+(line+4));
        Utils.finishDebugger();
    }

    private void waitEnabled(final Action action) {
        Waiter waiter = new Waiter(new Waitable() {

            public Object actionProduced(Object arg0) {
                if (!action.isEnabled()){
                    return null;
                }else{
                    return this;
                }
            }

            public String getDescription() {
                return "waiting for enabled Step Into Action";
            }
        });
        try {
            waiter.waitAction(null);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    /** Step out from servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - call Run|Step Out from main menu
     * - wait until debugger stops in doGet method
     * - finish debugger
     */
    public void testStepOut() {
        JSPDebuggingOverallTest.verifyActiveNode(servletNode);
        new DebugJavaFileAction().perform(servletNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet");
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        eo.select("<h1>"); // NOI18N
        line = eo.getLineNumber();
        stt.waitText("DivideServlet.java:" + line); //NOI18N
        stt.clear();
        waitEnabled(new StepOutAction());
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
        JSPDebuggingOverallTest.verifyActiveNode(servletNode);
        new DebugJavaFileAction().perform(servletNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet");
        stt.waitText("DivideServlet.java:" + line); //NOI18N
        new StepOverAction().perform();
        stt.waitText("DivideServlet.java:" + (line + 2)); //NOI18N
        new StepOverAction().perform();
        stt.waitText("DivideServlet.java:" + (line + 4)); //NOI18N
        Utils.finishDebugger();
    }

    /** Apply code changes in servlet.
     * - call Debug File popup on servlets node
     * - wait until debugger stops at previously set breakpoint
     * - replace "Servlet DIVIDE" by "Servlet DIVIDE Changed" in DivideServlet.java
     * - call Run|Apply Code Changes from main menu
     * - wait until debugger stops somewhere in DivideServlet.java
     * - finish debugger
     * - open URL connection and wait for changed text
     */
    public void testApplyCodeChanges() {
        JSPDebuggingOverallTest.verifyActiveNode(servletNode);
        new DebugJavaFileAction().perform(servletNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet");
        stt.waitText("DivideServlet.java:" + line); //NOI18N
        stt.clear();
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        eo.replace("Servlet DIVIDE", "Servlet DIVIDE Changed"); //NOI18N
        new ApplyCodeChangesAction().perform();
        stt.waitText("DivideServlet.java:"); //NOI18N
        Utils.finishDebugger();
        Utils.waitText(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet", 240000, "Servlet DIVIDE Changed");
    }

    /** Stop server just for clean-up.
     * - stop server and wait until it finishes
     */
    public void testStopServer() {
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        JSPDebuggingOverallTest.verifyServerNode(serverNode);
        serverNode.stop();
    }
}
