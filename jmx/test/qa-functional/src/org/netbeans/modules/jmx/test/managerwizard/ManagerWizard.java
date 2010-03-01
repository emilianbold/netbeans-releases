/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.managerwizard;

import java.io.File;
import java.io.IOException;
import javax.swing.JLabel;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JMXTestCase;
import org.netbeans.modules.jmx.test.helpers.Manager;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;


/**
 * Create New JMX Manager.
 * Check :
 * - wizards default values
 * - wizards behavior
 * - wizards robustness
 */
public class ManagerWizard extends JMXTestCase {
    
    // JMX manager names
    private static final String MANAGER_NAME_1 = "Test1Manager";
    private static final String MANAGER_NAME_2 = "Test2Manager";
    private static final String MANAGER_NAME_3 = "Test3Manager";
    private static final String MANAGER_NAME_4 = "Test4Manager";
    private static final String MANAGER_NAME_5 = "Test5Manager";
    private static final String MANAGER_NAME_6 = "Test6Manager";
    private static boolean initialized;
    
    /**
     * Creates a new instance of ConstructAgent
     */
    public ManagerWizard(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        waitNoEvent(2000);
        super.tearDown();
    }
    
    //========================= Init ==================================//
    
    public void init() throws IOException {
        
        System.out.println("====================  init  ====================");
        
        System.out.println("Create project for JMX Manager tests");
        newProject(
                PROJECT_CATEGORY_JAVA,
                PROJECT_TYPE_JAVA_APPLICATION,
                PROJECT_NAME_MANAGER_FUNCTIONAL);
        waitNoEvent(5000);
        System.out.println("Create package folder " + PACKAGE_COM_FOO_BAR);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MANAGER_FUNCTIONAL,
                FILE_CATEGORY_JAVA,
                FILE_TYPE_JAVA_PACKAGE);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                PACKAGE_COM_FOO_BAR, null);
        nfnlso.finish();
    }
    
    //========================= Test Wizard ==================================//
    
    /**
     * Create default JMX Manager.
     * Check wizard components and values.
     */
    public void test1ConstructManager() {
        
        System.out.println("============ test1ConstructManager ============");
        
        Manager manager = new Manager();
        
        // Name and location wizard
        //---------------------------------------
        System.out.println("Create JMX manager " + MANAGER_NAME_1);
        manager.setName(MANAGER_NAME_1);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MANAGER_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                FILE_TYPE_JMX_MANAGER);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                MANAGER_NAME_1, PACKAGE_COM_FOO_BAR);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, manager);
        // Check name and location wizard values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, manager);
        // Get the generated files before switching to next wizard
        String managerCreatedClassFile = nfnlso.txtCreatedFile().getText();
        nfnlso.next();
        
        // Agent URL popup
        // Handle the popup before the wizard as
        // some updates in popup may update the wizard
        //---------------------------------------
        pressAndRelease(AGENT_URL_EDIT_BUTTON, nfnlso);
        waitNoEvent(5000);
        NbDialogOperator ndo = new NbDialogOperator(AGENT_URL_DIALOG_TITLE);
        // Check agent URL popup components
        System.out.println("Check Agent URL popup components");
        checkAgentURLPopupComponents(ndo, manager);
        // Check agent URL popup values
        System.out.println("Check Agent URL popup values");
        checkAgentURLPopupValues(ndo, manager);
        ndo.cancel();
        
        // Agent URL wizard
        //---------------------------------------
        // Check agent URL wizard components
        System.out.println("Check Agent URL wizard components");
        checkAgentURLWizardComponents(nfnlso, manager);
        // Check agent URL wizard values
        System.out.println("Check Agent URL wizard values");
        checkAgentURLWizardValues(nfnlso, manager);
        nfnlso.finish();
        
        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(managerCreatedClassFile, manager);
    }

    /**
     * Create JMX Manager.
     * Update some values.
     * Check wizard components and values.
     */
    public void test2constructManager() {
        
        System.out.println("============ test2constructManager ============");
        
        Manager manager = new Manager();
        
        // Name and location wizard
        //---------------------------------------
        System.out.println("Create JMX manager " + MANAGER_NAME_2);
        manager.setName(MANAGER_NAME_2);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MANAGER_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                FILE_TYPE_JMX_MANAGER);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                MANAGER_NAME_2, PACKAGE_COM_FOO_BAR);
        // Update some values
        System.out.println("Update Name and location wizard values");
        manager.setGenerateMainMethod(false);
        setCheckBoxSelection(GENERATE_MAIN_METHOD_CHECK_BOX, nfnlso, false);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, manager);
        // Check name and location wizard values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, manager);
        // Get the generated files before switching to next wizard
        String managerCreatedClassFile = nfnlso.txtCreatedFile().getText();
        nfnlso.next();
        
        // Agent URL popup
        // Handle the popup before the wizard as
        // some updates in popup may update the wizard
        //---------------------------------------
        // Update some values
        System.out.println("Update Agent URL popup values");
        pressAndRelease(AGENT_URL_EDIT_BUTTON, nfnlso);
        waitNoEvent(5000);
        NbDialogOperator ndo = new NbDialogOperator(AGENT_URL_DIALOG_TITLE);
        manager.setHost("server.sun.com");
        manager.setPort("5000");
        manager.setPath("/jndi/rmi://server.sun.com:5000/jmxrmi");
        // The agent URL text field is not editable.
        // It is updated using the agent URL popup values.
        manager.updateAgentURL();
        setTextFieldContent(URL_HOST_TEXT_FIELD, ndo, "server.sun.com");
        setTextFieldContent(URL_PORT_TEXT_FIELD, ndo, "5000");
        // Check agent URL popup components
        System.out.println("Check Agent URL popup components");
        checkAgentURLPopupComponents(ndo, manager);
        // Check agent URL popup values
        System.out.println("Check Agent URL popup values");
        checkAgentURLPopupValues(ndo, manager);
        ndo.ok();
        
        // Agent URL wizard
        //---------------------------------------
        // Update some values
        System.out.println("Update Agent URL wizard values");
        manager.setAuthenticatedConnection(false);
        setCheckBoxSelection(AUTHENTICATED_CONNECTION_CHECK_BOX, nfnlso, false);
        // Check agent URL wizard components
        System.out.println("Check Agent URL wizard components");
        checkAgentURLWizardComponents(nfnlso, manager);
        // Check agent URL wizard values
        // The agent URL should be updated with agent URL popup values
        System.out.println("Check Agent URL wizard values");
        checkAgentURLWizardValues(nfnlso, manager);
        nfnlso.finish();
        
        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(managerCreatedClassFile, manager);
    }
    
    /**
     * Create JMX Manager.
     * Update some values.
     * Check wizard components and values.
     */
    public void test3constructManager() {
        
        System.out.println("============ test3constructManager ============");
        
        Manager manager = new Manager();
        
        // Name and location wizard
        //---------------------------------------
        System.out.println("Create JMX manager " + MANAGER_NAME_3);
        manager.setName(MANAGER_NAME_3);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MANAGER_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                FILE_TYPE_JMX_MANAGER);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                MANAGER_NAME_3, PACKAGE_COM_FOO_BAR);
        // Update some values
        System.out.println("Update Name and location wizard values");
        manager.setProjectMainClass(false);
        manager.setGenerateSampleDiscoveryCode(false);
        setCheckBoxSelection(SET_MAIN_PROJECT_CHECK_BOX, nfnlso, false);
        setCheckBoxSelection(GENERATE_DISCOVERY_CODE_CHECK_BOX, nfnlso, false);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, manager);
        // Check name and location wizard values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, manager);
        // Get the generated files before switching to next wizard
        String managerCreatedClassFile = nfnlso.txtCreatedFile().getText();
        nfnlso.next();
        
        // Agent URL popup
        // Handle the popup before the wizard as
        // some updates in popup may update the wizard
        //---------------------------------------
        // Update some values
        System.out.println("Update Agent URL popup values");
        pressAndRelease(AGENT_URL_EDIT_BUTTON, nfnlso);
        waitNoEvent(5000);
        NbDialogOperator ndo = new NbDialogOperator(AGENT_URL_DIALOG_TITLE);
        manager.setProtocol("jmxmp");
        manager.setHost("server.sun.com");
        manager.setPort("6000");
        manager.setPath("/jmxmppath");
        // The agent URL text field is not editable.
        // It is updated using the agent URL popup values.
        manager.updateAgentURL();
        setComboBoxItem(URL_PROTOCOL_COMBO_BOX, ndo, "jmxmp");
        setTextFieldContent(URL_HOST_TEXT_FIELD, ndo, "server.sun.com");
        setTextFieldContent(URL_PORT_TEXT_FIELD, ndo, "6000");
        setTextFieldContent(URL_PATH_TEXT_FIELD, ndo, "/jmxmppath");
        // Check agent URL popup components
        System.out.println("Check Agent URL popup components");
        checkAgentURLPopupComponents(ndo, manager);
        // Check agent URL popup values
        System.out.println("Check Agent URL popup values");
        checkAgentURLPopupValues(ndo, manager);
        ndo.ok();
        
        // Agent URL wizard
        //---------------------------------------
        // Update some values
        System.out.println("Update Agent URL wizard values");
        manager.setGenerateCredentialsConnectionCode(true);
        setRadioButtonSelection(GENERATE_CREDENTIALS_RADIO_BUTTON, nfnlso, true);
        // Check agent URL wizard warnings as no password has been set
        System.out.println("Check Agent URL wizard components");
        checkAgentURLWizardComponents(nfnlso, manager);
        manager.setUserName("username");
        manager.setPassword("password");
        setTextFieldContent(USERNAME_TEXT_FIELD, nfnlso, "username");
        setTextFieldContent(PASSWORD_TEXT_FIELD, nfnlso, "password");
        // Check agent URL wizard components
        System.out.println("Check Agent URL wizard components");
        checkAgentURLWizardComponents(nfnlso, manager);
        // Check agent URL wizard values
        // The agent URL should be updated with agent URL popup values
        System.out.println("Check Agent URL wizard values");
        checkAgentURLWizardValues(nfnlso, manager);
        nfnlso.finish();
        
        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(managerCreatedClassFile, manager);
    }
    
    //========================= Check Wizard ==================================//
    
    /**
     * Check name and location wizard components are enabled/disabled
     */
    private void checkNameAndLocationWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso, Manager manager) {
        assertTrue(getCheckBoxOperator(
                GENERATE_MAIN_METHOD_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(manager.getGenerateMainMethod(), getCheckBoxOperator(
                SET_MAIN_PROJECT_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(manager.getGenerateMainMethod(), getCheckBoxOperator(
                GENERATE_DISCOVERY_CODE_CHECK_BOX, nfnlso).isEnabled());
    }
    
    /**
     * Check name and location wizard values
     */
    private void checkNameAndLocationWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso, Manager manager) {
        assertEquals(manager.getGenerateMainMethod(), getCheckBoxOperator(
                GENERATE_MAIN_METHOD_CHECK_BOX, nfnlso).isSelected());
        assertEquals(manager.getProjectMainClass(), getCheckBoxOperator(
                SET_MAIN_PROJECT_CHECK_BOX, nfnlso).isSelected());
        assertEquals(manager.getGenerateSampleDiscoveryCode(), getCheckBoxOperator(
                GENERATE_DISCOVERY_CODE_CHECK_BOX, nfnlso).isSelected());
    }
    
    /**
     * Check JMX agent URL wizard components are enabled/disabled
     */
    private void checkAgentURLWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso, Manager manager) {
        
        assertTrue(getTextFieldOperator(AGENT_URL_TEXT_FIELD, nfnlso).isEnabled());
        assertFalse(getTextFieldOperator(AGENT_URL_TEXT_FIELD, nfnlso).isEditable());
        assertTrue(getCheckBoxOperator(AUTHENTICATED_CONNECTION_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(manager.getAuthenticatedConnection(),
                getRadioButtonOperator(GENERATE_SAMPLE_RADIO_BUTTON, nfnlso).isEnabled());
        assertEquals(manager.getAuthenticatedConnection(),
                getRadioButtonOperator(GENERATE_CREDENTIALS_RADIO_BUTTON, nfnlso).isEnabled());
        assertEquals(manager.getAuthenticatedConnection() &&
                manager.getGenerateCredentialsConnectionCode(),
                getTextFieldOperator(USERNAME_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(manager.getAuthenticatedConnection() &&
                manager.getGenerateCredentialsConnectionCode(),
                getTextFieldOperator(PASSWORD_TEXT_FIELD, nfnlso).isEnabled());
        // When a text field is disabled, it is uneditable.
        // In this case, it seems that the JTextFieldOperator.isEditable()
        // always returns true.
        // So we test the JTextFieldOperator.isEditable() returned value only when
        // the text field is enabled.
        if (manager.getAuthenticatedConnection() &&
                manager.getGenerateCredentialsConnectionCode()) {
            assertTrue(getTextFieldOperator(
                    USERNAME_TEXT_FIELD, nfnlso).isEditable());
            assertTrue(getTextFieldOperator(
                    PASSWORD_TEXT_FIELD, nfnlso).isEditable());
            JLabel jl = getLabel(EMPTY_USER_PASSWORD_WARNING, nfnlso.getContentPane());
            // Check warning message is displayed
            if (manager.getPassword().length() == 0) {
                assertFalse(nfnlso.btFinish().isEnabled());
                // Check warning message is displayed
                assertNotNull(jl);
            } else {
                assertTrue(nfnlso.btFinish().isEnabled());
                // Check warning message is not displayed
                assertNull(jl);
            }
        }
    }
    
    /**
     * Check JMX agent URL wizard values
     */
    private void checkAgentURLWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso, Manager manager) {
        
        assertEquals(manager.getAgentURL(), getTextFieldContent(
                AGENT_URL_TEXT_FIELD, nfnlso));
        assertEquals(manager.getAuthenticatedConnection(), getCheckBoxOperator(
                AUTHENTICATED_CONNECTION_CHECK_BOX, nfnlso).isSelected());
        assertEquals(manager.getGenerateSampleConnectionCode(), getRadioButtonOperator(
                GENERATE_SAMPLE_RADIO_BUTTON, nfnlso).isSelected());
        assertEquals(manager.getGenerateCredentialsConnectionCode(), getRadioButtonOperator(
                GENERATE_CREDENTIALS_RADIO_BUTTON, nfnlso).isSelected());
        assertEquals(manager.getUserName(), getTextFieldContent(
                USERNAME_TEXT_FIELD, nfnlso));
        assertEquals(manager.getPassword(), getTextFieldContent(
                PASSWORD_TEXT_FIELD, nfnlso));
    }
    
    /**
     * Check JMX agent URL popup components are enabled/disabled
     */
    private void checkAgentURLPopupComponents(
            NbDialogOperator ndo, Manager manager) {
        
        assertTrue(getComboBoxOperator(URL_PROTOCOL_COMBO_BOX, ndo).isEnabled());
        assertTrue(getComboBoxOperator(URL_PROTOCOL_COMBO_BOX, ndo).isEditable());
        assertTrue(getTextFieldOperator(URL_HOST_TEXT_FIELD, ndo).isEnabled());
        assertTrue(getTextFieldOperator(URL_HOST_TEXT_FIELD, ndo).isEditable());
        assertTrue(getTextFieldOperator(URL_PORT_TEXT_FIELD, ndo).isEnabled());
        assertTrue(getTextFieldOperator(URL_PORT_TEXT_FIELD, ndo).isEditable());
        assertTrue(getTextFieldOperator(URL_PATH_TEXT_FIELD, ndo).isEnabled());
        // The path text field is editable for other protocol than RMI
        if (manager.getProtocol().equals(Manager.DEFAULT_PROTOCOL)) {
            assertFalse(getTextFieldOperator(URL_PATH_TEXT_FIELD, ndo).isEditable());
        } else {
            assertTrue(getTextFieldOperator(URL_PATH_TEXT_FIELD, ndo).isEditable());
        }
        assertTrue(ndo.btOK().isEnabled());
        assertTrue(ndo.btCancel().isEnabled());
    }
    
    /**
     * Check JMX agent URL popup values
     */
    private void checkAgentURLPopupValues(
            NbDialogOperator ndo, Manager manager) {
        
        assertEquals(manager.getProtocol(), getComboBoxItem(
                URL_PROTOCOL_COMBO_BOX, ndo));
        assertEquals(manager.getHost(), getTextFieldContent(
                URL_HOST_TEXT_FIELD, ndo));
        assertEquals(manager.getPort(), getTextFieldContent(
                URL_PORT_TEXT_FIELD, ndo));
        assertEquals(manager.getPath(), getTextFieldContent(
                URL_PATH_TEXT_FIELD, ndo));
    }
    
    
    //========================= Check created files ==================================//
    
    /**
     * Check the created files with expected golden files.
     */
    private void checkCreatedFiles(String managerCreatedClassFile, Manager manager) {
        
        assertTrue(compareFiles(
                new File(managerCreatedClassFile),
                getGoldenFile(manager.getName())));
    }
}
