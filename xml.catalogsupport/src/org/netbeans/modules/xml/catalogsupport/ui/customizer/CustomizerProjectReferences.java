/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * CustomizerProjectReferences.java
 *
 * Created on December 12, 2006, 12:30 PM
 */

package org.netbeans.modules.xml.catalogsupport.ui.customizer;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JList;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.modules.xml.catalogsupport.ProjectReferenceUtility;

/**
 *
 * @author  Ajit
 */
public class CustomizerProjectReferences extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private ReferenceHelper refHelper;
    private Project owner;
    private SubprojectProvider subprojectProvider;
    private Project newlyAddedProject;
    
    /** Creates new form CustomizerProjectReferences */
    public CustomizerProjectReferences(Project owner, ReferenceHelper refHelper) {
        this.owner = owner;
        this.refHelper = refHelper;
        this.subprojectProvider = (SubprojectProvider)owner.getLookup().
                lookup(SubprojectProvider.class);
        initComponents();
        projectList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(projectList.getSelectedIndices().length==0) {
                    removeButton.setEnabled(false);
                } else {
                    removeButton.setEnabled(true);
                }
            }
        });
        refreshProjectList();
    }
    
    /**
     * Recreates the Project dependency list on UI based on subprojectprovider.
     */
    private void refreshProjectList() {
        DefaultListModel listModel =  (DefaultListModel) projectList.getModel();
        listModel.clear();
        Object[] raws = getRefHelper().getRawReferences();
        Set subprojects = getSubprojectProvider().getSubprojects();
        for(Object object:subprojects) {
            Project prj = (Project)object;
            ProjectInformation pInfo = ProjectUtils.getInformation(prj);
            listModel.addElement(pInfo);
        }
        ReferenceHelper.RawReference[] rawRefs = getRefHelper().getRawReferences();
        for(ReferenceHelper.RawReference rawRef:rawRefs) {
            if(rawRef.toAntArtifact(getRefHelper())==null) {
                if(newlyAddedProject == null) {
                    listModel.addElement(new MissingProjectInformation(rawRef));
                    continue;
                }
                ProjectInformation pInfo = ProjectUtils.getInformation(newlyAddedProject);
                listModel.addElement(pInfo);
            }
        }
        if (projectList.getModel().getSize() > 0 && projectList.getSelectedIndex() == -1) {
            projectList.setSelectedIndex(0);
        }
    }
    
    private Project getProject() {
        return owner;
    }
    
    private ReferenceHelper getRefHelper() {
        return refHelper;
    }
    
    private SubprojectProvider getSubprojectProvider() {
        return subprojectProvider;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        refLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        refLabel.setLabelFor(projectList);
        org.openide.awt.Mnemonics.setLocalizedText(refLabel, org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "LBL_CustomizerProjectReferences_ProjectReferences"));
        refLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "HINT_CustomizerProjectReferences_ProjectReferences"));

        projectList.setModel(new DefaultListModel());
        projectList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                ProjectInformation pInfo = (ProjectInformation)value;
                super.getListCellRendererComponent( projectList, pInfo, index, isSelected, cellHasFocus );
                setIcon(pInfo.getIcon());
                setText(pInfo.getDisplayName());
                setToolTipText(pInfo.getDisplayName());
                return this;
            }
        });
        jScrollPane1.setViewportView(projectList);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "LBL_CustomizerProjectReferences_AddProject"));
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "HINT_CustomizerProjectReferences_AddProject"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showProjectDialog(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "LBL_CustomizerProjectReferences_RemoveProject"));
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerProjectReferences.class, "HINT_CustomizerProjectReferences_RemoveProject"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProject(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(addButton)
                            .add(removeButton)))
                    .add(refLabel))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(refLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Removes project reference from UI.
     * The project dependencies are saved automatically to relevant files,
     * by the customizer provider, when user chooses to save project changes.
     */
    private void removeProject(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeProject
        Object[] selection = projectList.getSelectedValues();
        for(Object selected:selection) {
            ProjectInformation pInfo = (ProjectInformation)selected;
            Project refProject = pInfo.getProject();
            if(pInfo instanceof MissingProjectInformation) {
                ReferenceHelper.RawReference rawRef =
                        ((MissingProjectInformation)pInfo).getRawReference();
                getRefHelper().removeRawReference(rawRef.getForeignProjectName(),
                        rawRef.getID());
            } else {
                if(ProjectReferenceUtility.hasProjectReferenceInCatalog(
                        getProject(),refProject)) {
                    NotifyDescriptor confirmation = new NotifyDescriptor.Confirmation
                            (NbBundle.getMessage(CustomizerProjectReferences.class, 
                            "MSG_DeleteRefProject", pInfo.getName()),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(confirmation);
                    if(confirmation.getValue()!=NotifyDescriptor.YES_OPTION) {
                        return;
                    };
                }
                ProjectReferenceUtility.removeProjectReference(getProject(), getRefHelper(), refProject);
            }
        }
        refreshProjectList();
    }//GEN-LAST:event_removeProject

    /**
     * Shows the add project dialog and calls addProjectReference if user chooses
     * to the project.
     */
    
    private void showProjectDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showProjectDialog
        // create and show project chooser
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(
                CustomizerProjectReferences.class,
                "LBL_CustomizerProjectReferences_ProjectChooserTitle")); // NOI18N
        chooser.setApproveButtonText(org.openide.util.NbBundle.getMessage(
                CustomizerProjectReferences.class,
                "LBL_CustomizerProjectReferences_ProjectChooserButton")); // NOI18N
        chooser.setPreferredSize( new Dimension( 650, 380 ) );
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f == null || !f.isDirectory())
                    return false;
                return true;
            }
            public String getDescription() {
                return "Directories/Project roots only.";
            }
        });
        int option = chooser.showOpenDialog(this);
        // Add project if project selected
        if(option == JFileChooser.APPROVE_OPTION) {
            addProjectReference(chooser.getSelectedFile());
        }
    }//GEN-LAST:event_showProjectDialog

    /**
     * adds project reference on UI.
     * The project dependencies are saved automatically to relevant files,
     * by the customizer provider, when user chooses to save project changes.
     */
    private void addProjectReference(final File projectDirectory) {
        FileObject projectRoot = FileUtil.toFileObject(projectDirectory);
        if(projectRoot!=null) {
            try {
                Project refProject = ProjectManager.getDefault().findProject(projectRoot);
                if(refProject!=null) {
                    if(refProject == getProject()){
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(
                                CustomizerProjectReferences.class, "MSG_RefToItself")));
                        return;
                    }
                    if(ProjectUtils.hasSubprojectCycles(getProject(),refProject)){
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(
                                CustomizerProjectReferences.class, "MSG_Cycles")));
                        return;
                    }
                    ProjectReferenceUtility.addProjectReference(getProject(), getRefHelper(), refProject);
                    newlyAddedProject = refProject;
                }
            } catch (IllegalArgumentException ex) {
            } catch (IOException ex) {
            }
        }
        refreshProjectList();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerProjectReferences.class);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList projectList;
    private javax.swing.JLabel refLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    private static final class MissingProjectInformation implements ProjectInformation {
        
        ReferenceHelper.RawReference missingRef;
        
        static Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/java/j2seproject/ui/resources/brokenProjectBadge.gif", false); // NOI18N

        public MissingProjectInformation(ReferenceHelper.RawReference missingRef) {
            this.missingRef = missingRef;
        }
        
        ReferenceHelper.RawReference getRawReference() {
            return missingRef;
        }

        public String getName() {
            return missingRef.getForeignProjectName();
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(CustomizerProjectReferences.class,
                    "LBL_MISSING_PROJECT", missingRef.getForeignProjectName());
        }
        
        public Icon getIcon() {
            return icon;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public Project getProject() {
            return null;
        }
        
    }
    
}
