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

package org.netbeans.test.deployment.generic;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.NbTestSuite;
import junit.framework.Test;

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerExplorerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.websvc.AddWebServiceOperator;
import org.netbeans.modules.visualweb.gravy.actions.FindAction;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.model.project.*;
import org.netbeans.modules.visualweb.gravy.model.project.components.*;
import org.netbeans.modules.visualweb.gravy.model.*;
import org.netbeans.modules.visualweb.gravy.*;

import javax.swing.tree.TreePath;
import com.meterware.httpunit.*;
import java.awt.event.KeyEvent;
import java.awt.Point;

/**
 *
 * @author Roman Mostyka
 */

public class DeploymentTest extends RaveTestCase {
    
    private ServerNavigatorOperator server;
    private JTreeOperator sntree;
    private DeploymentAcceptanceTest dat;
    private static DeploymentTargetDescriptor dtd;
    private static DeploymentTarget dt;
    private static ApplicationServer as;
    public static String serverType = null;
    static String AS_PREF;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DeploymentTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("Deployment Tests");
        suite.addTest(new DeploymentTest("testStress"));
        suite.addTest(new DeploymentAcceptanceTest("testOpenServerContextMenu"));
        suite.addTest(new DeploymentTest("testCloseServerContextMenu"));
        suite.addTest(new DeploymentTest("testCorrectnessServerContextMenu"));
        suite.addTest(new DeploymentTest("testCorrectnessServerContextMenuAfterChangeServerState"));
        suite.addTest(new DeploymentTest("testServerLog"));
        suite.addTest(new DeploymentTest("testServerProperties"));
        suite.addTest(new DeploymentTest("testOpenDeployedComponentsContextMenu"));
        suite.addTest(new DeploymentTest("testCloseDeployedComponentsContextMenu"));
        suite.addTest(new DeploymentTest("testRefreshDeployedComponents"));
        suite.addTest(new DeploymentTest("testOpenResourcesContextMenu"));
        suite.addTest(new DeploymentTest("testCloseResourcesContextMenu"));
        suite.addTest(new DeploymentTest("testResourcesProperties"));
        suite.addTest(new DeploymentTest("testDeleteResources"));
        suite.addTest(new DeploymentTest("testDeleteMultipleResources"));
        suite.addTest(new DeploymentAcceptanceTest("testDeploySimpleProject"));
        suite.addTest(new DeploymentAcceptanceTest("testRedeploySimpleProject"));
        suite.addTest(new DeploymentAcceptanceTest("testOpenApplicationContextMenu"));
        suite.addTest(new DeploymentTest("testCloseApplicationContextMenu"));
        suite.addTest(new DeploymentTest("testApplicationProperties"));
        suite.addTest(new DeploymentTest("testDisableApplication"));
        suite.addTest(new DeploymentTest("testEnableApplication"));
        suite.addTest(new DeploymentAcceptanceTest("testUndeployApplication"));
        suite.addTest(new DeploymentAcceptanceTest("testDeployDBProject"));
        suite.addTest(new DeploymentAcceptanceTest("testRedeployDBProject"));
        suite.addTest(new DeploymentTest("testDeployWSProject"));
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
    }
    
    public void testStress() {
        dtd = new DeploymentTargetDescriptor();
        dtd.load();
        if (serverType != null) dtd.setProperty(dtd.SERVER_TYPE_KEY, serverType);
        dt = IDE.getIDE().addDeploymentTarget(dtd);
        as = (ApplicationServer) dt;
        AS_PREF = as.getName().replace(' ', '_').replace('.', '_');
        for (int i = 0; i < 1; i++) {
            try {as.start();} catch (Exception e) {
                System.out.println("Exception occured: ");
                e.printStackTrace();
            }
            TestUtils.wait(5000);
            try {as.stop();} catch (Exception e) {
                System.out.println("Exception occured: ");
                e.printStackTrace();
            }
            TestUtils.wait(5000);
        }
    }
    
    public void testCloseServerContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        snpm.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup Menu doesn't disappear!");
        } catch(Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
        }
    }
    
    public void testCorrectnessServerContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(3000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        for (int i = 0; i < as.server_popup.length; i++) {
            try {
                JMenuItemOperator mio = new JMenuItemOperator(snpm, as.server_popup[i][0]);
                String state = ((new Boolean(as.server_popup[i][1])).booleanValue())?"enabled":"disabled";
                if (!(new  Boolean(mio.isEnabled()).toString().equalsIgnoreCase(as.server_popup[i][1])))
                    fail("\"" + as.server_popup[i][0] + "\" item is not " + state + "!");
            } catch (Exception e) {
                fail("\"" + as.server_popup[i][0] + "\" is not exist!");
            }
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
    }
    
    public void testCorrectnessServerContextMenuAfterChangeServerState() {
        as.stop();
        TestUtils.wait(15000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(3000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        for (int i = 0; i < as.server_popup.length; i++) {
            try {
                JMenuItemOperator mio = new JMenuItemOperator(snpm, as.server_popup[i][0]);
                String state = ((new Boolean(as.server_popup[i][2])).booleanValue())?"enabled":"disabled";
                if (!(new  Boolean(mio.isEnabled()).toString().equalsIgnoreCase(as.server_popup[i][2])))
                    fail("\"" + as.server_popup[i][0] + "\" item is not " + state + "!");
            } catch (Exception e) {
                fail("\"" + as.server_popup[i][0] + "\" is not exist!");
            }
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
    }
    
    public void testServerLog() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        TestUtils.wait(1000);
        try {as.start();} catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
        }
        TestUtils.wait(15000);
        sntree.callPopupOnPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(3000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(snpm, as.SERVER_VIEW_LOG).pushNoBlock();
        TestUtils.wait(1000);
        String serverType = dt.getDescriptor().getProperty(dt.getDescriptor().SERVER_TYPE_KEY);
        String forMatch = "";
        if (serverType.equals(server.STR_NAME_WEBLOGIC)) forMatch = "WebLogic";
        if (serverType.equals(server.STR_NAME_JBOSS)) forMatch = "JBoss";
        if (serverType.equals(server.STR_NAME_APPSERVER)) forMatch = "Sun Java System Application Server";
        if (serverType.equals(server.STR_NAME_TOMCAT50)) forMatch = "Apache Tomcat/5.0";
        if (serverType.equals(server.STR_NAME_TOMCAT55)) forMatch = "Apache Tomcat/5.5";
        if (serverType.equals(server.STR_NAME_TOMCAT60)) forMatch = "Apache Tomcat/6.0";
        TopComponentOperator tcoOutput = new TopComponentOperator("Output");
        System.out.println("Output text: " + new JEditorPaneOperator(tcoOutput).getText());
        if (new JEditorPaneOperator(tcoOutput).getText().indexOf(forMatch) == -1)
            fail("Wrong information in Output window! String \"" + forMatch + "\" is not detected!");
        TestUtils.wait(1000);
        tcoOutput.close();
    }
    
    public void testServerProperties() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(3000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(snpm, as.PROPERTIES, 0).pushNoBlock();
        TestUtils.wait(1000);
        new JDialogOperator(server.STR_DIALOG_TITLE_SERVER_MANAGER).close();
        TestUtils.wait(1000);
    }
    
    public void testOpenDeployedComponentsContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.expandPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        try {
            new JMenuItemOperator(snpm, as.REFRESH);
        } catch (Exception e) {
            fail("Refresh not exist!");
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testCloseDeployedComponentsContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        TestUtils.wait(1000);
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        snpm.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup Menu doesn't disappear!");
        } catch(Exception e) {}
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testRefreshDeployedComponents() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path));
        TestUtils.wait(1000);
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._firstProjectName, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        if (as.getName().indexOf("WebLogic") == -1) {
            try {
                sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + AS_PREF + "_" + dat._firstProjectName);
            } catch(Exception e) {
                fail("Excetion in HTTP check : " + e);
            }
            finally {
                dat.finishProject(prj, true, true);
            }
        }
        else {
            dat.finishProject(prj, false, true);
        }
    }
    
    public void testOpenResourcesContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.expandPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        try {
            new JMenuItemOperator(snpm, as.REFRESH);
        } catch (Exception e) {
            fail("Refresh not exist!");
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testCloseResourcesContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        TestUtils.wait(1000);
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        snpm.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup Menu doesn't disappear!");
        } catch(Exception e) {}
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testResourcesProperties() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path));
        TestUtils.wait(1000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "|jdbc/Test1", as.PROPERTIES);
        TestUtils.wait(1000);
        new JDialogOperator(as.PROPERTIES).close();
        TestUtils.wait(1000);
    }
    
    public void testDeleteResources() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.expandPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path));
        TestUtils.wait(1000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "|jdbc/Test1", dat.deleteResPopup);
        TestUtils.wait(1000);
        try {
            sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "jdbc/Test1");
            fail("Resource is not deleted!");
        } catch(Exception e) {}
        TestUtils.wait(1000);
    }
    
    public void testDeleteMultipleResources() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.expandPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path));
        TestUtils.wait(1000);
        TreePath[] deleteResource = {sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "|jdbc/Test2"),
        sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "|jdbc/Test3")};
        sntree.callPopupOnPaths(deleteResource);
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(snpm, dat.deleteResPopup).pushNoBlock();
        TestUtils.wait(1000);
        try {
            sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "jdbc/Test2");
            fail("Resource is not deleted!");
        } catch(Exception e) {}
        try {
            sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.jdbc_resources_path + "jdbc/Test3");
            fail("Resource is not deleted!");
        } catch(Exception e) {}
        TestUtils.wait(1000);
    }
    
    public void testCloseApplicationContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + AS_PREF + "_" + dat._simpleProjectName));;
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        snpm.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup Menu doesn't disappear!");
        } catch(Exception e) {}
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testApplicationProperties() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.expandPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path));
        TestUtils.wait(1000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + AS_PREF + "_" + dat._simpleProjectName, as.PROPERTIES);
        TestUtils.wait(1000);
        new JDialogOperator(as.PROPERTIES).close();
        TestUtils.wait(1000);
    }
    
    public void testDisableApplication() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + AS_PREF + "_" + dat._simpleProjectName, as.APPLICATION_DISABLE);
        TestUtils.wait(5000);
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._simpleProjectName);
            if (response.getText().indexOf("HTTP Status 503 - This application is not currently available") == -1)
                fail("Application might be not disabled! Exception is not thrown!");
        } catch (Exception e) {
            if (e.getClass().getName().equals("com.meterware.httpunit.HttpException")) {
                if (e.toString().indexOf("Error on HTTP request: 503") == -1) fail("Wrong response! Application might be not disabled!");
            } else {
                System.out.println("Exception occured: ");
                e.printStackTrace();
                fail("Excetion in HTTP check : " + e);
            }
        }
        TestUtils.wait(5000);
    }
    
    public void testEnableApplication() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + AS_PREF + "_" + dat._simpleProjectName, as.APPLICATION_ENABLE);
        TestUtils.wait(5000);
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._simpleProjectName);
        } catch (Exception e) {
            if (e.toString().indexOf("Error on HTTP request: 503") != -1) fail("Wrong response! Application might be not enabled!");
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        TestUtils.wait(5000);
    }
    
    public void testDeployWSProject() {
        addWebService(dat.wsURL);
        as.start();
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._wsProjectName, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._wsProjectName, true);
        TestUtils.wait(1000);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualComponent vcmp = (VisualComponent) wp.add((WebComponent)
            IDE.getIDE().getDefaultComponentSet().getComponent(StaticTextComponent.STATIC_TEXT_ID), new Point(48, 48));
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(dat.wsPath + dat.wsName, dat.addToPagePopup);
        TestUtils.wait(30000);
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.switchToJavaSource();
        TestUtils.wait(1000);
        EditorOperator editor = new EditorOperator(RaveWindowOperator.getDefaultRave(), "Page1.java");
        TestUtils.wait(2000);
        new FindAction().performShortcut();
        TestUtils.wait(500);
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
        editor.txtEditorPane().typeText("try {\n");
        editor.txtEditorPane().typeText("staticText1.setText(USWeatherSoapClient1.getWeatherReport(\"94025\"));\n");
        editor.txtEditorPane().typeText("}\n");
        editor.txtEditorPane().typeText("catch(Exception e) {");
        editor.txtEditorPane().clickForPopup();
        JPopupMenuOperator epm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(epm, dat.reformatePopup).push();
        TestUtils.wait(2000);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        if (as.getName().indexOf("WebLogic") == -1) {
            server.pushPopup(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path, as.REFRESH);
            TestUtils.wait(1000);
            try {
                sntree.selectPath(sntree.findPath(ServerExplorerOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + as.app_pref + AS_PREF + "_" + dat._wsProjectName));
            } catch (Exception e) {fail("There is no WSProject application in Deployed Components node!");}
            TestUtils.wait(1000);
        }
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._wsProjectName);
            String verificationString = "Weather";
            if (response.getText().indexOf(verificationString) == -1) fail("There is no needed string in response!");
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
            as.stop();
        }
    }
    
    private void addWebService(String URL) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        server.pushPopup(dat.wsPath.substring(0, dat.wsPath.length() - 1), dat.popup_addWS);
        TestUtils.wait(1000);
        AddWebServiceOperator wsOp = new AddWebServiceOperator();
        wsOp.addWebService(URL);
        TestUtils.wait(2000);
    }
}
