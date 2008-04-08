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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 *
 */
package org.netbeans.modules.identity.qaf;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.ws.qaf.EjbWsValidation;

/**
 *
 * @author jp154641
 */
public class IdentityValidationInEJB extends EjbWsValidation {

    /**
     * Initialization of suite properties
     */
    public static String SERVER_PATH;
    public static File PROJECTS_FOLDER_FILE;
    public static File WSP_PROJECT_FILE;
    public static File WSC_PROJECT_FILE;

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.EJB;
    }

    @Override
    protected String getWsProjectName() {
        return "TestIdentityWSEJB"; //NOI18N
    }

    @Override
    protected String getWsClientProjectName() {
        return "TestIdentityWSCEJB"; //NOI18N
    }

    @Override
    protected String getWsName() {
        return "TestEJBWS"; //NOI18N
    }

    @Override
    protected String getWsPackage() {
        return "org.identity.ejbws.test"; //NOI18N
    }

    @Override
    protected String getWsClientPackage() {
        return "org.identity.ejbwsc.test"; //NOI18N
    }

    /** Creates a new instance of IdentityValidation */
    public IdentityValidationInEJB(String name) {
        super(name);
    }

    /**
     * This method creates test web project with web service and two
     * operations, then deploys it.
     * @throws java.io.IOException
     */
    public void prepareWSProject() throws IOException {
        testCreateNewWs();
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
        testCallWsOperationInSessionEJB();
        testDeployWsClientProject();
    }

    /** 
     * Test of AM security in ejb project 
     * - both ws provider and client 
     * - sets AM security in WS provider, deploys WS and then sets AM security in WSC and deploys it
     */
    public void testAMSecurityInEJB() {
        System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
        secureWSinEJBModule();
        secureWSCinEJBModule();
    }

    
     /** 
     * Setting AM security for WS provider in ejb module 
     */
    public void secureWSinEJBModule() {
        ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd = new ProjectRootNode(prjtree, getWsProjectName());
        Node websvc = new Node(prjnd, "Web Services|TestEJBWS"); //NOI18N
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.customization.core.ui.Bundle", "CTL_EditAttributesAction")); //NOI18N
        NbDialogOperator dialog = new NbDialogOperator("TestEJBWS"); //NOI18N
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
     * Setting AM security for WS client in ejb module 
     */
    public void secureWSCinEJBModule() {
         ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd = new ProjectRootNode(prjtree, getWsClientProjectName());
        Node websvc = new Node(prjnd, "Web Service References|TestEJBWS"); //NOI18N
        new EventTool().waitNoEvent(10000);
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_RefreshClientAction")); //NOI18N
        NbDialogOperator ccr = new NbDialogOperator("Confirm Client Refresh"); //NOI18N
        ccr.yes();
        new EventTool().waitNoEvent(10000);
        websvc.performPopupActionNoBlock(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.websvc.customization.core.ui.Bundle", "CTL_EditAttributesAction")); //NOI18N
        NbDialogOperator dialog = new NbDialogOperator("TestEJBWS"); //NOI18N
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
     * Tests Call Web Service Operation action in a servlet
     */
//    public void testCallWsOperationInSessionEJB() {
//        //create a session bean
//        String ejbName = "NewSession";
//        //Enterprise
//        String enterpriseLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.resources.Bundle", "Templates/J2EE");
//        //Session Bean
//        String sessionBeanLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.Bundle", "Templates/J2EE/Session");
//        createNewFile(getWsClientProject(), enterpriseLabel, sessionBeanLabel);
//        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
//        op.txtObjectName().clearText();
//        op.txtObjectName().typeText(ejbName);
//        op.cboPackage().clearText();
//        op.cboPackage().typeText("org.mycompany.ejbs"); //NOI18N
//        op.finish();
//        new org.netbeans.jemmy.EventTool().waitNoEvent(2000); //Temporary hack preventing occurence of issue
//        //Add business method
//        final EditorOperator eo = new EditorOperator(ejbName); //NOI18N
//        addBusinessMethod(eo, "myBm", "String"); //NOI18N
//        //edit code in the EJB
//        // add new line and select it
//        eo.setCaretPosition("myBm() {", false); //NOI18N
//        eo.insert("\n//xxx"); //NOI18N
//        eo.select("//xxx"); //NOI18N
//        callWsOperation(eo, "myIntMethod", 16); //NOI18N
//        eo.close(true);
//    }
//
//    protected void addBusinessMethod(EditorOperator eo, String mName, String mRetVal) {
//        //EJB Methods
//        String actionGroupName = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_EJBActionGroup");
//        //Add Business Method...
//        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//        }
//        try {
//            new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo);
//        } catch (TimeoutExpiredException tee) {
//            eo.select(16);
//            new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo);
//        }
//        addMethod(eo, actionName, mName, mRetVal);
//    }
}
