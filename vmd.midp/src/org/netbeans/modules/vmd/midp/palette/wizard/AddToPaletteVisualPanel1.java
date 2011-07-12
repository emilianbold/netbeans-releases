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
 */package org.netbeans.modules.vmd.midp.palette.wizard;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.vmd.api.io.javame.MidpProjectPropertiesSupport;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.concurrent.Future;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public final class AddToPaletteVisualPanel1 extends JPanel {

    private static final String MSG_NO_PROJECTS = "MSG_ERR_NoOpenedProjects"; // NOI18N
    private static final String MSG_WAIT_PROJECTS = "MSG_WaitOpenedProjects"; // NOI18N
    
    private AddToPaletteWizardPanel1 wizardPanel;

    public AddToPaletteVisualPanel1 (AddToPaletteWizardPanel1 wizardPanel) {
        this.wizardPanel = wizardPanel;
        initComponents();
    }

    public String getName() {
        return NbBundle.getMessage(AddToPaletteVisualPanel1.class, "TITLE_SelectProject"); // NOI18N
    }

    public Project getActiveProject () {
        Object item = projectCombo.getSelectedItem();
        if (item instanceof Project){
            return (Project)item;
        }
        return null;
    }

    public int getProjectsCount () {
        if (projectCombo.getItemAt(0)==null  ||
                projectCombo.getItemAt(0).equals(MSG_NO_PROJECTS))
        {
            return 0;
        }
        return projectCombo.getItemCount();
    }

    public void reload(final Project project) {
        reload( project , false );
    }

    public void reload(final Project project, boolean loaded) {
        Future<Project[]> future =  OpenProjects.getDefault ().openProjects();
        Project[] projects;
        Vector projectsVector = new Vector ();
        if ( loaded || future.isDone() ){
            projects = OpenProjects.getDefault ().getOpenProjects ();
        }
        else {
            new Thread(){
                @Override
                public void run() {
                    try {
                        OpenProjects.getDefault().openProjects().get();
                        reload( );
                    } catch (InterruptedException ex) {
                        reload( );
                    } catch (ExecutionException ex) {
                        reload();
                    }
                }

                private void reload(){
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            AddToPaletteVisualPanel1.this.reload( project, true);
                        }
                    });
                }
            }.start();
            projectsVector.add(getMessage(MSG_WAIT_PROJECTS));
            projectCombo.setEnabled( false );
            projectCombo.setRenderer(new DefaultListCellRenderer());
            projectCombo.setModel(new DefaultComboBoxModel(projectsVector));
            return;
        }
        //Project[] projects = OpenProjects.getDefault ().getOpenProjects ();
        for (Project prj : projects) {
            if (MidpProjectPropertiesSupport.isMobileProject(prj)) {
                projectsVector.add(prj);
            }
        }
        if(projectsVector.size() == 0){
            projectsVector.add(getMessage(MSG_NO_PROJECTS));
            projectCombo.setEnabled( false );
            projectCombo.setRenderer(new DefaultListCellRenderer());
            projectCombo.setModel(new DefaultComboBoxModel(projectsVector));
        } else {
            projectCombo.setEnabled( true );
            projectCombo.setRenderer (new ProjectListCellRenderer ());
            projectCombo.setModel(new DefaultComboBoxModel(projectsVector));
            setProjectSelection(projects, project);
        }
    }

    private void setProjectSelection(Project[] openedProjects, Project project){
            if (project == null) {
                Project prj = OpenProjects.getDefault().getMainProject();
                if (MidpProjectPropertiesSupport.isMobileProject(prj)) {
                    project = prj;
                }
            }
            if (project == null && openedProjects.length > 0) {
                project = openedProjects[0];
            }
            projectCombo.setSelectedItem(project);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectCombo = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

        projectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(projectCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(AddToPaletteVisualPanel1.class, "DISP_SelectProject")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(projectCombo, 0, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(243, Short.MAX_VALUE))
        );

        projectCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddToPaletteVisualPanel1.class, "ACCESSIBLE_NAME_projectCombo")); // NOI18N
        projectCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddToPaletteVisualPanel1.class, "ACCESSIBLE_DESCRIPTION_projectCombo")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddToPaletteVisualPanel1.class, "ACCESSIBLE_NAME_jLabel1_2")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddToPaletteVisualPanel1.class, "ACCESSIBLE_DESCRIPTION_jLabel1_2")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void projectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboActionPerformed
    wizardPanel.fireChangeEvent();
}//GEN-LAST:event_projectComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox projectCombo;
    // End of variables declaration//GEN-END:variables

    public static class ProjectListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null)
                return super.getListCellRendererComponent (list, value, index, isSelected, cellHasFocus);
            ProjectInformation info = ((Project) value).getLookup ().lookup (ProjectInformation.class);
            super.getListCellRendererComponent (list, info != null ? info.getDisplayName () : null, index, isSelected, cellHasFocus);
            if (info != null)
                setIcon (info.getIcon ());
            return this;
        }

    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(AddToPaletteVisualPanel1.class, key);
    }
    
}

