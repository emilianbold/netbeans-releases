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
package org.netbeans.modules.jmx.test.configwizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JLabel;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JMXTestCase;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create New Management Configuration files.
 * Check :
 * - wizards default values
 * - wizards behavior
 * - wizards robustness
 */
public class ManagementConfigurationWizard extends JMXTestCase {

    private static final String PROPERTIES_EXT = "properties";
    private static final String ACCESS_EXT = "access";
    private static final String PASSWORD_EXT = "password";
    private static final String ACL_EXT = "acl";
    private static final String FOLDER_TAG = "<defined folder>";

    /** Need to be defined because of JUnit */
    public ManagementConfigurationWizard(String name) {
        super(name);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ManagementConfigurationWizard("init"));
        suite.addTest(new ManagementConfigurationWizard("test1"));
        suite.addTest(new ManagementConfigurationWizard("test2"));
        suite.addTest(new ManagementConfigurationWizard("test3"));
        suite.addTest(new ManagementConfigurationWizard("test4"));
        suite.addTest(new ManagementConfigurationWizard("test5"));
        suite.addTest(new ManagementConfigurationWizard("test6"));
        suite.addTest(new ManagementConfigurationWizard("test7"));
        suite.addTest(new ManagementConfigurationWizard("test8"));
        suite.addTest(new ManagementConfigurationWizard("test9"));

