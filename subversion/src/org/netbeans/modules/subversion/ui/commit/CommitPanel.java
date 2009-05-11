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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

import java.awt.Component;
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
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.subversion.hooks.spi.SvnHookContext;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.awt.Mnemonics;
import static java.awt.Component.LEFT_ALIGNMENT;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import static org.jdesktop.layout.LayoutStyle.RELATED;

/**
 *
 * @author  pk97937
 * @author  Marian Petras
 */
public class CommitPanel extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener {

    static final Object EVENT_SETTINGS_CHANGED = new Object();

    final JLabel filesLabel = new JLabel();
    final JPanel filesPanel = new JPanel();
    final JLabel filesSectionButton = new JLabel();
    final JPanel filesSectionPanel = new JPanel();
    final JLabel hooksSectionButton = new JLabel();
    final PlaceholderPanel hooksSectionPanel = new PlaceholderPanel();
    final JTabbedPane hooksTabbedPane = new JTabbedPane();
    final JLabel jLabel1 = new JLabel();
    final JLabel jLabel2 = new JLabel();
    final JScrollPane jScrollPane1 = new JScrollPane();
    final JTextArea messageTextArea = new JTextArea();
    final JLabel recentLink = new JLabel();

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
            hooksSectionPanel.add(hook.createComponent(context));
            hooksSectionButton.setText(hook.getDisplayName());
        } else {
            hooksSectionPanel.add(hooksTabbedPane);
            hooksSectionButton.setText(getMessage("LBL_Advanced"));
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

        filesPanel.setPreferredSize(new Dimension(0, 2 * messageTextArea.getPreferredSize().height));

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
                    enlargeVerticallyAsNecessary();
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
        String message = StringSelector.select(getMessage("CTL_CommitForm_RecentTitle"),
                                               getMessage("CTL_CommitForm_RecentPrompt"),
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

    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Layout Code">
    private void initComponents() {

        jLabel1.setLabelFor(messageTextArea);
        Mnemonics.setLocalizedText(jLabel1, getMessage("CTL_CommitForm_Message")); // NOI18N

        recentLink.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/recent_messages.png"))); // NOI18N
        recentLink.setToolTipText(getMessage("CTL_CommitForm_RecentMessages")); // NOI18N

        messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(100, 18));
        jScrollPane1.setViewportView(messageTextArea);
        messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N

        Mnemonics.setLocalizedText(filesSectionButton, getMessage("LBL_CommitDialog_FilesToCommit")); // NOI18N
        Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit")); // NOI18N

        filesSectionPanel.setLayout(new BoxLayout(filesSectionPanel, Y_AXIS));
        filesSectionPanel.add(filesLabel);
        filesSectionPanel.add(makeVerticalStrut(filesLabel, filesPanel, RELATED));
        filesSectionPanel.add(filesPanel);
        filesLabel.setAlignmentX(LEFT_ALIGNMENT);
        filesPanel.setAlignmentX(LEFT_ALIGNMENT);

        Mnemonics.setLocalizedText(hooksSectionButton, getMessage("LBL_Advanced")); // NOI18N

        JPanel topPanel = new VerticallyNonResizingPanel();
        topPanel.setLayout(new BoxLayout(topPanel, X_AXIS));
        topPanel.add(jLabel1);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(recentLink);
        jLabel1.setAlignmentY(BOTTOM_ALIGNMENT);
        recentLink.setAlignmentY(BOTTOM_ALIGNMENT);

        setLayout(new BoxLayout(this, Y_AXIS));
        add(topPanel);
        add(makeVerticalStrut(jLabel1, jScrollPane1, RELATED));
        add(jScrollPane1);
        add(makeVerticalStrut(jScrollPane1, filesSectionButton, RELATED));
        add(filesSectionButton);
        add(makeVerticalStrut(filesSectionButton, filesSectionPanel, RELATED));
        add(filesSectionPanel);
        add(makeVerticalStrut(filesSectionPanel, hooksSectionButton, RELATED));
        add(hooksSectionButton);
        add(makeVerticalStrut(hooksSectionButton, hooksSectionPanel, RELATED));
        add(hooksSectionPanel);
        add(makeVerticalStrut(hooksSectionPanel, jLabel2, RELATED));
        add(jLabel2);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        jScrollPane1.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        hooksSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        hooksSectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        jLabel2.setAlignmentX(LEFT_ALIGNMENT);

        setBorder(createEmptyBorder(26,                       //top
                                    getContainerGap(WEST),    //left
                                    0,                        //bottom
                                    15));                     //right

        getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitDialog")); // NOI18N
    }// </editor-fold>

    private Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        int relatedUnrelated) {
        int height = LayoutStyle.getSharedInstance().getPreferredGap(
                            compA,
                            compB,
                            relatedUnrelated,
                            SOUTH,
                            this);
        return Box.createVerticalStrut(height);
    }

    private int getContainerGap(int direction) {
        return LayoutStyle.getSharedInstance().getContainerGap(this,
                                                               direction,
                                                               null);
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(CommitPanel.class, msgKey);
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

}
