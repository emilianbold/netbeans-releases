/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import javax.swing.JDialog;
import javax.swing.JTextField;

/**
 * Handle "Name And Location" panel of the New Project wizard.
 * Componentson the panel fiffers according to type of project selected.<br><br>
 * <u>Java Application</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseProjectLocation().push()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * <li>CheckBox Create Main Class: <code>cbCreateMainClass().setSelected(true)</code>
 * </ol>
 * <u>Java Class Library</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseProjectLocation().push()</code>
 * </ol>
 * <u>Java Project With Existing Ant script</u><br>
 * <ol>
 * <li>Label and TextField Location: <code>txtLocation().setText()</code>
 * <li>Label and TextField Build Script: <code>txtBuildScript().setText()</code>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().setText()</code>
 * <li>Button Browse... for browsing Location <code>btBrowseLocation().push()</code>
 * <li>Button Browse... for browsing Build Script <code>btBrowseBuildScript().push()</code>
 * <li>Button Browse... for browsing Project Folder <code>btBrowseProjectFolder().push()</code>
 * <li>CheckBox Set as Main Project <code>cbSetAsMainProject().setSelected(true)</code> 
 * </ol>
 * <u>Java project With Existing Sources</u><br>
 * <ol>
 * <li>Label and TextField Source Packages Folder: <code>txtSourcePackagesFolder().setText()</code>
 * <li>Label and TextField Test Packages Folder: <code>txtTestPackagesFolder().setText()</code>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Button for browsing Project location: <code>btBrowseLocation().push()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Source packages folder: <code>btBrowseSourcePackagesFolder().push()</code>
 * <li>Button for browsing Test packages folder: <code>btBrowseTestPackagesFolder().push()</code>
 * </ol>
 * <u>Web Application</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseProjectLocation().push()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * <li>Label and TextField Context Path: <code>txtContextPath().getText()</code>
 * <li>ComboBox J2EE Specification level <code>cbJ2EESpecificationLevel().selectItem("item")</code>
 * <li>EditorPanel Description: <code>txtDescription().getText()</code>
 * </ol>
 * 
 */
