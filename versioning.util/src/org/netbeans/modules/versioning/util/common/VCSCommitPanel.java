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

import java.awt.EventQueue;
import java.util.Map;
import java.awt.BorderLayout;
import java.util.prefs.Preferences;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.openide.cookies.EditorCookie;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import javax.swing.Box;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.cookies.SaveCookie;
import org.openide.util.NbBundle;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
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
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.versioning.hooks.VCSHook;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
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
 * @author  Tomas Stupka
 * @author  Marian Petras
 */
public class VCSCommitPanel extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener, ChangeListener {

    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";    // NOPI18N
    
    static final Object EVENT_SETTINGS_CHANGED = new Object();   
    
    private final AutoResizingPanel basePanel = new AutoResizingPanel();

    final PlaceholderPanel progressPanel = new PlaceholderPanel();
    private final JLabel errorLabel = new JLabel();
    private final JPanel parametersPane1 = new JPanel();
    
    private VCSCommitTable commitTable;
    
    private JTabbedPane tabbedPane;
//    private HashMap<File, MultiDiffPanel> displayedDiffs = new HashMap<File, MultiDiffPanel>();
        
    private final Preferences preferences;
    private final VCSCommitParameters parameters;

    /** Creates new form CommitPanel */
    public VCSCommitPanel(VCSCommitParameters parameters, Preferences preferences, Collection<? extends VCSHook> hooks, VCSHookContext hooksContext) {
        this.parameters = parameters;
        this.commitTable = new VCSCommitTable(new VCSCommitTableModel());
        
        if(hooks == null) {
            hooks = Collections.emptyList();
        }
        initComponents(hooks, hooksContext);

        commitTable.setCommitPanel(this);
        this.preferences = preferences;
    }

    public VCSCommitTable getCommitTable() {
        return commitTable;
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

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {        
        if (evt.getKey().startsWith(PROP_COMMIT_EXCLUSIONS)) { // XXX - need setting
            Runnable inAWT = new Runnable() {
                @Override
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

    @Override
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Layout Code">
    private void initComponents(Collection<? extends VCSHook> hooks, VCSHookContext hooksContext) {
        getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitDialog")); // NOI18N
        
        basePanel.setBorder(createEmptyBorder(10,             // top
                                    getContainerGap(WEST),    // left
                                    0,                        // bottom
                                    getContainerGap(EAST)));  // right
        
        basePanel.setLayout(new BoxLayout(basePanel, Y_AXIS));
        
        // parameters panel -> holds all commit parameters specific 
        // for the given VCS system - message, switches, etc.
        parametersPane1.setLayout(new BorderLayout());
        parametersPane1.add(parameters.getPanel());                
        parametersPane1.setAlignmentX(LEFT_ALIGNMENT);        
        basePanel.add(parametersPane1);
        
        // files table
        FilesPanel filesPanel = new FilesPanel();
        basePanel.add(makeVerticalStrut(parametersPane1, filesPanel, RELATED, this));        
        basePanel.add(filesPanel);
        
        // hooks area
        if(!hooks.isEmpty()) {            
            HookPanel hooksPanel = new HookPanel(hooks, hooksContext);                                                              
            basePanel.add(makeVerticalStrut(filesPanel, hooksPanel, RELATED, this));
            hooksPanel.setAlignmentX(LEFT_ALIGNMENT);
            basePanel.add(hooksPanel);
            basePanel.add(makeVerticalStrut(hooksPanel, errorLabel, RELATED, this));
        } else {
            basePanel.add(makeVerticalStrut(filesPanel, errorLabel, RELATED, this));            
        }
        
        // bottom panel -> error label, progres, ...
        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, X_AXIS));
        bottomPanel.add(errorLabel);
        bottomPanel.add(makeFlexibleHorizontalStrut(15, 90, Short.MAX_VALUE));
        bottomPanel.add(progressPanel);
        errorLabel.setAlignmentY(CENTER_ALIGNMENT);                
        errorLabel.setText("");
        progressPanel.setAlignmentY(CENTER_ALIGNMENT);        
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        basePanel.add(bottomPanel);
        setLayout(new BoxLayout(this, Y_AXIS));
        add(basePanel);                
    }// </editor-fold>

    static Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        ComponentPlacement relatedUnrelated, 
                                        JPanel parent) {
        int height = LayoutStyle.getInstance().getPreferredGap(
                            compA,
                            compB,
                            relatedUnrelated,
                            SOUTH,
                            parent);
        return Box.createVerticalStrut(height);
    }

    private static Component makeFlexibleHorizontalStrut(int minWidth,
                                                  int prefWidth,
                                                  int maxWidth) {
        return new Box.Filler(new Dimension(minWidth,  0),
                              new Dimension(prefWidth, 0),
                              new Dimension(maxWidth,  0));
    }

    static Component makeHorizontalStrut(JComponent compA,
                                              JComponent compB,
                                              ComponentPlacement relatedUnrelated,
                                              JPanel parent) {
            int width = LayoutStyle.getInstance().getPreferredGap(
                                compA,
                                compB,
                                relatedUnrelated,
                                WEST,
                                parent);
            return Box.createHorizontalStrut(width);
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

    private abstract class CollapsiblePanel extends JPanel {
        private final Icon expandedIcon;
        private final Icon collapsedIcon;
        protected final JLabel sectionButton;
        protected final JPanel sectionPanel;
        private boolean defaultSectionDisplayed;
        
        protected static final boolean DEFAULT_DISPLAY_FILES = true;
        protected static final boolean DEFAULT_DISPLAY_HOOKS = false;
    
        public CollapsiblePanel(boolean defaultSectionDisplayed) {
            this.sectionButton = new JLabel();
            this.sectionPanel = new JPanel();
            this.defaultSectionDisplayed = defaultSectionDisplayed;
            
            JTree tv = new JTree();
            BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
            expandedIcon = tvui.getExpandedIcon();
            collapsedIcon = tvui.getCollapsedIcon();            
                                    
            setLayout(new BoxLayout(this, Y_AXIS));
            add(sectionButton);
//            add(makeVerticalStrut(sectionButton, sectionPanel, RELATED, VCSCommitPanel.this));
            add(sectionPanel);    
         
            sectionPanel.setBorder(createEmptyBorder(10,         // top
                                    getContainerGap(WEST) * 2,   // left
                                    0,                           // bottom
                                    getContainerGap(EAST) * 2)); // right
            
            setAlignmentX(LEFT_ALIGNMENT);
            sectionPanel.setLayout(new BoxLayout(sectionPanel, Y_AXIS));
            sectionPanel.setAlignmentX(LEFT_ALIGNMENT);            
            sectionButton.setAlignmentX(LEFT_ALIGNMENT);
        }
        
        protected void initSection() {
            if (defaultSectionDisplayed) {
                displaySection();
            } else {
                hideSection();
            }
            sectionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (sectionPanel.isVisible()) {
                        hideSection();
                    } else {
                        displaySection();
                    }
                }
            });                   
        }    
        
