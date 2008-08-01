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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;


/**
 * @author Jan Becicka
 */
public class CopyClassPanel extends JPanel implements ActionListener, DocumentListener, CustomRefactoringPanel {
  
    private final ListCellRenderer GROUP_CELL_RENDERER = new MoveClassPanel.GroupCellRenderer();
    private final ListCellRenderer PROJECT_CELL_RENDERER = new MoveClassPanel.ProjectCellRenderer();
    
    private Project project;
    private ChangeListener parent;
    private FileObject fo;
    private SourceGroup[] groups;
    private String newName;
    
    public CopyClassPanel(final ChangeListener parent, String title, String startPackage, FileObject f, String newName) {
        setName(title);
        this.fo = f;
        this.parent = parent;
        this.newName = newName;
        initComponents();
        setCombosEnabled(true);
        setThisClassVisible(true);
        
        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer( PROJECT_CELL_RENDERER );
                
        rootComboBox.addActionListener( this );
        packageComboBox.addActionListener( this );
        projectsComboBox.addActionListener( this );
        
        Object textField = packageComboBox.getEditor().getEditorComponent();
        if (textField instanceof JTextField) {
            ((JTextField) textField).getDocument().addDocumentListener(this); 
        }
        newNameTextField.getDocument().addDocumentListener(this);
        
        project = fo != null ? FileOwnerQuery.getOwner(fo):OpenProjects.getDefault().getOpenProjects()[0];
        initValues(startPackage);
    }
    
    private boolean initialized = false;
    public void initialize() {
        if (initialized)
            return ;
        FileObject fob;
        do {
            fob = fo.getFileObject(newName + ".java"); //NOI18N
            if (fob!=null)
                newName += "1"; // NOI18N
        } while (fob!=null);
        newNameTextField.setText(newName);
        newNameTextField.setSelectionStart(0);
        newNameTextField.setSelectionEnd(newNameTextField.getText().length());
        initialized = true;
    }
    
    public void initValues(String preselectedFolder ) {
        
        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );                
        projectsComboBox.setSelectedItem( project );
        
        updateRoots();
        updatePackages(); 
        if (preselectedFolder != null) {
            packageComboBox.setSelectedItem(preselectedFolder);
        }
        // Determine the extension
    }
    
    public void requestFocus() {
        newNameTextField.requestFocus();
    }
    
    public FileObject getRootFolder() {
        SourceGroup sg = (SourceGroup) rootComboBox.getSelectedItem();
        return sg != null ? sg.getRootFolder() : null;
    }
    
    public String getPackageName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelProject = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        bottomPanel = new javax.swing.JPanel();
        newNameLabel = new javax.swing.JLabel();
        newNameTextField = new javax.swing.JTextField();
        isUpdateReferences = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        labelProject.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelProject, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectsComboBox, gridBagConstraints);
        projectsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.projectsComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        labelLocation.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelLocation, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.rootComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        labelPackage.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "LBL_ToPackage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(labelPackage, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(packageComboBox, gridBagConstraints);
        packageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.packageComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanel, gridBagConstraints);

        newNameLabel.setLabelFor(newNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(newNameLabel, org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "LBL_NewName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(newNameLabel, gridBagConstraints);

        newNameTextField.setText(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.newNameTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(newNameTextField, gridBagConstraints);
        newNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.newNameTextField.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(isUpdateReferences, org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "LBL_CopyWithoutRefactoring")); // NOI18N
        isUpdateReferences.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
        isUpdateReferences.setMargin(new java.awt.Insets(2, 2, 0, 2));
        isUpdateReferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isUpdateReferencesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(isUpdateReferences, gridBagConstraints);
        isUpdateReferences.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CopyClassPanel.class, "CopyClassPanel.isUpdateReferences.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void isUpdateReferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isUpdateReferencesActionPerformed
    parent.stateChanged(null);
}//GEN-LAST:event_isUpdateReferencesActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel bottomPanel;
    private javax.swing.JCheckBox isUpdateReferences;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JLabel newNameLabel;
    private javax.swing.JTextField newNameTextField;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(ActionEvent e) {
        if (projectsComboBox == e.getSource()) {
            project = (Project) projectsComboBox.getSelectedItem();
            updateRoots();
            updatePackages();
        } else 
        if ( rootComboBox == e.getSource() ) {            
            updatePackages();
        }
        else if ( packageComboBox == e.getSource() ) {
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(DocumentEvent e) {                
        fireChange();        
    }    
    
    public void insertUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    public void removeUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        ComboBoxModel model = g != null
                ? PackageView.createListView(g)
                : new DefaultComboBoxModel();
        packageComboBox.setModel(model);
    }
    
    void setCombosEnabled(boolean enabled) {
        packageComboBox.setEnabled(enabled);
        rootComboBox.setEnabled(enabled);
        projectsComboBox.setEnabled(enabled);
        isUpdateReferences.setVisible(!enabled);
    }
    
    void setThisClassVisible(boolean visible) {
        newNameLabel.setVisible(visible);
        newNameTextField.setVisible(visible);
    }

    public boolean isUpdateReferences() {
        if (isUpdateReferences.isVisible() && isUpdateReferences.isSelected())
            return false;
        return true;
    }
    
    
    public String getNewName() {
        return newNameTextField.getText();
    }
    
    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        int preselectedItem = 0;
        for( int i = 0; i < groups.length; i++ ) {
            if (fo!=null) {
                try {
                    if (groups[i].contains(fo)) {
                        preselectedItem = i;
                    }
                } catch (IllegalArgumentException e) {
                    // XXX this is a poor abuse of exception handling
                }
            }
        }
                
        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        if (groups.length > 0) {
            rootComboBox.setSelectedIndex(preselectedItem);
        }
    }
    
    public Component getComponent() {
        return this;
    }

}
