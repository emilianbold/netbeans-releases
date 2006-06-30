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
 * TestTypeAdvancedSettingsStepOperator.java
 *
 * Created on 7/19/02 11:30 AM
 */
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Wizard - Test Workspace" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class TestTypeAdvancedSettingsStepOperator extends NewWizardOperator {

    /** Creates new TestTypeAdvancedSettingsStepOperator that can handle it.
     */
    public TestTypeAdvancedSettingsStepOperator() {
        stepsWaitSelectedValue("Test Type Advanced Settings");
    }

    private JLabelOperator _lblTitle;
    private JLabelOperator _lblCompExclude;
    private JTextFieldOperator _txtCompExclude;
    private JLabelOperator _lblCompClassPath;
    private JTextFieldOperator _txtCompClassPath;
    private JButtonOperator _btCustomizeCompClassPath;
    private JLabelOperator _lblExecExtraJARs;
    private JTextFieldOperator _txtExecExtraJARs;
    private JButtonOperator _btCustomizeExecExtraJARs;
    private JLabelOperator _lblCMDSuffix;
    private JTextFieldOperator _txtCMDSuffix;
    private JLabelOperator _lblJemmyHome;
    private JTextFieldOperator _txtJemmyHome;
    private JButtonOperator _btCustomizeJemmyHome;
    private JLabelOperator _lblJellyHome;
    private JTextFieldOperator _txtJellyHome;
    private JButtonOperator _btCustomizeJellyHome;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Test Type Advanced Settings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTitle() {
        if (_lblTitle==null) {
            _lblTitle = new JLabelOperator(this, "Test Type Advanced Settings");
        }
        return _lblTitle;
    }

    /** Tries to find "Compilation Exclude Pattern: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCompExclude() {
        if (_lblCompExclude==null) {
            _lblCompExclude = new JLabelOperator(this, "Compilation Exclude Pattern: ");
        }
        return _lblCompExclude;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCompExclude() {
        if (_txtCompExclude==null) {
            _txtCompExclude = new JTextFieldOperator(this);
        }
        return _txtCompExclude;
    }

    /** Tries to find "Compliation Class Path: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCompClassPath() {
        if (_lblCompClassPath==null) {
            _lblCompClassPath = new JLabelOperator(this, "Compliation Class Path: ");
        }
        return _lblCompClassPath;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCompClassPath() {
        if (_txtCompClassPath==null) {
            _txtCompClassPath = new JTextFieldOperator(this, 1);
        }
        return _txtCompClassPath;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeCompClassPath() {
        if (_btCustomizeCompClassPath==null) {
            _btCustomizeCompClassPath = new JButtonOperator(this, "...");
        }
        return _btCustomizeCompClassPath;
    }

    /** Tries to find "Execution Extra JARs: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblExecExtraJARs() {
        if (_lblExecExtraJARs==null) {
            _lblExecExtraJARs = new JLabelOperator(this, "Execution Extra JARs: ");
        }
        return _lblExecExtraJARs;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtExecExtraJARs() {
        if (_txtExecExtraJARs==null) {
            _txtExecExtraJARs = new JTextFieldOperator(this, 2);
        }
        return _txtExecExtraJARs;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeExecExtraJARs() {
        if (_btCustomizeExecExtraJARs==null) {
            _btCustomizeExecExtraJARs = new JButtonOperator(this, "...", 1);
        }
        return _btCustomizeExecExtraJARs;
    }

    /** Tries to find "Command Line Suffix: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCMDSuffix() {
        if (_lblCMDSuffix==null) {
            _lblCMDSuffix = new JLabelOperator(this, "Command Line Suffix: ");
        }
        return _lblCMDSuffix;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCMDSuffix() {
        if (_txtCMDSuffix==null) {
            _txtCMDSuffix = new JTextFieldOperator(this, 3);
        }
        return _txtCMDSuffix;
    }

    /** Tries to find "Jemmy JAR Home: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJemmyHome() {
        if (_lblJemmyHome==null) {
            _lblJemmyHome = new JLabelOperator(this, "Jemmy JAR Home: ");
        }
        return _lblJemmyHome;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJemmyHome() {
        if (_txtJemmyHome==null) {
            _txtJemmyHome = new JTextFieldOperator(this, 4);
        }
        return _txtJemmyHome;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeJemmyHome() {
        if (_btCustomizeJemmyHome==null) {
            _btCustomizeJemmyHome = new JButtonOperator(this, "...", 2);
        }
        return _btCustomizeJemmyHome;
    }

    /** Tries to find "Jelly JAR Home: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJellyHome() {
        if (_lblJellyHome==null) {
            _lblJellyHome = new JLabelOperator(this, "Jelly JAR Home: ");
        }
        return _lblJellyHome;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJellyHome() {
        if (_txtJellyHome==null) {
            _txtJellyHome = new JTextFieldOperator(this, 5);
        }
        return _txtJellyHome;
    }

    /** Tries to find "..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCustomizeJellyHome() {
        if (_btCustomizeJellyHome==null) {
            _btCustomizeJellyHome = new JButtonOperator(this, "...", 3);
        }
        return _btCustomizeJellyHome;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtCompExclude
     * @return String text
     */
    public String getCompExclude() {
        return txtCompExclude().getText();
    }

    /** sets text for txtCompExclude
     * @param text String text
     */
    public void setCompExclude(String text) {
        txtCompExclude().setText(text);
    }

    /** types text for txtCompExclude
     * @param text String text
     */
    public void typeCompExclude(String text) {
        txtCompExclude().typeText(text);
    }

    /** gets text for txtCompClassPath
     * @return String text
     */
    public String getCompClassPath() {
        return txtCompClassPath().getText();
    }

    /** sets text for txtCompClassPath
     * @param text String text
     */
    public void setCompClassPath(String text) {
        txtCompClassPath().setText(text);
    }

    /** types text for txtCompClassPath
     * @param text String text
     */
    public void typeCompClassPath(String text) {
        txtCompClassPath().typeText(text);
    }

    /** clicks on "..." JButton
     */
    public void customizeCompClassPath() {
        btCustomizeCompClassPath().push();
    }

    /** gets text for txtExecExtraJARs
     * @return String text
     */
    public String getExecExtraJARs() {
        return txtExecExtraJARs().getText();
    }

    /** sets text for txtExecExtraJARs
     * @param text String text
     */
    public void setExecExtraJARs(String text) {
        txtExecExtraJARs().setText(text);
    }

    /** types text for txtExecExtraJARs
     * @param text String text
     */
    public void typeExecExtraJARs(String text) {
        txtExecExtraJARs().typeText(text);
    }

    /** clicks on "..." JButton
     */
    public void customizeExecExtraJARs() {
        btCustomizeExecExtraJARs().push();
    }

    /** gets text for txtCMDSuffix
     * @return String text
     */
    public String getCMDSuffix() {
        return txtCMDSuffix().getText();
    }

    /** sets text for txtCMDSuffix
     * @param text String text
     */
    public void setCMDSuffix(String text) {
        txtCMDSuffix().setText(text);
    }

    /** types text for txtCMDSuffix
     * @param text String text
     */
    public void typeCMDSuffix(String text) {
        txtCMDSuffix().typeText(text);
    }

    /** gets text for txtJemmyHome
     * @return String text
     */
    public String getJemmyHome() {
        return txtJemmyHome().getText();
    }

    /** sets text for txtJemmyHome
     * @param text String text
     */
    public void setJemmyHome(String text) {
        txtJemmyHome().setText(text);
    }

    /** types text for txtJemmyHome
     * @param text String text
     */
    public void typeJemmyHome(String text) {
        txtJemmyHome().typeText(text);
    }

    /** clicks on "..." JButton
     */
    public void customizeJemmyHome() {
        btCustomizeJemmyHome().push();
    }

    /** gets text for txtJellyHome
     * @return String text
     */
    public String getJellyHome() {
        return txtJellyHome().getText();
    }

    /** sets text for txtJellyHome
     * @param text String text
     */
    public void setJellyHome(String text) {
        txtJellyHome().setText(text);
    }

    /** types text for txtJellyHome
     * @param text String text
     */
    public void typeJellyHome(String text) {
        txtJellyHome().typeText(text);
    }

    /** clicks on "..." JButton
     */
    public void customizeJellyHome() {
        btCustomizeJellyHome().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TestTypeAdvancedSettingsStepOperator by accessing all its components.
     */
    public void verify() {
        lblTitle();
        lblCompExclude();
        txtCompExclude();
        lblCompClassPath();
        txtCompClassPath();
        btCustomizeCompClassPath();
        lblExecExtraJARs();
        txtExecExtraJARs();
        btCustomizeExecExtraJARs();
        lblCMDSuffix();
        txtCMDSuffix();
        lblJemmyHome();
        txtJemmyHome();
        btCustomizeJemmyHome();
        lblJellyHome();
        txtJellyHome();
        btCustomizeJellyHome();
    }
}

