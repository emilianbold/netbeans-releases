/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class HookPanel extends VerticallyNonResizingPanel implements ItemListener, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.vcshooks.HookPanel");  // NOI18N

    private QuickSearchComboBar qs;
    private Repository selectedRepository;

    private class FieldValues {
        private boolean addLinkInfo = false;
        private boolean resolve = false;
        private boolean commit = false;
        private boolean stored = false;
        void store() {
            addLinkInfo = linkCheckBox.isSelected();
            resolve = resolveCheckBox.isSelected();
            commit = commitRadioButton.isSelected();

            linkCheckBox.setSelected(false);
            resolveCheckBox.setSelected(false);
            commitRadioButton.setSelected(false);
            stored = true;
        }
        void restore() {
            linkCheckBox.setSelected(addLinkInfo);
            resolveCheckBox.setSelected(resolve);
            commitRadioButton.setSelected(commit);
            stored = false;
        }
    }
    private FieldValues fieldValues = null;
    
    public HookPanel(boolean link, boolean resolve, boolean commit) {
        initComponents();
        this.fieldValues = new FieldValues();

        qs = new QuickSearchComboBar(this);
        issuePanel.add(qs, BorderLayout.NORTH);
        issueLabel.setLabelFor(qs.getIssueComponent());

        linkCheckBox.setSelected(link);
        resolveCheckBox.setSelected(resolve);
        commitRadioButton.setSelected(commit);
        pushRadioButton.setSelected(!commit);

        enableFields();

        repositoryComboBox.addItemListener(this);
    }

    Issue getIssue() {
        return qs.getIssue();
    }

    public Repository getSelectedRepository() {
        return selectedRepository;
    }

    void enableFields() {
        boolean repoSelected = isRepositorySelected();
        boolean enableFields = repoSelected && (getIssue() != null);

        if(!enableFields && !fieldValues.stored) { // !fieldValues.stored ->
                                                   //  storing twice would override
                                                   //  the originaly stored values
            fieldValues.store();
        } else if (enableFields) {
            fieldValues.restore();
        }

        linkCheckBox.setEnabled(enableFields);
        resolveCheckBox.setEnabled(enableFields);
        pushRadioButton.setEnabled(enableFields);
        commitRadioButton.setEnabled(enableFields);
        changeFormatButton.setEnabled(enableFields);

        issueLabel.setEnabled(repoSelected);
        qs.enableFields(repoSelected);
    }

    private boolean isRepositorySelected() {
        Object selectedItem = repositoryComboBox.getSelectedItem();
        return selectedItem instanceof Repository;
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
        issuePanel = new javax.swing.JPanel();
        repositoryLabel = new javax.swing.JLabel();
        jButton2 = createDoubleWidthButton();
        issueLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setFocusable(false);

        issuePanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(resolveCheckBox, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.resolveCheckBox.text")); // NOI18N
        resolveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(linkCheckBox, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.linkCheckBox.text")); // NOI18N
        linkCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkCheckBoxActionPerformed(evt);
            }
        });

        repositoryLabel.setLabelFor(repositoryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(repositoryLabel, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.repositoryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(issueLabel, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.issueLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(changeFormatButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeFormatButton.text")); // NOI18N
        changeFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeFormatButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(commitRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(commitRadioButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.commitRadioButton.text")); // NOI18N

        buttonGroup1.add(pushRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(pushRadioButton, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.pushRadioButton.text")); // NOI18N

        jLabel2.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(repositoryLabel)
                    .addComponent(issueLabel))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolveCheckBox)
                            .addComponent(linkCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeFormatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(commitRadioButton)
                            .addComponent(pushRadioButton)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                            .addComponent(issuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                            .addComponent(repositoryComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 502, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repositoryLabel)
                    .addComponent(jButton2)
                    .addComponent(repositoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addComponent(issueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE))
                .addGap(9, 9, 9)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resolveCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(linkCheckBox)
                            .addComponent(changeFormatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(commitRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pushRadioButton)))
                .addContainerGap())
        );

        resolveCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.resolveCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        linkCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.addRevisionCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        repositoryComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.repositoryComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.jButton2.AccessibleContext.accessibleDescription")); // NOI18N
        changeFormatButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.changeRevisionFormatButton.AccessibleContext.accessibleDescription")); // NOI18N
        commitRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.commitRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        pushRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HookPanel.class, "HookPanel.pushRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
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

    private void linkCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_linkCheckBoxActionPerformed

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

    private void changeFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeFormatButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_changeFormatButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    final org.netbeans.modules.bugtracking.util.LinkButton changeFormatButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JRadioButton commitRadioButton = new javax.swing.JRadioButton();
    private javax.swing.JLabel issueLabel;
    private javax.swing.JPanel issuePanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    final javax.swing.JCheckBox linkCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JRadioButton pushRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JComboBox repositoryComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel repositoryLabel;
    final javax.swing.JCheckBox resolveCheckBox = new javax.swing.JCheckBox();
    // End of variables declaration//GEN-END:variables

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "itemStateChanged() - selected item: {0}", e.getItem()); //NOI18N
        }
        enableFields();
        if(e.getStateChange() == ItemEvent.SELECTED) {
            Object item = e.getItem();
            Repository repo = (item instanceof Repository) ? (Repository) item : null;
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(QuickSearchComboBar.EVT_ISSUE_CHANGED)) {
            enableFields();
        }
    }

}
