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
 * TestTypeTemplateStepOperator.java
 *
 * Created on 7/19/02 11:24 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestTypeTemplateStepOperator extends NewWizardOperator {

    /** Creates new TestTypeTemplateStepOperator that can handle it.
     */
    public TestTypeTemplateStepOperator() {
        stepsWaitSelectedValue("Test Type Name and Template");
    }

    private JLabelOperator _lblTitle;
    private JLabelOperator _lblName;
    private JTextFieldOperator _txtName;
    private JLabelOperator _lblTemplate;
    private JComboBoxOperator _cboTemplate;
    public static final String ITEM_QAFUNCTIONAL = "QA Functional Test Type"; 
    public static final String ITEM_UNIT = "Unit Test Type"; 
    private JLabelOperator _lblTemplateDescription;
    private JEditorPaneOperator _txtTemplateDescription;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Test Type Name and Template" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Test Type Name and Template");
        }
        return _lblTitle;
    }

    /** Tries to find "Name: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, "Name: ");
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

    /** Tries to find "Select a Template: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTemplate() {
        if (_lblTemplate==null) {
            _lblTemplate = new JLabelOperator(this, "Select a Template: ");
        }
        return _lblTemplate;
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

    /** Tries to find "Template Description: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTemplateDescription() {
        if (_lblTemplateDescription==null) {
            _lblTemplateDescription = new JLabelOperator(this, "Template Description: ");
        }
        return _lblTemplateDescription;
    }

    /** Tries to find null SwingBrowserImpl$SwingBrowser in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtTemplateDescription() {
        if (_txtTemplateDescription==null) {
            _txtTemplateDescription = new JEditorPaneOperator(this);
        }
        return _txtTemplateDescription;
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

    /** gets text for txtTemplateDescription
     * @return String text
     */
    public String getTemplateDescription() {
        return txtTemplateDescription().getText();
    }

    /** sets text for txtTemplateDescription
     * @param text String text
     */
    public void setTemplateDescription(String text) {
        txtTemplateDescription().setText(text);
    }

    /** types text for txtTemplateDescription
     * @param text String text
     */
    public void typeTemplateDescription(String text) {
        txtTemplateDescription().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestTypeTemplateStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        lblName();
        txtName();
        lblTemplate();
        cboTemplate();
        lblTemplateDescription();
        txtTemplateDescription();
    }
}

