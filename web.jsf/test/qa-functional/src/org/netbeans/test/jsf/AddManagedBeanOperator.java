/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.jsf;

import javax.swing.JTextField;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Add Managed Bean" Dialog.
 *
 */
public class AddManagedBeanOperator extends NbDialogOperator {

    /** Creates new AddManagedBeanOperator that can handle it.
     */
    public AddManagedBeanOperator() {
        super("Add Managed Bean");
    }

    private JLabelOperator _lblBeanClass;
    private JTextFieldOperator _txtBeanClass;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblScope;
    private JComboBoxOperator _cboScope;
    private JLabelOperator _lblBeanDescription;
    private JTextAreaOperator _txtBeanDescription;
    private JLabelOperator _lblBeanName;
    private JTextFieldOperator _txtBeanName;
    private JButtonOperator _btAdd;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Bean Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanClass() {
        if (_lblBeanClass==null) {
            _lblBeanClass = new JLabelOperator(this, "Bean Class:");
        }
        return _lblBeanClass;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBeanClass() {
        if (_txtBeanClass==null) {
            _txtBeanClass = new JTextFieldOperator((JTextField)lblBeanClass().getLabelFor());
        }
        return _txtBeanClass;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Scope:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblScope() {
        if (_lblScope==null) {
            _lblScope = new JLabelOperator(this, "Scope:");
        }
        return _lblScope;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboScope() {
        if (_cboScope==null) {
            _cboScope = new JComboBoxOperator(this);
        }
        return _cboScope;
    }

    /** Tries to find "Bean Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanDescription() {
        if (_lblBeanDescription==null) {
            _lblBeanDescription = new JLabelOperator(this, "Bean Description:");
        }
        return _lblBeanDescription;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtBeanDescription() {
        if (_txtBeanDescription==null) {
            _txtBeanDescription = new JTextAreaOperator(this);
        }
        return _txtBeanDescription;
    }

    /** Tries to find "Bean Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanName() {
        if (_lblBeanName==null) {
            _lblBeanName = new JLabelOperator(this, "Bean Name:");
        }
        return _lblBeanName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBeanName() {
        if (_txtBeanName==null) {
            _txtBeanName = new JTextFieldOperator((JTextField)lblBeanName().getLabelFor());
        }
        return _txtBeanName;
    }

    /** Tries to find "Add" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, "Add");
        }
        return _btAdd;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtBeanClass
     * @return String text
     */
    public String getBeanClass() {
        return txtBeanClass().getText();
    }

    /** sets text for txtBeanClass
     * @param text String text
     */
    public void setBeanClass(String text) {
        txtBeanClass().setText(text);
    }

    /** types text for txtBeanClass
     * @param text String text
     */
    public void typeBeanClass(String text) {
        txtBeanClass().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** returns selected item for cboScope
     * @return String item
     */
    public String getSelectedScope() {
        return cboScope().getSelectedItem().toString();
    }

    /** selects item for cboScope
     * @param item String item
     */
    public void selectScope(String item) {
        cboScope().selectItem(item);
    }

    /** gets text for txtBeanDescription
     * @return String text
     */
    public String getBeanDescription() {
        return txtBeanDescription().getText();
    }

    /** sets text for txtBeanDescription
     * @param text String text
     */
    public void setBeanDescription(String text) {
        txtBeanDescription().setText(text);
    }

    /** types text for txtBeanDescription
     * @param text String text
     */
    public void typeBeanDescription(String text) {
        txtBeanDescription().typeText(text);
    }

    /** gets text for txtBeanName
     * @return String text
     */
    public String getBeanName() {
        return txtBeanName().getText();
    }

    /** sets text for txtBeanName
     * @param text String text
     */
    public void setBeanName(String text) {
        txtBeanName().setText(text);
    }

    /** types text for txtBeanName
     * @param text String text
     */
    public void typeBeanName(String text) {
        txtBeanName().typeText(text);
    }

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AddManagedBeanOperator by accessing all its components.
     */
    public void verify() {
        lblBeanClass();
        txtBeanClass();
        btBrowse();
        lblScope();
        cboScope();
        lblBeanDescription();
        txtBeanDescription();
        lblBeanName();
        txtBeanName();
        btAdd();
    }
}

