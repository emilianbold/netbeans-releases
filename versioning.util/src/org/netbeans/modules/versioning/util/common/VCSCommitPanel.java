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

import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import java.util.Set;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import javax.swing.Box;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.util.NbBundle;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.Dimension;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.hooks.VCSHook;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.BoxLayout.X_AXIS;
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
public abstract class VCSCommitPanel extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener, ChangeListener {

    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";    // NOPI18N
    
    static final Object EVENT_SETTINGS_CHANGED = new Object();   
    
    private final AutoResizingPanel basePanel = new AutoResizingPanel();

    final JPanel progressPanel = new JPanel();
    private final JLabel errorLabel = new JLabel();
    private final JPanel parametersPane1 = new JPanel();
    
    private final JButton commitButton = new JButton();
    private final JButton cancelButton = new JButton();
        
    private VCSCommitTable commitTable;
    
    private JTabbedPane tabbedPane;
        
    private final Preferences preferences;
    private final VCSCommitParameters parameters;
    private final Map<String, VCSCommitFilter> filters = new LinkedHashMap<String, VCSCommitFilter>();
    private final MultiDiffProvider diffProvider;

    /** Creates new form CommitPanel */
    public VCSCommitPanel(VCSCommitParameters parameters, Preferences preferences, Collection<? extends VCSHook> hooks, VCSHookContext hooksContext, List<VCSCommitFilter> filters, MultiDiffProvider diffProvider) {
        this.parameters = parameters;
        this.commitTable = new VCSCommitTable(new VCSCommitTableModel());
        this.diffProvider = diffProvider;
        
        boolean selected = false;
        for (VCSCommitFilter f : filters) {
            assert (f.isSelected() && !selected) || !f.isSelected();
            selected = f.isSelected();
            this.filters.put(f.getID(), f);
        }       
        
        if(hooks == null) {
            hooks = Collections.emptyList();
        }
        initComponents(hooks, hooksContext, filters);

        commitTable.setCommitPanel(this);
        this.preferences = preferences;
    }

    protected abstract void commitTableChanged();
    
    public VCSCommitTable getCommitTable() {
        return commitTable;
    }
    
    public VCSCommitFilter getSelectedFilter() {
        for (VCSCommitFilter f : filters.values()) {
            if(f.isSelected()) {
                return f;
            }
        }
        assert false : "no filter selected"; // there always must be one
        return null;
    }

    private JPanel getProgressPanel() {
        return progressPanel;
    }
    
    protected void stopProgress() {
        JPanel p = getProgressPanel();
        p.removeAll();
        p.setVisible(false);
    }

    protected void startProgress(String message, JComponent progressComponent) {
        JPanel p = getProgressPanel();
        p.setLayout(new BoxLayout(p, X_AXIS));
        JLabel l = new JLabel(message);
        p.add(l);
        p.add(makeHorizontalStrut(l, progressComponent, RELATED, p));
        p.add(progressComponent);       
        p.setVisible(true);
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
    private void initComponents(Collection<? extends VCSHook> hooks, VCSHookContext hooksContext, Collection<VCSCommitFilter> filters) {
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "CTL_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSN_Commit_Action_Commit"));
        commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSD_Commit_Action_Commit"));
        
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "CTL_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSN_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSD_Commit_Action_Cancel"));        
        
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
//            basePanel.add(makeVerticalStrut(filesPanel, hooksPanel, RELATED, this));
            hooksPanel.setAlignmentX(LEFT_ALIGNMENT);
            basePanel.add(hooksPanel);
            basePanel.add(makeVerticalStrut(hooksPanel, errorLabel, RELATED, this));
        } else {
            basePanel.add(makeVerticalStrut(filesPanel, errorLabel, RELATED, this));            
        }
        
        // bottom panel -> error label, progres, ...
        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, Y_AXIS));
        bottomPanel.add(progressPanel);
