/*
 * NewWebApplication.java
 *
 * Created on 25.3.08 18:32
 */
package org.netbeans.jellytools;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "New Web Application" NbDialog.
 *
 * @author Dan Kolar
 * @version 1.0
 */
public class NewWebProjectServerSettingsStepOperator extends NewProjectWizardOperator {

    /** Creates new NewWebApplication that can handle it.
     */

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    public static final String ITEM_4 = "4.";
    private JLabelOperator _lblServerAndSettings;
    private JLabelOperator _lblContextPath;
    private JLabelOperator _lblServer;
    private JLabelOperator _lblJavaEEVersion;
    private JTextFieldOperator _txtContextPath;
    private JCheckBoxOperator _cbCopyServerJARFilesToLibrariesFolder;
    private JComboBoxOperator _cboServer;
    public static final String ITEM_APACHETOMCAT6016 = "Apache Tomcat 6.0.16";
    public static final String ITEM_GLASSFISHV2 = "GlassFish V2";
    private JButtonOperator _btAdd;
    private JComboBoxOperator _cboJavaEEVersion;
    public static final String ITEM_JAVAEE5 = "Java EE 5";
    public static final String ITEM_J2EE14 = "J2EE 1.4";
    public static final String ITEM_J2EE13 = "J2EE 1.3";
    private JLabelOperator _lblAddToEnterpriseApplication;
    private JComboBoxOperator _cboAddToEnterpriseApplication;
    public static final String ITEM_NONE = "<None>";
    private JLabelOperator _lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Server and Settings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblServerAndSettings() {
        if (_lblServerAndSettings==null) {
            _lblServerAndSettings = new JLabelOperator(this, "Server and Settings");
        }
        return _lblServerAndSettings;
    }

    /** Tries to find "Context Path:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if (_lblContextPath==null) {
            _lblContextPath = new JLabelOperator(this, "Context Path:");
        }
        return _lblContextPath;
    }

    /** Tries to find "Server:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblServer() {
        if (_lblServer==null) {
            _lblServer = new JLabelOperator(this, "Server:");
        }
        return _lblServer;
    }

    /** Tries to find "Java EE Version:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJavaEEVersion() {
        if (_lblJavaEEVersion==null) {
            _lblJavaEEVersion = new JLabelOperator(this, "Java EE Version:");
        }
        return _lblJavaEEVersion;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (_txtContextPath==null) {
            _txtContextPath = new JTextFieldOperator(this);
        }
        return _txtContextPath;
    }

    /** Tries to find "Copy Server JAR Files to Libraries Folder" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCopyServerJARFilesToLibrariesFolder() {
        if (_cbCopyServerJARFilesToLibrariesFolder==null) {
            _cbCopyServerJARFilesToLibrariesFolder = new JCheckBoxOperator(this, "Copy Server JAR Files to Libraries Folder");
        }
        return _cbCopyServerJARFilesToLibrariesFolder;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboServer() {
        if (_cboServer==null) {
            _cboServer = new JComboBoxOperator(this);
        }
        return _cboServer;
    }

    /** Tries to find "Add..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, "Add...");
        }
        return _btAdd;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJavaEEVersion() {
        if (_cboJavaEEVersion==null) {
            _cboJavaEEVersion = new JComboBoxOperator(this, 1);
        }
        return _cboJavaEEVersion;
    }

    /** Tries to find "Add to Enterprise Application:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAddToEnterpriseApplication() {
        if (_lblAddToEnterpriseApplication==null) {
            _lblAddToEnterpriseApplication = new JLabelOperator(this, "Add to Enterprise Application:");
        }
        return _lblAddToEnterpriseApplication;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboAddToEnterpriseApplication() {
        if (_cboAddToEnterpriseApplication==null) {
            _cboAddToEnterpriseApplication = new JComboBoxOperator(this, 2);
        }
        return _cboAddToEnterpriseApplication;
    }

    /** Tries to find "<html>There may be legal considerations when sharing server JAR files. Be sure to check the license for your server to make sure you can distribute server JAR files to other developers.</html>" WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml() {
        if (_lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml==null) {
            _lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml = new JLabelOperator(this, "<html>There may be legal considerations when sharing server JAR files. Be sure to check the license for your server to make sure you can distribute server JAR files to other developers.</html>");
        }
        return _lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml;
    }

    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtContextPath
     * @return String text
     */
    public String getContextPath() {
        return txtContextPath().getText();
    }

    /** sets text for txtContextPath
     * @param text String text
     */
    public void setContextPath(String text) {
        txtContextPath().setText(text);
    }

    /** types text for txtContextPath
     * @param text String text
     */
    public void typeContextPath(String text) {
        txtContextPath().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCopyServerJARFilesToLibrariesFolder(boolean state) {
        if (cbCopyServerJARFilesToLibrariesFolder().isSelected()!=state) {
            cbCopyServerJARFilesToLibrariesFolder().push();
        }
    }

    /** returns selected item for cboServer
     * @return String item
     */
    public String getSelectedServer() {
        return cboServer().getSelectedItem().toString();
    }

    /** selects item for cboServer
     * @param item String item
     */
    public void selectServer(String item) {
        cboServer().selectItem(item);
    }

    /** clicks on "Add..." JButton
     */
    public void add() {
        btAdd().push();
    }

    /** returns selected item for cboJavaEEVersion
     * @return String item
     */
    public String getSelectedJavaEEVersion() {
        return cboJavaEEVersion().getSelectedItem().toString();
    }

    /** selects item for cboJavaEEVersion
     * @param item String item
     */
    public void selectJavaEEVersion(String item) {
        cboJavaEEVersion().selectItem(item);
    }

    /** returns selected item for cboAddToEnterpriseApplication
     * @return String item
     */
    public String getSelectedAddToEnterpriseApplication() {
        return cboAddToEnterpriseApplication().getSelectedItem().toString();
    }

    /** selects item for cboAddToEnterpriseApplication
     * @param item String item
     */
    public void selectAddToEnterpriseApplication(String item) {
        cboAddToEnterpriseApplication().selectItem(item);
    }

    /** clicks on "< Back" JButton
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    public void finish() {
        btFinish().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewWebApplication by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lblServerAndSettings();
        lblContextPath();
        lblServer();
        lblJavaEEVersion();
        txtContextPath();
        cbCopyServerJARFilesToLibrariesFolder();
        cboServer();
        btAdd();
        cboJavaEEVersion();
        lblAddToEnterpriseApplication();
        cboAddToEnterpriseApplication();
        lblHtmlThereMayBeLegalConsiderationsWhenSharingServerJARFilesBeSureToCheckTheLicenseForYourServerToMakeSureYouCanDistributeServerJARFilesToOtherDevelopersHtml();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }

    /** Performs simple test of NewWebApplication
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NewWebProjectServerSettingsStepOperator().verify();
        System.out.println("NewWebApplication verification finished.");
    }
}

