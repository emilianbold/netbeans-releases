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

/*
 * SaveProjectsDialog.java
 *
 * Created on April 10, 2005, 7:31 AM
 */

package org.netbeans.modules.uml.project;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.openide.awt.Mnemonics;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Administrator
 */
public class SaveProjectsDialog extends JCenterDialog
{
    private DefaultListModel mModel = new DefaultListModel();
    private boolean mIsCanceled = true;
    
    /** Creates new form SaveProjectsDialog */
    public SaveProjectsDialog(Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        //initDirtyProjectList();
        
        mProjectsList.setCellRenderer(new UMLProjectRender());
        mProjectsList.setModel(mModel);
    }
    
    /**
     * Adds all the UML projects that are marked as being dirty to list model.
     */
    protected void initDirtyProjectList()
    {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        for(Project curProj : allProjects)
        {
            UMLProjectHelper helper = (UMLProjectHelper)curProj.getLookup()
            .lookup(UMLProjectHelper.class);
            
            if(helper != null)
            {
                IProject umlProject = helper.getProject();
                if((umlProject != null) && (umlProject.isDirty() == true))
                {
                    mModel.addElement(curProj);
                }
            }
        }
    }
    
    public void addProject(Project project)
    {
        mModel.addElement(project);
    }
    
    public void showDialog()
    {
        mProjectsList.setSelectedIndex(0);
        setVisible(true);
    }
    
