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

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/** Class implementing all necessary methods for handling "Add Navigation Case" NbDialog.
 *
 * @author luke
 */
public class AddNavigationCaseDialogOperator extends NbDialogOperator {

    /** Creates new AddNavigationCase that can handle it.
     */
    public AddNavigationCaseDialogOperator() {
        super("Add Navigation Case");
    }

    private JLabelOperator _lblFromView;
    private JComboBoxOperator _cboFromView;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblFromAction;
    private JTextFieldOperator _txtFromAction;
    private JLabelOperator _lblFromOutcome;
    private JTextFieldOperator _txtFromOutcome;
    private JLabelOperator _lblToView;
    private JButtonOperator _btBrowse2;
    private JCheckBoxOperator _cbRedirect;
    private JLabelOperator _lblRuleDescription;
    private JTextAreaOperator _txtRuleDescription;
    private JComboBoxOperator _cboToView;
    private JButtonOperator _btAdd;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "From View:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFromView() {
        if (_lblFromView==null) {
            _lblFromView = new JLabelOperator(this, "From View:");
        }
        return _lblFromView;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFromView() {
        if (_cboFromView==null) {
            _cboFromView = new JComboBoxOperator((JComboBox)lblFromView().getLabelFor());
        }
        return _cboFromView;
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

    /** Tries to find "From Action:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFromAction() {
        if (_lblFromAction==null) {
            _lblFromAction = new JLabelOperator(this, "From Action:");
        }
        return _lblFromAction;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFromAction() {
        if (_txtFromAction==null) {
            _txtFromAction = new JTextFieldOperator((JTextField)lblFromAction().getLabelFor());
        }
        return _txtFromAction;
    }

    /** Tries to find "From Outcome:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFromOutcome() {
        if (_lblFromOutcome==null) {
            _lblFromOutcome = new JLabelOperator(this, "From Outcome:");
        }
        return _lblFromOutcome;
    }
    
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFromOutcome() {
        if (_txtFromOutcome==null) {
            _txtFromOutcome = new JTextFieldOperator((JTextField)lblFromOutcome().getLabelFor());
        }
        return _txtFromOutcome;
    }

    /** Tries to find "To View:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblToView() {
        if (_lblToView==null) {
            _lblToView = new JLabelOperator(this, "To View:");
        }
        return _lblToView;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse2() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, "Browse...", 1);
        }
        return _btBrowse2;
    }

    /** Tries to find "Redirect" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRedirect() {
        if (_cbRedirect==null) {
            _cbRedirect = new JCheckBoxOperator(this, "Redirect");
        }
        return _cbRedirect;
    }

    /** Tries to find "Rule Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRuleDescription() {
        if (_lblRuleDescription==null) {
            _lblRuleDescription = new JLabelOperator(this, "Rule Description:");
        }
        return _lblRuleDescription;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtRuleDescription() {
        if (_txtRuleDescription==null) {
            _txtRuleDescription = new JTextAreaOperator(this);
        }
        return _txtRuleDescription;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboToView() {
        if (_cboToView==null) {
            _cboToView = new JComboBoxOperator((JComboBox)lblToView().getLabelFor());
        }
        return _cboToView;
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
    
    public void add(){
        btAdd().push();
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboFromView
     * @return String item
     */
    public String getSelectedFromView() {
        return cboFromView().getSelectedItem().toString();
    }

    /** selects item for cboFromView
     * @param item String item
     */
    public void selectFromView(String item) {
        cboFromView().selectItem(item);
    }

    /** types text for cboFromView
     * @param text String text
     */
    public void typeFromView(String text) {
        cboFromView().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** gets text for txtFromAction
     * @return String text
     */
    public String getFromAction() {
        return txtFromAction().getText();
    }

    /** sets text for txtFromAction
     * @param text String text
     */
    public void setFromAction(String text) {
        txtFromAction().setText(text);
    }

    /** types text for txtFromAction
     * @param text String text
     */
    public void typeFromAction(String text) {
        txtFromAction().typeText(text);
    }

    /** gets text for txtFromOutcome
     * @return String text
     */
    public String getFromOutcome() {
        return txtFromOutcome().getText();
    }

    /** sets text for txtFromOutcome
     * @param text String text
     */
    public void setFromOutcome(String text) {
        txtFromOutcome().setText(text);
    }

    /** types text for txtFromOutcome
     * @param text String text
     */
    public void typeFromOutcome(String text) {
        txtFromOutcome().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse2() {
        btBrowse2().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRedirect(boolean state) {
        if (cbRedirect().isSelected()!=state) {
            cbRedirect().push();
        }
    }

    /** gets text for txtRuleDescription
     * @return String text
     */
    public String getRuleDescription() {
        return txtRuleDescription().getText();
    }

    /** sets text for txtRuleDescription
     * @param text String text
     */
    public void setRuleDescription(String text) {
        txtRuleDescription().setText(text);
    }

    /** types text for txtRuleDescription
     * @param text String text
     */
    public void typeRuleDescription(String text) {
        txtRuleDescription().typeText(text);
    }

    /** returns selected item for cboToView
     * @return String item
     */
    public String getSelectedToView() {
        return cboToView().getSelectedItem().toString();
    }

    /** selects item for cboToView
     * @param item String item
     */
    public void selectToView(String item) {
        cboToView().selectItem(item);
    }

    /** types text for cboToView
     * @param text String text
     */
    public void typeToView(String text) {
        cboToView().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AddNavigationCase by accessing all its components.
     */
    public void verify() {
        lblFromView();
        cboFromView();
        btBrowse();
        lblFromAction();
        txtFromAction();
        lblFromOutcome();
        txtFromOutcome();
        lblToView();
        btBrowse2();
        cbRedirect();
        lblRuleDescription();
        txtRuleDescription();
        cboToView();
        btAdd();
        btCancel();
        btHelp();
    }
}

