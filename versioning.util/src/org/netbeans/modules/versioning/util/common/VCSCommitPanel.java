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

package org.netbeans.modules.versioning.util.common;

import org.netbeans.modules.versioning.util.TemplateSelector;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.openide.cookies.EditorCookie;
import javax.swing.LayoutStyle;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.awt.Container;
import javax.swing.Box;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 *
 * @author  pk97937
 * @author  Marian Petras
 */
public class VCSCommitPanel extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener, ChangeListener {

    private final AutoResizingPanel basePanel = new AutoResizingPanel();
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
    private final JLabel errorLabel = new JLabel();
        
    private final JPanel parametersPane1 = new JPanel();

    private Icon expandedIcon, collapsedIcon;
    
    private VCSCommitTable commitTable;
    private Collection<HgHook> hooks = Collections.emptyList();
    private HgHookContext hookContext;
    private JTabbedPane tabbedPane;
//    private HashMap<File, MultiDiffPanel> displayedDiffs = new HashMap<File, MultiDiffPanel>();
    private final Preferences preferences;
    private final VCSCommitParameters parameters;

    /** Creates new form CommitPanel */
    public VCSCommitPanel(VCSCommitParameters parameters, VCSCommitTable commitTable, Preferences preferences) {
        this.parameters = parameters;
        this.commitTable = commitTable;

        initComponents();

        commitTable.setCommitPanel(this);
        filesLabel.setLabelFor(commitTable.getTable());
        this.preferences = preferences;
    }

    public PlaceholderPanel getProgressPanel() {
        return progressPanel;
    }
    
    public void setErrorLabel(String htmlErrorLabel) {
        errorLabel.setText(htmlErrorLabel);
    }    

    @Override
    public void addNotify() {
        super.addNotify();
        
        preferences.addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
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
                                       ? hooks.iterator().next().getDisplayName()
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
        filesPanel.setPreferredSize(new Dimension(0, 2 * parameters.getPanel().getPreferredSize().height));

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
            hookSectionPanel.add(hooks.iterator().next().createComponent(hookContext));
        } else {
            JTabbedPane hooksTabbedPane = new JTabbedPane();
            for (HgHook hook : hooks) {
                hooksTabbedPane.add(hook.createComponent(hookContext),
                                    hook.getDisplayName());
            }
            hookSectionPanel.add(hooksTabbedPane);
        }
    }

    public VCSCommitParameters getParameters() {
        return parameters;
    }

    @Override
    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        preferences.removePreferenceChangeListener(this);
        super.removeNotify();
    }

    public void setHooks(Collection<HgHook> hooks, HgHookContext context) {
        if (hooks == null) {
            hooks = Collections.emptyList();
        }
        this.hooks = hooks;
        this.hookContext = context;
    }


    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        // XXX
