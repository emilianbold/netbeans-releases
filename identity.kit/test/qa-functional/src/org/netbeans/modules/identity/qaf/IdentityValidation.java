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
package org.netbeans.modules.identity.qaf;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.WsValidation;

/**
 *
 * @author jp154641
 *
 * This test suite was created to automate most basic Identity test cases
 * such as editing of AM Profiles, securing WS and WSC in Web and EJB Module
 * Basic functionality for web services area is implemented in WSValidation class
 */
public class IdentityValidation extends WsValidation {

    /**
     * Initialization of suite properties
     */
    public static String SERVER_PATH;
    private static final String SERVERS = Bundle.getString(
            "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
            "SERVER_REGISTRY_NODE"); //NOI18N
    private static final String TARGET_SERVER_NAME = "Glassfish V2"; //NOI18N
    public static File PROJECTS_FOLDER_FILE;
    public static File WSP_PROJECT_FILE;
    public static File WSC_PROJECT_FILE;

    @Override
     protected String getWsProjectName() {
        return "TestIdentityWSWebApp"; //NOI18N
    }

    @Override
    protected String getWsClientProjectName() {
        return "TestIdentityWSCWebApp"; //NOI18N
    }

    @Override
    protected String getWsName() {
        return "TestWS"; //NOI18N
    }

    @Override
    protected String getWsPackage() {
        return "org.identity.ws.test"; //NOI18N
    }

    @Override
    protected String getWsClientPackage() {
        return "org.identity.wsc.test"; //NOI18N
    }

    /** Creates a new instance of IdentityValidation */
    public IdentityValidation(String name) {
        super(name);
    }

    public static Test suite() {

        // This "nicely recursive" implementation is due to limitations in J2eeTestCase API
        return NbModuleSuite.create(
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH,
                addServerTests(Server.GLASSFISH, NbModuleSuite.emptyConfiguration(), IdentityValidation.class,
                    "prepareWSProject",
                    "prepareWSClientProject"
                    ), IdentityValidationInEJB.class,
                    "prepareWSProject",
                    "prepareWSClientProject"
                    ), IdentitySamplesTest.class,
                    "testStockQuoteService",
                    "testStockQuoteClient"
                    ), IdentityValidation.class,
                    "checkAMProfiles",
                    "testAMSecurityInWeb"
                    ), IdentityValidationInEJB.class,
                    "testAMSecurityInEJB"
                    ), IdentitySamplesTest.class,
                    "testUndeployAll"
                    ), IdentityValidation.class,
                    "stopSDKGlassfish"
                    ).enableModules(".*").clusters(".*")
                );



//
//        return NbModuleSuite.create(addServerTests(Server.GLASSFISH, NbModuleSuite.createConfiguration(IdentityValidation.class),
//                "prepareWSProject",
//                "prepareWSClientProject",
//                "prepareWSProject",
//                "prepareWSClientProject",
//                "testStockQuoteService",
//                "testStockQuoteClient",
//                "checkAMProfiles",
//                "testAMSecurityInWeb",
//                "testAMSecurityInEJB",
//                "testUndeployAll",
//                "stopSDKGlassfish"
//                ).enableModules(".*").clusters(".*"));
    }

//    public static TestSuite suite() {
//        TestSuite suite = new NbTestSuite();
//        suite.addTest(new IdentityValidation("prepareWSProject")); //NOI18N
//        suite.addTest(new IdentityValidation("prepareWSClientProject")); //NOI18N
//        suite.addTest(new IdentityValidationInEJB("prepareWSProject")); //NOI18N
//        suite.addTest(new IdentityValidationInEJB("prepareWSClientProject")); //NOI18N
//        suite.addTest(new IdentitySamplesTest("testStockQuoteService"));
//        suite.addTest(new IdentitySamplesTest("testStockQuoteClient"));
//        suite.addTest(new IdentityValidation("checkAMProfiles")); //NOI18N
//        suite.addTest(new IdentityValidation("testAMSecurityInWeb")); //NOI18N
//        suite.addTest(new IdentityValidationInEJB("testAMSecurityInEJB")); //NOI18N
//        suite.addTest(new IdentitySamplesTest("testUndeployAll"));
//        suite.addTest(new IdentityValidation("stopSDKGlassfish")); //NOI18N
//        return suite;
//    }
//
//    /**
//     * Use for execution inside IDE
//     */
//    public static void main(java.lang.String[] args) {
//        // run whole suite
//        TestRunner.run(suite());
//    }

    /**
     * This method creates test web project with web service and two
     * operations, then deploys it.
     * @throws java.io.IOException
     */
    public void prepareWSProject() throws IOException {
        testCreateNewWs();
//        testChangeServer(false);
        testAddOperation();
        testDeployWsProject();
    }

    /**
     * This method creates web module and web service client to previously
     * deployed web service. The word Client in name ot the suite method
     * is essential for correct working of this method!
     * @throws java.io.IOException
     */
    public void prepareWSClientProject() throws IOException {
        testCreateWsClient();
//        testChangeServer(true);
        testCallWsOperationInServlet();
        testDeployWsClientProject();
    }

     /**
     * Add SDK Glassfish - key component to use AM security
     * - adds SDK Glassfish with Access manager component
     */
