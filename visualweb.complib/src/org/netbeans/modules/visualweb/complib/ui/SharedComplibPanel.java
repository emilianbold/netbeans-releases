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

package org.netbeans.modules.visualweb.complib.ui;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider;
import org.netbeans.modules.visualweb.complib.IdeUtil;
import org.netbeans.modules.visualweb.complib.SharedComplib;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Temporary UI to implement shared component libraries.
 * 
 * @author Edwin Goei
 */
public class SharedComplibPanel extends javax.swing.JPanel {

    private static final ComplibServiceProvider csp = ComplibServiceProvider
            .getInstance();

    private List<String> postedMessages;

    /** Creates new form SharedComplibPanel */
    public SharedComplibPanel() {
        initComponents();
        
        clearStatusMessage();

        initOptionsJList();

        updateSharedComplibList();
    }

    private void clearStatusMessage() {
        postedMessages = new ArrayList<String>();
        messageJLabel.setText(" ");
    }

    private void addErrorMessage(String message) {
        if (postedMessages.size() == 0) {
            messageJLabel.setText("Error: " + message);
            postedMessages.add(message);
        } else {
            messageJLabel.setText("First Error: " + postedMessages.get(0));
        }
    }

    private static class ProjectWrapper {
        private Project project;

        public ProjectWrapper(Project project) {
            this.project = project;
        }

        public String toString() {
            ProjectInformation info = ProjectUtils.getInformation(project);
            return info.getDisplayName();
        }

        public Project getProject() {
            return project;
        }
    }

    private void initOptionsJList() {
        List<Project> projects = csp.getEligibleSharedComplibProjects();
        final ProjectWrapper[] options = new ProjectWrapper[projects.size()];
        int i = 0;
        for (Project project : projects) {
            options[i++] = new ProjectWrapper(project);
        }

        optionsJList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return options.length;
            }

            public Object getElementAt(int i) {
                return options[i];
            }
        });
        
        // Set a default selection
        if (options.length > 0) {
            optionsJList.setSelectedIndex(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        messageJLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        optionsJList = new javax.swing.JList();
        addJButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sharedComplibJList = new javax.swing.JList();
        removeJButton = new javax.swing.JButton();
        refreshJButton = new javax.swing.JButton();

        messageJLabel.setForeground(java.awt.Color.red);
        messageJLabel.setText("Status message goes here");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel3.setText("Select an Open Shared Component Library Project*:");

        optionsJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(optionsJList);

        addJButton.setText("Add");
        addJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        jLabel2.setText("* Shared Component Library Project must contain a functional \"build/complib\" directory.");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(addJButton)
                    .add(jLabel2))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addJButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setText("Shared Component Libraries for Active Project:");

        sharedComplibJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(sharedComplibJList);

        removeJButton.setText("Remove");
        removeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        refreshJButton.setText("Refresh All");
        refreshJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(removeJButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshJButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(removeJButton)
                    .add(refreshJButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(messageJLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageJLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_refreshActionPerformed
        try {
            csp.refreshSharedComplibsForActiveProject();
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
            IdeUtil.logError(e);
        }
        updateSharedComplibList();
    }// GEN-LAST:event_refreshActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeActionPerformed
        Object[] selected = sharedComplibJList.getSelectedValues();
        Set<SharedComplib> selectedSet = new HashSet<SharedComplib>();
        for (Object object : selected) {
            selectedSet.add((SharedComplib) object);
        }

        try {
            csp.removeSharedComplibsFromActiveProject(selectedSet);
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
            IdeUtil.logError(e);
        }
        updateSharedComplibList();
    }                                      

    private void addActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addActionPerformed
        ProjectWrapper pw = (ProjectWrapper) optionsJList.getSelectedValue();
        if (pw == null) {
            addErrorMessage("Select a Shared Component Library");
            return;
        } else {
            clearStatusMessage();
        }

        Project project = pw.getProject();
        try {
            csp.addSharedComplibProject(project);
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
            IdeUtil.logError(e);
        }
        updateSharedComplibList();
    }// GEN-LAST:event_addActionPerformed

    private void updateSharedComplibList() {
        /*
         * Get the shared complibs for the active project and convert to an
         * array for the JList model
         */
        SharedComplib[] complibsArray0;
        try {
            Set<SharedComplib> complibs = csp
                    .getSharedComplibsForActiveProject();
            complibsArray0 = (SharedComplib[]) complibs
                    .toArray(new SharedComplib[complibs.size()]);
        } catch (ComplibException e) {
            addErrorMessage(e.getMessage());
            IdeUtil.logError(e);
            complibsArray0 = new SharedComplib[0];
        }

        final SharedComplib[] complibsArray = complibsArray0;
        sharedComplibJList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return complibsArray.length;
            }

            public Object getElementAt(int i) {
                return complibsArray[i];
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addJButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel messageJLabel;
    private javax.swing.JList optionsJList;
    private javax.swing.JButton refreshJButton;
    private javax.swing.JButton removeJButton;
    private javax.swing.JList sharedComplibJList;
    // End of variables declaration//GEN-END:variables
    
    /**
     * This method is the main entry point for this dialog panel
     * 
     */
    public void showDialog() {
        String title = NbBundle.getMessage(SharedComplibPanel.class,
                "sharedComplib.dialogTitle"); // NOI18N
        JButton closeButton = new JButton();
        Mnemonics.setLocalizedText(closeButton, NbBundle.getMessage(
                SharedComplibPanel.class, "sharedComplib.closeButton"));

        Object[] options = new Object[] { closeButton };
        DialogDescriptor descriptor = new DialogDescriptor(this, title, true,
                options, closeButton, DialogDescriptor.DEFAULT_ALIGN, null,
                null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        descriptor.setValid(true);

        dialog.setVisible(true);
    }
}
