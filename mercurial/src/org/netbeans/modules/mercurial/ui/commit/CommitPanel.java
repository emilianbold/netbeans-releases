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

package org.netbeans.modules.mercurial.ui.commit;

import java.awt.Component;
import java.awt.Container;
import javax.swing.Box;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
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
import org.netbeans.modules.mercurial.hooks.spi.HgHook;
import org.netbeans.modules.mercurial.hooks.spi.HgHookContext;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.openide.awt.Mnemonics;
import static java.awt.Component.BOTTOM_ALIGNMENT;
import static java.awt.Component.CENTER_ALIGNMENT;
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
    private static final boolean DEFAULT_DISPLAY_FILES = true;
    private static final boolean DEFAULT_DISPLAY_HOOKS = false;

    final JLabel filesLabel = new JLabel();
    final PlaceholderPanel progressPanel = new PlaceholderPanel();
    private final JPanel filesPanel = new JPanel(new GridLayout(1, 1));
    private final JLabel filesSectionButton = new JLabel();
    private final JPanel filesSectionPanel2 = new JPanel();
    private final PlaceholderPanel hookSectionPanel = new PlaceholderPanel();
    private final JLabel hooksSectionButton = new JLabel();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTextArea messageTextArea = new JTextArea();
    private final JLabel recentLink = new JLabel();
    private Icon expandedIcon, collapsedIcon;
    
    private CommitTable commitTable;
    private List<HgHook> hooks = Collections.emptyList();
    private HgHookContext hookContext;

    /** Creates new form CommitPanel */
    public CommitPanel() {
        initComponents();
        initInteraction();
    }

    void setCommitTable(CommitTable commitTable) {
        this.commitTable = commitTable;
    }
    
    void setErrorLabel(String htmlErrorLabel) {
        jLabel2.setText(htmlErrorLabel);
    }    

    @Override
    public void addNotify() {
        super.addNotify();

        HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);

        final List<String> messages = Utils.getStringList(HgModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (messages.size() > 0) {
                    messageTextArea.setText(messages.get(0));
                }
                messageTextArea.selectAll();
            }
        });

        initCollapsibleSections();
    }

    private void initCollapsibleSections() {
        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        expandedIcon = tvui.getExpandedIcon();
        collapsedIcon = tvui.getCollapsedIcon();

        initSectionButton(filesSectionButton, filesSectionPanel2,
                          "initFilesPanel",                             //NOI18N
                          DEFAULT_DISPLAY_FILES);
        if (!hooks.isEmpty()) {
            hooksSectionButton.setText((hooks.size() == 1)
                                       ? hooks.get(0).getDisplayName()
                                       : getMessage("LBL_Advanced"));   //NOI18N
            initSectionButton(hooksSectionButton, hookSectionPanel,
                              "initHooksPanel",                         //NOI18N
                              DEFAULT_DISPLAY_HOOKS);
        } else {
            hooksSectionButton.setVisible(false);
        }
    }

    private void initSectionButton(final JLabel label,
                                   final JPanel panel,
                                   final String initPanelMethodName,
                                   final boolean defaultSectionDisplayed) {
        if (defaultSectionDisplayed) {
            displaySection(label, panel, initPanelMethodName);
        } else {
            hideSection(label, panel);
        }
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (panel.isVisible()) {
                    hideSection(label, panel);
                } else {
                    displaySection(label, panel, initPanelMethodName);
                }
            }
        });
    }

    private void displaySection(JLabel sectionButton,
                                Container sectionPanel,
                                String initPanelMethodName) {
        if (sectionPanel.getComponentCount() == 0) {
            invokeInitPanelMethod(initPanelMethodName);
        }
        sectionPanel.setVisible(true);
        sectionButton.setIcon(expandedIcon);
        enlargeVerticallyAsNecessary();
    }

    private void hideSection(JLabel sectionButton,
                             JPanel sectionPanel) {
        sectionPanel.setVisible(false);
        sectionButton.setIcon(collapsedIcon);
    }

    private void invokeInitPanelMethod(String methodName) {
        try {
            getClass().getDeclaredMethod(methodName).invoke(this);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void initFilesPanel() {

        /* this method is called using reflection from 'invokeInitPanelMethod()' */

        filesPanel.add(commitTable.getComponent());
        filesPanel.setPreferredSize(new Dimension(0, 2 * messageTextArea.getPreferredSize().height));

        filesSectionPanel2.setLayout(new BoxLayout(filesSectionPanel2, Y_AXIS));
        filesSectionPanel2.add(filesLabel);
        filesSectionPanel2.add(makeVerticalStrut(filesLabel, filesPanel, RELATED));
        filesSectionPanel2.add(filesPanel);

        filesLabel.setAlignmentX(LEFT_ALIGNMENT);
        filesPanel.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void initHooksPanel() {

        /* this method is called using reflection from 'invokeInitPanelMethod()' */

        assert !hooks.isEmpty();

        if (hooks.size() == 1) {
            hookSectionPanel.add(hooks.get(0).createComponent(hookContext));
        } else {
            JTabbedPane hooksTabbedPane = new JTabbedPane();
            for (HgHook hook : hooks) {
                hooksTabbedPane.add(hook.createComponent(hookContext),
                                    hook.getDisplayName());
            }
            hookSectionPanel.add(hooksTabbedPane);
        }
    }

    String getCommitMessage() {
        return messageTextArea.getText();
    }

    @Override
    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        HgModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        super.removeNotify();
    }

    void setHooks(List<HgHook> hooks, HgHookContext context) {
        if (hooks == null) {
            hooks = Collections.emptyList();
        }
        this.hooks = hooks;
        this.hookContext = context;
    }

    private void onBrowseRecentMessages() {
        String message = StringSelector.select(getMessage("CTL_CommitForm_RecentTitle"),  // NOI18N
                                               getMessage("CTL_CommitForm_RecentPrompt"),  // NOI18N
            Utils.getStringList(HgModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES));
        if (message != null) {
            messageTextArea.replaceSelection(message);
        }
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            Runnable inAWT = new Runnable() {
                public void run() {
                    commitTable.dataChanged();
                    listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
                }
            };
            // this can be called from a background thread - e.g. change of exclusion status in Versioning view
            if (EventQueue.isDispatchThread()) {
                inAWT.run();
            } else {
                EventQueue.invokeLater(inAWT);
            }
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

        recentLink.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/recent_messages.png"))); // NOI18N
        recentLink.setToolTipText(getMessage("CTL_CommitForm_RecentMessages")); // NOI18N

        messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(80, 18));
        jScrollPane1.setViewportView(messageTextArea);
        messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N

        Mnemonics.setLocalizedText(filesSectionButton, getMessage("LBL_CommitDialog_FilesToCommit")); // NOI18N
        Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit")); // NOI18N

        Mnemonics.setLocalizedText(hooksSectionButton, getMessage("LBL_Advanced")); // NOI18N

        Mnemonics.setLocalizedText(jLabel2, "jLabel2");

        JPanel topPanel = new VerticallyNonResizingPanel();
        topPanel.setLayout(new BoxLayout(topPanel, X_AXIS));
        topPanel.add(jLabel1);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(recentLink);
        jLabel1.setAlignmentY(BOTTOM_ALIGNMENT);
        recentLink.setAlignmentY(BOTTOM_ALIGNMENT);

        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, X_AXIS));
        bottomPanel.add(jLabel2);
        bottomPanel.add(makeFlexibleHorizontalStrut(15, 90, Short.MAX_VALUE));
        bottomPanel.add(progressPanel);
        jLabel2.setAlignmentY(CENTER_ALIGNMENT);
        progressPanel.setAlignmentY(CENTER_ALIGNMENT);

        setLayout(new BoxLayout(this, Y_AXIS));
        add(topPanel);
        add(makeVerticalStrut(jLabel1, jScrollPane1, RELATED));
        add(jScrollPane1);
        add(makeVerticalStrut(jScrollPane1, filesSectionButton, RELATED));
        add(filesSectionButton);
        add(makeVerticalStrut(filesSectionButton, filesSectionPanel2, RELATED));
        add(filesSectionPanel2);
        add(makeVerticalStrut(filesSectionPanel2, hooksSectionButton, RELATED));
        add(hooksSectionButton);
        add(makeVerticalStrut(hooksSectionButton, hookSectionPanel, RELATED));
        add(hookSectionPanel);
        add(makeVerticalStrut(hookSectionPanel, jLabel2, RELATED));
        add(bottomPanel);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        jScrollPane1.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionPanel2.setAlignmentX(LEFT_ALIGNMENT);
        hooksSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        hookSectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        setBorder(createEmptyBorder(26,                       //top
                                    getContainerGap(WEST),    //left
                                    0,                        //bottom
                                    15));                     //right

        getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitDialog")); // NOI18N
    }// </editor-fold>

    private void initInteraction() {
        recentLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recentLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onBrowseRecentMessages();
            }
        });
    }

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

    private Component makeFlexibleHorizontalStrut(int minWidth,
                                                  int prefWidth,
                                                  int maxWidth) {
        return new Box.Filler(new Dimension(minWidth,  0),
                              new Dimension(prefWidth, 0),
                              new Dimension(maxWidth,  0));
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
