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
package org.netbeans.qa.form;

import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Bind" NbDialog.
 * 
 * @author Jiri Vagner
 */
public class BindDialogOperator extends JDialogOperator {
    
    private JButtonOperator _btCancel;
    private JButtonOperator _btOk;    
    private JTabbedPaneOperator _tbdPane;
    private JComboBoxOperator _cboBindSource;
    private JComboBoxOperator _cboBindExpression;
    private JTextFieldOperator _txtBindExpression;

    /**
     * Creates new instance using default name
     */
    public BindDialogOperator() {
        super("Bind"); // NOI18N
    }
    
    /**
     * Creates new instance using dialog name
     */
    public BindDialogOperator(String name) {
        super(name);
    }
    

    /** Tries to find JTextFieldOperator in this dialog.
     * @return JTextFieldOperator
     */
    private JTextFieldOperator txtBindExpression() {
        if (_txtBindExpression==null) {
            _txtBindExpression = new JTextFieldOperator(cboBindExpression());
        }
        return _txtBindExpression;
    }
    
    /** Tries to find JComboBoxOperator in this dialog.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboBindSource() {
        if (_cboBindSource==null) {
            _cboBindSource = new JComboBoxOperator(tbdPane(),0);
        }
        return _cboBindSource;
    }

    /** Tries to find JComboBoxOperator in this dialog.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboBindExpression() {
        if (_cboBindExpression==null) {
            _cboBindExpression = new JComboBoxOperator(tbdPane(),1);
        }
        return _cboBindExpression;
    }
    
    /** Tries to find JTabbedPaneOperator in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbdPane() {
        if (_tbdPane==null) {
            _tbdPane = new JTabbedPaneOperator(this);
        }
        return _tbdPane;
    }

    /** Tries to find JButtonOperator in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }

    /** Tries to find JButtonOperator in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOk() {
        if (_btOk==null) {
            _btOk = new JButtonOperator(this, "Ok"); // NOI18N
        }
        return _btOk;
    }
    
    /** clicks Cancel button */
    public void cancel() {
        btCancel().push();
    }

    /** clicks OK button */
    public void ok() {
        btOk().push();
    }
    
    /** selects binding source */
    public void selectBindSource(String item) {
        cboBindSource().selectItem(item);
    }
    
    /** gets selected binding source
     * @return String text
     */    
    public String getSelectedBindSource() {
        return cboBindSource().getSelectedItem().toString();
    }
    
    /** sets binding expression */    
    public void setBindExpression(String text) {
        txtBindExpression().getFocus();
        txtBindExpression().clearText();
        txtBindExpression().typeText(text);
        btOk().getFocus();
    }
    
    /** returns binding expression
     * @return String text
     */
    public String getBindExpression() {
        return txtBindExpression().getText();
    }
}
