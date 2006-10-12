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

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "New JSF Managed Bean" NbDialog.
 *
 * @author luke
 * @version 1.0
 */
public class NewJSFBeanStepOperator extends WizardOperator {

    /** Creates new NewJSFManagedBean that can handle it.
     */
    public NewJSFBeanStepOperator() {
        super("New JSF Managed Bean");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblNameAndLocation;
    private JLabelOperator _lblClassName;
    private JTextFieldOperator _txtClassName;
    private JLabelOperator _lblProject;
    private JTextFieldOperator _txtProject;
    private JLabelOperator _lblLocation;
    private JComboBoxOperator _cboLocation;
    private JLabelOperator _lblPackage;
    private JComboBoxOperator _cboPackage;
    private JLabelOperator _lblCreatedFile;
    private JTextFieldOperator _txtCreatedFile;
    private JLabelOperator _lblConfigurationFile;
    private JComboBoxOperator _cboConfigurationFile;
    private JLabelOperator _lblScope;
    private JComboBoxOperator _cboScope;
    private JLabelOperator _lblBeanDescription;
    private JTextAreaOperator _txtBeanDescription;
   


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Name and Location" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNameAndLocation() {
        if (_lblNameAndLocation==null) {
            _lblNameAndLocation = new JLabelOperator(this, "Name and Location");
        }
        return _lblNameAndLocation;
    }

    /** Tries to find "Class Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClassName() {
        if (_lblClassName==null) {
            _lblClassName = new JLabelOperator(this, "Class Name:");
        }
        return _lblClassName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtClassName() {
        if (_txtClassName==null) {
            _txtClassName = new JTextFieldOperator(this);
        }
        return _txtClassName;
    }

    /** Tries to find "Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, "Project:");
        }
        return _lblProject;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProject() {
        if (_txtProject==null) {
            _txtProject = new JTextFieldOperator(this, 1);
        }
        return _txtProject;
    }

    /** Tries to find "Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if (_lblLocation==null) {
            _lblLocation = new JLabelOperator(this, "Location:");
        }
        return _lblLocation;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if (_cboLocation==null) {
            _cboLocation = new JComboBoxOperator(this);
        }
        return _cboLocation;
    }

    /** Tries to find "Package:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPackage() {
        if (_lblPackage==null) {
            _lblPackage = new JLabelOperator(this, "Package:");
        }
        return _lblPackage;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPackage() {
        if (_cboPackage==null) {
            _cboPackage = new JComboBoxOperator(this, 1);
        }
        return _cboPackage;
    }

    /** Tries to find "Created File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if (_lblCreatedFile==null) {
            _lblCreatedFile = new JLabelOperator(this, "Created File:");
        }
        return _lblCreatedFile;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if (_txtCreatedFile==null) {
            _txtCreatedFile = new JTextFieldOperator(this, 3);
        }
        return _txtCreatedFile;
    }

    /** Tries to find "Configuration File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblConfigurationFile() {
        if (_lblConfigurationFile==null) {
            _lblConfigurationFile = new JLabelOperator(this, "Configuration File:");
        }
        return _lblConfigurationFile;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboConfigurationFile() {
        if (_cboConfigurationFile==null) {
            _cboConfigurationFile = new JComboBoxOperator(this, 2);
        }
        return _cboConfigurationFile;
    }

    /** Tries to find "Scope:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblScope() {
        if (_lblScope==null) {
            _lblScope = new JLabelOperator(this, "Scope:");
        }
        return _lblScope;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboScope() {
        if (_cboScope==null) {
            _cboScope = new JComboBoxOperator(this, 3);
        }
        return _cboScope;
    }

    /** Tries to find "Bean Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanDescription() {
        if (_lblBeanDescription==null) {
            _lblBeanDescription = new JLabelOperator(this, "Bean Description:");
        }
        return _lblBeanDescription;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtBeanDescription() {
        if (_txtBeanDescription==null) {
            _txtBeanDescription = new JTextAreaOperator(this);
        }
        return _txtBeanDescription;
    }
   


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtClassName
     * @return String text
     */
    public String getClassName() {
        return txtClassName().getText();
    }

    /** sets text for txtClassName
     * @param text String text
     */
    public void setClassName(String text) {
        txtClassName().setText(text);
    }

    /** types text for txtClassName
     * @param text String text
     */
    public void typeClassName(String text) {
        txtClassName().typeText(text);
    }

    /** gets text for txtProject
     * @return String text
     */
    public String getProject() {
        return txtProject().getText();
    }

    /** sets text for txtProject
     * @param text String text
     */
    public void setProject(String text) {
        txtProject().setText(text);
    }

    /** types text for txtProject
     * @param text String text
     */
    public void typeProject(String text) {
        txtProject().typeText(text);
    }

    /** returns selected item for cboLocation
     * @return String item
     */
    public String getSelectedLocation() {
        return cboLocation().getSelectedItem().toString();
    }

    /** selects item for cboLocation
     * @param item String item
     */
    public void selectLocation(String item) {
        cboLocation().selectItem(item);
    }

    /** returns selected item for cboPackage
     * @return String item
     */
    public String getSelectedPackage() {
        return cboPackage().getSelectedItem().toString();
    }

    /** selects item for cboPackage
     * @param item String item
     */
    public void selectPackage(String item) {
        cboPackage().selectItem(item);
    }

    /** types text for cboPackage
     * @param text String text
     */
    public void typePackage(String text) {
        cboPackage().typeText(text);
    }

    /** gets text for txtCreatedFile
     * @return String text
     */
    public String getCreatedFile() {
        return txtCreatedFile().getText();
    }

    /** sets text for txtCreatedFile
     * @param text String text
     */
    public void setCreatedFile(String text) {
        txtCreatedFile().setText(text);
    }

    /** types text for txtCreatedFile
     * @param text String text
     */
    public void typeCreatedFile(String text) {
        txtCreatedFile().typeText(text);
    }

    /** returns selected item for cboConfigurationFile
     * @return String item
     */
    public String getSelectedConfigurationFile() {
        return cboConfigurationFile().getSelectedItem().toString();
    }

    /** selects item for cboConfigurationFile
     * @param item String item
     */
    public void selectConfigurationFile(String item) {
        cboConfigurationFile().selectItem(item);
    }

    /** returns selected item for cboScope
     * @return String item
     */
    public String getSelectedScope() {
        return cboScope().getSelectedItem().toString();
    }

    /** selects item for cboScope
     * @param item String item
     */
    public void selectScope(String item) {
        cboScope().selectItem(item);
    }

    /** gets text for txtBeanDescription
     * @return String text
     */
    public String getBeanDescription() {
        return txtBeanDescription().getText();
    }

    /** sets text for txtBeanDescription
     * @param text String text
     */
    public void setBeanDescription(String text) {
        txtBeanDescription().setText(text);
    }

    /** types text for txtBeanDescription
     * @param text String text
     */
    public void typeBeanDescription(String text) {
        txtBeanDescription().typeText(text);
    }

    



    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewJSFManagedBean by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lblNameAndLocation();
        lblClassName();
        txtClassName();
        lblProject();
        txtProject();
        lblLocation();
        cboLocation();
        lblPackage();
        cboPackage();
        lblCreatedFile();
        txtCreatedFile();
        lblConfigurationFile();
        cboConfigurationFile();
        lblScope();
        cboScope();
        lblBeanDescription();
        txtBeanDescription();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }

}

