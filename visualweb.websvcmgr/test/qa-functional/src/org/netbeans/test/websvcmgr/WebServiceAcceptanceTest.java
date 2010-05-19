/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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

package org.netbeans.test.websvcmgr;

import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import junit.framework.Test;

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerExplorerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.actions.FindAction;
import org.netbeans.modules.visualweb.gravy.websvc.*;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.model.project.components.*;
import org.netbeans.modules.visualweb.gravy.model.project.*;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.model.*;
import org.netbeans.modules.visualweb.gravy.*;

import com.meterware.httpunit.*;
import java.util.Properties;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author Roman Mostyka
 */

public class WebServiceAcceptanceTest extends RaveTestCase {

    private static String addBindingAttribute = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.insync.action.Bundle","LBL_AddBindingAttributeActionName");
    private static String sep = File.separator;
    private static String wsPath = "Web Services|";
    private static String deletePopup = "Delete";
    private static String reformatPopup = "Format";
    private static String addToPagePopup = "Add to Page";
    private static String menuCopy = "Edit|Copy";
    private static String menuPaste = "Edit|Paste";
    private static String testMethod = "Test Method";
    private static String dlgQuestion = "Question";
    private static String dlgTestMethod = "Test Web Service Method";
    private static String btnYes = "Yes";
    private static String btnSubmit = "Submit";
    private static String _wsProjectName = "AcceptanceWSProject";
    private static String _projectPath = System.getProperty("xtest.workdir") + sep + "projects" + sep;
    private static String wsURL = "URL";
    private static String wsName = "PATH";
    private static String wsMethod = "METHOD";
    private static String wsMethodParameter = "PARAMETER";
    private static String pluginName = "Visual Web JSF Backwards Compatibility Kit";
    private static String javaCode = "CODE";
    private static String wsResponse = "RESPONSE";
    private static String serverType = "GlassFish V2";

    private static Project prj, J2EE14prj, JavaEE5prj;
    private static DeploymentTargetDescriptor dtd;
    private static DeploymentTarget dt;
    private static ApplicationServer as;
    private static Properties properties;

