/*
 * CreateTestsDialogOperator.java
 *
 * Created on 2/10/03 11:16 PM
 */
package org.netbeans.jellytools.modules.junit.dialogs;

import java.io.PrintStream;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Create Tests" NbDialog.
 *
 * @author dave
 * @version 1.0
 */
public class CreateTestsDialogOperator extends NbDialogOperator {

    /** Creates new CreateTestsDialogOperator that can handle it.
     */
    public CreateTestsDialogOperator() {
        super("Create Tests");
    }

    private JLabelOperator _lblFileSystem;
    private JComboBoxOperator _cboFileSystem;
    public static final String ITEM_NOFILESYSTEMSELECTED = "(no file system selected)";
    private JLabelOperator _lblSuiteClass;
    private JLabelOperator _lblTestClass;
    private JComboBoxOperator _cboSuiteClass;
    public static final String ITEM_SIMPLEJUNITTEST = "SimpleJUnitTest";
    public static final String ITEM_SIMPLENBJUNITTEST = "SimpleNbJUnitTest";
    private JComboBoxOperator _cboTestClass;
    private JCheckBoxOperator _cbPublicMethods;
    private JCheckBoxOperator _cbProtectedMethods;
    private JCheckBoxOperator _cbPackageMethods;
    private JCheckBoxOperator _cbComments;
    private JCheckBoxOperator _cbDefaultBodies;
    private JCheckBoxOperator _cbJavaDoc;
    private JCheckBoxOperator _cbIncludeExceptionClasses;
    private JCheckBoxOperator _cbIncludeAbstractClasses;
    private JCheckBoxOperator _cbRegenerateSuiteMethod;
    private JCheckBoxOperator _cbIncludePackagePrivateClasses;
    private JCheckBoxOperator _cbShowCreateTestsConfigurationDialog;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "File System:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileSystem() {
        if (_lblFileSystem==null) {
            _lblFileSystem = new JLabelOperator(this, "File System:");
        }
        return _lblFileSystem;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFileSystem() {
        if (_cboFileSystem==null) {
            _cboFileSystem = new JComboBoxOperator(this);
        }
        return _cboFileSystem;
    }

    /** Tries to find "Suite Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSuiteClass() {
        if (_lblSuiteClass==null) {
            _lblSuiteClass = new JLabelOperator(this, "Suite Class:");
        }
        return _lblSuiteClass;
    }

    /** Tries to find "Test Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTestClass() {
        if (_lblTestClass==null) {
            _lblTestClass = new JLabelOperator(this, "Test Class:");
        }
        return _lblTestClass;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSuiteClass() {
        if (_cboSuiteClass==null) {
            _cboSuiteClass = new JComboBoxOperator(this, 1);
        }
        return _cboSuiteClass;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTestClass() {
        if (_cboTestClass==null) {
            _cboTestClass = new JComboBoxOperator(this, 2);
        }
        return _cboTestClass;
    }

    /** Tries to find " Public Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPublicMethods() {
        if (_cbPublicMethods==null) {
            _cbPublicMethods = new JCheckBoxOperator(this, " Public Methods");
        }
        return _cbPublicMethods;
    }

    /** Tries to find " Protected Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbProtectedMethods() {
        if (_cbProtectedMethods==null) {
            _cbProtectedMethods = new JCheckBoxOperator(this, " Protected Methods");
        }
        return _cbProtectedMethods;
    }

    /** Tries to find " Package Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPackageMethods() {
        if (_cbPackageMethods==null) {
            _cbPackageMethods = new JCheckBoxOperator(this, " Package Methods");
        }
        return _cbPackageMethods;
    }

    /** Tries to find " Comments" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbComments() {
        if (_cbComments==null) {
            _cbComments = new JCheckBoxOperator(this, " Comments");
        }
        return _cbComments;
    }

    /** Tries to find " Default Bodies" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbDefaultBodies() {
        if (_cbDefaultBodies==null) {
            _cbDefaultBodies = new JCheckBoxOperator(this, " Default Bodies");
        }
        return _cbDefaultBodies;
    }

    /** Tries to find " JavaDoc" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbJavaDoc() {
        if (_cbJavaDoc==null) {
            _cbJavaDoc = new JCheckBoxOperator(this, " JavaDoc");
        }
        return _cbJavaDoc;
    }

    /** Tries to find " Include Exception Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeExceptionClasses() {
        if (_cbIncludeExceptionClasses==null) {
            _cbIncludeExceptionClasses = new JCheckBoxOperator(this, " Include Exception Classes");
        }
        return _cbIncludeExceptionClasses;
    }

    /** Tries to find " Include Abstract Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeAbstractClasses() {
        if (_cbIncludeAbstractClasses==null) {
            _cbIncludeAbstractClasses = new JCheckBoxOperator(this, " Include Abstract Classes");
        }
        return _cbIncludeAbstractClasses;
    }

    /** Tries to find " Regenerate Suite Method" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRegenerateSuiteMethod() {
        if (_cbRegenerateSuiteMethod==null) {
            _cbRegenerateSuiteMethod = new JCheckBoxOperator(this, " Regenerate Suite Method");
        }
        return _cbRegenerateSuiteMethod;
    }

    /** Tries to find " Include Package Private Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludePackagePrivateClasses() {
        if (_cbIncludePackagePrivateClasses==null) {
            _cbIncludePackagePrivateClasses = new JCheckBoxOperator(this, " Include Package Private Classes");
        }
        return _cbIncludePackagePrivateClasses;
    }

    /** Tries to find " Show Create Tests Configuration Dialog" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbShowCreateTestsConfigurationDialog() {
        if (_cbShowCreateTestsConfigurationDialog==null) {
            _cbShowCreateTestsConfigurationDialog = new JCheckBoxOperator(this, " Show Create Tests Configuration Dialog");
        }
        return _cbShowCreateTestsConfigurationDialog;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboFileSystem
     * @return String item
     */
    public String getSelectedFileSystem() {
        return cboFileSystem().getSelectedItem().toString();
    }

