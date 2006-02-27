/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.db;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handles "Add JDBC Driver" dialog.<br>
 * Usage:
 * <pre>
 *      DriversNode.invoke().addDriver();
 *      AddJDBCDriverOperator addDlgOperator = new AddJDBCDriverOperator();
 *      addDlgOperator.add();
 *      ...
 *      addDlgOperator.ok();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class AddJDBCDriverOperator extends NbDialogOperator {

    /** Creates new AddJDBCDriverOperator that can handle it.
     */
    public AddJDBCDriverOperator() {
        // "Add JDBC Driver"
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDialogTitle"));
    }

    private JLabelOperator _lblDriverFiles;
    private JListOperator _lstDriverFiles;
    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JLabelOperator _lblDriverClass;
    private JComboBoxOperator _cboDriverClass;
    private JButtonOperator _btFind;
    private JLabelOperator _lblName;
    private JTextFieldOperator _txtName;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Driver File(s):" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDriverFiles() {
        if (_lblDriverFiles==null) {
            _lblDriverFiles = new JLabelOperator(this,Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "DriverFiles"));
        }
        return _lblDriverFiles;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstDriverFiles() {
        if (_lstDriverFiles==null) {
            _lstDriverFiles = new JListOperator((JList) lblDriverFiles().
                    getLabelFor());
        }
        return _lstDriverFiles;
    }

    /** Tries to find "Add..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd == null) {
            _btAdd = new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDriverAdd"));
        }
        return _btAdd;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDriverRemove"));
        }
        return _btRemove;
    }

    /** Tries to find "Driver Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDriverClass() {
        if (_lblDriverClass==null) {
            _lblDriverClass = new JLabelOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDriverClass"));
        }
        return _lblDriverClass;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDriverClass() {
        if (_cboDriverClass==null) {
            _cboDriverClass = new JComboBoxOperator((JComboBox) lblDriverClass().
                    getLabelFor());
        }
        return _cboDriverClass;
    }

    /** Tries to find "Find" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFind() {
        if (_btFind==null) {
            _btFind = new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDriverFind"));
        }
        return _btFind;
    }

    /** Tries to find "Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "AddDriverDriverName"));
        }
        return _lblName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator((JTextField) lblName().
                    getLabelFor());
        }
        return _txtName;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "Add..." JButton
     */
    public void add() {
        btAdd().push();
    }

    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }

    /** returns selected item for cboDriverClass
     * @return String item
     */
    public String getSelectedDriverClass() {
        return cboDriverClass().getSelectedItem().toString();
    }

    /** selects item for cboDriverClass
     * @param item String item
     */
    public void selectDriverClass(String item) {
        cboDriverClass().selectItem(item);
    }

    /** types text for cboDriverClass
     * @param text String text
     */
    public void typeDriverClass(String text) {
        cboDriverClass().typeText(text);
    }

    /** clicks on "Find" JButton
     */
    public void find() {
        btFind().push();
    }

    /** gets text for txtName
     * @return String text
     */
    public String getName() {
        return txtName().getText();
    }

    /** sets text for txtName
     * @param text String text
     */
    public void setName(String text) {
        txtName().setText(text);
    }

    /** types text for txtName
     * @param text String text
     */
    public void typeName(String text) {
        txtName().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AddJDBCDriverOperator by accessing all its
     * components.
     */
    public void verify() {
        lblDriverFiles();
        lstDriverFiles();
        btAdd();
        btRemove();
        lblDriverClass();
        cboDriverClass();
        btFind();
        lblName();
        txtName();
        btCancel();
        btOK();
    }
}

