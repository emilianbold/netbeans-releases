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

package org.netbeans.jellytools.modules.testtools;

/*
 * TestBagSettingsStepOperator.java
 *
 * Created on 7/19/02 11:32 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestBagSettingsStepOperator extends NewWizardOperator {

    /** Creates new TestBagSettingsStepOperator that can handle it.
     */
    public TestBagSettingsStepOperator() {
        stepsWaitSelectedValue("Test Bag Settings");
    }

    private JLabelOperator _lblTitle;
    private JLabelOperator _lblName;
    private JTextFieldOperator _txtName;
    private JLabelOperator _lblExecutor;
    private JRadioButtonOperator _rbIDE;
    private JRadioButtonOperator _rbCode;
    private JLabelOperator _lblAttributes;
    private JTextFieldOperator _txtAttributes;
    private JLabelOperator _lblExecIncludePattern;
    private JTextFieldOperator _txtExecIncludePattern;
    private JLabelOperator _lblExecExcludePattern;
    private JTextFieldOperator _txtExecExcludePattern;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Test Bag Settings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Test Bag Settings");
        }
        return _lblTitle;
    }

    /** Tries to find "Test Bag Name: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, "Test Bag Name: ");
        }
        return _lblName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(this);
        }
        return _txtName;
    }

    /** Tries to find "Executor: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblExecutor() {
        if (_lblExecutor==null) {
            _lblExecutor = new JLabelOperator(this, "Executor: ");
        }
        return _lblExecutor;
    }

    /** Tries to find "IDE" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbIDE() {
        if (_rbIDE==null) {
            _rbIDE = new JRadioButtonOperator(this, "IDE");
        }
        return _rbIDE;
    }

    /** Tries to find "Code" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbCode() {
        if (_rbCode==null) {
            _rbCode = new JRadioButtonOperator(this, "Code");
        }
        return _rbCode;
    }

    /** Tries to find "Attributes: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAttributes() {
        if (_lblAttributes==null) {
            _lblAttributes = new JLabelOperator(this, "Attributes: ");
        }
        return _lblAttributes;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtAttributes() {
        if (_txtAttributes==null) {
            _txtAttributes = new JTextFieldOperator(this, 1);
        }
        return _txtAttributes;
    }

    /** Tries to find "Execution Include Pattern: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblExecIncludePattern() {
        if (_lblExecIncludePattern==null) {
            _lblExecIncludePattern = new JLabelOperator(this, "Execution Include Pattern: ");
        }
        return _lblExecIncludePattern;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtExecIncludePattern() {
        if (_txtExecIncludePattern==null) {
            _txtExecIncludePattern = new JTextFieldOperator(this, 2);
        }
        return _txtExecIncludePattern;
    }

    /** Tries to find "Execution Exclude Pattern: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblExecExcludePattern() {
        if (_lblExecExcludePattern==null) {
            _lblExecExcludePattern = new JLabelOperator(this, "Execution Exclude Pattern: ");
        }
        return _lblExecExcludePattern;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtExecExcludePattern() {
        if (_txtExecExcludePattern==null) {
            _txtExecExcludePattern = new JTextFieldOperator(this, 3);
        }
        return _txtExecExcludePattern;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

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

    /** clicks on "IDE" JRadioButton
     */
    public void setIDE() {
        rbIDE().push();
    }

    /** clicks on "Code" JRadioButton
     */
    public void setCode() {
        rbCode().push();
    }

    /** gets text for txtAttributes
     * @return String text
     */
    public String getAttributes() {
        return txtAttributes().getText();
    }

    /** sets text for txtAttributes
     * @param text String text
     */
    public void setAttributes(String text) {
        txtAttributes().setText(text);
    }

    /** types text for txtAttributes
     * @param text String text
     */
    public void typeAttributes(String text) {
        txtAttributes().typeText(text);
    }

    /** gets text for txtExecIncludePattern
     * @return String text
     */
    public String getExecIncludePattern() {
        return txtExecIncludePattern().getText();
    }

    /** sets text for txtExecIncludePattern
     * @param text String text
     */
    public void setExecIncludePattern(String text) {
        txtExecIncludePattern().setText(text);
    }

    /** types text for txtExecIncludePattern
     * @param text String text
     */
    public void typeExecIncludePattern(String text) {
        txtExecIncludePattern().typeText(text);
    }

    /** gets text for txtExecExcludePattern
     * @return String text
     */
    public String getExecExcludePattern() {
        return txtExecExcludePattern().getText();
    }

    /** sets text for txtExecExcludePattern
     * @param text String text
     */
    public void setExecExcludePattern(String text) {
        txtExecExcludePattern().setText(text);
    }

    /** types text for txtExecExcludePattern
     * @param text String text
     */
    public void typeExecExcludePattern(String text) {
        txtExecExcludePattern().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestBagSettingsStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        lblName();
        txtName();
        lblExecutor();
        rbIDE();
        rbCode();
        lblAttributes();
        txtAttributes();
        lblExecIncludePattern();
        txtExecIncludePattern();
        lblExecExcludePattern();
        txtExecExcludePattern();
    }
}