//    public void addSDKGlassfish() {
//        SERVER_PATH = "/space/SDK"; //NOI18N
//        String osType = System.getProperty("os.name"); //NOI18N
//        if (osType.contains("Windows")) { //NOI18N
//            SERVER_PATH = "E:\\Space\\Sun\\SDK";
////            SERVER_PATH = "C:\\Sun\\SDK";      //NOI18N For testing purpose only
//        }
//        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); //NOI18N
//        RuntimeTabOperator runtime = new RuntimeTabOperator();
//        runtime.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 90000); //NOI18N
//        Node servers = new Node(runtime.getRootNode(), SERVERS);
//        servers.expand();
//        servers.callPopup().pushMenuNoBlock(actionName);
//        WizardOperator dialog = new WizardOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title")); //NOI18N
//        new JTextFieldOperator(dialog, 0).setText(TARGET_SERVER_NAME);
//        dialog.next();
//        new JTextFieldOperator(dialog, 0).setText(SERVER_PATH);
//        new org.netbeans.jemmy.EventTool().waitNoEvent(10000);
//        dialog.next();
//        new JTextFieldOperator(dialog, 1).setText("adminadmin"); //NOI18N
//        new JButtonOperator(dialog, "Finish").push(); //NOI18N
//    }

    /**
     * Start SDK Glassfish  - to test AM functionality
     * - this feature is neccessary,when we want to test AM profiles,since these are
     * available in Runtime tab only if server is running
     */
