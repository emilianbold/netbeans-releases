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
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.TreePath;

/**
 * Handles NetBeans New Project wizard and its components
 * Categories and Projecs. 
  * It is invoked from main menu File -> New Project... <br>
 * Usage:
 * <pre>
 *  NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
 *  npwop.selectCategory("Standard");
 *  npwop.selectProject("Java Application");
 *  npwop.next();
 *  npwop.getDescription();
 * </pre>
 * @author tb115823
 */
public class NewProjectWizardOperator extends WizardOperator {

    private JLabelOperator      _lblCategories;
    private JLabelOperator      _lblProjects;
    private JTreeOperator       _treeCategories;
    private JListOperator       _lstProjects;
    private JLabelOperator      _lblDescription;
    private JEditorPaneOperator _txtDescription;
    
            
    /** Creates new NewProjectWizardOperator that can handle it.
     */
    public NewProjectWizardOperator() {
        super(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewProjectWizard_Subtitle"));
    }

    public NewProjectWizardOperator(String title) {
        super(title);
    }
    
    /** Invokes new wizard and returns instance of NewProjectWizardOperator.
     * @return  instance of NewProjectWizardOperator
     */
    public static NewProjectWizardOperator invoke() {
        new NewProjectAction().perform();
        return new NewProjectWizardOperator();
    }

    
    /** Selects given project category
     * @param category name of the category to select
     */
    public void selectCategory(String category) {
        new Node(treeCategories(), category).select();
    }
    
    /** Selects given project
     * @param project name of project to select
     */
    public void selectProject(String project) {
        lstProjects().selectItem(project);
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
    public JLabelOperator lblProjects() {
        if (_lblProjects==null) {
            _lblProjects = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Projects"));
        }
        return _lblProjects;
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

    /** Tries to find null ListView$NbList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstProjects() {
        if (_lstProjects==null) {
            _lstProjects = new JListOperator(this, 1);
        }
        return _lstProjects;
    }


    /** returns selected item for lstProject
     * @return selected project
     */
    public String getSelectedProject() {
        return lstProjects().getSelectedValue().toString();
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

    

    /** Performs verification of NewProjectWizardOperator by accessing all its components.
     */
    public void verify() {
        lblCategories();
        lblProjects();
        treeCategories();
        lstProjects();
        lblDescription();
        txtDescription();
    }

}

