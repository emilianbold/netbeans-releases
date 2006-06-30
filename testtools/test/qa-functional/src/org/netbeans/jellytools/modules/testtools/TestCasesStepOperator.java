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
 * TestCasesStepOperator.java
 *
 * Created on 7/19/02 11:35 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestCasesStepOperator extends NewWizardOperator {

    /** Creates new TestCasesStepOperator that can handle it.
     */
    public TestCasesStepOperator() {
        stepsWaitSelectedValue("Create Test Cases");
    }

    private JLabelOperator _lblTitle;
    private JComboBoxOperator _cboTemplate;
    public static final String ITEM_SIMPLETESTCASE = "testSimpleTestCase"; 
    public static final String ITEM_GOLDENTESTCASE = "testGoldenTestCase"; 
    private JTextFieldOperator _txtName;
    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btUp;
    private JButtonOperator _btDown;
    private JListOperator _lstTestCasesList;
    private JLabelOperator _lblName;
    private JLabelOperator _lblTemplate;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Create Test Cases" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Create Test Cases");
        }
        return _lblTitle;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTemplate() {
        if (_cboTemplate==null) {
            _cboTemplate = new JComboBoxOperator(this);
        }
        return _cboTemplate;
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

    /** Tries to find "Add" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, "Add");
        }
        return _btAdd;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, "Remove");
        }
        return _btRemove;
    }

    /** Tries to find "Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btUp() {
        if (_btUp==null) {
            _btUp = new JButtonOperator(this, "Up");
        }
        return _btUp;
    }

    /** Tries to find "Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btDown() {
        if (_btDown==null) {
            _btDown = new JButtonOperator(this, "Down");
        }
        return _btDown;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstTestCasesList() {
        if (_lstTestCasesList==null) {
            _lstTestCasesList = new JListOperator(this, 1);
        }
        return _lstTestCasesList;
    }

    /** Tries to find "Test Case Name: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, "Test Case Name: ");
        }
        return _lblName;
    }

    /** Tries to find "Template: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTemplate() {
        if (_lblTemplate==null) {
            _lblTemplate = new JLabelOperator(this, "Template: ");
        }
        return _lblTemplate;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboTemplate
     * @return String item
     */
    public String getSelectedTemplate() {
        return cboTemplate().getSelectedItem().toString();
    }

    /** selects item for cboTemplate
     * @param item String item
     */
    public void selectTemplate(String item) {
        cboTemplate().selectItem(item);
    }

    /** types text for cboTemplate
     * @param text String text
     */
    public void typeTemplate(String text) {
        cboTemplate().typeText(text);
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

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }

    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }

    /** clicks on "Up" JButton
     */
    public void up() {
        btUp().push();
    }

    /** clicks on "Down" JButton
     */
    public void down() {
        btDown().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestCasesStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        cboTemplate();
        txtName();
        btAdd();
        btRemove();
        btUp();
        btDown();
        lstTestCasesList();
        lblName();
        lblTemplate();
    }
}

