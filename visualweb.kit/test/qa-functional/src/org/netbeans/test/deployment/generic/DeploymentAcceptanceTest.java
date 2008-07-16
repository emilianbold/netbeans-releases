/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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

package org.netbeans.test.deployment.generic;

import org.netbeans.jemmy.operators.*;
import junit.framework.Test;

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.RaveTestCase;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.model.project.components.*;
import org.netbeans.modules.visualweb.gravy.model.project.*;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.model.*;

import com.meterware.httpunit.*;
import java.util.Iterator;
import java.util.List;
import java.awt.Point;
import java.io.File;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Roman Mostyka
 */

public class DeploymentAcceptanceTest extends RaveTestCase {
    
    public final static String btn_OK = "OK";
    public final static String btn_Yes = "Yes";
    public final static String btn_Add = "Add";
    public final static String dlg_bindDB = "Bind to Data";
    public final static String dbPath = "Databases|";
    public final static String wsPath = "Web Services|";
    public final static String dbConnect = "Connect";
    public final static String dbDisconnect = "Disconnect";
    public final static String testDBName = "jdbc:derby://localhost:1527/sample [app on APP]";
    public final static String bindDBPopup = "Bind to Data...";
    public final static String deleteResPopup = "Delete Resource";
    public final static String deletePopup = "Delete";
    public final static String reformatePopup = "Format";
    public final static String dlg_ConfirmDeletion = "Confirm Object Deletion";
    public final static String dlg_addWS = "Add Web Service";
    public final static String popup_addWS = "Add Web Service...";
    public final static String addToPagePopup = "Add to Page";
    public final static String bindToDP = "Bind to Data Provider";
    public static String _simpleProjectName = "SimpleProject";
    public static String _simpleJavaEE5ProjectName = "SimpleJavaEE5Project";
    public static String _dbProjectName = "DBProject";
    public static String _wsProjectName = "WSProject";
    public static String _firstProjectName = "FirstProject";
    public static String _testProjectName = "TestProject";
    public static String _projectPath = System.getProperty("xtest.workdir") + File.separator + "projects" + File.separator;
    public static String serverFullPath = "";
    public static String path_to_applications = "";
    public static String wsURL = "http://www.webservicex.net/usweather.asmx?WSDL";
    public static String wsName = "USWeather|USWeatherSoap";
    public static String wsLabel = "Services tab web services compiling script";
    public static String pluginName = "Visual Web JSF Backwards Compatibility Kit";
    private ServerNavigatorOperator server;
    private JTreeOperator sntree, aotree;
    private DesignerPaneOperator designer;
    private DocumentOutlineOperator outline;
    private static ProjectDescriptor pd;
    private static Project prj;
    private static WebPageFolder wpf;
    private static WebPage wp;
    private static DeploymentTargetDescriptor dtd;
    private static DeploymentTarget dt;
    private static ApplicationServer as;
    public static String serverType = null;
    static String AS_PREF;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DeploymentAcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(DeploymentAcceptanceTest.class);
        conf = addServerTests(J2eeTestCase.Server.GLASSFISH, conf,
              "testOpenServerContextMenu", "testDeploySimpleProject",
              "testRedeploySimpleProject", "testOpenApplicationContextMenu",
              "testUndeployApplication", "testDeploySimpleJavaEE5Project",
              "testDeployDBProject","testRedeployDBProject");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
     }
    
    /** method called before each testcase
     */
    protected void setUp() {
    }
    
    public void testOpenServerContextMenu() {
        PluginsOperator.getInstance().installAvailablePlugins(pluginName);
        TestUtils.wait(1000);
        dtd = new DeploymentTargetDescriptor();
        dtd.load();
        if (serverType != null) dtd.setProperty(dtd.SERVER_TYPE_KEY, serverType);
        dt = IDE.getIDE().addDeploymentTarget(dtd);
        as = (ApplicationServer) dt;
        AS_PREF = as.getName().replace(' ', '_').replace('.', '_');
        String serverName = dt.getName();
        String serverType = dt.getDescriptor().getProperty(dt.getDescriptor().SERVER_TYPE_KEY);
        as.start();
        TestUtils.wait(1000);
        path_to_applications = server.STR_SERVERS_PATH + as.web_applications_path;
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        serverFullPath = server.STR_SERVERS_PATH + serverName;
        sntree.callPopupOnPath(sntree.findPath(serverFullPath));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        for (int i = 0; i < as.server_popup.length; i++) {
            try {
                new JMenuItemOperator(snpm, as.server_popup[i][0]);
            } catch (Exception e) {
                fail("\"" + as.server_popup[i][0] + "\" is not exist!");
            }
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(serverFullPath));
        TestUtils.wait(1000);
    }
    
    public void testDeploySimpleProject() {
        pd = new ProjectDescriptor(AS_PREF + "_" + _simpleProjectName, _projectPath, ProjectDescriptor.J2EE14, dt.getName());
        prj = IDE.getIDE().createProject(pd);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        if (as.getName().indexOf("WebLogic") == -1) {
            sntree.selectPath(sntree.findPath(path_to_applications));
            TestUtils.wait(1000);
            server.pushPopup(sntree, path_to_applications, ((ApplicationServer) dt).REFRESH);
            TestUtils.wait(1000);
            try {
                sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleProjectName));
            } catch (Exception e) {
                fail("There is no SimpleProject application in Deployed Components node!");
            }
            TestUtils.wait(1000);
        }
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _simpleProjectName);
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        TestUtils.wait(5000);
    }
    
    public void testRedeploySimpleProject() {
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(sntree, serverFullPath, as.REFRESH);
        TestUtils.wait(1000);
        if (as.getName().indexOf("WebLogic") == -1) {
            try {
                sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleProjectName));
            } catch (Exception e) {
                fail("There is no SimpleProject application in Deployed Components node!");
            }
            TestUtils.wait(1000);
        }
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _simpleProjectName);
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        TestUtils.wait(5000);
    }
    
    public void testOpenApplicationContextMenu() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleProjectName));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        ApplicationServer as = (ApplicationServer) dt;
        TestUtils.wait(1000);
        for (int i = 0; i < as.application_popup.length; i++) {
            try {
                new JMenuItemOperator(snpm, as.application_popup[i]);
            } catch (Exception e) {
                fail("\"" + as.application_popup[i] + "\" is not exist!");
            }
        }
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(serverFullPath));
    }
    
    public void testUndeployApplication() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleProjectName, as.APPLICATION_UNDEPLOY);
        TestUtils.wait(10000);
        try {
            sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleProjectName));
            fail("SimpleProject application is still in Deployed Components!");
        } catch (Exception e) {}
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _simpleProjectName);
            if (response.getText().indexOf("HTTP Status 404") == -1)
                fail("SimpleProject application is not undeployed!");
        } catch (Exception e) {}
        finally {
            finishProject(prj, false, true);
        }
    }
    
    public void testDeploySimpleJavaEE5Project() {
        pd = new ProjectDescriptor(AS_PREF + "_" + _simpleJavaEE5ProjectName, _projectPath, ProjectDescriptor.JavaEE5, dt.getName());
        prj = IDE.getIDE().createProject(pd);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(path_to_applications));
        TestUtils.wait(1000);
        server.pushPopup(sntree, path_to_applications, ((ApplicationServer) dt).REFRESH);
        TestUtils.wait(1000);
        try {
            server = ServerNavigatorOperator.showNavigatorOperator();
            TestUtils.wait(1000);
            sntree = server.getTree();
            sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _simpleJavaEE5ProjectName));
        } catch (Exception e) {
            fail("There is no SimpleProject application in Deployed Components node!");
        }
        TestUtils.wait(1000);
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _simpleJavaEE5ProjectName);
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            finishProject(prj, true, true);
        }
    }
    
    public void testDeployDBProject() {
        pd = new ProjectDescriptor(AS_PREF + "_" + _dbProjectName, _projectPath, ProjectDescriptor.J2EE14, dt.getName());
        prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + _dbProjectName, true);
        TestUtils.wait(1000);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualComponent vcmp = (VisualComponent) wp.add((WebComponent)
            IDE.getIDE().getDefaultComponentSet().getComponent(TableComponent.TABLE_ID), new Point(48, 48));
        connectToDB(testDBName);
        TestUtils.wait(500);
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dbPath + testDBName + "|Tables|CUSTOMER"));
        designer.requestFocus();
        designer.clickMouse(50, 50, 1);
        TestUtils.wait(2000);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(sntree, serverFullPath, as.REFRESH);
        TestUtils.wait(1000);
        if (as.getName().indexOf("WebLogic") == -1) {
            try {
                sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _dbProjectName));
            } catch (Exception e) {fail("There is no DBProject application in Deployed Components node!");}
            TestUtils.wait(1000);
            try {
                WebConversation conversation = new WebConversation();
                WebResponse response = null;
                System.out.println("requestPrefix=" + as.requestPrefix);
                response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _dbProjectName);
            } catch (Exception e) {
                System.out.println("Exception occured: ");
                e.printStackTrace();
                fail("Excetion in HTTP check : " + e);
            }
            TestUtils.wait(1000);
        }
        server.pushPopup(sntree, serverFullPath, as.REFRESH);
        TestUtils.wait(1000);
    }
    
    public void testRedeployDBProject() {
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        deleteComponentFromPage(wp.getName(), "page1|html1|body1|form1|table1");
        deleteComponentFromPage(wp.getName(), "customerDataProvider");
        deleteComponentFromPage("SessionBean1", "customerRowSet");
        TestUtils.wait(1000);
        prj.run();
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        server.pushPopup(sntree, serverFullPath, as.REFRESH);
        TestUtils.wait(1000);
        if (as.getName().indexOf("WebLogic") == -1) {
            try {
                sntree.selectPath(sntree.findPath(path_to_applications + "|" + as.app_pref + AS_PREF + "_" + _dbProjectName));
            } catch (Exception e) {fail("There is no DBProject application in Deployed Components node!");}
            TestUtils.wait(1000);
        }
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + _dbProjectName);
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        } finally {
            finishProject(prj, true, true);
            disconnectFromDB(testDBName);
            TestUtils.wait(1000);
            stopDB();
            TestUtils.wait(1000);
            as.stop();
        }
    }
    
    public static void startDB() {
        try {
            Util.getMainMenu().pushMenu("Tools|Java DB Database|Start Server");
        } catch (Exception e) {
            System.out.println("Exception by attempt to call \"Tools|Java DB Database|Start Server\"");
        }
        TestUtils.wait(2000);
    }
    
    public static void stopDB() {
        try {
            Util.getMainMenu().pushMenu("Tools|Java DB Database|Stop Server");
        } catch (Exception e) {
            System.out.println("Exception by attempt to call \"Tools|Java DB Database|Stop Server\"");
        }
        TestUtils.wait(2000);
    }
    
    public static void connectToDB(String dbName) {
        ServerNavigatorOperator serverOperator = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        JTreeOperator sotree = serverOperator.getTree();
        serverOperator.pushPopup(sotree, dbPath + dbName, dbConnect);
        TestUtils.wait(5000);
    }
    
    public static void disconnectFromDB(String dbName) {
        ServerNavigatorOperator serverOperator = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        JTreeOperator sotree = serverOperator.getTree();
        serverOperator.pushPopup(sotree, dbPath + dbName, dbDisconnect);
        TestUtils.wait(2000);
    }
    
    public static void finishProject(Project project, boolean undeploy, boolean close) {
        ApplicationServer as = null;
        String asName = project.getDescriptor().getProperty(ProjectDescriptor.SERVER_KEY);
        List asList = IDE.getIDE().getDeploymentTargets();
        Iterator asIterator = asList.iterator();
        while (asIterator.hasNext()) {
            as = (ApplicationServer) asIterator.next();
            if (asName != null && as.getName().equals(asName)) break;
        }
        if (as != null && as.getName().indexOf("WebLogic") == -1 && undeploy) {
            ServerNavigatorOperator serverOperator = ServerNavigatorOperator.showNavigatorOperator();
            TestUtils.wait(4000);
            JTreeOperator sntree = serverOperator.getTree();
            serverOperator.pushPopup(serverOperator.STR_SERVERS_PATH + as.web_applications_path, as.REFRESH);
            TestUtils.wait(1000);
            serverOperator.pushPopup(serverOperator.STR_SERVERS_PATH + as.web_applications_path + "|" + project.getName(), as.APPLICATION_UNDEPLOY);
            TestUtils.wait(3000);
        }
        Util.saveAllAPICall();
        TestUtils.wait(1000);
        if (close) project.close();
        TestUtils.wait(2000);
    }
    
    public void deleteComponentFromPage(String page, String full_component_path) {
        outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        aotree = outline.getStructTreeOperator();
        aotree.callPopupOnPath(aotree.findPath(page + "|" + full_component_path));
        TestUtils.wait(1000);
        JPopupMenuOperator aopm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(aopm, deletePopup).pushNoBlock();
        TestUtils.wait(1000);
        new JButtonOperator(new JDialogOperator(dlg_ConfirmDeletion), btn_Yes).push();
        TestUtils.wait(1000);
    }
}