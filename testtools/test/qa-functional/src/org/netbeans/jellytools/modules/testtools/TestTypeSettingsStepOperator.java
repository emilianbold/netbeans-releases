/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.testtools;

/*
 * TestTypeSettingsStepOperator.java
 *
 * Created on 7/19/02 11:28 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestTypeSettingsStepOperator extends NewWizardOperator {

    /** Creates new TestTypeSettingsStepOperator that can handle it.
     */
    public TestTypeSettingsStepOperator() {
        stepsWaitSelectedValue("Test Type Settings");
    }

    private JLabelOperator _lblTitle;
    private JCheckBoxOperator _cbSetDefault;
    private JLabelOperator _lblWindowsSystem;
    private JRadioButtonOperator _rbSDI;
    private JRadioButtonOperator _rbMDI;
    private JCheckBoxOperator _cbUseJemmy;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Test Type Settings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Test Type Settings");
        }
        return _lblTitle;
    }

    /** Tries to find "Set Test Type as default in Test Workspace" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetDefault() {
        if (_cbSetDefault==null) {
            _cbSetDefault = new JCheckBoxOperator(this, "Set Test Type as default in Test Workspace");
        }
        return _cbSetDefault;
    }

    /** Tries to find "Windows System: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWindowsSystem() {
        if (_lblWindowsSystem==null) {
            _lblWindowsSystem = new JLabelOperator(this, "Windows System: ");
        }
        return _lblWindowsSystem;
    }

    /** Tries to find "SDI (Multiple Smaller Windows Mode)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSDI() {
        if (_rbSDI==null) {
            _rbSDI = new JRadioButtonOperator(this, "SDI (Multiple Smaller Windows Mode)");
        }
        return _rbSDI;
    }

    /** Tries to find "MDI (Full Screen Mode)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbMDI() {
        if (_rbMDI==null) {
            _rbMDI = new JRadioButtonOperator(this, "MDI (Full Screen Mode)");
        }
        return _rbMDI;
    }

    /** Tries to find "Use Jemmy and Jelly libraries" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbUseJemmy() {
        if (_cbUseJemmy==null) {
            _cbUseJemmy = new JCheckBoxOperator(this, "Use Jemmy and Jelly libraries");
        }
        return _cbUseJemmy;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSetDefault(boolean state) {
        if (cbSetDefault().isSelected()!=state) {
            cbSetDefault().push();
        }
    }

    /** clicks on "SDI (Multiple Smaller Windows Mode)" JRadioButton
     */
    public void setSDI() {
        rbSDI().push();
    }

    /** clicks on "MDI (Full Screen Mode)" JRadioButton
     */
    public void setMDI() {
        rbMDI().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkUseJemmy(boolean state) {
        if (cbUseJemmy().isSelected()!=state) {
            cbUseJemmy().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestTypeSettingsStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        cbSetDefault();
        lblWindowsSystem();
        rbSDI();
        rbMDI();
        cbUseJemmy();
    }
}