//    public void startSDKGlassfish() {
//        System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
//        J2eeServerNode serverNode = J2eeServerNode.invoke(TARGET_SERVER_NAME);
//        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 120000); //NOI18N
//        serverNode.start();
//    }

    /**
     * Test AM Profiles
     * - this test kills many birds with one stone, when it tests if AM node is present,
     * if AM Profiles are editable/configuration test and edit each of them
     */
    public void checkAMProfiles() {
        System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
        J2eeServerNode serverNode = J2eeServerNode.invoke(TARGET_SERVER_NAME);
        Node profiles = new Node(serverNode, "Access Manager|Profiles"); //NOI18N
        editProfile(profiles, "Anonymous"); //NOI18N
        editProfile(profiles, "SAML-SenderVouches"); //NOI18N
        editProfile(profiles, "SAML-HolderOfKey"); //NOI18N
        editProfile(profiles, "X509Token"); //NOI18N
        editProfile(profiles, "UserNameToken"); //NOI18N
        editProfile(profiles, "UserNameToken-Plain"); //NOI18N
        editProfile(profiles, "LibertyX509Token"); //NOI18N
        editProfile(profiles, "LibertyBearerToken"); //NOI18N
        editProfile(profiles, "LibertySAMLToken"); //NOI18N
    }

    /**
     * Method for editing of selected profile
     * - we check if Verify Response checkbox is editable
     */
    public void editProfile(Node profiles, String nodeName) {
        Node myNode = new Node(profiles, nodeName);
        myNode.callPopup().pushMenuNoBlock("Edit"); //NOI18N
        if (nodeName.equals("Anonymous")) { //NOI18N
            new EventTool().waitNoEvent(10000);
        }

        NbDialogOperator dialog = new NbDialogOperator("Edit " + nodeName + " Profile"); //NOI18N
        if (!nodeName.startsWith("Liberty")) { //NOI18N
            JCheckBoxOperator checkB = new JCheckBoxOperator(dialog, "Sign Response"); //NOI18N
            checkB.push();
        }

        dialog.ok();
        new EventTool().waitNoEvent(2000);
    }

    /**
     * Stop SDK Glassfish
     * - just stops the server,after testing's done
     */
    public void stopSDKGlassfish() {
        System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
        J2eeServerNode serverNode = J2eeServerNode.invoke(TARGET_SERVER_NAME);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 60000); //NOI18N
        serverNode.stop();
        new EventTool().waitNoEvent(2000);
    }

    /**
     * Test of AM security in web project
     * - both ws provider and client
     * - sets AM security in WS provider, deploys WS and then sets AM security in WSC and deploys it
     */
    public void testAMSecurityInWeb() {
        System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
        secureWSinWebModule();
        secureWSCinWebModule();
    }

    /**
     * Test of AM security in ejb project
     * - both ws provider and client
     * - sets AM security in WS provider, deploys WS and then sets AM security in WSC and deploys it
     */


    /**
     * Setting AM security for WS provider in web module
     */
    public void secureWSinWebModule() {
        ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd = new ProjectRootNode(prjtree, getWsProjectName());
        Node websvc = new Node(prjnd, "Web Services|TestWS"); //NOI18N
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.customization.core.ui.Bundle", "CTL_EditAttributesAction")); //NOI18N
        NbDialogOperator dialog = new NbDialogOperator("TestWS"); //NOI18N
        JCheckBoxOperator activate = new JCheckBoxOperator(dialog, 0);
        activate.push();
        JComboBoxOperator profiles = new JComboBoxOperator(dialog, 0);
        profiles.selectItem("SAML-HolderOfKey"); //NOI18N
        new EventTool().waitNoEvent(1000);
        dialog.ok();
        prjnd.performPopupActionNoBlock("Undeploy and Deploy"); //NOI18N
        OutputTabOperator oto = new OutputTabOperator(getWsProjectName());
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 120000); //NOI18N
        oto.waitText("(total time: "); //NOI18N
        assertTrue(oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
    }


    /**
     * Setting AM security for WS client in web module
     */
    public void secureWSCinWebModule() {
        ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd = new ProjectRootNode(prjtree, getWsClientProjectName());
        Node websvc = new Node(prjnd, "Web Service References|TestWSService"); //NOI18N
        new EventTool().waitNoEvent(10000);
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_RefreshClientAction")); //NOI18N
        NbDialogOperator ccr = new NbDialogOperator("Confirm Client Refresh"); //NOI18N
        ccr.yes();
        new EventTool().waitNoEvent(10000);
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.customization.core.ui.Bundle", "CTL_EditAttributesAction")); //NOI18N
        NbDialogOperator dialog = new NbDialogOperator("TestWSService"); //NOI18N
        JCheckBoxOperator activate = new JCheckBoxOperator(dialog, org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.identity.profile.ui.Bundle", "LBL_EnableSecurity")); //NOI18N
        activate.push();
        JComboBoxOperator profiles = new JComboBoxOperator(dialog, 0);
        profiles.selectItem("SAML-HolderOfKey"); //NOI18N
        new EventTool().waitNoEvent(1000);
        dialog.ok();
        prjnd.performPopupActionNoBlock("Undeploy and Deploy"); //NOI18N
        OutputTabOperator oto = new OutputTabOperator(getWsClientProjectName());
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 120000); //NOI18N
        oto.waitText("(total time: "); //NOI18N
        assertTrue(oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
    }

     /**
     * Changes server for test projects,created on default server to SDK Glassfish.
     * @param client is for determination of project name.
     */
//    private void testChangeServer(boolean client) {
//        String actual = getWsProjectName();
//        if(client){
//            actual = getWsClientProjectName();
//        }
//        ProjectRootNode prj = ProjectsTabOperator.invoke().getProjectRootNode(actual);
//        prj.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.nodes.Bundle", "Properties")); //NOI18N
//        // "Project Properties"
//        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"); //NOI18N
//        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
//        // "Run"
//        String runLabel = Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run"); //NOI18N
//        // select "Run" category
//        new Node(new JTreeOperator(propertiesDialogOper), runLabel).select();
//        JComboBoxOperator server = new JComboBoxOperator(propertiesDialogOper,0);
//        server.selectItem(TARGET_SERVER_NAME);
//        propertiesDialogOper.ok();
//    }
}
