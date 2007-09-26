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

package org.netbeans.modules.uihandler.test;

import java.awt.Component;
import org.netbeans.modules.uihandler.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jindra
 */
public class FunctionalTest extends NbTestCase {
    
    public FunctionalTest(String testName) {
        super(testName);
    }
    
    public void setUp(){
        System.setProperty("netbeans.exception.report.min.level", "100");
    }
    
    public void textExceptionThrown()throws Exception{
        String userName = "testUserName";
        String summary = "testSummary";
        String comment = "TESTING COMMENT";
        Throwable t1 = new NullPointerException("TESTING EXCEPTION");
        Throwable t2 = new ClassNotFoundException("NESTED EXCEPTION");
        t1.initCause(t2);
        LogRecord log = new LogRecord(Level.INFO, "TESTING LOG");
        log.setThrown(t1);
        Logger.getLogger("").log(log);
        ErrorDialogOperator errDialOp = new ErrorDialogOperator();
        assertNotNull("ERROR DIALOG SHOULD APPEAR", errDialOp);
        errDialOp.report();
        ReportDialogOperator reportOper = new ReportDialogOperator();
        assertEquals(reportOper.summary().getText(), "NullPointerException : TESTING EXCEPTION");
        reportOper.summary().setText(summary);
        reportOper.comment().setText(comment);
        reportOper.userName().setText(userName);
        reportOper.submitData();// verify button
        reportOper.viewData().pushNoBlock();
        ViewDataDialogOperator viewOper = new ViewDataDialogOperator("Report Problem");
        viewOper.submitData();//verify button
        assertEquals("USER CONFIGURATION SHOULD BE SHOWN",
                viewOper.listView().getModel().getElementAt(0).toString(), "User Configuration");
        assertEquals("TESTING LOG SHOULD BE SHOWN",
                viewOper.listView().getModel().getElementAt(3).toString(), "TESTING LOG");
        String text = viewOper.paneRawContent();
        assertTrue(text.contains("<message>TESTING LOG</message>"));
        assertTrue(text.contains("<message>java.lang.NullPointerException: TESTING EXCEPTION</message>"));
        assertTrue(text.contains("<method>textExceptionThrown</method>"));
        assertTrue(text.contains("<key>UI_ENABLED_MODULES</key>"));
        assertTrue(text.contains("<key>UI_DISABLED_MODULES</key>"));
        assertTrue(text.contains("<key>UI_USER_CONFIGURATION</key>"));
        assertTrue(text.contains("<param>testUserName</param>"));
        assertTrue(text.contains("<param>testSummary</param>"));
        assertTrue(text.contains("<param>TESTING COMMENT</param>"));
        viewOper.backtoReport().pushNoBlock();
        reportOper = new ReportDialogOperator();
        reportOper.cancel();
    }
    
    public void testUIGestures(){
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        ContainerOperator toolbar = mainWindow.getToolbar("UI Gestures");
        assertNotNull("UIGESTURES Toolbar", toolbar);
        assertEquals("ToolbarBump, NrButton", 2, toolbar.getComponentCount());
        Component label = toolbar.getComponent(1);
        assertNotNull("UIGESTURES BUTTON", label);
        new ComponentOperator(label).clickMouse();
        WelcomeDialogOperator welcome = new WelcomeDialogOperator();
        welcome.submitData();//verify existance
        welcome.viewData().pushNoBlock();
        ViewDataDialogOperator viewData = new ViewDataDialogOperator("UI Gestures Collector");
        viewData.submitData();//verify existance
        assertEquals("USER CONFIGURATION SHOULD BE SHOWN",
                viewData.listView().getModel().getElementAt(0).toString(), "User Configuration");
        String text = viewData.paneRawContent();
        assertTrue(text.contains("<key>UI_ENABLED_MODULES</key>"));
        assertTrue(text.contains("<key>UI_DISABLED_MODULES</key>"));
        assertTrue(text.contains("<key>UI_USER_CONFIGURATION</key>"));
        assertTrue(text.contains("<param>GUEST</param>\n</record>"));

        viewData.hideData().pushNoBlock();
        welcome = new WelcomeDialogOperator();
        welcome.cancel();
    }
    
    private class WelcomeDialogOperator extends NbDialogOperator{
        private JButtonOperator viewData;
        private JButtonOperator submitData;
        
        public WelcomeDialogOperator() {
            super("UI Gestures Collector");
        }
        
        public JButtonOperator viewData(){
            if (viewData == null){
                viewData = new JButtonOperator(this, "View Data");
            }
            assertNotNull("THERE SHOULD BE A VIEW DATA BUTTON", viewData);
            return viewData;
        }
        
