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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/** Class implementing all necessary methods for handling "Add Navigation Rule" NbDialog.
 *
 * @author luke
 */
public class AddNavigationRuleDialogOperator extends NbDialogOperator {

    /** Creates new AddNavigationRule that can handle it.
     */
    public AddNavigationRuleDialogOperator() {
        super("Add Navigation Rule");
    }

    private JLabelOperator _lblRuleFromView;
    private JTextFieldOperator _txtRuleFromView;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblRuleDescription;
    private JTextAreaOperator _txtRuleDescription;
    private JButtonOperator _btAdd;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Rule from View:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRuleFromView() {
        if (_lblRuleFromView==null) {
            _lblRuleFromView = new JLabelOperator(this, "Rule from View:");
        }
        return _lblRuleFromView;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRuleFromView() {
        if (_txtRuleFromView==null) {
            _txtRuleFromView = new JTextFieldOperator(this);
        }
        return _txtRuleFromView;
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

    /** gets text for txtRuleFromView
     * @return String text
     */
    public String getRuleFromView() {
        return txtRuleFromView().getText();
    }

    /** sets text for txtRuleFromView
     * @param text String text
     */
    public void setRuleFromView(String text) {
        txtRuleFromView().setText(text);
    }

    /** types text for txtRuleFromView
     * @param text String text
     */
    public void typeRuleFromView(String text) {
        txtRuleFromView().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
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

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AddNavigationRule by accessing all its components.
     */
    public void verify() {
        lblRuleFromView();
        txtRuleFromView();
        btBrowse();
        lblRuleDescription();
        txtRuleDescription();
        btAdd();
        btCancel();
        btHelp();
    }
}

