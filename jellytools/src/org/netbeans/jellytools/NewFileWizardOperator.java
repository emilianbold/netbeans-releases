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

import javax.swing.JDialog;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.TreePath;

/**
 * Handle NetBeans New File wizard.
 * It is invoked either from main menu File -> New File... 
 * <code>NewFileAction.performMenu();</code>
   or from popup menu on folder <code>NewFileAction.performPopup();</code><br>
 * Usage:
 *
 * <pre>
 * NewFileWizardOperator op = NewFileWizardOperator.invoke();
 * op.selectCategory("Java Classes");
 * op.selectFileType("Java Class");
 * </pre>
 *
 * @author tb115823
 */
public class NewFileWizardOperator extends WizardOperator {

    private JLabelOperator      _lblProject;
    private JLabelOperator      _lblCategories;
    private JLabelOperator      _lblFileTypes;
    private JTreeOperator       _treeCategories;
    private JListOperator       _lstFileTypes;
    private JLabelOperator      _lblDescription;
    private JEditorPaneOperator _txtDescription;
    private JComboBoxOperator   _cboProject;
    
    
    
    /** Creates new NewFileWizardOperator that can handle it.
     */
    public NewFileWizardOperator() {
        super(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Subtitle"));
    }

    
    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke() {
        new NewFileAction().perform();
        return new NewFileWizardOperator();
    }

    
    /** Select given project in combobox of projects
     *  @param project name of project
     */
    public void selectProject(String project) {
        cboProject().selectItem(project);
    }
    
    
    /** Selects given project category
     * @param category name of the category to select
     */
    public void selectCategory(String category) {
        new Node(treeCategories(), category).select();
    }
    
    /** Selects given file type
     * @param filetype name of file type to select
     */
    public void selectFileType(String filetype) {
        lstFileTypes().selectItem(filetype);
    }
    
    
    /** Tries to find "Project:"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Project"));
        }
        return _lblCategories;
    }
    
    /** Tries to find "Categories:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCategories() {
        if (_lblCategories==null) {
            _lblCategories = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Categories"));
        }
        return _lblCategories;
    }

    /** Tries to find "Projects:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileTypes() {
        if (_lblFileTypes==null) {
            _lblFileTypes = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_FileTypes"));
        }
        return _lblFileTypes;
    }

    /** Tries to find JComboBox Project
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboProject() {
        if (_cboProject==null) {
            _cboProject = new JComboBoxOperator(this);
        }
        return _cboProject;
    }
    
    /** returns selected item for cboProject
     * @return selected project
     */
    public String getSelectedProject() {
        return cboProject().getSelectedItem().toString();
    }
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeCategories() {
        if (_treeCategories==null) {
            _treeCategories = new JTreeOperator(this);
        }
        return _treeCategories;
    }
    
    /** returns selected path in treeCategories
     * @return TreePath
     */
    public TreePath getSelectedCategory() {
        return treeCategories().getSelectionPath();
    }

    /** Tries to find FileTypes ListView in this dialog.
     * @return JListOperator
     */
    public JListOperator lstFileTypes() {
        if (_lstFileTypes==null) {
            _lstFileTypes = new JListOperator(this, 1);
        }
        return _lstFileTypes;
    }

    
    /** returns selected item in lstFileType
     * @return String selected file type
     */
    public String getSelectedFileType() {
        return lstFileTypes().getSelectedValue().toString();
    }
    
    /** Tries to find "Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescription() {
        if (_lblDescription==null) {
            _lblDescription = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Description"));
        }
        return _lblDescription;
    }

    
    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtDescription() {
        if (_txtDescription==null) {
            _txtDescription = new JEditorPaneOperator(this);
        }
        return _txtDescription;
    }


    
    /** gets text for txtDescription
     * @return String text
     */
    public String getDescription() {
        return txtDescription().getText();
    }

    

    /** Performs verification of NewFileWizardOperator by accessing all its components.
     */
    public void verify() {
        lblCategories();
        lblFileTypes();
        cboProject();
        treeCategories();
        lstFileTypes();
        lblDescription();
        txtDescription();
    }
}

