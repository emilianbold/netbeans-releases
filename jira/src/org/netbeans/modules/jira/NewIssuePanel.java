/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * NewIssuePanel.java
 *
 * Created on Oct 21, 2008, 12:04:28 PM
 */

package org.netbeans.modules.jira;

import java.awt.Component;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;

/**
 *
 * @author tomas
 */
public class NewIssuePanel extends javax.swing.JPanel {

    /** Creates new form NewIssuePanel */
    public NewIssuePanel() {
        initComponents();
        
    }

    void preset(IssueType[] types, Priority[] prios, Project[] projects) {
        typeCbo.setModel(new DefaultComboBoxModel(types));
        typeCbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    IssueType t =  (IssueType) value;
                    value = t.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });        

        prioCbo.setModel(new DefaultComboBoxModel(prios));
        prioCbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    Priority p = (Priority) value;
                    value = p.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        projectsCbo.setModel(new DefaultComboBoxModel(projects));
        projectsCbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    Project p = (Project) value;
                    value = p.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        componentsCbo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    Component c = (Component) value;
                    value = c.getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jLabel10 = new javax.swing.JLabel();
        attachFileButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        jLabel5.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel5.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel6.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel4.text")); // NOI18N

        summaryField.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.summaryField.text")); // NOI18N

        jLabel11.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel11.text")); // NOI18N

        descTextArea.setColumns(20);
        descTextArea.setRows(5);
        jScrollPane8.setViewportView(descTextArea);

        prioCbo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prioCboActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel1.text")); // NOI18N

        projectsCbo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectsCboItemStateChanged(evt);
            }
        });

        jScrollPane7.setViewportView(attachmentsList);

        jLabel10.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel10.text")); // NOI18N

        attachFileButton.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.attachFileButton.text")); // NOI18N
        attachFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachFileButtonActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(NewIssuePanel.class, "NewIssuePanel.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(summaryField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel5))
                        .add(27, 27, 27)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectsCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(typeCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(componentsCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(prioCbo, 0, 139, Short.MAX_VALUE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE)
                        .add(attachFileButton))
                    .add(jScrollPane7, 0, 228, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(jLabel11)
                .addContainerGap())
            .add(jScrollPane8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(projectsCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(componentsCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(attachFileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(typeCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(prioCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4)))
                    .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void attachFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachFileButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser("", null);// NOI18N
        fileChooser.setDialogTitle("Got file?");// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this, "");// NOI18N
        File f = fileChooser.getSelectedFile();
        if (f == null) return;
//        try {
//            issue.addAttachment(f);
//            issue.refresh();
//        } catch (JiraException e) {
//            Jira.LOG.log(Level.SEVERE, null, e);
//        }
//        refresh();
}//GEN-LAST:event_attachFileButtonActionPerformed

    private void prioCboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prioCboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prioCboActionPerformed

    private void projectsCboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_projectsCboItemStateChanged
        Project p = (Project) projectsCbo.getSelectedItem();
        if(p == null) return;
        componentsCbo.setModel(new DefaultComboBoxModel(p.getComponents()));
    }//GEN-LAST:event_projectsCboItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attachFileButton;
    final javax.swing.JList attachmentsList = new javax.swing.JList();
    final javax.swing.JComboBox componentsCbo = new javax.swing.JComboBox();
    final javax.swing.JTextArea descTextArea = new javax.swing.JTextArea();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    final javax.swing.JComboBox prioCbo = new javax.swing.JComboBox();
    final javax.swing.JComboBox projectsCbo = new javax.swing.JComboBox();
    final javax.swing.JTextField summaryField = new javax.swing.JTextField();
    final javax.swing.JComboBox typeCbo = new javax.swing.JComboBox();
    // End of variables declaration//GEN-END:variables

}