        return suite;
    }

    //========================= Init ==================================//
    public void init() {

        System.out.println("====================  init  ====================");

        System.out.println("Create project for Management Configuration file tests");
        newProject(
                PROJECT_CATEGORY_JAVA,
                PROJECT_TYPE_JAVA_APPLICATION,
                PROJECT_NAME_CONFIGURATION_FUNCTIONAL);
        waitNoEvent(5000);
        System.out.println("Create package folder " + PACKAGE_COM_FOO_BAR);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_CONFIGURATION_FUNCTIONAL,
                FILE_CATEGORY_JAVA,
                FILE_TYPE_JAVA_PACKAGE);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                PACKAGE_COM_FOO_BAR, null);
        nfnlso.finish();
    }

    //========================= Test Wizard ==================================//
    /**
     * Check management configuration file creation when the properties file
     * already exists.
     */
    public void test1() {

        NewJavaFileNameLocationStepOperator nfnlso = null;

        String objectName = "Test1";

        System.out.println("====================  test1  ====================");

        System.out.println("Create empty property file " +
                objectName + "." + PROPERTIES_EXT);
        nfnlso = createEmptyFile(objectName + "." + PROPERTIES_EXT);
        nfnlso.finish();

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, objectName);
        // Check name and location wizard default values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, objectName, false);
        nfnlso.cancel();
    }

    /**
     * Check management configuration file creation when the access file
     * already exists.
     */
    public void test2() {

        NewJavaFileNameLocationStepOperator nfnlso = null;

        String objectName = "Test2";

        System.out.println("====================  test2  ====================");

        System.out.println("Create empty access file " +
                objectName + "." + ACCESS_EXT);
        nfnlso = createEmptyFile(objectName + "." + ACCESS_EXT);
        nfnlso.finish();

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, objectName + "." + ACCESS_EXT);
        // Check name and location wizard default values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, objectName, false);
        nfnlso.cancel();
    }

    /**
     * Check management configuration file creation when the password file
     * already exists.
     */
    public void test3() {

        NewJavaFileNameLocationStepOperator nfnlso = null;

        String objectName = "Test3";

        System.out.println("====================  test3  ====================");

        System.out.println("Create empty password file " +
                objectName + "." + PASSWORD_EXT);
        nfnlso = createEmptyFile(objectName + "." + PASSWORD_EXT);
        nfnlso.finish();

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, objectName + "." + PASSWORD_EXT);
        // Check name and location wizard default values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, objectName, false);
        nfnlso.cancel();
    }

    /**
     * Create basic management configuration from menu.
     * Check wizard components and values.
     */
    public void test4() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;
        SNMPManagementConfiguration snmpConfig = null;

        String objectName = "Test4";

        System.out.println("====================  test4  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, null);
        // Check name and location wizard default values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, objectName, false);
        // Before going on, keep the created file path
        // It will be used when checking created files
        String createdFile = nfnlso.txtCreatedFile().getText();
        String path = createdFile.substring(0,
                createdFile.lastIndexOf(File.separator) + 1);
        // The path returned above is platform dependent and may contain 
        // either "/" (UNIX systems) or "\" (WINDOWS systems).
        // BUT the generated properties file will contain only "UNIX like" path values.
        // So we update the returned path replacing "\" with "/".
        //path = path.replace("\\", "/");
        path = path.replace(File.separatorChar, '/') ;
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        // Do not allow remote manager to connect with RMI
        System.out.println("Do not allow remote manager to connect with RMI");
        rmiConfig.allowConnection = false;
        setCheckBoxSelection(ENABLE_RMI_CHECK_BOX, nfnlso, false);
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard updated values
        System.out.println("Check Enable RMI wizard value wits");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        nfnlso.next();

        // SNMP configuration wizard
        //---------------------------------------
        snmpConfig = new SNMPManagementConfiguration();
        // Check SNMP Configuration wizard components
        System.out.println("Check SNMP Configuration wizard components");
        checkSNMPConfigurationWizardComponents(nfnlso, snmpConfig);
        // Check SNMP Configuration wizard default values
        System.out.println("Check SNMP Configuration wizard values");
        checkSNMPConfigurationWizardValues(nfnlso, snmpConfig);
        nfnlso.finish();

        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(objectName, path);
    }

    /**
     * Create management configuration from menu.
     * Then update some fields with valid values.
     * Check wizard components and values.
     */
    public void test5() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;
        SNMPManagementConfiguration snmpConfig = null;

        String objectName = "Test5";

        System.out.println("====================  test5  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Enable thread contention monitoring
        System.out.println("Enable thread contention monitoring");
        setCheckBoxSelection(ENABLE_THREAD_CONTENTION_CHECK_BOX, nfnlso, true);
        // Check name and location wizard components
        System.out.println("Check Name And Location wizard components");
        checkNameAndLocationWizardComponents(nfnlso, null);
        // Check name and location wizard default values
        System.out.println("Check Name And Location wizard values");
        checkNameAndLocationWizardValues(nfnlso, objectName, true);
        // Before going on, keep the created file path
        // It will be used when checking created files
        String createdFile = nfnlso.txtCreatedFile().getText();
        String path = createdFile.substring(0,
                createdFile.lastIndexOf(File.separator) + 1);
        // The path returned above is platform dependent and may contain 
        // either "/" (UNIX systems) or "\" (WINDOWS systems).
        // BUT the generated properties file will contain only "UNIX like" path values.
        // So we update the returned path replacing "\" with "/".
        //path = path.replace("\\", "/");
        path = path.replace(File.separatorChar, '/') ;
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Require remote manager to authenticate
        System.out.println("Require remote manager to authenticate");
        rmiConfig.requireAuthentication = true;
        setCheckBoxSelection(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso, true);
        // Update text field contents
        System.out.println("Update Enable RMI wizard with valid values");
        rmiConfig.port = "5000";
        setTextFieldContent(RMI_PORT_TEXT_FIELD, nfnlso, "5000");
        // Use SSL
        System.out.println("Use SSL");
        rmiConfig.useSSL = true;
        setCheckBoxSelection(RMI_SSL_CHECK_BOX, nfnlso, true);
        rmiConfig.protocol = "4.0";
        setTextFieldContent(RMI_SSL_PROTOCOL_TEXT_FIELD, nfnlso, "4.0");
        rmiConfig.cipher = "CipherValue";
        setTextFieldContent(RMI_SSL_CIPHER_TEXT_FIELD, nfnlso, "CipherValue");
        // Require client authentication
        System.out.println("Require client authentication");
        rmiConfig.requireClientAuthentication = true;
        setCheckBoxSelection(RMI_SSL_CLIENT_CHECK_BOX, nfnlso, true);
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        nfnlso.next();

        // SNMP configuration wizard
        //---------------------------------------
        snmpConfig = new SNMPManagementConfiguration();
        // Allow remote manager to connect with SNMP
        System.out.println("Allow remote manager to connect with SNMP");
        snmpConfig.allowConnection = true;
        setCheckBoxSelection(ENABLE_SNMP_CHECK_BOX, nfnlso, true);
        // Enable SNMP authentication
        System.out.println("Enable SNMP authentication");
        snmpConfig.enableAuthentication = true;
        setCheckBoxSelection(SNMP_ACL_CHECK_BOX, nfnlso, true);
        // Update text field contents
        System.out.println("Update SNMP Configuration wizard with valid values");
        snmpConfig.port = "5000";
        setTextFieldContent(SNMP_PORT_TEXT_FIELD, nfnlso, "5000");
        snmpConfig.intf = "server";
        setTextFieldContent(SNMP_INTERFACE_TEXT_FIELD, nfnlso, "server");
        snmpConfig.trapPort = "5162";
        setTextFieldContent(SNMP_TRAP_PORT_TEXT_FIELD, nfnlso, "5162");
        String aclFile = path + objectName + "." + ACL_EXT;
        snmpConfig.aclFile = aclFile;
        setTextFieldContent(SNMP_ACL_FILE_TEXT_FIELD, nfnlso, aclFile);
        // Check SNMP Configuration wizard components
        System.out.println("Check SNMP Configuration wizard components");
        checkSNMPConfigurationWizardComponents(nfnlso, snmpConfig);
        // Check SNMP Configuration wizard default values
        System.out.println("Check SNMP Configuration wizard values");
        checkSNMPConfigurationWizardValues(nfnlso, snmpConfig);
        nfnlso.finish();

        // When require remote manager to authenticate is selected
        // (enable RMI wizard), an information dialog is displayed
        // when clicking on the finish button.
        NbDialogOperator dialog = new NbDialogOperator(INFORMATION_DIALOG_TITLE);
        dialog.ok();

        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(objectName, path);
    }

    /**
     * Create management configuration from node.
     * Then update some fields with invalid values.
     * Check wizard components and values.
     */
    public void test6() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;
        SNMPManagementConfiguration snmpConfig = null;

        String objectName = "Test6";

        System.out.println("====================  test6  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromNode(objectName);
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Update RMI port with a letter
        System.out.println("Update RMI port with invalid value (letter)");
        rmiConfig.port = "";
        try {
            setTextFieldContent(RMI_PORT_TEXT_FIELD, nfnlso, "a");
            fail("The user shouldn't be able to enter a letter for RMI port.");
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
        }
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        // Need to update RMI port with a valid value to go to the next SNMP step.
        System.out.println("Update RMI port with valid value");
        setTextFieldContent(RMI_PORT_TEXT_FIELD, nfnlso, "1099");
        nfnlso.next();

        // SNMP configuration wizard
        //---------------------------------------
        // Update SNMP port with a letter
        snmpConfig = new SNMPManagementConfiguration();
        // Allow remote manager to connect with SNMP
        System.out.println("Allow remote manager to connect with SNMP");
        snmpConfig.allowConnection = true;
        setCheckBoxSelection(ENABLE_SNMP_CHECK_BOX, nfnlso, true);
        System.out.println("Update SNMP port with invalid value (letter)");
        snmpConfig.port = "";
        try {
            setTextFieldContent(SNMP_PORT_TEXT_FIELD, nfnlso, "a");
            fail("The user shouldn't be able to enter a letter for SNMP port.");
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
        }
        // Update SNMP trap port with a letter
        System.out.println("Update SNMP trap port with invalid value (letter)");
        snmpConfig.trapPort = "";
        try {
            setTextFieldContent(SNMP_TRAP_PORT_TEXT_FIELD, nfnlso, "a");
            fail("The user shouldn't be able to enter a letter for SNMP trap port.");
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
        }
        // Check SNMP Configuration wizard components
        System.out.println("Check SNMP Configuration wizard components");
        checkSNMPConfigurationWizardComponents(nfnlso, snmpConfig);
        // Check SNMP Configuration wizard default values
        System.out.println("Check SNMP Configuration wizard values");
        checkSNMPConfigurationWizardValues(nfnlso, snmpConfig);

        nfnlso.cancel();
    }

    /**
     * Create management configuration from menu.
     * Then add valid credential (non null role/password).
     * Check wizard components and values.
     */
    public void test7() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;

        String objectName = "Test7";

        System.out.println("====================  test7  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Before going on, keep the created file path
        // It will be used when checking created files
        String createdFile = nfnlso.txtCreatedFile().getText();
        String path = createdFile.substring(0,
                createdFile.lastIndexOf(File.separator) + 1);
        // The path returned above is platform dependent and may contain 
        // either "/" (UNIX systems) or "\" (WINDOWS systems).
        // BUT the generated properties file will contain only "UNIX like" path values.
        // So we update the returned path replacing "\" with "/".
        //path = path.replace("\\", "/");
        path = path.replace(File.separatorChar, '/') ;
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Require remote manager to authenticate
        System.out.println("Require remote manager to authenticate");
        rmiConfig.requireAuthentication = true;
        setCheckBoxSelection(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso, true);
        JTableOperator jto = getTableOperator(RMI_CREDENTIALS_TABLE, nfnlso);
        JTableMouseDriver jtmd = new JTableMouseDriver();
        // Add valid credential
        System.out.println("Add valid credential (non null role/password)");
        rmiConfig.allowedCredentials.add(new AllowedCredential("role", "password", "readwrite"));
        pressAndRelease(RMI_CREDENTIALS_ADD_BUTTON, nfnlso);
        waitNoEvent(5000);
        jtmd.editCell(jto, 0, jto.findColumn(CREDENTIAL_ROLE_COLUMN_NAME), "role");
        jtmd.editCell(jto, 0, jto.findColumn(CREDENTIAL_PASSWORD_COLUMN_NAME), "password");
        jtmd.selectCell(jto, 0, jto.findColumn(CREDENTIAL_ACCESS_COLUMN_NAME));
        selectComboBoxItem(RMI_CREDENTIALS_ACCESS_COMBO_BOX, jto, "readwrite");
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        nfnlso.next();
        nfnlso.finish();

        // When require remote manager to authenticate is selected
        // (enable RMI wizard), an information dialog is displayed
        // when clicking on the finish button.
        NbDialogOperator dialog = new NbDialogOperator(INFORMATION_DIALOG_TITLE);
        dialog.ok();

        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(objectName, path);
    }

    /**
     * Create management configuration from menu.
     * Then add invalid credential (null role/password).
     * Check wizard components and values.
     */
    public void test8() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;

        String objectName = "Test8";

        System.out.println("====================  test8  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Require remote manager to authenticate
        System.out.println("Require remote manager to authenticate");
        rmiConfig.requireAuthentication = true;
        setCheckBoxSelection(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso, true);
        JTableOperator jto = getTableOperator(RMI_CREDENTIALS_TABLE, nfnlso);
        JTableMouseDriver jtmd = new JTableMouseDriver();

        // Add invalid credential (null role)
        System.out.println("Add invalid credential (null role)");
        rmiConfig.invalidCredential = true;
        rmiConfig.allowedCredentials.add(new AllowedCredential(null, "password", "readonly"));
        pressAndRelease(RMI_CREDENTIALS_ADD_BUTTON, nfnlso);
        waitNoEvent(5000);
        jtmd.editCell(jto, 0, jto.findColumn(CREDENTIAL_PASSWORD_COLUMN_NAME), "password");
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);

        // Remove invalid credential (null role)
        System.out.println("Remove invalid credential (null role)");
        rmiConfig.invalidCredential = false;
        rmiConfig.allowedCredentials.remove(0);
        jtmd.selectCell(jto, 0, 0);
        pressAndRelease(RMI_CREDENTIALS_REMOVE_BUTTON, nfnlso);
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);

        // Add invalid credential (null password)
        System.out.println("Add invalid credential (null password)");
        rmiConfig.invalidCredential = true;
        rmiConfig.allowedCredentials.add(new AllowedCredential("role", null, "readonly"));
        pressAndRelease(RMI_CREDENTIALS_ADD_BUTTON, nfnlso);
        waitNoEvent(5000);
        jtmd.editCell(jto, 0, jto.findColumn(CREDENTIAL_ROLE_COLUMN_NAME), "role");
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);

        nfnlso.cancel();
    }

    /**
     * Create management configuration from menu.
     * Then add/remove credentials.
     * Check wizard components and values.
     */
    public void test9() {

        NewJavaFileNameLocationStepOperator nfnlso = null;
        RMIManagementConfiguration rmiConfig = null;

        String objectName = "Test9";

        System.out.println("====================  test9  ====================");

        // Name and location wizard
        //---------------------------------------
        System.out.println("Create management configuration file " + objectName);
        nfnlso = createManagementConfigurationFileFromMenu(objectName);
        // Before going on, keep the created file path
        // It will be used when checking created files
        String createdFile = nfnlso.txtCreatedFile().getText();
        String path = createdFile.substring(0,
                createdFile.lastIndexOf(File.separator) + 1);
        // The path returned above is platform dependent and may contain 
        // either "/" (UNIX systems) or "\" (WINDOWS systems).
        // BUT the generated properties file will contain only "UNIX like" path values.
        // So we update the returned path replacing "\" with "/".
        //path = path.replace("\\", "/");
        path = path.replace(File.separatorChar, '/') ;
        nfnlso.next();

        // Enable RMI wizard
        //---------------------------------------
        rmiConfig = new RMIManagementConfiguration();
        // Require remote manager to authenticate
        System.out.println("Require remote manager to authenticate");
        rmiConfig.requireAuthentication = true;
        setCheckBoxSelection(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso, true);
        JTableOperator jto = getTableOperator(RMI_CREDENTIALS_TABLE, nfnlso);
        JTableMouseDriver jtmd = new JTableMouseDriver();
        // Add 5 credentials
        System.out.println("Add 5 credentials");
        for (int i = 0; i < 5; i++) {
            pressAndRelease(RMI_CREDENTIALS_ADD_BUTTON, nfnlso);
            waitNoEvent(5000);
        }
        // Edit 2 credentials (rows 2 and 3)
        System.out.println("Edit 2 credentials (rows 2 and 3)");
        rmiConfig.allowedCredentials.add(new AllowedCredential("role1", "password1", "readonly"));
        jtmd.editCell(jto, 2, jto.findColumn(CREDENTIAL_ROLE_COLUMN_NAME), "role1");
        jtmd.editCell(jto, 2, jto.findColumn(CREDENTIAL_PASSWORD_COLUMN_NAME), "password1");
        jtmd.selectCell(jto, 2, jto.findColumn(CREDENTIAL_ACCESS_COLUMN_NAME));
        selectComboBoxItem(RMI_CREDENTIALS_ACCESS_COMBO_BOX, jto, "readonly");
        rmiConfig.allowedCredentials.add(new AllowedCredential("role2", "password2", "readwrite"));
        jtmd.editCell(jto, 3, jto.findColumn(CREDENTIAL_ROLE_COLUMN_NAME), "role2");
        jtmd.editCell(jto, 3, jto.findColumn(CREDENTIAL_PASSWORD_COLUMN_NAME), "password2");
        jtmd.selectCell(jto, 3, jto.findColumn(CREDENTIAL_ACCESS_COLUMN_NAME));
        selectComboBoxItem(RMI_CREDENTIALS_ACCESS_COMBO_BOX, jto, "readwrite");
        // Remove invalid credentials (rows 0, 1 and 4)
        System.out.println("Remove invalid credentials (rows 0, 1 and 4)");
        jtmd.selectCell(jto, 4, 0);
        pressAndRelease(RMI_CREDENTIALS_REMOVE_BUTTON, nfnlso);
        waitNoEvent(5000);
        jtmd.selectCell(jto, 1, 0);
        pressAndRelease(RMI_CREDENTIALS_REMOVE_BUTTON, nfnlso);
        waitNoEvent(5000);
        jtmd.selectCell(jto, 0, 0);
        pressAndRelease(RMI_CREDENTIALS_REMOVE_BUTTON, nfnlso);
        waitNoEvent(5000);
        // Check Enable RMI wizard components
        System.out.println("Check Enable RMI wizard components");
        checkEnableRMIWizardComponents(nfnlso, rmiConfig);
        // Check Enable RMI wizard default values
        System.out.println("Check Enable RMI wizard values");
        checkEnableRMIWizardValues(nfnlso, rmiConfig);
        nfnlso.next();
        nfnlso.finish();

        // When require remote manager to authenticate is selected
        // (enable RMI wizard), an information dialog is displayed
        // when clicking on the finish button.
        NbDialogOperator dialog = new NbDialogOperator(INFORMATION_DIALOG_TITLE);
        dialog.ok();

        // Check generated file contents
        System.out.println("Check created files");
        checkCreatedFiles(objectName, path);
    }

    //========================= Check Wizard ==================================//
    /**
     * Check name and location wizard components are enabled/disabled
     */
    private void checkNameAndLocationWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            String fileNameAlreadyExists) {

        // Check text fields
        assertTrue(nfnlso.txtObjectName().isEnabled());
        assertTrue(nfnlso.txtObjectName().isEditable());
        assertTrue(getTextFieldOperator(RMI_ACCESS_FILE_TEXT_FIELD, nfnlso).isEnabled());
        assertFalse(getTextFieldOperator(RMI_ACCESS_FILE_TEXT_FIELD, nfnlso).isEditable());
        assertTrue(getTextFieldOperator(RMI_PASSWORD_FILE_TEXT_FIELD, nfnlso).isEnabled());
        assertFalse(getTextFieldOperator(RMI_PASSWORD_FILE_TEXT_FIELD, nfnlso).isEditable());
        assertTrue(getCheckBoxOperator(ENABLE_THREAD_CONTENTION_CHECK_BOX, nfnlso).isEnabled());

        // Check buttons
        assertTrue(nfnlso.btBack().isEnabled());
        if (fileNameAlreadyExists != null) {
            assertFalse(nfnlso.btNext().isEnabled());
            // Check warning message is displayed
            JLabel jl = getLabel("The file " + fileNameAlreadyExists + " already exists.",
                    nfnlso.getContentPane());
            assertNotNull(jl);
        } else {
            assertTrue(nfnlso.btNext().isEnabled());
        }
        assertFalse(nfnlso.btFinish().isEnabled());
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
    }

    /**
     * Check name and location wizard values
     */
    private void checkNameAndLocationWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso,
            String objectName,
            boolean enableThreadContentionMonitoring) {

        String rmiAccessFile = getTextFieldContent(
                RMI_ACCESS_FILE_TEXT_FIELD, nfnlso);
        String rmiPasswordFile = getTextFieldContent(
                RMI_PASSWORD_FILE_TEXT_FIELD, nfnlso);
        String expectedPackage = PACKAGE_COM_FOO_BAR.replace(".", File.separator);
        assertTrue(rmiAccessFile.endsWith(expectedPackage +
                File.separator + objectName + "." + ACCESS_EXT));
        assertTrue(rmiPasswordFile.endsWith(expectedPackage +
                File.separator + objectName + "." + PASSWORD_EXT));
        // Check box values
        assertEquals(enableThreadContentionMonitoring,
                getCheckBoxOperator(ENABLE_THREAD_CONTENTION_CHECK_BOX, nfnlso).isSelected());
    }

    /**
     * Check Enable RMI wizard components are enabled/disabled
     */
    private void checkEnableRMIWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            RMIManagementConfiguration rmiConfig) {

        assertTrue(getCheckBoxOperator(ENABLE_RMI_CHECK_BOX, nfnlso).isEnabled());

        assertEquals(rmiConfig.allowConnection,
                getLabelOperator(RMI_PORT_LABEL, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection,
                getTextFieldOperator(RMI_PORT_TEXT_FIELD, nfnlso).isEnabled());

        // Require Remore Manager to Authenticate
        assertEquals(rmiConfig.allowConnection,
                getCheckBoxOperator(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.requireAuthentication,
                getLabelOperator(RMI_CREDENTIALS_LABEL, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.requireAuthentication,
                getTableOperator(RMI_CREDENTIALS_TABLE, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.requireAuthentication,
                getButtonOperator(RMI_CREDENTIALS_ADD_BUTTON, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.requireAuthentication &&
                !rmiConfig.allowedCredentials.isEmpty(),
                getButtonOperator(RMI_CREDENTIALS_REMOVE_BUTTON, nfnlso).isEnabled());

        // Use SSL
        assertEquals(rmiConfig.allowConnection,
                getCheckBoxOperator(RMI_SSL_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.useSSL,
                getLabelOperator(RMI_SSL_PROTOCOL_LABEL, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.useSSL,
                getTextFieldOperator(RMI_SSL_PROTOCOL_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.useSSL,
                getLabelOperator(RMI_SSL_CIPHER_LABEL, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.useSSL,
                getTextFieldOperator(RMI_SSL_CIPHER_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(rmiConfig.allowConnection && rmiConfig.useSSL,
                getCheckBoxOperator(RMI_SSL_CLIENT_CHECK_BOX, nfnlso).isEnabled());

        // Check buttons
        assertTrue(nfnlso.btBack().isEnabled());
        JLabel jl = getLabel(INVALID_CREDENTIAL_WARNING, nfnlso.getContentPane());
        if (rmiConfig.invalidCredential) {
            assertFalse(nfnlso.btNext().isEnabled());
            // Check warning message is displayed
            assertNotNull(jl);
        } else {
            assertTrue(nfnlso.btNext().isEnabled());
            // Check warning message is not displayed
            assertNull(jl);
        }
        assertFalse(nfnlso.btFinish().isEnabled());
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
    }

    /**
     * Check Enable RMI wizard values
     */
    private void checkEnableRMIWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso,
            RMIManagementConfiguration rmiConfig) {

        JTableOperator jto = getTableOperator(RMI_CREDENTIALS_TABLE, nfnlso);

        int rowIndex = 0;
        int roleColumnIndex = jto.findColumn(CREDENTIAL_ROLE_COLUMN_NAME);
        int passwordColumnIndex = jto.findColumn(CREDENTIAL_PASSWORD_COLUMN_NAME);
        int accessColumnIndex = jto.findColumn(CREDENTIAL_ACCESS_COLUMN_NAME);

        // Check box values
        assertEquals(rmiConfig.allowConnection,
                getCheckBoxOperator(ENABLE_RMI_CHECK_BOX, nfnlso).isSelected());
        assertEquals(rmiConfig.requireAuthentication,
                getCheckBoxOperator(RMI_REQUIRE_AUTH_CHECK_BOX, nfnlso).isSelected());
        assertEquals(rmiConfig.useSSL,
                getCheckBoxOperator(RMI_SSL_CHECK_BOX, nfnlso).isSelected());
        assertEquals(rmiConfig.requireClientAuthentication,
                getCheckBoxOperator(RMI_SSL_CLIENT_CHECK_BOX, nfnlso).isSelected());
        // Text field values
        assertEquals(rmiConfig.port,
                getTextFieldContent(RMI_PORT_TEXT_FIELD, nfnlso));
        assertEquals(rmiConfig.protocol,
                getTextFieldContent(RMI_SSL_PROTOCOL_TEXT_FIELD, nfnlso));
        assertEquals(rmiConfig.cipher,
                getTextFieldContent(RMI_SSL_CIPHER_TEXT_FIELD, nfnlso));
        // Check allowed credentials list value
        // Empty list
        if (rmiConfig.allowedCredentials.isEmpty()) {
            assertTrue(jto.getRowCount() == 0);
        } // Not empty list
        else {
            assertTrue(jto.getRowCount() == rmiConfig.allowedCredentials.size());
            for (AllowedCredential allowedCredential : rmiConfig.allowedCredentials) {
                assertEquals(allowedCredential.role,
                        jto.getValueAt(rowIndex, roleColumnIndex));
                assertEquals(allowedCredential.password,
                        jto.getValueAt(rowIndex, passwordColumnIndex));
                assertEquals(allowedCredential.access,
                        jto.getValueAt(rowIndex, accessColumnIndex));

                rowIndex++;
            }
        }
    }

    /**
     * Check SNMP Configuration wizard components are enabled/disabled
     */
    private void checkSNMPConfigurationWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            SNMPManagementConfiguration snmpConfig) {

        assertTrue(getCheckBoxOperator(ENABLE_SNMP_CHECK_BOX, nfnlso).isEnabled());

        assertEquals(snmpConfig.allowConnection,
                getLabelOperator(SNMP_PORT_LABEL, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection,
                getTextFieldOperator(SNMP_PORT_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection,
                getLabelOperator(SNMP_INTERFACE_LABEL, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection,
                getTextFieldOperator(SNMP_INTERFACE_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection,
                getLabelOperator(SNMP_TRAP_PORT_LABEL, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection,
                getTextFieldOperator(SNMP_TRAP_PORT_TEXT_FIELD, nfnlso).isEnabled());

        // Use ACL
        assertEquals(snmpConfig.allowConnection,
                getCheckBoxOperator(SNMP_ACL_CHECK_BOX, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection && snmpConfig.enableAuthentication,
                getLabelOperator(SNMP_ACL_FILE_LABEL, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection && snmpConfig.enableAuthentication,
                getTextFieldOperator(SNMP_ACL_FILE_TEXT_FIELD, nfnlso).isEnabled());
        assertEquals(snmpConfig.allowConnection && snmpConfig.enableAuthentication,
                getButtonOperator(SNMP_ACL_FILE_BROWSE_BUTTON, nfnlso).isEnabled());

        // Check buttons
        assertTrue(nfnlso.btBack().isEnabled());
        assertFalse(nfnlso.btNext().isEnabled());
        assertTrue(nfnlso.btFinish().isEnabled());
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
    }

    /**
     * Check SNMP Configuration wizard values
     */
    private void checkSNMPConfigurationWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso,
            SNMPManagementConfiguration snmpConfig) {

        // Check box values
        assertEquals(snmpConfig.allowConnection,
                getCheckBoxOperator(ENABLE_SNMP_CHECK_BOX, nfnlso).isSelected());
        assertEquals(snmpConfig.enableAuthentication,
                getCheckBoxOperator(SNMP_ACL_CHECK_BOX, nfnlso).isSelected());
        // Text field values
        assertEquals(snmpConfig.port,
                getTextFieldContent(SNMP_PORT_TEXT_FIELD, nfnlso));
        assertEquals(snmpConfig.intf,
                getTextFieldContent(SNMP_INTERFACE_TEXT_FIELD, nfnlso));
        assertEquals(snmpConfig.trapPort,
                getTextFieldContent(SNMP_TRAP_PORT_TEXT_FIELD, nfnlso));
        assertEquals(snmpConfig.aclFile,
                getTextFieldContent(SNMP_ACL_FILE_TEXT_FIELD, nfnlso));
    }

    //========================= Check created files ==================================//
    /**
     * Check the created files with expected golden files.
     */
    private void checkCreatedFiles(String objectName, String path) {

        Properties properties = new Properties();
        properties.put(FOLDER_TAG, path);

        assertTrue(compareFiles(
                new File(path + objectName + "." + PROPERTIES_EXT),
                getGoldenFile(objectName + "." + PROPERTIES_EXT), properties));
        assertTrue(compareFiles(
                new File(path + objectName + "." + ACCESS_EXT),
                getGoldenFile(objectName + "." + ACCESS_EXT), properties));
        assertTrue(compareFiles(
                new File(path + objectName + "." + PASSWORD_EXT),
                getGoldenFile(objectName + "." + PASSWORD_EXT), properties));
    }

    //========================= Files creation ==================================//
    /**
     * Create empty file into PACKAGE_NAME directory.
     */
    private NewJavaFileNameLocationStepOperator createEmptyFile(String objectName) {
        // First select the folder node to initialize the folder JTextField
        selectNode(PROJECT_NAME_CONFIGURATION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + PACKAGE_COM_FOO_BAR);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_CONFIGURATION_FUNCTIONAL,
                FILE_CATEGORY_OTHER,
                FILE_TYPE_EMPTY_FILE);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                objectName, null);
        return nfnlso;
    }

    /**
     * Create management configuration file into PACKAGE_NAME directory.
     */
    private NewJavaFileNameLocationStepOperator createManagementConfigurationFileFromMenu(
            String objectName) {
        // First select the folder node to initialize the folder JTextField
        selectNode(PROJECT_NAME_CONFIGURATION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + PACKAGE_COM_FOO_BAR);
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_CONFIGURATION_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                FILE_TYPE_MANAGEMENT_CONFIGURATION_FILE);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                objectName, null);
        return nfnlso;
    }

    /**
     * Create management configuration file into PACKAGE_NAME directory.
     */
    private NewJavaFileNameLocationStepOperator createManagementConfigurationFileFromNode(
            String objectName) {
        NewFileWizardOperator nfwo = newFileWizardFromNode(
                PROJECT_NAME_CONFIGURATION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + PACKAGE_COM_FOO_BAR,
                FILE_CATEGORY_JMX,
                FILE_TYPE_MANAGEMENT_CONFIGURATION_FILE);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                objectName, null);
        return nfnlso;
    }

    //========================= Inner class ==================================//
    /**
     * Inner class used to check RMI management configuration wizard
     * components/values.
     */
    private class RMIManagementConfiguration {

        // Variables initialized with wizard default values
        boolean allowConnection = true;
        boolean requireAuthentication = false;
        boolean useSSL = false;
        boolean requireClientAuthentication = false;
        boolean invalidCredential = false;
        String port = "1099";
        String protocol = "";
        String cipher = "";
        // The key is the role, the value is the password
        ArrayList<AllowedCredential> allowedCredentials =
                new ArrayList<AllowedCredential>();
    }

    /**
     * Inner class used to check RMI allowed credentials.
     */
    private class AllowedCredential {

        // Variables initialized with wizard default values
        String role = null;
        String password = null;
        String access = "readonly";

        public AllowedCredential() {
        }

        public AllowedCredential(String role, String password, String access) {
            this.role = role;
            this.password = password;
            this.access = access;
        }
    }

    /**
     * Inner class used to check SNMP management configuration wizard
     * components/values.
     */
    private class SNMPManagementConfiguration {

        // Variables initialized with wizard default values
        boolean allowConnection = false;
        boolean enableAuthentication = false;
        String port = "161";
        String intf = "localhost";
        String trapPort = "162";
        String aclFile = "";
    }
}
