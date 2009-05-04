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

package org.netbeans.modules.subversion.ui.commit;

import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.hooks.spi.SvnHook;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.StringSelector;
import org.openide.util.NbBundle;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.subversion.hooks.spi.SvnHookContext;

/**
 *
 * @author  pk97937
 */
public class CommitPanel extends javax.swing.JPanel implements PreferenceChangeListener, TableModelListener {

    static final Object EVENT_SETTINGS_CHANGED = new Object();

    private CommitTable commitTable;
    private boolean hooksOn;

    /** Creates new form CommitPanel */
    public CommitPanel() {
        initComponents();
    }

    void initHooks(List<SvnHook> hooks, SvnHookContext context) {
        if(hooks.size() < 1) {
            hooksSectionButton.setVisible(false);
            hooksSectionPanel.setVisible(false);
            hooksOn = false;
            return;
        }
        hooksOn = true;
        if (hooks.size() == 1) {
            SvnHook hook = hooks.get(0);
            hooksSectionPanel.remove(hooksTabbedPane);
            hooksSectionPanel.add(hook.createComponent(context));
            hooksSectionButton.setText(hook.getDisplayName());
        } else {
            hooksSectionButton.setText(NbBundle.getMessage(CommitPanel.class, "LBL_Advanced"));
            for (SvnHook hook : hooks) {
                hooksTabbedPane.add(hook.createComponent(context), hook.getDisplayName());
            }
        }
        hooksSectionPanel.setVisible(false);
        hooksSectionButton.setVisible(true);
    }

    void setCommitTable(CommitTable commitTable) {
        this.commitTable = commitTable;
    }

    void setErrorLabel(String htmlErrorLabel) {
        jLabel2.setText(htmlErrorLabel);
    }

    public void addNotify() {
        super.addNotify();
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);

        recentLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recentLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onBrowseRecentMessages();
            }
        });
        final List<String> messages = Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (messages.size() > 0) {
                    messageTextArea.setText(messages.get(0));
                }
                messageTextArea.selectAll();
            }
        });

        if(hooksOn) {
            JTree tv = new JTree();
            BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
            final Icon ei = tvui.getExpandedIcon();
            final Icon ci = tvui.getCollapsedIcon();
            filesSectionButton.setIcon(ei);
            hooksSectionButton.setIcon(ci);
            hooksSectionPanel.setVisible(false);

            initSectionButton(filesSectionButton, filesSectionPanel, ci, ei);
            initSectionButton(hooksSectionButton, hooksSectionPanel, ci, ei);
        }
    }

    private void initSectionButton(final JLabel label, final JPanel panel, final Icon ci, final Icon ei) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panel.isVisible()) {
                    panel.setVisible(false);
                    label.setIcon(ci);
                } else {
                    panel.setVisible(true);
                    label.setIcon(ei);
                }
            }
        });
    }

    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        super.removeNotify();
    }

    private void onBrowseRecentMessages() {
        String message = StringSelector.select(NbBundle.getMessage(CommitPanel.class, "CTL_CommitForm_RecentTitle"),
                                               NbBundle.getMessage(CommitPanel.class, "CTL_CommitForm_RecentPrompt"),
            Utils.getStringList(SvnModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES));
        if (message != null) {
            messageTextArea.replaceSelection(message);
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            commitTable.dataChanged();
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(400, 300));
        setPreferredSize(new java.awt.Dimension(650, 400));

        jLabel1.setLabelFor(messageTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CTL_CommitForm_Message")); // NOI18N

        messageTextArea.setColumns(30);
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(6);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new java.awt.Dimension(100, 18));
        jScrollPane1.setViewportView(messageTextArea);
        messageTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitPanel.class, "ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitPanel.class, "ACSD_CommitForm_Message")); // NOI18N

        recentLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/recent_messages.png"))); // NOI18N
        recentLink.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CTL_CommitForm_RecentMessages")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(filesSectionButton, org.openide.util.NbBundle.getMessage(CommitPanel.class, "LBL_CommitDialog_FilesToCommit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hooksSectionButton, org.openide.util.NbBundle.getMessage(CommitPanel.class, "LBL_Advanced")); // NOI18N

        hooksSectionPanel.setLayout(new java.awt.BorderLayout());
        hooksSectionPanel.add(hooksTabbedPane, java.awt.BorderLayout.CENTER);

        filesPanel.setPreferredSize(new java.awt.Dimension(240, 108));

        org.jdesktop.layout.GroupLayout filesPanelLayout = new org.jdesktop.layout.GroupLayout(filesPanel);
        filesPanel.setLayout(filesPanelLayout);
        filesPanelLayout.setHorizontalGroup(
            filesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 614, Short.MAX_VALUE)
        );
        filesPanelLayout.setVerticalGroup(
            filesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 123, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(filesLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CTL_CommitForm_FilesToCommit")); // NOI18N

        org.jdesktop.layout.GroupLayout filesSectionPanelLayout = new org.jdesktop.layout.GroupLayout(filesSectionPanel);
        filesSectionPanel.setLayout(filesSectionPanelLayout);
        filesSectionPanelLayout.setHorizontalGroup(
            filesSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filesLabel)
            .add(filesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
        );
        filesSectionPanelLayout.setVerticalGroup(
            filesSectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filesSectionPanelLayout.createSequentialGroup()
                .add(filesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(hooksSectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                    .add(hooksSectionButton)
                    .add(filesSectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                    .add(filesSectionButton)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 480, Short.MAX_VALUE)
                        .add(recentLink)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(recentLink))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filesSectionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filesSectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hooksSectionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hooksSectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitPanel.class, "ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitPanel.class, "ACSD_CommitDialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JLabel filesLabel = new javax.swing.JLabel();
    final javax.swing.JPanel filesPanel = new javax.swing.JPanel();
    final javax.swing.JLabel filesSectionButton = new javax.swing.JLabel();
    final javax.swing.JPanel filesSectionPanel = new javax.swing.JPanel();
    final javax.swing.JLabel hooksSectionButton = new javax.swing.JLabel();
    final javax.swing.JPanel hooksSectionPanel = new javax.swing.JPanel();
    final javax.swing.JTabbedPane hooksTabbedPane = new javax.swing.JTabbedPane();
    final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    final javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    final javax.swing.JTextArea messageTextArea = new javax.swing.JTextArea();
    final javax.swing.JLabel recentLink = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

}
