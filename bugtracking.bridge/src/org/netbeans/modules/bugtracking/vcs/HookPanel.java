/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

/*
 * IzPanel.java
 *
 * Created on Nov 11, 2008, 3:32:39 PM
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 *
 * @author Tomas Stupka
 */
public class HookPanel extends javax.swing.JPanel implements ItemListener, PropertyChangeListener {
    private QuickSearchComboBar qs;
    private Repository selectedRepository;

    private class UpdateFiledsState {
        private boolean addComment = false;
        private boolean addRevisionInfo = false;
        private boolean addIssueInfo = false;
        private boolean resolve = false;
        private boolean commit = false;
        void store() {
            addComment = addCommentCheckBox.isSelected();
            addRevisionInfo = addRevisionCheckBox.isSelected();
            addIssueInfo = addIssueCheckBox1.isSelected();
            resolve = resolveCheckBox.isSelected();
            commit = commitRadioButton.isSelected();

            addCommentCheckBox.setSelected(false);
            addRevisionCheckBox.setSelected(false);
            resolveCheckBox.setSelected(false);
            commitRadioButton.setSelected(false);
        }
        void restore() {
            addCommentCheckBox.setSelected(addComment);
            addRevisionCheckBox.setSelected(addRevisionInfo);
            addIssueCheckBox1.setSelected(addIssueInfo);
            resolveCheckBox.setSelected(resolve);
            commitRadioButton.setSelected(commit);
        }
    }
    private UpdateFiledsState updateFiledsState = null;
    
    public HookPanel(Repository[] repos, Repository toSelect) {
        initComponents();

        qs = new QuickSearchComboBar(this);
        issuePanel.add(qs, BorderLayout.NORTH);
        issueLabel.setLabelFor(qs.getCommand());
        
        repositoryComboBox.setModel(new DefaultComboBoxModel(repos != null ? repos : new Repository[0]));
        repositoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    Repository r = (Repository) value;
                    value = r.getDisplayName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        repositoryComboBox.addItemListener(this);
        if(toSelect != null) {
            repositoryComboBox.setSelectedItem(toSelect);
            qs.setRepository(toSelect);
        } else {
            if(repositoryComboBox.getItemCount() > 0) {
                Repository repo = (Repository) repositoryComboBox.getItemAt(0);
                repositoryComboBox.setSelectedItem(repo);
                qs.setRepository(repo);
            }
        }
        enableFields();        
        
    }

    Issue getIssue() {
        return qs.getIssue();
    }

    public Repository getSelectedRepository() {
        return selectedRepository;
    }