    public static boolean saveProjects()
    {
        boolean retVal = true;
        
        Frame frame = WindowManager.getDefault().getMainWindow();
        SaveProjectsDialog dialog = new SaveProjectsDialog(frame, true);

        dialog.getRootPane().setDefaultButton(dialog.mSaveBtn);
        
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
            SaveProjectsDialog.class, "ACSN_SaveProjectsDialog")); // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
            SaveProjectsDialog.class, "ACSD_SaveProjectsDialog")); // NOI18N
        
        // Load the projects that need to be saved into the dialog. If no projects
        // needed to be saved then do not show the dialog.
        boolean showDialog = false;
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        for (Project curProj : allProjects)
        {
            UMLProjectHelper helper = (UMLProjectHelper)curProj
                .getLookup().lookup(UMLProjectHelper.class);
            
            if (helper != null)
            {
                IProject umlProject = helper.getProject();
                
                if ((umlProject != null) && (umlProject.isDirty() == true))
                {
                    dialog.addProject(curProj);
                    showDialog = true;
                }
            }
        }
        
        if (showDialog == true)
        {
            dialog.showDialog();
            if(dialog.isCanceled() == true)
            {
                retVal = false;
            }
        }
        
        return retVal;
    }
    
    /**
     * Retruns whether or not the user canceled the save operation.
     */
    public boolean isCanceled()
    {
        return mIsCanceled;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        mProjectsList = new javax.swing.JList();
        mSaveAllBtn = new javax.swing.JButton();
        mSaveBtn = new javax.swing.JButton();
        mDiscardAllBtn = new javax.swing.JButton();
        mCancelBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/project/Bundle"); // NOI18N
        setTitle(bundle.getString("SAVE_DIALOG_TITLE")); // NOI18N
        getAccessibleContext().setAccessibleName("");
        getAccessibleContext().setAccessibleDescription("");
        jScrollPane1.setViewportView(mProjectsList);
        mProjectsList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSN_ProjectsList")); // NOI18N
        mProjectsList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSD_ProjectsList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mSaveAllBtn, bundle.getString("SAVE_ALL_BTN")); // NOI18N
        mSaveAllBtn.setActionCommand("Save_All");
        mSaveAllBtn.setDefaultCapable(false);
        mSaveAllBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mSaveAllBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 10);
        getContentPane().add(mSaveAllBtn, gridBagConstraints);
        mSaveAllBtn.getAccessibleContext().setAccessibleName("");
        mSaveAllBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSD_SaveAllButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mSaveBtn, bundle.getString("SAVE_BTN")); // NOI18N
        mSaveBtn.setActionCommand("Save");
        mSaveBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mSaveBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        getContentPane().add(mSaveBtn, gridBagConstraints);
        mSaveBtn.getAccessibleContext().setAccessibleName("");
        mSaveBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSD_SaveButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mDiscardAllBtn, bundle.getString("DISCARD_ALL_BTN")); // NOI18N
        mDiscardAllBtn.setActionCommand("Discard_All");
        mDiscardAllBtn.setDefaultCapable(false);
        mDiscardAllBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mDiscardAllBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        getContentPane().add(mDiscardAllBtn, gridBagConstraints);
        mDiscardAllBtn.getAccessibleContext().setAccessibleName("");
        mDiscardAllBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSD_DiscardAllButton")); // NOI18N

        mCancelBtn.setText(bundle.getString("CANCEL_BTN")); // NOI18N
        mCancelBtn.setDefaultCapable(false);
        mCancelBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mCancelBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        getContentPane().add(mCancelBtn, gridBagConstraints);
        mCancelBtn.getAccessibleContext().setAccessibleName("");
        mCancelBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SaveProjectsDialog.class, "ACSD_CancelButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents
        
   private void mCancelBtnActionPerformed(ActionEvent evt)//GEN-FIRST:event_mCancelBtnActionPerformed
   {//GEN-HEADEREND:event_mCancelBtnActionPerformed
       closeWindow(true);
   }//GEN-LAST:event_mCancelBtnActionPerformed
   
   private void mDiscardAllBtnActionPerformed(ActionEvent evt)//GEN-FIRST:event_mDiscardAllBtnActionPerformed
   {//GEN-HEADEREND:event_mDiscardAllBtnActionPerformed
       int count = mModel.getSize();
       UMLProject[] projects = new UMLProject[count];
       
       for(int index = 0; index < count; index++)
       {
           projects[index] = (UMLProject)mModel.getElementAt(index);
           
           UMLProjectHelper helper = (UMLProjectHelper)projects[index]
               .getLookup().lookup(UMLProjectHelper.class);
           
           helper.getProject().setDirty(false);
           helper.getProject().setChildrenDirty(false);
       }
       
       closeWindow(false);
   }//GEN-LAST:event_mDiscardAllBtnActionPerformed
   
   private void mSaveAllBtnActionPerformed(ActionEvent evt)//GEN-FIRST:event_mSaveAllBtnActionPerformed
   {//GEN-HEADEREND:event_mSaveAllBtnActionPerformed
       int count = mModel.getSize();
       UMLProject[] projects = new UMLProject[count];
       
       for (int index = 0; index < count; index++)
           projects[index] = (UMLProject)mModel.getElementAt(index);
       
       saveProjects(projects);
       closeWindow(false);
   }//GEN-LAST:event_mSaveAllBtnActionPerformed
   
   private void mSaveBtnActionPerformed(ActionEvent evt)//GEN-FIRST:event_mSaveBtnActionPerformed
   {//GEN-HEADEREND:event_mSaveBtnActionPerformed
       int[] indices = mProjectsList.getSelectedIndices();
       UMLProject[] projects = new UMLProject[indices.length];
       int totUnsaved = mModel.size();
       
       for (int index = 0; index < indices.length; index++)
       {
           // get selected projects to be saved
           projects[index] = (UMLProject)mModel.getElementAt(indices[index]);
           
           // remove the project from the listbox because once it is saved
           // it no longer needs to be listed as dirty
           mModel.removeElementAt(indices[index]);
       }
       
       saveProjects(projects);

       // only close the dialog if all the dirty projects were saved
       // otherwise leave dialog open to allow the rest to be saved
       if (indices.length == totUnsaved)
           closeWindow(false);
       
       else
           mProjectsList.setSelectedIndex(0);
   }//GEN-LAST:event_mSaveBtnActionPerformed
   
   protected void closeWindow(boolean isCanceled)
   {
       mIsCanceled = isCanceled;
       setVisible(false);
   }
   
   protected void saveProjects(UMLProject[] projects)
   {
       for(UMLProject curProj : projects)
       {
           curProj.saveProject();
       }
   }

   
   public class UMLProjectRender extends DefaultListCellRenderer
   {
       public Component getListCellRendererComponent(JList list,
           Object value,
           int index,
           boolean isSelected,
           boolean cellHasFocus)
       {
           Project project = (Project)value;
           
           ProjectInformation info = (ProjectInformation)project
               .getLookup().lookup(ProjectInformation.class);
           
           super.getListCellRendererComponent(
               list, info.getDisplayName(), index, isSelected, cellHasFocus);
           
           setIcon(info.getIcon());
           return this;
       }
   }
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton mCancelBtn;
    private javax.swing.JButton mDiscardAllBtn;
    private javax.swing.JList mProjectsList;
    private javax.swing.JButton mSaveAllBtn;
    private javax.swing.JButton mSaveBtn;
    // End of variables declaration//GEN-END:variables
    
}
