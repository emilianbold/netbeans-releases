/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools;

import javax.swing.JDialog;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
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

    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewFileWizardOperator(String title) {
        super(title);
    }
    
    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke() {
        new NewFileAction().perform();
        return new NewFileWizardOperator();
    }

    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @param title initial wizard title
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(String title) {
        new NewFileAction().perform();
        return new NewFileWizardOperator(title);
    }

    /** Selects specified node and invokes new file wizard by default action.
     * In "Choose File Type" wizard's page it selects given category and filetype.
     * It returns instance of NewFileWizardOperator representing "Name and Location"
     * page of the wizard.
     * @param node node which should be selected before new file wizard is invoked
     * @param category category to be selected
     * @param filetype file type to be selected (exact name - not substring)
     * @return instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(Node node, String category, String filetype) {
        new NewFileAction().perform(node);
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = new NewFileWizardOperator(wizardTitle);
        nfwo.selectCategory(category);
        nfwo.selectFileType(filetype);
        nfwo.next();
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
        // we need to wait until some node is selected because 'please, wait' node
        // is shown before tree is initialized. Then we can change selection.
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    return treeCategories().isSelectionEmpty() ? null: Boolean.TRUE;
                }
                public String getDescription() {
                    return("Wait node is selected");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        } catch(TimeoutExpiredException tee) {
            // ignore it because sometimes can happen that no category is selected by default
        }
        new Node(treeCategories(), category).select();
    }
    
    /** Selects given file type
     * @param filetype name of file type to select (exact name - not substring)
     */
    public void selectFileType(String filetype) {
        lstFileTypes().selectItem(filetype);
    }
    
    
    /** Tries to find "Project:"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_TargetChooser_Project_Label"));
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

    /** Tries to find "File Types:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileTypes() {
        if (_lblFileTypes==null) {
            _lblFileTypes = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Files"));
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
            // set exact comparator because Java Classes has types 'Java Package Info'
            // and 'Java Package'.
            _lstFileTypes.setComparator(new Operator.DefaultStringComparator(true, true));
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