        private void displaySection() {
            sectionPanel.setVisible(true);
            sectionButton.setIcon(expandedIcon);
            enlargeVerticallyAsNecessary();
        }

        private void hideSection() {
            sectionPanel.setVisible(false);
            sectionButton.setIcon(collapsedIcon);
        }        
    }
    
    private class FilesPanel extends CollapsiblePanel {
        final JLabel filesLabel = new JLabel();        
        
        public FilesPanel() {
            super(DEFAULT_DISPLAY_FILES);
            
            Mnemonics.setLocalizedText(sectionButton, getMessage("LBL_CommitDialog_FilesToCommit"));    // NOI18N            
                        
            JComponent table = commitTable.getComponent();
            
            filesLabel.setLabelFor(table);
            Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit"));         // NOI18N
            table.setPreferredSize(new Dimension(0, 2 * parameters.getPanel().getPreferredSize().height));
            
            sectionPanel.setAlignmentX(LEFT_ALIGNMENT);
            sectionPanel.add(filesLabel);
            sectionPanel.add(makeVerticalStrut(filesLabel, table, RELATED, sectionPanel));
            sectionPanel.add(table);
                        
            initSection();
        }

    }
    
    private class HookPanel extends CollapsiblePanel {
            
        private Collection<? extends VCSHook> hooks = Collections.emptyList();
        private VCSHookContext hookContext;    

        public HookPanel(Collection<? extends VCSHook> hooks, VCSHookContext hookContext) {            
            super(DEFAULT_DISPLAY_HOOKS);
            this.hooks = hooks;
            this.hookContext = hookContext;
            
            sectionButton.setText((hooks.size() == 1)
                                           ? hooks.iterator().next().getDisplayName()
                                           : getMessage("LBL_Advanced"));   //NOI18N                        
            initSection();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // need this to happen in addNotify() - depends on how 
            // repositoryComboSupport in hook.createComponents works for bugzilla|jira
            if (hooks.size() == 1) {                
                sectionPanel.add(hooks.iterator().next().createComponent(hookContext));
            } else {
                JTabbedPane hooksTabbedPane = new JTabbedPane();
                for (VCSHook hook : hooks) {
                    hooksTabbedPane.add(hook.createComponent(hookContext), hook.getDisplayName());
                }
                sectionPanel.add(hooksTabbedPane);
            }                
        }
    }
    
}