public class NewProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //Java Class Library
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JButtonOperator     _btBrowseLocation;
    //Java Application
    private JCheckBoxOperator   _cbSetAsMainProject;
    private JCheckBoxOperator   _cbCreateMainClass;
    private JTextFieldOperator  _txtCreateMainClass;
    //Java Project With Existing Ant script
    private JLabelOperator      _lblLocation;
    private JTextFieldOperator  _txtLocation;
    private JLabelOperator      _lblBuildScript;
    private JTextFieldOperator  _txtBuildScript;
    private JButtonOperator     _btBrowseBuildScript;
    private JButtonOperator     _btBrowseProjectFolder;
    //Java Project with Existing Sources
    private JLabelOperator      _lblSourcePackagesFolder;
    private JTextFieldOperator  _txtSourcePackagesFolder;
    private JLabelOperator      _lblTestPackagesFolder;
    private JTextFieldOperator  _txtTestPackagesFolder;
    private JButtonOperator     _btBrowseTestPackagesFolder;
    private JButtonOperator     _btBrowseSourcePackagesFolder;
    //Web Application
    private JLabelOperator      _lblContextPath;
    private JTextFieldOperator  _txtContextPath;
    private JLabelOperator      _lblOnlineError;
    private JComboBoxOperator   _cbJ2EESpecificationLevel;
    private JEditorPaneOperator _txtDescription;
    
    
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                                            "LBL_NWP1_ProjectLocation_Label"));
        }
        return _lblProjectLocation;
    }

    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectLocation() {
        if(_txtProjectLocation == null) {
            if ( lblProjectLocation().getLabelFor()!=null ) {
                _txtProjectLocation = new JTextFieldOperator((JTextField)lblProjectLocation().getLabelFor());
            }
        }
        return _txtProjectLocation;
    }
    
    
    /** Returns operator for label Project Name
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if(_lblProjectName == null) {
            _lblProjectName = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
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
                _txtProjectName = new JTextFieldOperator((JTextField)lblProjectName().getLabelFor());
            }
        }
        return _txtProjectName;
    }
    
    
    /** Returns operator for label Project Folder
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectFolder() {
        if(_lblProjectFolder == null) {
            _lblProjectFolder = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                                            "LBL_NWP1_CreatedProjectFolder_Lablel"));
        }
        return _lblProjectFolder;
    }

    
    /** Returns operator of project folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectFolder() {
        if(_txtProjectFolder == null) {
            if ( lblProjectFolder().getLabelFor()!=null ) {
                _txtProjectFolder = new JTextFieldOperator((JTextField)lblProjectFolder().getLabelFor());
            }    
        }
        return _txtProjectFolder;
    }
    
    
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectLocation() {
        if ( _btBrowseLocation==null ) {
             _btBrowseLocation = new JButtonOperator(this,Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                        "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseLocation;
    }
    
    
    /** Returns operator for browse location button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocation() {
        if ( _btBrowseLocation==null ) {
            _btBrowseLocation = new JButtonOperator(
                                        this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                                "BTN_BasicProjectInfoPanel_browseProjectLocation"), 
                                        2);
        }
        return _btBrowseLocation;
    }
    
    
    /** Returns operator for checkbox 'Set as Main Project'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetAsMainProject() {
        if ( _cbSetAsMainProject==null ) {
            _cbSetAsMainProject = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                                                "LBL_setAsMainCheckBox"));
        }
        return _cbSetAsMainProject;    
    }
    
    
    /** Returns operator for checkbox 'Create Main Class'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCreateMainClass() {
        if ( _cbCreateMainClass==null ) {
            _cbCreateMainClass = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                                                "LBL_createMainCheckBox"));
        }
        return _cbCreateMainClass;    
    }
    
    
    /** Returns operator for text field 'Create Main Class'
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreateMainClass() {
        if ( _txtCreateMainClass==null ) {
            _txtCreateMainClass = new JTextFieldOperator(this, 3);
        }
        return _txtCreateMainClass;
    }
    
    
    /** Returns operator for label 'Location:' in Java Project with existing 
     * Ant script wizard.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
           _lblLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                            "LBL_BasicProjectInfoPanel_jLabel6"));
        }
        return _lblLocation;
    }
    
    /** Returns operator of Location: text field in Java Project with existing 
     * Ant script wizard.
     * @return JTextOperator
     */
    public JTextFieldOperator txtLocation() {
        if(_txtLocation == null) {
            if ( lblLocation().getLabelFor()!=null ) {
                _txtLocation = new JTextFieldOperator((JTextField)lblLocation().getLabelFor());
            }
        }
        return _txtLocation;
    }
    
    
    /** Returns operator for label 'Build Script:' in Java Project with existing 
     * Ant script wizard.
     * @return JLabelOperator
     */
    public JLabelOperator lblBuildScript() {
        if(_lblBuildScript == null) {
            _lblBuildScript = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                            "LBL_BasicProjectInfoPanel_jLabel2"));
        }
        return _lblBuildScript;
    }
    
    /** Returns operator of 'Build Script:' text field in Java Project with existing 
     * Ant script wizard.
     * @return JTextOperator
     */
    public JTextFieldOperator txtBuildScript() {
        if(_txtBuildScript == null) {
            if (  lblBuildScript().getLabelFor()!=null ) {
                _txtBuildScript = new JTextFieldOperator((JTextField)lblBuildScript().getLabelFor());
            }    
        }
        return _txtBuildScript;
    }
    
    
    /** Returns operator for browse Build Script button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseBuildScript() {
        if ( _btBrowseBuildScript==null ) {
            _btBrowseBuildScript = new JButtonOperator(
                                this,
                                Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                        "BTN_BasicProjectInfoPanel_browseAntScript"), 
                                0);
        }
        return _btBrowseBuildScript;
    }
    
    
    /** Returns operator for browse Project Folder button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectFolder() {
        if ( _btBrowseProjectFolder==null ) {
            _btBrowseProjectFolder = new JButtonOperator(
                                            this, 
                                            Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                                    "BTN_BasicProjectInfoPanel_browseProjectFolder"), 
                                            1);
        }
        return _btBrowseProjectFolder;
    }
    
    
    
    /** Returns operator for label SourcePackagesFolder:
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackagesFolder() {
        if(_lblSourcePackagesFolder == null) {
            _lblSourcePackagesFolder = new JLabelOperator(this,"Source Packages Folder:"); //TODO I18N
        }
        return _lblSourcePackagesFolder;
    }
    
    /** Returns operator of Source Packages Folder: text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtSourcePackagesFolder() {
        if (  lblSourcePackagesFolder().getLabelFor()!=null ) {
            _txtSourcePackagesFolder = new JTextFieldOperator((JTextField)lblSourcePackagesFolder().getLabelFor());
      }
      return _txtSourcePackagesFolder;
    }
    
    
    /** Returns operator for label TestPackagesFolder:
     * @return JLabelOperator
     */
    public JLabelOperator lblTestPackagesFolder() {
        if(_lblTestPackagesFolder == null) {
            _lblTestPackagesFolder = new JLabelOperator(this,"Test Packages Folder:"); //TODO I18N
        }
        return _lblTestPackagesFolder;
    }
    
    /** Returns operator of Test Packages Folder: text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtTestPackagesFolder() {
        if (  lblTestPackagesFolder().getLabelFor()!=null ) {
            _txtTestPackagesFolder = new JTextFieldOperator((JTextField)lblTestPackagesFolder().getLabelFor());
      }
      return _txtTestPackagesFolder;
    }
    
    
    /** Returns operator for browse Source Packages Folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseSourcePackagesFolder() {
        if ( _btBrowseSourcePackagesFolder==null ) {
            _btBrowseSourcePackagesFolder = new JButtonOperator(this, "Browse...",0); //TODO I18N
        }
        return _btBrowseSourcePackagesFolder;
    }

    
    /** Returns operator for browse Test Packages Folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseTestPackagesFolder() {
        if ( _btBrowseTestPackagesFolder==null ) {
            _btBrowseTestPackagesFolder = new JButtonOperator(this, "Browse...",1); //TODO I18N
        }
        return _btBrowseTestPackagesFolder;
    }
    
    
    /** Returns operator for label ContextPath:
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if(_lblContextPath == null) {
            _lblContextPath = new JLabelOperator(this,
                                            Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", 
                                                                    "LBL_NWP1_ContextPath_Label"));
        }
        return _lblContextPath;
    }
    
    /** Returns operator of Context Path: text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (  lblContextPath().getLabelFor()!=null ) {
            _txtContextPath = new JTextFieldOperator((JTextField)lblContextPath().getLabelFor());
      }
      return _txtContextPath;
    }
    
    
    /** Returns operator of J2EE Specification Level combo box
     *  @return JComboBoxOperator
     */
    public JComboBoxOperator cbJ2EESpecificationLevel() {
        if ( _cbJ2EESpecificationLevel==null ) {
            _cbJ2EESpecificationLevel = new JComboBoxOperator(this, 0);
        }
        return _cbJ2EESpecificationLevel;
    }
    
    /** Selects level in J2EE Specification Level combo box
     */
    public void selectJ2EESpecificationLevel(String level) {
        cbJ2EESpecificationLevel().selectItem(level);
    }
    
    /** returns operator of Description
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtDescription() {
        if ( _txtDescription==null ) {
            _txtDescription = new JEditorPaneOperator(this,0);
        }
        return _txtDescription;
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
        cbSetAsMainProject();
        cbCreateMainClass();
        txtCreateMainClass();
        lblLocation();
        lblBuildScript();
        txtLocation();
        txtBuildScript();
        btBrowseBuildScript();
        btBrowseProjectFolder();
        */
    }
}