//        bottomPanel.add(makeFlexibleHorizontalStrut(15, 90, Short.MAX_VALUE));
        bottomPanel.add(errorLabel);
        errorLabel.setAlignmentY(CENTER_ALIGNMENT);                
        errorLabel.setText("");
        progressPanel.setAlignmentY(LEFT_ALIGNMENT);        
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setBorder(createEmptyBorder(10,           // top
                                    getContainerGap(WEST),    // left
                                    0,                        // bottom
                                    getContainerGap(EAST)));  // right
        
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
            if(diffProvider != null) {
                commitTable.setModifiedFiles(diffProvider.getModifiedFiles());
            }
        }
    }

    void openDiff (VCSFileNode[] nodes) {
        if(diffProvider == null) {
            return;
        }
        boolean newDiff = false;
        for (VCSFileNode node : nodes) {
            if (tabbedPane == null) {
                tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
                 tabbedPane.addTab(NbBundle.getMessage(VCSCommitPanel.class, "CTL_CommitDialog_Tab_Commit"), basePanel); //NOI18N
                 tabbedPane.setPreferredSize(basePanel.getPreferredSize());
                 add(tabbedPane);
                 tabbedPane.addChangeListener(this);                
            }
            File file = node.getFile();
            JComponent component = diffProvider.getDiffComponent(file); //new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
            if (component != null) {                
                tabbedPane.addTab(file.getName(), component);
                tabbedPane.setSelectedComponent(component);
                newDiff = true;
            }
        }
        if(newDiff) {
            revalidate();
            repaint();
        }
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

    public boolean open(VCSContext context, HelpCtx helpCtx) {
       
        String contentTitle = Utils.getContextDisplayName(context);
        
        final DialogDescriptor dd = new DialogDescriptor(this,
              org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "CTL_CommitDialog_Title", contentTitle), // NOI18N
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              helpCtx,
              null);
        ActionListener al;
        dd.setButtonListener(al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dd.setClosingOptions(new Object[] {commitButton, cancelButton});
                SaveCookie[] saveCookies = diffProvider.getSaveCookies();
                if (cancelButton == e.getSource()) {
                    if (saveCookies.length > 0) {
                        if (SaveBeforeClosingDiffConfirmation.allSaved(saveCookies) || !isShowing()) {
                            EditorCookie[] editorCookies = diffProvider.getEditorCookies();
                            for (EditorCookie cookie : editorCookies) {
                                cookie.open();
                            }
                        } else {
                            dd.setClosingOptions(new Object[0]);
                        }
                    }
                    dd.setValue(cancelButton);
                } else if (commitButton == e.getSource()) {
                    if (saveCookies.length > 0 && !SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
                        dd.setClosingOptions(new Object[0]);
                    } else if (!canCommit()) {
                        dd.setClosingOptions(new Object[0]);
                    }
                    dd.setValue(commitButton);
                }
            }
        });
        
        final VCSCommitTable table = getCommitTable();
        computeNodes();
        addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                commitTableChanged();
            }
        });
        table.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                commitTableChanged();
            }
        });

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        dialog.addWindowListener(new DialogBoundsPreserver(preferences, "git.commit.dialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);
        
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
        }
        return dd.getValue() == commitButton;
    }

    protected boolean isCommitButtonEnabled() {
        return commitButton.isEnabled();
    }

    protected void enableCommitButton(boolean enabled) {
        commitButton.setEnabled(enabled);
    }

    protected abstract void computeNodes();
    
    private abstract class CollapsiblePanel extends JPanel {
        protected final CategoryButton sectionButton;
        protected final JPanel sectionPanel;
                
        public CollapsiblePanel(boolean defaultSectionDisplayed) {
            ActionListener al = new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e) {
                   if (sectionPanel.isVisible()) {
                       hideSection();
                   } else {
                       displaySection();
                   }
               }
            };
            
            this.sectionButton = new CategoryButton(al);
            this.sectionPanel = new JPanel();
            
            this.sectionButton.setSelected(defaultSectionDisplayed);
            
            setLayout(new BoxLayout(this, Y_AXIS));
            sectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionButton.getMaximumSize().height));
            add(sectionButton);
            add(makeVerticalStrut(sectionButton, sectionPanel, RELATED, VCSCommitPanel.this));
            add(sectionPanel);  
            
            setAlignmentX(LEFT_ALIGNMENT);
            sectionPanel.setLayout(new BoxLayout(sectionPanel, Y_AXIS));
            sectionPanel.setAlignmentX(LEFT_ALIGNMENT);            
            sectionButton.setAlignmentX(LEFT_ALIGNMENT);
            
            Icon i = sectionButton.getIcon();
            Border b = sectionButton.getBorder();
            int left = (b != null ? b.getBorderInsets(sectionButton).left : 0) + (i != null ? i.getIconWidth() : 16) + sectionButton.getIconTextGap();
            int bottom = getContainerGap(SOUTH);
            sectionPanel.setBorder(createEmptyBorder(0,     // top
                                    left,                   // left
                                    bottom,                 // bottom
                                    0));                    // right
            
            if(defaultSectionDisplayed) {
                displaySection();
            } else {
                hideSection();
            }
        }
        
        private void displaySection() {
            sectionPanel.setVisible(true);
            enlargeVerticallyAsNecessary();
        }

        private void hideSection() {
            sectionPanel.setVisible(false);            
        }        
    }
    
    private class FilesPanel extends CollapsiblePanel implements ActionListener {
        public static final String TOOLBAR_FILTER = "toolbar.filter";           // NOI18N
        final JLabel filesLabel = new JLabel();        
        private static final boolean DEFAULT_DISPLAY_FILES = true;
        private final JToolBar toolbar;
        public FilesPanel()  {
            super(DEFAULT_DISPLAY_FILES);
            
            Mnemonics.setLocalizedText(sectionButton, getMessage("LBL_CommitDialog_FilesToCommit"));    // NOI18N            
                        
            JComponent table = commitTable.getComponent();
            
            filesLabel.setLabelFor(table);
            filesLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, filesLabel.getMaximumSize().height));
            
            Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit"));         // NOI18N
            table.setPreferredSize(new Dimension(0, 2 * parameters.getPanel().getPreferredSize().height));
            
            ButtonGroup bg = new ButtonGroup();
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            for (VCSCommitFilter filter : filters.values()) {
                
                JToggleButton tgb = new JToggleButton();
                tgb.setIcon(filter.getIcon()); 
                tgb.setToolTipText(filter.getTooltip()); 
                tgb.setFocusable(false);
                tgb.setSelected(filter.isSelected());
                tgb.addActionListener(this);                
                tgb.putClientProperty(TOOLBAR_FILTER, filter);
                bg.add(tgb);
                toolbar.add(tgb);
                
            }
            toolbar.setAlignmentX(LEFT_ALIGNMENT);        
            
            sectionPanel.add(toolbar);
            sectionPanel.add(table);
            sectionPanel.add(makeVerticalStrut(filesLabel, table, RELATED, sectionPanel));
            sectionPanel.add(filesLabel);
            
            sectionPanel.setAlignmentX(LEFT_ALIGNMENT);
            filesLabel.setAlignmentX(LEFT_ALIGNMENT);
            table.setAlignmentX(LEFT_ALIGNMENT);
        }        

        @Override
        public void actionPerformed(ActionEvent e) {
            Object s = e.getSource();            
            if(s instanceof JToggleButton) {
                JToggleButton tgb = (JToggleButton)s;
                Object o = tgb.getClientProperty(TOOLBAR_FILTER);
                assert o != null;
                if(o != null) {
                    VCSCommitFilter f = (VCSCommitFilter) o;
                    boolean selected = tgb.isSelected();
                    if(selected != f.isSelected()) {
                        f.setSelected(selected);
                        String id = f.getID();                        
                        for (VCSCommitFilter filter : filters.values()) {
                            if(!filter.getID().equals(id)) {
                                filter.setSelected(!selected);
                            }
                        }
                        computeNodes();
                    } else {
                        return;
                    }
                } 
            }
        }
    }
    
    private class HookPanel extends CollapsiblePanel {
            
        private Collection<? extends VCSHook> hooks = Collections.emptyList();
        private VCSHookContext hookContext;    
        private static final boolean DEFAULT_DISPLAY_HOOKS = false;
        
        public HookPanel(Collection<? extends VCSHook> hooks, VCSHookContext hookContext) {            
            super(DEFAULT_DISPLAY_HOOKS);
            this.hooks = hooks;
            this.hookContext = hookContext;
            
            sectionButton.setText((hooks.size() == 1)
                                           ? hooks.iterator().next().getDisplayName()
                                           : getMessage("LBL_Advanced"));   //NOI18N                        
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

    public abstract static class MultiDiffProvider {
        private HashMap<File, JComponent> displayedDiffs = new HashMap<File, JComponent>();
        
        
        JComponent getDiffComponent(File file) {
            JComponent component = displayedDiffs.get(file);
            if (component == null) {
                component = createDiffComponent(file); //new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, false); // switch the last parameter to true if editable diff works poorly
                displayedDiffs.put(file, component);                
            }   
            return component;
        }

        protected abstract JComponent createDiffComponent(File file);
        
        protected Set<File> getModifiedFiles() {
            return Collections.emptySet();
        }
        
        protected SaveCookie[] getSaveCookies() {
            return new SaveCookie[0];
        }
        
        protected EditorCookie[] getEditorCookies() {
            return new EditorCookie[0];
        }
    }

    // inspired by org.netbeans.modules.palette.ui.CategoryButton
    private class CategoryButton extends JCheckBox {

        final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() );
        final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() );
        final boolean isAqua = "Aqua".equals( UIManager.getLookAndFeel().getID() );
        private final ActionListener al;


        @Override
        public String getUIClassID() {
            String classID = super.getUIClassID();
            if (isGTK) {
                classID = "MetalCheckBoxUI_4_GTK";
            }
            return classID;
        }

        CategoryButton(ActionListener al) {
            this.al = al;
            if (isGTK) {
                UIManager.put("MetalCheckBoxUI_4_GTK", "javax.swing.plaf.metal.MetalCheckBoxUI");
            }

            //force initialization of PropSheet look'n'feel values 
            UIManager.get( "nb.propertysheet" );

            setFont( getFont().deriveFont( Font.BOLD ) );
            setMargin(new Insets(0, 3, 0, 3));
            setFocusPainted( false );

            setHorizontalAlignment( SwingConstants.LEFT );
            setHorizontalTextPosition( SwingConstants.RIGHT );
            setVerticalTextPosition( SwingConstants.CENTER );

            updateProperties();

            if( getBorder() instanceof CompoundBorder ) { // from BasicLookAndFeel
                Dimension pref = getPreferredSize();
                pref.height -= 3;
                setPreferredSize( pref );
            }

            addActionListener(al);
            initActions();
        }

        private void initActions() {
            InputMap inputMap = getInputMap( WHEN_FOCUSED );
            inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0, false ), "collapse" ); //NOI18N
            inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0, false ), "expand" ); //NOI18N

            ActionMap actionMap = getActionMap();
            actionMap.put( "collapse", new ExpandAction( false ) ); //NOI18N
            actionMap.put( "expand", new ExpandAction( true ) ); //NOI18N
        }

        private void updateProperties() {
            setIcon( UIManager.getIcon(isGTK ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon") );
            setSelectedIcon( UIManager.getIcon(isGTK ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon") );
            Mnemonics.setLocalizedText( this, getText() );
            getAccessibleContext().setAccessibleName( getText() );
            getAccessibleContext().setAccessibleDescription( getText() );
            if( isAqua ) {
                setContentAreaFilled(true);
                setOpaque(true);
                setBackground( new Color(0,0,0) );
                setForeground( new Color(255,255,255) );
            }
            if( isNimbus ) {
                setOpaque(true);
                setContentAreaFilled(true);
            }
        }

        private boolean isExpanded() {
            return isSelected();
        }

        private void setExpanded( boolean expand ) {
            setSelected(expand);
            requestFocus ();
        }

        @Override
        public Color getBackground() {
            if( isFocusOwner() ) {
                if( isGTK || isNimbus )
                    return UIManager.getColor("Tree.selectionBackground"); //NOI18N
                return UIManager.getColor( "PropSheet.selectedSetBackground" ); //NOI18N
            } else {
                if( isAqua ) {
                    Color defBk = UIManager.getColor("NbExplorerView.background");
                    if( null == defBk )
                        defBk = Color.gray;
                    return new Color( defBk.getRed()-10, defBk.getGreen()-10, defBk.getBlue()-10);
                }
                if( isGTK || isNimbus ) {
                    if( getModel().isRollover() )
                        return new Color( UIManager.getColor( "Menu.background" ).getRGB() ).darker(); //NOI18N
                    return new Color( UIManager.getColor( "Menu.background" ).getRGB() );//NOI18N
                }
                return UIManager.getColor( "PropSheet.setBackground" ); //NOI18N
            }
        }

        @Override
        public Color getForeground() {
            if( isFocusOwner() ) {
                if( isAqua )
                    return UIManager.getColor( "Table.foreground" ); //NOI18N
                else if( isGTK || isNimbus )
                    return UIManager.getColor( "Tree.selectionForeground" ); //NOI18N
                return UIManager.getColor( "PropSheet.selectedSetForeground" ); //NOI18N
            } else {
                if( isAqua ) {
                    Color res = UIManager.getColor("PropSheet.setForeground"); //NOI18N

                    if (res == null) {
                        res = UIManager.getColor("Table.foreground"); //NOI18N

                        if (res == null) {
                            res = UIManager.getColor("textText");

                            if (res == null) {
                                res = Color.BLACK;
                            }
                        }
                    }
                    return res;
                }
                if( isGTK || isNimbus ) {
                    return new Color( UIManager.getColor( "Menu.foreground" ).getRGB() ); //NOI18N
                }
                return super.getForeground();
            }
        }

        private class ExpandAction extends AbstractAction {
            private boolean expand;
            public ExpandAction( boolean expand ) {
                this.expand = expand;
            }
            public void actionPerformed(ActionEvent e) {
                if( expand == isExpanded() ) {
                    return;                    
                }
                setExpanded( expand );
                al.actionPerformed(e);
            }
        }
    }    
}
