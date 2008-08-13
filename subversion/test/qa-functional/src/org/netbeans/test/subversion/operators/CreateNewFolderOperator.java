/*
 * CreateNewFolderOperator.java
 *
 * Created on 11/05/06 10:38
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Create a new folder" NbPresenter.
 *
 * @author peter
 * @version 1.0
 */
public class CreateNewFolderOperator extends NbDialogOperator {

    /**
     * Creates new CreateNewFolderOperator that can handle it.
     */
    public CreateNewFolderOperator() {
        super("Specify a new folder");
    }

    private JLabelOperator _lblFolderName;
    private JTextFieldOperator _txtFolderName;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Folder name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolderName() {
        if (_lblFolderName==null) {
            _lblFolderName = new JLabelOperator(this, "Folder name:");
        }
        return _lblFolderName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFolderName() {
        if (_txtFolderName==null) {
            _txtFolderName = new JTextFieldOperator(this);
        }
        return _txtFolderName;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtFolderName
     * @return String text
     */
    public String getFolderName() {
        return txtFolderName().getText();
    }

    /** sets text for txtFolderName
     * @param text String text
     */
    public void setFolderName(String text) {
        txtFolderName().setText(text);
    }

    /** types text for txtFolderName
     * @param text String text
     */
    public void typeFolderName(String text) {
        txtFolderName().typeText(text);
    }

    /** clicks on "OK" JButton
     */
    @Override
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    @Override
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of CreateNewFolderOperator by accessing all its components.
     */
    public void verify() {
        lblFolderName();
        txtFolderName();
        btOK();
        btCancel();
    }
}