    /** selects item for cboFileSystem
     * @param item String item
     */
    public void selectFileSystem(String item) {
        cboFileSystem().selectItem(item);
    }

    /** types text for cboFileSystem
     * @param text String text
     */
    public void typeFileSystem(String text) {
        cboFileSystem().typeText(text);
    }

    /** returns selected item for cboSuiteClass
     * @return String item
     */
    public String getSelectedSuiteClass() {
        return cboSuiteClass().getSelectedItem().toString();
    }

    /** selects item for cboSuiteClass
     * @param item String item
     */
    public void selectSuiteClass(String item) {
        cboSuiteClass().selectItem(item);
    }

    /** types text for cboSuiteClass
     * @param text String text
     */
    public void typeSuiteClass(String text) {
        cboSuiteClass().typeText(text);
    }

    /** returns selected item for cboTestClass
     * @return String item
     */
    public String getSelectedTestClass() {
        return cboTestClass().getSelectedItem().toString();
    }

    /** selects item for cboTestClass
     * @param item String item
     */
    public void selectTestClass(String item) {
        cboTestClass().selectItem(item);
    }

    /** types text for cboTestClass
     * @param text String text
     */
    public void typeTestClass(String text) {
        cboTestClass().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPublicMethods(boolean state) {
        if (cbPublicMethods().isSelected()!=state) {
            cbPublicMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkProtectedMethods(boolean state) {
        if (cbProtectedMethods().isSelected()!=state) {
            cbProtectedMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPackageMethods(boolean state) {
        if (cbPackageMethods().isSelected()!=state) {
            cbPackageMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkComments(boolean state) {
        if (cbComments().isSelected()!=state) {
            cbComments().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkDefaultBodies(boolean state) {
        if (cbDefaultBodies().isSelected()!=state) {
            cbDefaultBodies().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkJavaDoc(boolean state) {
        if (cbJavaDoc().isSelected()!=state) {
            cbJavaDoc().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeExceptionClasses(boolean state) {
        if (cbIncludeExceptionClasses().isSelected()!=state) {
            cbIncludeExceptionClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeAbstractClasses(boolean state) {
        if (cbIncludeAbstractClasses().isSelected()!=state) {
            cbIncludeAbstractClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRegenerateSuiteMethod(boolean state) {
        if (cbRegenerateSuiteMethod().isSelected()!=state) {
            cbRegenerateSuiteMethod().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludePackagePrivateClasses(boolean state) {
        if (cbIncludePackagePrivateClasses().isSelected()!=state) {
            cbIncludePackagePrivateClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkShowCreateTestsConfigurationDialog(boolean state) {
        if (cbShowCreateTestsConfigurationDialog().isSelected()!=state) {
            cbShowCreateTestsConfigurationDialog().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CreateTestsDialogOperator by accessing all its components.
     */
    public void verify() {
        lblFileSystem();
        cboFileSystem();
        lblSuiteClass();
        lblTestClass();
        cboSuiteClass();
        cboTestClass();
        cbPublicMethods();
        cbProtectedMethods();
        cbPackageMethods();
        cbComments();
        cbDefaultBodies();
        cbJavaDoc();
        cbIncludeExceptionClasses();
        cbIncludeAbstractClasses();
        cbRegenerateSuiteMethod();
        cbIncludePackagePrivateClasses();
        cbShowCreateTestsConfigurationDialog();
    }

    /** Performs simple test of CreateTestsDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CreateTestsDialogOperator().verify();
        System.out.println("CreateTestsDialogOperator verification finished.");
    }
    
    public void dumpAll (PrintStream out) {
        out.println ("FileSystem: " + cboFileSystem().getSelectedItem());
        out.println ("SuiteClass: " + cboSuiteClass().getSelectedItem());
        out.println ("TestClass: " + cboTestClass().getSelectedItem());
        out.println ("PublicMethod: " + cbPublicMethods().isSelected());
        out.println ("ProtectedMethod: " + cbProtectedMethods().isSelected());
        out.println ("PackageMethod: " + cbPackageMethods().isSelected());
        out.println ("IncludeAbstract: " + cbIncludeAbstractClasses().isSelected());
        out.println ("IncludeException: " + cbIncludeAbstractClasses().isSelected());
        out.println ("IncludePackagePrivateClass: " + cbIncludePackagePrivateClasses().isSelected());
        out.println ("Comments: " + cbComments().isSelected());
        out.println ("JavaDoc: " + cbJavaDoc().isSelected());
        out.println ("DefaultBodies: " + cbDefaultBodies().isSelected());
        out.println ("RegenerateSuite: " + cbRegenerateSuiteMethod().isSelected());
        out.println ("ShowDialog: " + cbShowCreateTestsConfigurationDialog().isSelected());
    }
}

