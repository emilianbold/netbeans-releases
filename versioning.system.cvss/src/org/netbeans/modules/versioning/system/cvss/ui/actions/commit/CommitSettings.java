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

package org.netbeans.modules.versioning.system.cvss.ui.actions.commit;

import java.util.prefs.PreferenceChangeEvent;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.prefs.PreferenceChangeListener;
import java.util.*;
import java.io.File;
import java.io.StringWriter;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.SwingUtilities;

/**
 * Customization of commits.
 * 
 * @author Maros Sandor
 */
public class CommitSettings extends javax.swing.JPanel implements PreferenceChangeListener, TableModelListener, DocumentListener {
    
    public static final String COLUMN_NAME_COMMIT    = "commit"; // NOI18N
    static final String COLUMN_NAME_NAME    = "name"; // NOI18N
    static final String COLUMN_NAME_STICKY  = "sticky"; // NOI18N
    static final String COLUMN_NAME_STATUS  = "status"; // NOI18N
    static final String COLUMN_NAME_ACTION  = "action"; // NOI18N
    static final String COLUMN_NAME_PATH    = "path"; // NOI18N
    
    static final Object EVENT_SETTINGS_CHANGED = new Object();

    private CommitTable     commitTable;

    public static class CommitFile {
        private final CommitOptions options;
        private final CvsFileNode   node;

        public CommitFile(CvsFileNode node, CommitOptions options) {
            this.node = node;
            this.options = options;
        }

        public CommitOptions getOptions() {
            return options;
        }

        public CvsFileNode getNode() {
            return node;
        }
    }
    
