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
import org.netbeans.junit.NbTestSuite;
import junit.framework.Test;

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.RaveTestCase;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.model.IDE;
import org.netbeans.modules.visualweb.gravy.model.components.DropDownListComponent;
import org.netbeans.modules.visualweb.gravy.model.components.TableComponent;
import org.netbeans.modules.visualweb.gravy.model.components.WebComponent;
import org.netbeans.modules.visualweb.gravy.model.deployment.ApplicationServer;
import org.netbeans.modules.visualweb.gravy.model.deployment.DeploymentTarget;
import org.netbeans.modules.visualweb.gravy.model.deployment.DeploymentTargetDescriptor;
import org.netbeans.modules.visualweb.gravy.model.project.Project;
import org.netbeans.modules.visualweb.gravy.model.project.ProjectDescriptor;
import org.netbeans.modules.visualweb.gravy.model.project.WebPage;
import org.netbeans.modules.visualweb.gravy.model.project.WebPageFolder;
import org.netbeans.modules.visualweb.gravy.model.project.components.VisualComponent;
import org.netbeans.modules.visualweb.gravy.model.project.components.VisualEventHandlingComponent;

import com.meterware.httpunit.*;
import java.awt.*;

/**
 *
 * @author Roman Mostyka
 */

public class OtherDeploymentTest extends RaveTestCase {
   
