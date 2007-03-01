/*
 * CreateNewProfileDialog.java
 *
 * Created on 2/27/07 5:45 PM
 */
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "Create New Profile Dialog" NbPresenter.
 *
 * @author jp159440
 * @version 1.0
 */
public class CreateNewProfileDialog extends JDialogOperator {

    /** Creates new CreateNewProfileDialog that can handle it.
     */
    public CreateNewProfileDialog() {
        super("Create New Profile Dialog");
    }

    private JLabelOperator _lblProfileName;
    private JTextFieldOperator _txtProfileName;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Profile Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProfileName() {
        if (_lblProfileName==null) {
            _lblProfileName = new JLabelOperator(this, "Profile Name:");
        }
        return _lblProfileName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProfileName() {
        if (_txtProfileName==null) {
            _txtProfileName = new JTextFieldOperator(this);
        }
        return _txtProfileName;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
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


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtProfileName
     * @return String text
     */
    public String getProfileName() {
        return txtProfileName().getText();
    }

    /** sets text for txtProfileName
     * @param text String text
     */
    public void setProfileName(String text) {
        txtProfileName().setText(text);
    }

    /** types text for txtProfileName
     * @param text String text
     */
    public void typeProfileName(String text) {
        txtProfileName().typeText(text);
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CreateNewProfileDialog by accessing all its components.
     */
    public void verify() {
        lblProfileName();
        txtProfileName();
        btOK();
        btCancel();
    }

    /** Performs simple test of CreateNewProfileDialog
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CreateNewProfileDialog().verify();
        System.out.println("CreateNewProfileDialog verification finished.");
    }
}

