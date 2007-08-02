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
package org.netbeans.jellytools;

import javax.swing.JComboBox;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import javax.swing.JTextField;

/**
 * Handle "Name And Location" panel of the New Web Project wizard.
 * Components on the panel differs according to type of project selected.<br><br>
 * <u>Web Application</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>ComboBox SourceStructure: <code>cbSourceStructure().selectItem("item")</code>
 * <li>ComboBox Server: <code>cbSourceStructure().selectItem("item")</code>
 * <li>ComboBox J2EE Version: <code>cbJ2EEVersion().selectItem("item")</code>
 * <li>Label and TextField Context Path: <code>txtContextPath().getText()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * </ol>
 * <u>Web Project with Existing Sources</u><br>
 * <ol>
 * <li>Label and TextField Location: <code>txtProjectLocation().setText()</code>
 * <li>Button for browsing Location: <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Button for browsing Project Folder: <code>btBrowseFolder().pushNoBlock()</code>
 * <ol>
 * @author ms113234
 */
public class NewWebProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //Web Application
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JButtonOperator     _btBrowseProjectLocation;
    private JLabelOperator      _lblLocation;
    private JTextFieldOperator  _txtLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JLabelOperator      _lblSourceStructure;
    private JComboBoxOperator   _cbSourceStructure;
    private JLabelOperator      _lblServer;
    private JComboBoxOperator   _cbServer;
    private JLabelOperator      _lblJ2EEVersion;
    private JComboBoxOperator   _cbJ2EEVersion;
    private JLabelOperator      _lblContextPath;
    private JTextFieldOperator  _txtContextPath;
    private JCheckBoxOperator   _cbSetAsMainProject;
    //Web Project With Existing Sources
    private JButtonOperator     _btBrowseFolder;
    
    
    /** Returns operator for label Project Name
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if(_lblProjectName == null) {
            _lblProjectName = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_ProjectName_Label"));
        }
        return _lblProjectName;
    }
    
    
    /** Returns operator of project name textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectName() {
        if(_txtProjectName == null) {
            if ( lblProjectName().getLabelFor()!=null ) {
                _txtProjectName = new JTextFieldOperator(
                        (JTextField)lblProjectName().getLabelFor());
            }
        }
        return _txtProjectName;
    }
    
    
    /** Sets given name in text field Project Name.
     * @param name project name
     */
    public void setProjectName(String name) {
        txtProjectName().setText(name);
    }
    
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_ProjectLocation_Label"));
        }
        return _lblProjectLocation;
    }
    
    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectLocation() {
        if(_txtProjectLocation == null) {
            if (lblProjectLocation().getLabelFor()!=null) {
                _txtProjectLocation = new JTextFieldOperator(
                        (JTextField)lblProjectLocation().getLabelFor());
            }
        }
        return _txtProjectLocation;
    }
    
    /** Sets given project location
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectLocation() {
        if ( _btBrowseProjectLocation==null ) {
            _btBrowseProjectLocation = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseProjectLocation;
    }
    
    /** Sets given location in text field Project Location.
     * @param location project location
     */
    public void setProjectLocation(String location) {
        txtProjectLocation().setText(location);
    }
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
            _lblLocation = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_IW_LocationSrc_Label"));
        }
        return _lblLocation;
    }
    
    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtLocation() {
        if(_txtLocation == null) {
            if (lblLocation().getLabelFor()!=null) {
                _txtLocation = new JTextFieldOperator(
                        (JTextField)lblLocation().getLabelFor());
            }
        }
        return _txtLocation;
    }
    
    
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocation() {
        if ( _btBrowseProjectLocation==null ) {
            _btBrowseProjectLocation = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseProjectLocation;
    }
    
    /** Sets given location in text field Location.
     * @param location Project With Existing Sources location
     */
    public void setLocation(String location) {
        txtLocation().setText(location);
    }
    
    /** Returns operator for label Project Folder
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectFolder() {
        if(_lblProjectFolder == null) {
            _lblProjectFolder = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_CreatedProjectFolder_Label"));
        }
        return _lblProjectFolder;
    }
    
    
    /** Returns operator of project folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectFolder() {
        if(_txtProjectFolder == null) {
            if ( lblProjectFolder().getLabelFor()!=null ) {
                _txtProjectFolder = new JTextFieldOperator(
                        (JTextField)lblProjectFolder().getLabelFor());
            }
        }
        return _txtProjectFolder;
    }
    
    /** Returns operator for label Source Structure:
     * @return JLabelOperator
     */
    public JLabelOperator lblSourceStructure() {
        if(_lblSourceStructure == null) {
            _lblSourceStructure = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_SourceStructure_Label"));
        }
        return _lblSourceStructure;
    }
    
    /** Returns operator of Source Structure combo box
     *  @return JComboBoxOperator
     */
    public JComboBoxOperator cbSourceStructure() {
        if ( _cbSourceStructure==null ) {
            if (lblSourceStructure().getLabelFor()!=null ) {
                _cbSourceStructure = new JComboBoxOperator(
                        (JComboBox)lblSourceStructure().getLabelFor());
            }
        }
        return _cbSourceStructure;
    }
    
    /** Selects structure in Source Structure combo box
     */
    public void selectSourceStructure(String structure) {
        cbSourceStructure().selectItem(structure);
    }
    
    /** Returns operator of J2EE Specification Level combo box
     *  @return JComboBoxOperator
     */
    public JComboBoxOperator cbServer() { //TODO fix using labelFor - #55369
        if ( _cbServer==null ) {
            _cbServer = new JComboBoxOperator(this, 3);
        }
        return _cbServer;
    }
    
    /** Selects level in J2EE Specification Level combo box
     */
    public void selectServer(String server) {
        cbServer().selectItem(server);
    }
    
    /** Returns operator for label J2EE Version:
     * @return JLabelOperator
     */
    public JLabelOperator lblJ2EEVersion() {
        if(_lblSourceStructure == null) {
            _lblSourceStructure = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_J2EESpecLevel_Label"));
        }
        return _lblSourceStructure;
    }
    
    /** Returns operator of J2EE version combo box
     *  @return JComboBoxOperator
     */
    public JComboBoxOperator cbJ2EEVersion() {
        if ( _cbJ2EEVersion==null ) {
            if (lblJ2EEVersion().getLabelFor()!=null) {
                _cbJ2EEVersion = new JComboBoxOperator(
                        (JComboBox)lblJ2EEVersion().getLabelFor());
            }
        }
        return _cbJ2EEVersion;
    }
    
    /** Selects structure in J2EE version combo box
     */
    public void selectJ2EEVersion(String version) {
        cbJ2EEVersion().selectItem(version);
    }
    
    /** Returns operator for label ContextPath:
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if(_lblContextPath == null) {
            _lblContextPath = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_ContextPath_Label"));
        }
        return _lblContextPath;
    }
    
    /** Returns operator of Context Path: text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (lblContextPath().getLabelFor()!=null) {
            _txtContextPath = new JTextFieldOperator(
                    (JTextField)lblContextPath().getLabelFor());
        }
        return _txtContextPath;
    }
    
    
    /** Returns operator for checkbox 'Set as Main Project'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetAsMainProject() {
        if ( _cbSetAsMainProject==null ) {
            _cbSetAsMainProject = new JCheckBoxOperator(this,
                    Bundle.getString(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_SetAsMain_CheckBox"));
        }
        return _cbSetAsMainProject;
    }
    
    
    /** Returns operator for browse project folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseFolder() {
        if ( _btBrowseFolder==null ) {
            _btBrowseFolder = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_BrowseLocation_Button"), 1);
        }
        return _btBrowseFolder;
    }
    
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        /*
        lblProjectName();
        txtProjectName();
        lblProjectLocation();
        txtProjectLocation();
        lblProjectFolder();
        txtProjectFolder();
        btBrowseLocation();
        lblSourceStructure();
        cbSourceStructure();
        lblServer();
        cbServer();
        lblJ2EEVersion();
        cbJ2EEVersion();
        lblContextPath();
        txtContextPath();
        cbSetAsMainProject();
        btBrowseFolder();
         */
    }
}