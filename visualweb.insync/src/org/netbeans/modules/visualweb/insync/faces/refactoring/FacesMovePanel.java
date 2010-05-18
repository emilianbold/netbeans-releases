/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Asks where to move a Page to.
 * @author Jan Becicka, Jesse Glick
 */
public class FacesMovePanel extends JPanel implements ActionListener, CustomRefactoringPanel {
  
    private static final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    private static final ProjectByDisplayNameComparator PROJECT_BY_DISPLAY_NAME_COMPARATOR = new ProjectByDisplayNameComparator();
    
    private ChangeListener parent;
    private Project project;
    private FileObject webRootFolder;
    private FileObject fo;
    
    public FacesMovePanel(final ChangeListener parent, String title, FileObject f) {
        this.parent = parent;
        this.fo = f;
        
        initComponents();
        
        setCombosEnabled(true);
        setName(title);

        projectsComboBox.setRenderer(PROJECT_CELL_RENDERER);
        folderComboBox.setRenderer(new FolderRenderer());
        
        projectsComboBox.addActionListener(this);
        folderComboBox.addActionListener(this);        
        
        project = fo != null ? FileOwnerQuery.getOwner(fo):OpenProjects.getDefault().getOpenProjects()[0];
    }
    
    private boolean initialized = false;
    
    public void initialize() {
        if (initialized)
            return ;
        //put initialization code here
        initValues(fo);
        initialized = true;
    }
    
    public void initValues(FileObject preselectedFolder) {        
        Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
        Set<? super Project> webProjects = new TreeSet<Project>(PROJECT_BY_DISPLAY_NAME_COMPARATOR);
        for (Project project : openProjects) {
            if (JsfProjectUtils.isJsfProject(project)) {
                webProjects.add(project);
            }
        }

        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel(webProjects.toArray());
        projectsComboBox.setModel( projectsModel );                
        projectsComboBox.setSelectedItem( project );
        
        updateRoot();
        updateFolders(); 
        if (preselectedFolder != null) {
            folderComboBox.setSelectedItem(preselectedFolder);
        }
        // Determine the extension
    }
    
    public void requestFocus() {
        folderComboBox.requestFocus();
    }
    
    public FileObject getWebRootFolder() {
        return webRootFolder;
    }
    
    public FileObject getTargeFolder() {
        FileObject fileObject = (FileObject) folderComboBox.getSelectedItem();
        if (fileObject != null) {
            return fileObject;
        }
        return null;
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelProject = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelWebRoot = new javax.swing.JLabel();
        webRootTextField = new javax.swing.JTextField();
        labelFolder = new javax.swing.JLabel();
        folderComboBox = new javax.swing.JComboBox();
        bottomPanel = new javax.swing.JPanel();
        labelHeadLine = new javax.swing.JLabel();
        updateReferencesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        labelProject.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(FacesMovePanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelProject, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectsComboBox, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/insync/faces/refactoring/Bundle"); // NOI18N
        projectsComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectsCombo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelWebRoot, org.openide.util.NbBundle.getMessage(FacesMovePanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelWebRoot, gridBagConstraints);

        webRootTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(webRootTextField, gridBagConstraints);

        labelFolder.setLabelFor(folderComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelFolder, org.openide.util.NbBundle.getMessage(FacesMovePanel.class, "LBL_Folder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelFolder, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(folderComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanel, gridBagConstraints);

        labelHeadLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 6, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(labelHeadLine, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(updateReferencesCheckBox, org.openide.util.NbBundle.getBundle(FacesMovePanel.class).getString("LBL_MoveWithoutReferences")); // NOI18N
        updateReferencesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
        updateReferencesCheckBox.setMargin(new java.awt.Insets(2, 2, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(updateReferencesCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel bottomPanel;
    private javax.swing.JComboBox folderComboBox;
    private javax.swing.JLabel labelFolder;
    private javax.swing.JLabel labelHeadLine;
    private javax.swing.JLabel labelProject;
    private javax.swing.JLabel labelWebRoot;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JCheckBox updateReferencesCheckBox;
    private javax.swing.JTextField webRootTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(ActionEvent e) {
        if (projectsComboBox == e.getSource()) {
            project = (Project) projectsComboBox.getSelectedItem();
            updateRoot();
            updateFolders();
            fireChange();
        }
        else if ( folderComboBox == e.getSource() ) {
            fireChange();
        }
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updateFolders() {
        Set<FileObject> folders = new LinkedHashSet<FileObject>();
        // Update folders under web folder
        if (webRootFolder != null) {
            folders.add(webRootFolder);
            Enumeration<? extends FileObject> children = webRootFolder.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject child = children.nextElement();
                // Only folders
                if (child.isFolder()) {
                    // Not special folders
                    if (child.getNameExt().equalsIgnoreCase("META-INF") || // NOI18N
                            child.getNameExt().equalsIgnoreCase("WEB-INF")) { // NOI18N
                        continue;
                    }
                    // Not children of special folders
                    String relativePath = FileUtil.getRelativePath(webRootFolder, child);
                    if (relativePath != null) {
                    	if (relativePath.startsWith("META-INF/") || // NOI18N
                    			relativePath.startsWith("WEB-INF/")) { // NOI18N
                    		continue;
                    	}
                    }
                    folders.add(child);
                }
            }
            
        }
        folderComboBox.setModel(new DefaultComboBoxModel(folders.toArray(new FileObject[folders.size()])));
    }
    
    void setCombosEnabled(boolean enabled) {
        projectsComboBox.setEnabled(enabled);
        folderComboBox.setEnabled(enabled);
        updateReferencesCheckBox.setVisible(!enabled);
    }

    public boolean isUpdateReferences() {
        if (updateReferencesCheckBox.isVisible() && updateReferencesCheckBox.isSelected())
            return false;
        return true;
    }
    
    private void updateRoot() {
        webRootTextField.setText("");
        webRootFolder = null;
        Project selectedProject = (Project) projectsComboBox.getSelectedItem();
        if (selectedProject != null) {
            FileObject projectDirectory = selectedProject.getProjectDirectory();
            if (projectDirectory != null) {
                webRootFolder = JsfProjectUtils.getDocumentRoot(project);
                if (webRootFolder != null) {
                    webRootTextField.setText(FileUtil.getRelativePath(projectDirectory, webRootFolder));
                }
            }
        }
    }
    
    private abstract static class BaseCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public BaseCellRenderer () {
            setOpaque(true);
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }    
    
    /** Projects combo renderer, used also in CopyClassPanel */
    static class ProjectCellRenderer extends BaseCellRenderer {
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    class FolderRenderer extends BaseCellRenderer {
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            FileObject folder = (FileObject) value;
            if (webRootFolder != null) {
                if (webRootFolder == folder) {
                    setText(".");
                } else {
                    setText(FileUtil.getRelativePath(webRootFolder, folder));
                }
            } else {
                setText(folder.getPath());
            }            
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }

    //Copy/pasted from OpenProjectList
    //remove this code as soon as #68827 is fixed.
    private static class ProjectByDisplayNameComparator implements Comparator {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2) {
            
            if ( !( o1 instanceof Project ) ) {
                return 1;
            }
            if ( !( o2 instanceof Project ) ) {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
            return COLLATOR.compare(ProjectUtils.getInformation(p1).getDisplayName(), ProjectUtils.getInformation(p2).getDisplayName());
        }
    }    

    public Component getComponent() {
        return this;
    }
}