    private static String dlg_addNewDataProvider = "Add New Data Provider";
    private static int projectNumber = 1;
    private ServerNavigatorOperator server;
    private JTreeOperator sntree;
    private DesignerPaneOperator designer;
    private DeploymentAcceptanceTest dat;
    private static DeploymentTargetDescriptor dtd;
    private static DeploymentTarget dt;
    private static ApplicationServer as;
    public static String serverType = null;
    static String AS_PREF;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public OtherDeploymentTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("Other Deployment Tests");
        suite.addTest(new OtherDeploymentTest("testRestartIDEandDeploy_2"));
        suite.addTest(new OtherDeploymentTest("testStopStartASandDeploy"));
        suite.addTest(new OtherDeploymentTest("testStopStartDBandDeploy"));
        suite.addTest(new OtherDeploymentTest("testStopASandDeploy"));
        suite.addTest(new OtherDeploymentTest("testStopDBandDeploy"));
        suite.addTest(new OtherDeploymentTest("testChangeDSandDeploy"));
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
    }
    
    public void testRestartIDEandDeploy_2() {
        PluginsOperator.getInstance().installAvailablePlugins(dat.pluginName);
        dtd = new DeploymentTargetDescriptor();
        dtd.load();
        if (serverType != null) dtd.setProperty(dtd.SERVER_TYPE_KEY, serverType);
        dt = IDE.getIDE().addDeploymentTarget(dtd);
        as = (ApplicationServer) dt;
        AS_PREF = as.getName().replace(' ', '_').replace('.', '_');
        dat.startDB();
        TestUtils.wait(1000);
        dat.connectToDB(dat.testDBName);
        TestUtils.wait(1000);
        as.start();
        TestUtils.wait(1000);
        dat.path_to_applications = server.STR_SERVERS_PATH + as.web_applications_path;
        if (as.getName().indexOf("WebLogic") == -1) {
            Project prj = null;
            try {
                prj = IDE.getIDE().openProject(dat._projectPath + AS_PREF + "_" + dat._testProjectName + projectNumber);
            }
            catch (Exception e) {
                new JButtonOperator(new JDialogOperator("Open Project"), "Cancel").push();
                fail("There is no specified project!");
            }
            TestUtils.wait(5000);
            System.out.println("Project " + AS_PREF + "_" + dat._testProjectName + projectNumber + " is opened.");
            prj.run();
            try {
                WebConversation conversation = new WebConversation();
                WebResponse response = null;
                System.out.println("requestPrefix=" + as.requestPrefix);
                response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._testProjectName + projectNumber);
                if (response.getText().indexOf("JumboCom") == -1) {
                    server.pushPopup(dat.path_to_applications + "|" + as.app_pref + AS_PREF + "_" + dat._testProjectName + projectNumber, as.APPLICATION_UNDEPLOY);
                    TestUtils.wait(1000);
                    prj.close();
                    fail("Application doesn't contain right data!");
                }
            } catch (Exception e) {
                System.out.println("Exception occured: ");
                e.printStackTrace();
                fail("Excetion in HTTP check : " + e);
            }
            finally {
                dat.finishProject(prj, true, true);
            }
            WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
            WebPage wp = wpf.getWebPage("Page1");
            wp.open();
            VisualEventHandlingComponent vehcmp = (VisualEventHandlingComponent) wp.add((WebComponent)
                IDE.getIDE().getDefaultComponentSet().getComponent(DropDownListComponent.DROP_DOWN_LIST_ID), new Point(48, 96));
            server = ServerNavigatorOperator.showNavigatorOperator();
            TestUtils.wait(4000);
            sntree = server.getTree();
            sntree.selectPath(sntree.findPath(dat.dbPath + dat.testDBName + "|Tables|CUSTOMER"));
            TestUtils.wait(1000);
            designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
           
            //workaround
            // --- start ---
            new Thread(new Runnable() {
                public void run() {
                    designer.clickMouse(50, 100, 1);
                }
            }).start();
            // --- stop ---
            
            TestUtils.wait(3000);
            new JButtonOperator(new JDialogOperator(dlg_addNewDataProvider), dat.btn_OK).pushNoBlock();
            TestUtils.wait(1000);
            designer.requestFocus();
            designer.clickForPopup(50, 100);
            JPopupMenuOperator popup = new JPopupMenuOperator();
            popup.pushMenuNoBlock(dat.bindDBPopup);
            JDialogOperator dataBind = new JDialogOperator(dat.dlg_bindDB);
            new JTabbedPaneOperator(dataBind).selectPage(dat.bindToDP);
            TestUtils.wait(1000);
            JListOperator valueList = new JListOperator(dataBind, 0);
            TestUtils.wait(1000);
            valueList.selectItem("CUSTOMER.CUSTOMER_ID");
            TestUtils.wait(1000);
            JListOperator displayList = new JListOperator(dataBind, 1);
            TestUtils.wait(1000);
            displayList.selectItem("CUSTOMER.CITY");
            TestUtils.wait(1000);
            new JButtonOperator(dataBind, dat.btn_OK).pushNoBlock();
            TestUtils.wait(1000);
            prj.run();
            try {
                WebConversation conversation = new WebConversation();
                WebResponse response = null;
                System.out.println("requestPrefix=" + as.requestPrefix);
                response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._testProjectName + projectNumber);
                if (response.getText().indexOf("JumboCom") == -1 ||
                        response.getText().indexOf("Fort Lauderdale") == -1) fail("Application doesn't contain right data!");
            } catch (Exception e) {
                System.out.println("Exception occured: ");
                e.printStackTrace();
                fail("Excetion in HTTP check : " + e);
            }
            finally {
                dat.finishProject(prj, true, true);
            }
        }
    }
    
    public void testStopStartASandDeploy() {
        projectNumber++;
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._testProjectName + projectNumber, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._testProjectName + projectNumber, true);
        TestUtils.wait(1000);
        as.stop();
        TestUtils.wait(5000);
        as.start();
        TestUtils.wait(5000);
        prj.run();
        try {
            verifyHTTPSimple();
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
        }
    }
    
    public void testStopStartDBandDeploy() {
        projectNumber++;
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._testProjectName + projectNumber, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._testProjectName + projectNumber, true);
        TestUtils.wait(1000);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualComponent vcmp = (VisualComponent) wp.add((WebComponent)
            IDE.getIDE().getDefaultComponentSet().getComponent(TableComponent.TABLE_ID), new Point(48, 48));
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.dbPath + dat.testDBName + "|Tables|CUSTOMER"));
        designer.requestFocus();
        designer.clickMouse(50, 50, 1);
        TestUtils.wait(2000);
        dat.stopDB();
        TestUtils.wait(5000);
        dat.startDB();
        TestUtils.wait(5000);
        prj.run();
        try {
            verifyHTTPDB("JumboCom");
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
        }
    }
    
    public void testStopASandDeploy() {
        projectNumber++;
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._testProjectName + projectNumber, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._testProjectName + projectNumber, true);
        TestUtils.wait(1000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        as.stop();
        TestUtils.wait(5000);
        prj.run();
        try {
            verifyHTTPSimple();
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
        }
    }
    
    public void testStopDBandDeploy() {
        projectNumber++;
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._testProjectName + projectNumber, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._testProjectName + projectNumber, true);
        TestUtils.wait(1000);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualComponent vcmp = (VisualComponent) wp.add((WebComponent)
            IDE.getIDE().getDefaultComponentSet().getComponent(TableComponent.TABLE_ID), new Point(48, 48));
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.dbPath + dat.testDBName + "|Tables|CUSTOMER"));
        designer.requestFocus();
        designer.clickMouse(50, 50, 1);
        TestUtils.wait(2000);
        dat.stopDB();
        TestUtils.wait(5000);
        prj.run();
        try {
            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            System.out.println("requestPrefix=" + as.requestPrefix);
            response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._testProjectName + projectNumber);
        } catch (Exception e) {
            if (e.getClass().getName().equals("com.meterware.httpunit.HttpInternalErrorException")) {
                if (e.toString().indexOf("Error on HTTP request: 500 Internal Error") == -1) fail("Wrong response!");
            } else {
                System.out.println("Exception occured: ");
                e.printStackTrace();
                fail("Excetion in HTTP check : " + e);
            }
        }
        finally {
            dat.finishProject(prj, true, true);
        }
    }
    
    public void testChangeDSandDeploy() {
        projectNumber++;
        ProjectDescriptor pd = new ProjectDescriptor(AS_PREF + "_" + dat._testProjectName + projectNumber, dat._projectPath, ProjectDescriptor.J2EE14, as.getName());
        Project prj = IDE.getIDE().createProject(pd);
        TestUtils.disableBrowser(AS_PREF + "_" + dat._testProjectName + projectNumber, true);
        TestUtils.wait(1000);
        dat.startDB();
        TestUtils.wait(5000);
        WebPageFolder wpf = prj.getRoot().getWebPageRootFolder();
        WebPage wp = wpf.getWebPage("Page1");
        VisualEventHandlingComponent vehcmp = (VisualEventHandlingComponent) wp.add((WebComponent)
            IDE.getIDE().getDefaultComponentSet().getComponent(DropDownListComponent.DROP_DOWN_LIST_ID), new Point(48, 48));
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(4000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(dat.dbPath + dat.testDBName + "|Tables|CUSTOMER"));
        TestUtils.wait(1000);
        designer.requestFocus();
        designer.clickMouse(50, 50, 1);
        TestUtils.wait(1000);
        designer.requestFocus();
        designer.clickForPopup(50, 50);
        JPopupMenuOperator popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock(dat.bindDBPopup);
        JDialogOperator dataBind = new JDialogOperator(dat.dlg_bindDB);
        new JTabbedPaneOperator(dataBind).selectPage(dat.bindToDP);
        TestUtils.wait(1000);
        JListOperator valueList = new JListOperator(dataBind, 0);
        TestUtils.wait(1000);
        valueList.selectItem("CUSTOMER.CUSTOMER_ID");
        TestUtils.wait(1000);
        JListOperator displayList = new JListOperator(dataBind, 1);
        TestUtils.wait(1000);
        displayList.selectItem("CUSTOMER.NAME");
        TestUtils.wait(1000);
        new JButtonOperator(dataBind, dat.btn_OK).pushNoBlock();
        TestUtils.wait(1000);
        prj.run();
        try {
            verifyHTTPDB("JumboCom");
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
            dat.disconnectFromDB(dat.testDBName);
            TestUtils.wait(1000);
            dat.stopDB();
            TestUtils.wait(1000);
            as.stop();
            TestUtils.wait(1000);
        }
        designer.requestFocus();
        designer.clickForPopup(50, 50);
        popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock(dat.bindDBPopup);
        dataBind = new JDialogOperator(dat.dlg_bindDB);
        new JTabbedPaneOperator(dataBind).selectPage(dat.bindToDP);
        TestUtils.wait(1000);
        valueList = new JListOperator(dataBind, 0);
        TestUtils.wait(1000);
        valueList.selectItem("CUSTOMER.CUSTOMER_ID");
        TestUtils.wait(1000);
        displayList = new JListOperator(dataBind, 1);
        TestUtils.wait(1000);
        displayList.selectItem("CUSTOMER.CITY");
        TestUtils.wait(1000);
        new JButtonOperator(dataBind, dat.btn_OK).pushNoBlock();
        TestUtils.wait(1000);
        prj.run();
        try {
            verifyHTTPDB("Fort Lauderdale");
        } catch (Exception e) {
            System.out.println("Exception occured: ");
            e.printStackTrace();
            fail("Excetion in HTTP check : " + e);
        }
        finally {
            dat.finishProject(prj, true, true);
            dat.disconnectFromDB(dat.testDBName);
            TestUtils.wait(1000);
            dat.stopDB();
            TestUtils.wait(1000);
            as.stop();
            TestUtils.wait(1000);
        }
    }
    
    public void verifyHTTPSimple() throws Exception {
        WebConversation conversation = new WebConversation();
        WebResponse response = null;
        System.out.println("requestPrefix=" + as.requestPrefix);
        response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._testProjectName + projectNumber);
    }
    
    public void verifyHTTPDB(String toCompare) throws Exception{
        WebConversation conversation = new WebConversation();
        WebResponse response = null;
        System.out.println("requestPrefix=" + as.requestPrefix);
        response = conversation.getResponse(as.requestPrefix + AS_PREF + "_" + dat._testProjectName + projectNumber);
        if (response.getText().indexOf(toCompare) == -1) fail("String \"" + toCompare + "\" is not found in response!");
    }
}