        public JButtonOperator submitData(){
            if (submitData == null){
                submitData = new JButtonOperator(this, "Submit Data");
            }
            assertNotNull("THERE SHOULD BE A SUBMIT DATA BUTTON", submitData);
            return submitData;
        }
        
        
    }
    
    
    private class ViewDataDialogOperator extends NbDialogOperator{
        private JButtonOperator backtoReport;
        private JButtonOperator submitData;
        private JTabbedPaneOperator pane;
        private JButtonOperator hideData;
        private JListOperator listView;
        
        public ViewDataDialogOperator(String title) {
            super(title);
        }
        
        public JButtonOperator hideData(){
            if (hideData == null){
                hideData = new JButtonOperator(this, "Hide Data");
            }
            assertNotNull("THERE SHOULD BE A HIDE DATA BUTTON", hideData);
            return hideData;
        }
        
        public JButtonOperator submitData(){
            if (submitData == null){
                submitData = new JButtonOperator(this, "Submit Data");
            }
            assertNotNull("THERE SHOULD BE A SUBMIT DATA BUTTON", submitData);
            return submitData;
        }
        
        public JButtonOperator backtoReport(){
            if (backtoReport == null){
                backtoReport = new JButtonOperator(this, "Back to Report");
            }
            assertNotNull("THERE SHOULD BE A BACK TO REPORT BUTTON", backtoReport);
            return backtoReport;
        }
        
        public String paneRawContent(){
            listView();
            pane().selectPage("Raw");
            JEditorPaneOperator textArea;
            textArea = new JEditorPaneOperator(pane(), 0);
            assertNotNull("THERE SHOULD BE A TEXT AREA", textArea);
            return textArea.getText();
        }
        
        public JListOperator listView(){
            pane().selectPage("Structured");
            if (listView == null){
                listView = new JListOperator(this, 0);
            }
            assertNotNull("THERE SHOULD BE A LIST VIEW ON VIEW DATA WINDOW", listView);
            return listView;
        }
        
        private JTabbedPaneOperator pane(){
            if (pane == null) {
                pane = new JTabbedPaneOperator(this, 0);
            }
            assertNotNull("THERE SHOULD BE A PANE", pane);
            assertEquals("THERE SHOULD BE 2 TABS", 2, pane.getTabCount());
            return pane;
        }
    }
    
    
    private class ErrorDialogOperator extends NbDialogOperator{
        public ErrorDialogOperator(){
            super("Unexpected Exception");
        }
        
        public void report(){
            JButtonOperator op = new JButtonOperator(this, "Review and Report Problem");
            assertNotNull("THERE SHOULD BE A REPORT BUTTON", op);
            op.pushNoBlock();
        }
        
        
    }
    
    private class ReportDialogOperator extends NbDialogOperator{
        private JButtonOperator viewData;
        private JButtonOperator submitData;
        private JTextFieldOperator summary;
        private JTextFieldOperator userName;
        private JTextAreaOperator comment;
        
        public ReportDialogOperator(){
            super("Report Problem");
        }
        
        public JButtonOperator viewData(){
            if (viewData == null){
                viewData = new JButtonOperator(this, "View Data");
            }
            assertNotNull("THERE SHOULD BE A VIEW DATA BUTTON", viewData);
            return viewData;
        }
        
        public JButtonOperator submitData(){
            if (submitData == null){
                submitData = new JButtonOperator(this, "Submit Data");
            }
            assertNotNull("THERE SHOULD BE A SUBMIT DATA BUTTON", submitData);
            return submitData;
        }
        
        public JTextFieldOperator summary(){
            if (summary == null) {
                summary = new JTextFieldOperator(this, 1);
            }
            assertNotNull("THERE SHOULD BE A SUMMARY FIELD", summary);
            return summary;
        }
        
        public JTextFieldOperator userName(){
            if (userName == null) {
                userName = new JTextFieldOperator(this, 0);
            }
            assertNotNull("THERE SHOULD BE A USER NAME FIELD", userName);
            return userName;
        }
        
        public JTextAreaOperator comment(){
            if (comment == null) {
                comment = new JTextAreaOperator(this, 0);
            }
            assertNotNull("THERE SHOULD BE A COMMENT AREA", comment);
            return comment;
        }
    }
    
    
    
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        //suite.addTest(new FunctionalTest("textExceptionThrown"));
        suite.addTest(new FunctionalTest("testUIGestures"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        TestRunner.run(suite());
    }
    
}
