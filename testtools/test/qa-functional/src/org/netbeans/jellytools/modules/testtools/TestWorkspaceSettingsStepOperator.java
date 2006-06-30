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
 * TestWorkspaceSettingsStepOperator.java
 *
 * Created on 7/19/02 11:22 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestWorkspaceSettingsStepOperator extends NewWizardOperator {

    /** Creates new TestWorkspaceSettingsStepOperator that can handle it.
     */
    public TestWorkspaceSettingsStepOperator() {
        stepsWaitSelectedValue("Test Workspace Settings");
    }

    private JLabelOperator _lblTitle;
    private JLabelOperator _lblLevel;
    private JComboBoxOperator _cboLevel;
    public static final String ITEM_TOPLEVEL = "On top of the module (repository / module)"; 
    public static final String ITEM_ONELEVELLOWER = "One level lower (repository / module / package)"; 
    public static final String ITEM_TWOLEVELSLOWER = "Two levels lower (repository / module / package / package)"; 
    public static final String ITEM_OUTOFCVS = "Out of CVS structute (for local use only)"; 
    private JLabelOperator _lblDefaultTestType;
    private JTextFieldOperator _txtDefaultTestType;
    private JLabelOperator _lblDefaultAttributes;
    private JTextFieldOperator _txtDefaultAttributes;
    private JCheckBoxOperator _cbAdvancedSettings;
    private JLabelOperator _lblNetbeansHome;
    private JTextFieldOperator _txtNetbeansHome;
    private JLabelOperator _lblXTestHome;
    private JTextFieldOperator _txtXTestHome;
    private JButtonOperator _btCustomizeNetbeansHome;
    private JButtonOperator _btCustomizeXTestHome;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Test Workspace Settings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Test Workspace Settings");
        }
        return _lblTitle;
    }

    /** Tries to find "Test Workspace possition in CVS: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLevel() {
        if (_lblLevel==null) {
            _lblLevel = new JLabelOperator(this, "Test Workspace possition in CVS: ");
        }
        return _lblLevel;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLevel() {
        if (_cboLevel==null) {
            _cboLevel = new JComboBoxOperator(this);
        }
        return _cboLevel;
    }

    /** Tries to find "Default Test Type: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDefaultTestType() {
        if (_lblDefaultTestType==null) {
            _lblDefaultTestType = new JLabelOperator(this, "Default Test Type: ");
        }
        return _lblDefaultTestType;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDefaultTestType() {
        if (_txtDefaultTestType==null) {
            _txtDefaultTestType = new JTextFieldOperator(this);
        }
        return _txtDefaultTestType;
    }

    /** Tries to find "Default Attributes: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDefaultAttributes() {
        if (_lblDefaultAttributes==null) {
            _lblDefaultAttributes = new JLabelOperator(this, "Default Attributes: ");
        }
        return _lblDefaultAttributes;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDefaultAttributes() {
        if (_txtDefaultAttributes==null) {
            _txtDefaultAttributes = new JTextFieldOperator(this, 1);
        }
        return _txtDefaultAttributes;
    }

    /** Tries to find "Advanced Settings" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbAdvancedSettings() {
        if (_cbAdvancedSettings==null) {
            _cbAdvancedSettings = new JCheckBoxOperator(this, "Advanced Settings");
        }
        return _cbAdvancedSettings;
    }

    /** Tries to find "Select Netbeans Home Directory (different than current)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNetbeansHome() {
        if (_lblNetbeansHome==null) {
            _lblNetbeansHome = new JLabelOperator(this, "Select Netbeans Home Directory (different than current)");
        }
        return _lblNetbeansHome;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtNetbeansHome() {
        if (_txtNetbeansHome==null) {
            _txtNetbeansHome = new JTextFieldOperator(this, 2);
        }
        return _txtNetbeansHome;
    }

    /** Tries to find "Select XTest Home Directory" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblXTestHome() {
        if (_lblXTestHome==null) {
            _lblXTestHome = new JLabelOperator(this, "Select XTest Home Directory");
        }
        return _lblXTestHome;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtXTestHome() {
        if (_txtXTestHome==null) {
            _txtXTestHome = new JTextFieldOperator(this, 3);
        }
        return _txtXTestHome;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeNetbeansHome() {
        if (_btCustomizeNetbeansHome==null) {
            _btCustomizeNetbeansHome = new JButtonOperator(this, "...");
        }
        return _btCustomizeNetbeansHome;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeXTestHome() {
        if (_btCustomizeXTestHome==null) {
            _btCustomizeXTestHome = new JButtonOperator(this, "...", 1);
        }
        return _btCustomizeXTestHome;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboLevel
     * @return String item
     */
    public String getSelectedLevel() {
        return cboLevel().getSelectedItem().toString();
    }

    /** selects item for cboLevel
     * @param item String item
     */
    public void selectLevel(String item) {
        cboLevel().selectItem(item);
    }

    /** types text for cboLevel
     * @param text String text
     */
    public void typeLevel(String text) {
        cboLevel().typeText(text);
    }

    /** gets text for txtDefaultTestType
     * @return String text
     */
    public String getDefaultTestType() {
        return txtDefaultTestType().getText();
    }

    /** sets text for txtDefaultTestType
     * @param text String text
     */
    public void setDefaultTestType(String text) {
        txtDefaultTestType().setText(text);
    }

    /** types text for txtDefaultTestType
     * @param text String text
     */
    public void typeDefaultTestType(String text) {
        txtDefaultTestType().typeText(text);
    }

    /** gets text for txtDefaultAttributes
     * @return String text
     */
    public String getDefaultAttributes() {
        return txtDefaultAttributes().getText();
    }

    /** sets text for txtDefaultAttributes
     * @param text String text
     */
    public void setDefaultAttributes(String text) {
        txtDefaultAttributes().setText(text);
    }

    /** types text for txtDefaultAttributes
     * @param text String text
     */
    public void typeDefaultAttributes(String text) {
        txtDefaultAttributes().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkAdvancedSettings(boolean state) {
        if (cbAdvancedSettings().isSelected()!=state) {
            cbAdvancedSettings().push();
        }
    }

    /** gets text for txtNetbeansHome
     * @return String text
     */
    public String getNetbeansHome() {
        return txtNetbeansHome().getText();
    }

    /** sets text for txtNetbeansHome
     * @param text String text
     */
    public void setNetbeansHome(String text) {
        txtNetbeansHome().setText(text);
    }

    /** types text for txtNetbeansHome
     * @param text String text
     */
    public void typeNetbeansHome(String text) {
        txtNetbeansHome().typeText(text);
    }

    /** gets text for txtXTestHome
     * @return String text
     */
    public String getXTestHome() {
        return txtXTestHome().getText();
    }

    /** sets text for txtXTestHome
     * @param text String text
     */
    public void setXTestHome(String text) {
        txtXTestHome().setText(text);
    }

    /** types text for txtXTestHome
     * @param text String text
     */
    public void typeXTestHome(String text) {
        txtXTestHome().typeText(text);
    }

    /** clicks on "..." JButton
     */
    public void customizeNetbeansHome() {
        btCustomizeNetbeansHome().push();
    }

    /** clicks on "..." JButton
     */
    public void customizeXTestHome() {
        btCustomizeXTestHome().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestWorkspaceSettingsStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        lblLevel();
        cboLevel();
        lblDefaultTestType();
        txtDefaultTestType();
        lblDefaultAttributes();
        txtDefaultAttributes();
        cbAdvancedSettings();
        lblNetbeansHome();
        txtNetbeansHome();
        lblXTestHome();
        txtXTestHome();
        btCustomizeNetbeansHome();
        btCustomizeXTestHome();
    }
}