    public CommitSettings() {
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(ss.width / 2, ss.height / 5 * 2));
        init();
    }

    /**
     * Set columns to display in the Commit table.
     * 
     * @param cols array of column names
     */ 
    void setColumns(String[] cols) {
        commitTable.setColumns(cols);
    }

    /**
     * Set file nodes to display in the Commit table.
     * 
     * @param nodes array of nodes
     */ 
    void setNodes(CvsFileNode[] nodes) {
        commitTable.setNodes(nodes);
    }
    
    public String getCommitMessage() {
        return taMessage.getText();
    }
    
    public void addNotify() {
        super.addNotify();
        CvsModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                taMessage.selectAll();
                taMessage.requestFocus();  // #67106
            }
        });
    }

    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        CvsModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        super.removeNotify();
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(CvsModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            commitTable.dataChanged();
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    public CommitFile [] getCommitFiles() {
        return commitTable.getCommitFiles();
    }
    
    private void init() {
        initComponents();
        errorLabel.setText(""); // NOI18N
        messageErrorLabel.setText(""); // NOI18N
        jScrollPane1.setMinimumSize(jScrollPane1.getPreferredSize());
        commitTable = new CommitTable(jLabel3);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(commitTable.getComponent(), gridBagConstraints);
        String message = CvsModuleConfig.getDefault().getLastCommitMessage();
        if (!message.isEmpty()) {
            taMessage.setText(message);
        } else {
            loadTemplate(true);
        }

        taMessage.getDocument().addDocumentListener(this);
        onCommitMessageChanged();
    }

    public void insertUpdate(DocumentEvent e) {
        onCommitMessageChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        onCommitMessageChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        onCommitMessageChanged();
    }

    private void onCommitMessageChanged() {
        if (taMessage.getText().trim().length() == 0) {
            messageErrorLabel.setText(NbBundle.getMessage(CommitSettings.class,"MSG_CommitForm_MessageEmpty"));
        } else {
            messageErrorLabel.setText("");  // NOI18N
        }
    }
    
    private void loadTemplate(boolean quiet) {
        CommitFile [] files = getCommitFiles();
        for (CommitFile commitFile : files) {
            File file = commitFile.getNode().getFile();
            File templateFile = new File(file.getParentFile(), CvsVersioningSystem.FILENAME_CVS + "/Template");  // NOI18N
            if (templateFile.canRead()) {
                StringWriter sw = new StringWriter();
                try {
                    Utils.copyStreamsCloseAll(sw, new FileReader(templateFile));
                    String message = sw.toString();
                    if (message.trim().length() == 0) {
                        NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(CommitSettings.class, "CTL_LoadTemplate_Empty"),  // NOI18N
                                                                   NbBundle.getMessage(CommitSettings.class, "CTL_LoadTemplate_Title"),  // NOI18N
                                                                   NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, NotifyDescriptor.NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.YES_OPTION) return;
                    }
                    taMessage.setText(message);
                } catch (IOException e) {
                    if (!quiet) ErrorManager.getDefault().notify(e);
                }
                return;
            }
        }
        if (!quiet) {
            NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(CommitSettings.class, "CTL_LoadTemplate_NoTemplate"),   // NOI18N
                                                       NbBundle.getMessage(CommitSettings.class, "CTL_LoadTemplate_Title"), NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);  // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void onBrowseRecentMessages() {
        String message = StringSelector.select(NbBundle.getMessage(CommitSettings.class, "CTL_RecentMessages_Prompt"),   // NOI18N
                                               NbBundle.getMessage(CommitSettings.class, "CTL_RecentMessages_Title"),   // NOI18N
            Utils.getStringList(CvsModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES));
        if (message != null) {
            taMessage.replaceSelection(message);
        }
    }

    void setErrorLabel(String htmlErrorLabel) {
        errorLabel.setText(htmlErrorLabel);
        revalidate();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taMessage = new org.netbeans.modules.versioning.system.cvss.ui.components.KTextArea();
        jLabel3 = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        messageErrorLabel = new javax.swing.JLabel();
        recentMessagesButton = new javax.swing.JButton();
        loadTemplateButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(taMessage);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/commit/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("CTL_CommitForm_Message")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 2, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(jLabel2, gridBagConstraints);

        taMessage.setColumns(30);
        taMessage.setLineWrap(true);
        taMessage.setRows(6);
        taMessage.setTabSize(4);
        taMessage.setWrapStyleWord(true);
        jScrollPane1.setViewportView(taMessage);
        taMessage.getAccessibleContext().setAccessibleDescription(bundle.getString("TT_CommitForm_Message")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_CommitForm_FilesToCommit")); // NOI18N
        jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(jLabel3, gridBagConstraints);

        errorLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(errorLabel, gridBagConstraints);

        messageErrorLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(messageErrorLabel, gridBagConstraints);

        recentMessagesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/recent_messages.png"))); // NOI18N
        recentMessagesButton.setToolTipText(org.openide.util.NbBundle.getMessage(CommitSettings.class, "CTL_CommitForm_RecentMessages")); // NOI18N
        recentMessagesButton.setBorderPainted(false);
        recentMessagesButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        recentMessagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentMessagesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(recentMessagesButton, gridBagConstraints);

        loadTemplateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/load_template.png"))); // NOI18N
        loadTemplateButton.setToolTipText(org.openide.util.NbBundle.getMessage(CommitSettings.class, "TT_CommitForm_LoadTemplate")); // NOI18N
        loadTemplateButton.setBorderPainted(false);
        loadTemplateButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        loadTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTemplateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(loadTemplateButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void loadTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadTemplateButtonActionPerformed
        loadTemplate(false);//GEN-LAST:event_loadTemplateButtonActionPerformed
    }                                                  

    private void recentMessagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentMessagesButtonActionPerformed
        onBrowseRecentMessages();//GEN-LAST:event_recentMessagesButtonActionPerformed
    }                                                    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadTemplateButton;
    private javax.swing.JLabel messageErrorLabel;
    private javax.swing.JButton recentMessagesButton;
    private org.netbeans.modules.versioning.system.cvss.ui.components.KTextArea taMessage;
    // End of variables declaration//GEN-END:variables
    
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
}