    private void enableFields() {
        boolean repoSelected = repositoryComboBox.getSelectedItem() != null;
        boolean enableUpdateFields = getIssue() != null && repoSelected;

        if(updateFiledsState == null) {
            updateFiledsState = new UpdateFiledsState();
        }
        if(!enableUpdateFields) {            
            updateFiledsState.store();
        } else {
            updateFiledsState.restore();
        }

        addCommentCheckBox.setEnabled(enableUpdateFields);
        addRevisionCheckBox.setEnabled(enableUpdateFields);
        addIssueCheckBox1.setEnabled(enableUpdateFields);
        resolveCheckBox.setEnabled(enableUpdateFields);
        pushRadioButton.setEnabled(enableUpdateFields);
        commitRadioButton.setEnabled(enableUpdateFields);
        changeRevisionFormatButton.setEnabled(enableUpdateFields);
        changeIssueFormatButton.setEnabled(enableUpdateFields);

        issueLabel.setEnabled(repoSelected);
        qs.enableFields(repoSelected);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        repositoryLabel = new javax.swing.JLabel();
        jButton2 = createDoubleWidthButton();
        issueLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        issuePanel = new javax.swing.JPanel();

        buttonGroup1.add(commitRadioButton);
        commitRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(commitRadioButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.commitRadioButton.text")); // NOI18N
        commitRadioButton.setNextFocusableComponent(pushRadioButton);

        buttonGroup1.add(pushRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(pushRadioButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.pushRadioButton.text")); // NOI18N
        pushRadioButton.setNextFocusableComponent(repositoryComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(resolveCheckBox, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.resolveCheckBox.text")); // NOI18N
        resolveCheckBox.setNextFocusableComponent(addCommentCheckBox);
        resolveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addCommentCheckBox, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addCommentCheckBox.text")); // NOI18N
        addCommentCheckBox.setNextFocusableComponent(addRevisionCheckBox);

        org.openide.awt.Mnemonics.setLocalizedText(addRevisionCheckBox, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addRevisionCheckBox.text")); // NOI18N
        addRevisionCheckBox.setNextFocusableComponent(commitRadioButton);
        addRevisionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRevisionCheckBoxActionPerformed(evt);
            }
        });

        repositoryLabel.setLabelFor(repositoryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(repositoryLabel, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.repositoryLabel.text")); // NOI18N

        repositoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(issueLabel, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.issueLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(changeRevisionFormatButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeRevisionFormatButton.text")); // NOI18N

        jLabel2.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jLabel2.text")); // NOI18N

        issuePanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(issuePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
            .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(issuePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2))
        );

        org.openide.awt.Mnemonics.setLocalizedText(addIssueCheckBox1, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addIssueCheckBox1.text")); // NOI18N
        addIssueCheckBox1.setNextFocusableComponent(commitRadioButton);
        addIssueCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addIssueCheckBox1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(changeIssueFormatButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeIssueFormatButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(resolveCheckBox)
                            .add(addCommentCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 240, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(commitRadioButton)
                            .add(pushRadioButton)))
                    .add(layout.createSequentialGroup()
                        .add(addRevisionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changeRevisionFormatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 213, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(125, 125, 125))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(addIssueCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changeIssueFormatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(316, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(repositoryLabel)
                    .add(issueLabel))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, repositoryComboBox, 0, 502, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton2)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(repositoryLabel)
                    .add(repositoryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(issueLabel)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(resolveCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addCommentCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(addRevisionCheckBox)
                            .add(changeRevisionFormatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(commitRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pushRadioButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addIssueCheckBox1)
                    .add(changeIssueFormatButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        commitRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.commitRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        pushRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.pushRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        resolveCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.resolveCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        addCommentCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addCommentCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        addRevisionCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addRevisionCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        repositoryComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.repositoryComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jButton2.AccessibleContext.accessibleDescription")); // NOI18N
        changeRevisionFormatButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeRevisionFormatButton.AccessibleContext.accessibleDescription")); // NOI18N
        addIssueCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addIssueCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        changeIssueFormatButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeIssueFormatButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private JButton createDoubleWidthButton() {
        class DoubleWidthButton extends JButton {
            @Override
            public Dimension getPreferredSize() {
                Dimension defPrefSize = super.getPreferredSize();
                return new Dimension((int) (1.8f * defPrefSize.width),
                                     defPrefSize.height);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        }
        return new DoubleWidthButton();
    }

    private void addRevisionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRevisionCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_addRevisionCheckBoxActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Repository repo = BugtrackingUtil.createRepository();
        if(repo == null) {
            return;
        }
        repositoryComboBox.addItem(repo);
        repositoryComboBox.setSelectedItem(repo);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void resolveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_resolveCheckBoxActionPerformed

    private void addIssueCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addIssueCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addIssueCheckBox1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox addCommentCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox addIssueCheckBox1 = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox addRevisionCheckBox = new javax.swing.JCheckBox();
    private javax.swing.ButtonGroup buttonGroup1;
    final org.netbeans.modules.bugtracking.util.LinkButton changeIssueFormatButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final org.netbeans.modules.bugtracking.util.LinkButton changeRevisionFormatButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JRadioButton commitRadioButton = new javax.swing.JRadioButton();
    private javax.swing.JLabel issueLabel;
    private javax.swing.JPanel issuePanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    final javax.swing.JRadioButton pushRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JComboBox repositoryComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel repositoryLabel;
    final javax.swing.JCheckBox resolveCheckBox = new javax.swing.JCheckBox();
    // End of variables declaration//GEN-END:variables

    public void itemStateChanged(ItemEvent e) {
        enableFields();
        if(e.getStateChange() == ItemEvent.SELECTED) {
            Repository repo = (Repository) e.getItem();
            selectedRepository = repo;
            if(repo != null) {
                qs.setRepository(repo);
            }
        }
    }

    @Override
    public void addNotify() {
        qs.addPropertyChangeListener(this);
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        qs.removePropertyChangeListener(this);
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(QuickSearchComboBar.EVT_ISSUE_CHANGED)) {
            enableFields();
        }
    }

}