//        if (evt.getKey().startsWith(GitModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
//            Runnable inAWT = new Runnable() {
//                @Override
//                public void run() {
//                    commitTable.dataChanged();
//                    listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
//                }
//            };
//            // this can be called from a background thread - e.g. change of exclusion status in Versioning view
//            if (EventQueue.isDispatchThread()) {
//                inAWT.run();
//            } else {
//                EventQueue.invokeLater(inAWT);
//            }
//        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Layout Code">
    private void initComponents() {
        parametersPane1.setLayout(new BorderLayout());
        parametersPane1.add(parameters.getPanel());

        Mnemonics.setLocalizedText(filesSectionButton, getMessage("LBL_CommitDialog_FilesToCommit")); // NOI18N
        Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit")); // NOI18N

        Mnemonics.setLocalizedText(hooksSectionButton, getMessage("LBL_Advanced")); // NOI18N

        Mnemonics.setLocalizedText(errorLabel, "jLabel2");
        
        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, X_AXIS));
        bottomPanel.add(errorLabel);
        bottomPanel.add(makeFlexibleHorizontalStrut(15, 90, Short.MAX_VALUE));
        bottomPanel.add(progressPanel);
        errorLabel.setAlignmentY(CENTER_ALIGNMENT);        
        errorLabel.setText("");
        progressPanel.setAlignmentY(CENTER_ALIGNMENT);
        
        basePanel.setLayout(new BoxLayout(basePanel, Y_AXIS));
        basePanel.add(parametersPane1);
        basePanel.add(makeVerticalStrut(parametersPane1, filesSectionButton, RELATED));
        basePanel.add(filesSectionButton);
        basePanel.add(makeVerticalStrut(filesSectionButton, filesSectionPanel2, RELATED));
        basePanel.add(filesSectionPanel2);
        basePanel.add(makeVerticalStrut(filesSectionPanel2, hooksSectionButton, RELATED));
        basePanel.add(hooksSectionButton);
        basePanel.add(makeVerticalStrut(hooksSectionButton, hookSectionPanel, RELATED));
        basePanel.add(hookSectionPanel);
        basePanel.add(makeVerticalStrut(hookSectionPanel, errorLabel, RELATED));
        basePanel.add(bottomPanel);
        setLayout(new BoxLayout(this, Y_AXIS));
        add(basePanel);
        
        parametersPane1.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionPanel2.setAlignmentX(LEFT_ALIGNMENT);
        hooksSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        hookSectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        basePanel.setBorder(createEmptyBorder(10,             //top
                                    getContainerGap(WEST),    //left
                                    0,                        //bottom
                                    getContainerGap(EAST)));  //right

        getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitDialog")); // NOI18N
    }// </editor-fold>

    private Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        ComponentPlacement relatedUnrelated) {
        int height = LayoutStyle.getInstance().getPreferredGap(
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
        return LayoutStyle.getInstance().getContainerGap(this,
                                                               direction,
                                                               null);
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(VCSCommitPanel.class, msgKey);
    }
    
    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tabbedPane && tabbedPane.getSelectedComponent() == basePanel) {
            commitTable.setModifiedFiles(new HashSet<File>(getModifiedFiles().keySet()));
        }
    }

    void openDiff (VCSFileNode[] nodes) {
        // XXX
//        for (VCSFileNode node : nodes) {
//            if (tabbedPane == null) {
//                initializeTabs();
//            }
//            File file = node.getFile();
//            MultiDiffPanel panel = displayedDiffs.get(file);
//            if (panel == null) {
//                panel = new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
//                displayedDiffs.put(file, panel);
//                tabbedPane.addTab(file.getName(), panel);
//            }
//            tabbedPane.setSelectedComponent(panel);
//        }
//        revalidate();
//        repaint();
    }

    /**
     * Returns save cookies available for files in the commit table
     * @return
     */
    SaveCookie[] getSaveCookies() {
        return getModifiedFiles().values().toArray(new SaveCookie[0]);
    }

    /**
     * Returns editor cookies available for modified and not open files in the commit table
     * @return
     */
    EditorCookie[] getEditorCookies() {
        LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
// XXX      
//        for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
//            EditorCookie[] cookies = e.getValue().getEditorCookies(true);
//            if (cookies.length > 0) {
//                allCookies.add(cookies[0]);
//            }
//        }
        return allCookies.toArray(new EditorCookie[allCookies.size()]);
    }

    /**
     * Returns true if trying to commit from the commit tab or the user confirmed his action
     * @return
     */
    boolean canCommit() {
        boolean result = true;
        if (tabbedPane != null && tabbedPane.getSelectedComponent() != basePanel) {
            NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(VCSCommitPanel.class, "MSG_CommitDialog_CommitFromDiff"), //NOI18N
                    NbBundle.getMessage(VCSCommitPanel.class, "LBL_CommitDialog_CommitFromDiff"), //NOI18N
                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, NotifyDescriptor.YES_OPTION);
            result = NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
        }
        return result;
    }

    private void initializeTabs () {
         tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
         tabbedPane.addTab(NbBundle.getMessage(VCSCommitPanel.class, "CTL_CommitDialog_Tab_Commit"), basePanel); //NOI18N
         tabbedPane.setPreferredSize(basePanel.getPreferredSize());
         add(tabbedPane);
         tabbedPane.addChangeListener(this);
    }

    private HashMap<File, SaveCookie> getModifiedFiles () {
        HashMap<File, SaveCookie> modifiedFiles = new HashMap<File, SaveCookie>();
//         XXX
//        for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
//            SaveCookie[] cookies = e.getValue().getSaveCookies(false);
//            if (cookies.length > 0) {
//                modifiedFiles.put(e.getKey(), cookies[0]);
//            }
//        }
        return modifiedFiles;
    }
    
}