    private ServerNavigatorOperator server;
    private JTreeOperator sntree;
   
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public WebServiceAcceptanceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("Web Service Consumption Acceptance Tests");
        suite.addTest(new WebServiceAcceptanceTest("testPreparation"));
        suite.addTest(new WebServiceAcceptanceTest("testAddWebService"));
        suite.addTest(new WebServiceAcceptanceTest("testTestMethodDialog"));
        suite.addTest(new WebServiceAcceptanceTest("testAddWebServiceToJ2EE14Project"));
        suite.addTest(new WebServiceAcceptanceTest("testDeployJ2EE14Project"));
        suite.addTest(new WebServiceAcceptanceTest("testAddWebServiceToJavaEE5Project"));
        suite.addTest(new WebServiceAcceptanceTest("testDeployJavaEE5Project"));
        suite.addTest(new WebServiceAcceptanceTest("testRemoveWebService"));
        return suite;
    }

    /** method called before each testcase
     */
    protected void setUp() {
    }

    public void testPreparation() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(System.getProperty("xtest.ws-properties.file")));
        } catch(Exception e) {
            System.out.println("Properties can not be loaded!");
            System.out.println("e=" + e.toString());
            return;
        }
        PluginsOperator.getInstance().installAvailablePlugins(pluginName);
        dtd = new DeploymentTargetDescriptor();
        dtd.load();
        if (serverType != null) dtd.setProperty(dtd.SERVER_TYPE_KEY, serverType);
        dt = IDE.getIDE().addDeploymentTarget(dtd);
        as = (ApplicationServer) dt;
        as.start();
        TestUtils.wait(1000);
    }

    public void testAddWebService() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        server.pushPopup("Web Services", "Add Web Service...");
        AddWebServiceOperator wsOp = new AddWebServiceOperator();
        wsOp.addWebService(properties.get(wsURL).toString());
        TestUtils.wait(2000);
    }

    public void testTestMethodDialog(){
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        server.pushPopup(wsPath + properties.get(wsMethod).toString(), testMethod);
        TestUtils.wait(1000);
        JDialogOperator jdo = new JDialogOperator(dlgTestMethod);
        TestUtils.wait(1000);
        JTableOperator jto1 = new JTableOperator(jdo, 0);
        TestUtils.wait(1000);
        jto1.changeCellObject(0, 2, properties.get(wsMethodParameter).toString());
        TestUtils.wait(1000);
        new JButtonOperator(jdo, btnSubmit).push();
        TestUtils.wait(10000);
        JTableOperator jto2 = new JTableOperator(jdo, 1);
        if (jto2.getValueAt(0, 1).toString().indexOf(properties.get(wsResponse).toString()) == -1) {
            fail("There is no needed string in response!");
            jdo.close();
        }
        jdo.close();
    }
    
    public void testAddWebServiceToJ2EE14Project() {
        J2EE14prj = createProject(ProjectDescriptor.J2EE14);
        addComponentToProject(J2EE14prj);
        addWebServiceToProject();
        editPrerenderMethod(properties.get(javaCode).toString());
    }

    public void testDeployJ2EE14Project() {
        DeployAndCheckProject(J2EE14prj);
        verifyHTTP(J2EE14prj, wsResponse);
        projectUndeployAndClose(J2EE14prj);
    }

    public void testAddWebServiceToJavaEE5Project() {
        JavaEE5prj = createProject(ProjectDescriptor.JavaEE5);
        addComponentToProject(JavaEE5prj);
        addWebServiceToProject();
        editPrerenderMethod(properties.get(javaCode).toString());
    }

    public void testDeployJavaEE5Project() {
        DeployAndCheckProject(JavaEE5prj);
        verifyHTTP(JavaEE5prj, wsResponse);
        projectUndeployAndClose(JavaEE5prj);
        as.stop();
    }
    
    public void testRemoveWebService() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        String wsClientPath = properties.get(wsName).toString();
        String realPath = wsPath + 
            wsClientPath.substring(0, wsClientPath.indexOf("|"));
        server.pushPopup(realPath, deletePopup);
        new JButtonOperator(new DialogOperator(dlgQuestion), btnYes).push();
        TestUtils.wait(2000);
    }

    private Project createProject(String J2EEVersion) {
        String J2EEVersion_PREF = J2EEVersion.replace(' ', '_').replace('.', '_');
        ProjectDescriptor pd = new ProjectDescriptor(_wsProjectName + "_" + J2EEVersion_PREF, _projectPath, J2EEVersion, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(prj.getName(), true);
        TestUtils.wait(1000);
        return prj;
    }

    private void addComponentToProject(Project prj) {
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualComponent vcmp = (VisualComponent)
        wp.add((WebComponent)
        IDE.getIDE().getDefaultComponentSet().getComponent(StaticTextComponent.STATIC_TEXT_ID),
        new Point(48, 48));
        addBindingAttribute(wp.getName(), "page1|html1|body1|form1|" + vcmp.getName());
    }

    private void addWebServiceToProject() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.selectPath(wsPath + properties.get(wsName).toString());
        Util.getMainMenu().pushMenu(menuCopy);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        wp.open();
        TestUtils.wait(1000);
        Util.getMainMenu().pushMenu(menuPaste);
        TestUtils.wait(30000);
    }

    private void editPrerenderMethod(String code) {
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.switchToJavaSource();
        TestUtils.wait(1000);
        EditorOperator editor = new EditorOperator(RaveWindowOperator.getDefaultRave(), "Page1.java");
        TestUtils.wait(2000);
        new FindAction().performShortcut();
        TestUtils.wait(2000);
        JComboBoxOperator jcbo = new JComboBoxOperator(editor);
        TestUtils.wait(500);
        jcbo.clearText();
        TestUtils.wait(500);
        jcbo.enterText("prerender() {");
        TestUtils.wait(500);
        jcbo.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(500);
        jcbo.pressKey(KeyEvent.VK_RIGHT);
        TestUtils.wait(500);
        editor.setCaretPositionToEndOfLine(editor.getLineNumber());
        TestUtils.wait(500);
        editor.pressKey(KeyEvent.VK_ENTER);
        TestUtils.wait(2000);
        editor.txtEditorPane().typeText(code);
        editor.txtEditorPane().clickForPopup();
        JPopupMenuOperator epm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(epm, reformatPopup).push();
        TestUtils.wait(2000);
    }

    private void DeployAndCheckProject(Project prj) {
        Util.getMainWindow().deploy();
        TestUtils.wait(20000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path, as.REFRESH);
        TestUtils.wait(1000);
        try {
            sntree.selectPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + prj.getName()));
        }
        catch (Exception e) {
            fail("There is no " + prj.getName() + " application in Deployed Components node!");
        }
        TestUtils.wait(1000);
    }

    private void verifyHTTP(Project prj, String verificationString) {
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + prj.getName());
            if (response.getText().indexOf(properties.get(wsResponse).toString()) == -1) fail("There is no needed string in response!");
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
    }

    private void projectUndeployAndClose(Project prj) {
        TestUtils.wait(1000);
        Util.saveAllAPICall();
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path, as.REFRESH);
        TestUtils.wait(1000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + prj.getName(), as.APPLICATION_UNDEPLOY);
        TestUtils.wait(1000);
        prj.close();
        TestUtils.wait(1000);
    }

    public void addBindingAttribute(String page, String full_component_path) {
        DocumentOutlineOperator outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        JTreeOperator aotree = outline.getStructTreeOperator();
        aotree.callPopupOnPath(aotree.findPath(page + "|" + full_component_path));
        TestUtils.wait(1000);
        JPopupMenuOperator aopm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(aopm, addBindingAttribute).pushNoBlock();
        TestUtils.wait(1000);
    }
}

