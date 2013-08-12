/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs.options;

import edu.umd.cs.findbugs.BugCategory;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.findbugs.DetectorCollectionProvider;
import org.netbeans.modules.findbugs.RunFindBugs;
import org.netbeans.modules.findbugs.RunInEditor;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.modules.options.editor.spi.OptionsFilter.Acceptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

public final class FindBugsPanel extends javax.swing.JPanel {

    private static final RequestProcessor WORKER = new RequestProcessor(FindBugsPanel.class.getName(), 1, false, false);
    private Preferences settings;
    private List<String> modifiedPluginsList;
    private final boolean defaultsToDisabled;
    private final Map<BugCategory, List<BugPattern>> categorizedBugs = new HashMap<BugCategory, List<BugPattern>>();
    private final Map<String, TreePath> bug2Path =  new HashMap<String, TreePath>();
    private DefaultTreeModel treeModel;
    private final FindBugsOptionsPanelController controller;
    private final OptionsFilter filter;
    private final CustomizerContext<?, ?> cc;

    @Messages("LBL_Loading=Loading...")
    public FindBugsPanel(@NullAllowed FindBugsOptionsPanelController controller, final @NullAllowed OptionsFilter filter, final @NullAllowed CustomizerContext<?, ?> cc) {
        defaultsToDisabled = cc != null;
        this.controller = controller;
        this.filter = filter;
        this.cc = cc;
        reinitialize();
    }
    
    private void reinitialize() {
        final Boolean previousRunInEditor = this.runInEditor != null ? this.runInEditor.isSelected() : null;
        WORKER.post(new Runnable() {
            @Override public void run() {
                final TreeNode root = backgroundInit();
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        FindBugsPanel.this.removeAll();
                        FindBugsPanel.this.uiInit(filter, root, cc, previousRunInEditor);
                    }
                });
            }
        });

        setLayout(new GridBagLayout());
        removeAll();
        add(new JLabel(Bundle.LBL_Loading()), new GridBagConstraints());
    }
    
    private synchronized TreeNode backgroundInit() {
        TreeNode rootNode = createRootNode();
        
        for (List<BugPattern> bl : categorizedBugs.values()) {
            for (BugPattern bp : bl) {
                getFilterText(bp);
            }
        }
        
        return rootNode;
    }
    
    private synchronized void uiInit(OptionsFilter filter, TreeNode rootNode, final CustomizerContext<?, ?> cc, Boolean previousRunInEditor) {
        if (this.settings == null) { //might have already been set through setSettings method, do not reset!
            this.settings = new ModifiedPreferences(NbPreferences.forModule(FindBugsPanel.class).node("global-settings"));
        }
        
        initComponents();
        
        this.treeModel = new DefaultTreeModel(rootNode);
        if (filter != null) {
            filter.installFilteringModel(bugsTree, treeModel, new Acceptor() {
                @Override public boolean accept(Object originalTreeNode, String filterText) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) originalTreeNode;
                    Object user = node.getUserObject();

                    if (user instanceof BugPattern) {
                        BugPattern bp = (BugPattern) user;
                        return contains(getFilterText(bp), filterText);
                    } else if (user instanceof BugCategory) {
                        BugCategory bc = (BugCategory) user;
                        return    contains(bc.getShortDescription(), filterText)
                               || contains(bc.getDetailText(), filterText)
                               || contains(bc.getCategory(), filterText);
                    } else {
                        return true;
                    }
                }
            });
        } else {
            bugsTree.setModel(treeModel);
        }
        bugsTree.setRootVisible(false);
        bugsTree.setShowsRootHandles(true);

        if (cc == null || cc.getPreselectId() == null) {
            bugsTree.setCellRenderer(new CheckBoxRenderer());
        } else {
            bugsTree.setCellRenderer(new PlainRenderer());
            selectById(cc.getPreselectId().substring(RunFindBugs.PREFIX_FINDBUGS.length()));
            bugsTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) bugsTree.getSelectionPath().getLastPathComponent();
                    Object user = node.getUserObject();

                    if (user instanceof BugPattern) {
                        BugPattern bp = (BugPattern) user;

                        cc.setSelectedId(RunFindBugs.PREFIX_FINDBUGS + bp.getType());
                    }
                }
            });
        }

        bugsTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                TreePath selection = bugsTree.getSelectionPath();

                if (selection == null) {
                    description.setText("");
                } else {
                    Object user = ((DefaultMutableTreeNode) selection.getLastPathComponent()).getUserObject();

                    if (user instanceof BugCategory) {
                        description.setText(((BugCategory) user).getDetailText()); //XXX: not HTML!!
                    } else if (user instanceof BugPattern) {
                        description.setText(((BugPattern) user).getDetailText());
                    }
                }
            }
        });

        bugsTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                TreePath path = bugsTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                if (path != null) {
                    Rectangle r = bugsTree.getPathBounds(path);
                    if (r != null) {
                        r.width = r.height;
                        if (r.contains(p)) {
                            toggle(path);
                        }
                    }
                }
            }
        });

        runInEditor.setVisible(cc == null);
        customPlugins.setVisible(cc == null);

        if (cc == null) {
            if (previousRunInEditor == null)
                load();
            else 
                runInEditor.setSelected(previousRunInEditor);
        }
    }

    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;

        if (!(bugsTree.getCellRenderer() instanceof CheckBoxRenderer)) {
            //no checkboxes, no toggle
            return false;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        Object user = node.getUserObject();

        if ( user instanceof BugPattern ) {
            BugPattern bp = (BugPattern)user;
            boolean value = enabled(bp);
            settings.putBoolean(bp.getType(), !value);
            if (controller != null) controller.changed();
            treeModel.nodeChanged(node);
            treeModel.nodeChanged(node.getParent());
        }
        else if ( user instanceof BugCategory ) {
            boolean newValue = enabled((BugCategory) user) == State.NOT_SELECTED;
            boolean changed = false;

            for ( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
                Object cho = ch.getUserObject();
                if (cho instanceof BugPattern) {
                    BugPattern pattern = (BugPattern)cho;
                    boolean cv = enabled(pattern);
                    if ( cv != newValue ) {
                        settings.putBoolean(pattern.getType(), newValue);
                        changed |= true;
                        treeModel.nodeChanged( ch );
                    }
                }
            }
            if (changed && controller != null) controller.changed();
            treeModel.nodeChanged(node);
        }

        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        bugsTree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        runInEditor = new javax.swing.JCheckBox();
        customPlugins = new javax.swing.JButton();

        jSplitPane1.setDividerLocation(200);

        jScrollPane1.setViewportView(bugsTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        description.setContentType(org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.description.contentType")); // NOI18N
        description.setEditable(false);
        description.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                descriptionHyperlinkUpdate(evt);
            }
        });
        jScrollPane2.setViewportView(description);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 229, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 147, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        org.openide.awt.Mnemonics.setLocalizedText(runInEditor, org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.runInEditor.text")); // NOI18N
        runInEditor.setToolTipText(org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "TP_RunInEditor")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(customPlugins, org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.customPlugins.text")); // NOI18N
        customPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customPluginsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(runInEditor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(customPlugins)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runInEditor)
                    .addComponent(customPlugins))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSplitPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void descriptionHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_descriptionHyperlinkUpdate
        if (evt.getEventType() == EventType.ACTIVATED && evt.getURL() != null) {
            URLDisplayer.getDefault().showURL(evt.getURL());
        }
    }//GEN-LAST:event_descriptionHyperlinkUpdate

    @Messages("CAP_CustomPlugins=Custom Plugins Selector")
    private void customPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customPluginsActionPerformed
        if (modifiedPluginsList == null) {
            modifiedPluginsList = new ArrayList<String>(DetectorCollectionProvider.customPlugins());
        }
        
        CustomPluginsPanel panel = new CustomPluginsPanel(modifiedPluginsList);
        DialogDescriptor dd = new DialogDescriptor(panel, Bundle.CAP_CustomPlugins(), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            modifiedPluginsList = panel.getPlugins();
            reinitialize();
        }
    }//GEN-LAST:event_customPluginsActionPerformed

    void load() {
        if (this.runInEditor == null) {
            //not initialized yet
            return;
        }
        this.runInEditor.setSelected(NbPreferences.forModule(FindBugsPanel.class).getBoolean(RunInEditor.RUN_IN_EDITOR, RunInEditor.RUN_IN_EDITOR_DEFAULT));
    }

    void store() {
        if (this.runInEditor == null) {
            //not initialized yet
            return;
        }
        ((ModifiedPreferences) this.settings).store(NbPreferences.forModule(FindBugsPanel.class).node("global-settings"));
        NbPreferences.forModule(RunInEditor.class).putBoolean(RunInEditor.RUN_IN_EDITOR, this.runInEditor.isSelected());
        if (modifiedPluginsList != null) DetectorCollectionProvider.setCustomPlugins(modifiedPluginsList);
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    public void setSettings(Preferences settings) {
        this.settings = settings;
        if (bugsTree != null) {//prevent NPE when not initialized yet
            bugsTree.repaint();
        }
    }

    void selectById(final String id) {
        if (bugsTree == null) {
            //when not initialized yet, wait for the initialization:
            WORKER.post(new Runnable() {
                @Override public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        selectById(id);
                    } else {
                        SwingUtilities.invokeLater(this);
                    }
                }
            });
            return ;
        }
        TreePath toSelect = bug2Path.get(id);

        if (toSelect != null) {
            bugsTree.setSelectionPath(toSelect);
            bugsTree.scrollPathToVisible(toSelect);
        } else {
            Logger.getLogger(FindBugsPanel.class.getName()).log(Level.WARNING, "cannot find bug to select ({0})", id);
        }
    }
    
    private TreeNode createRootNode() {
        categorizedBugs.clear();
        bug2Path.clear();
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DetectorFactoryCollection dfc = modifiedPluginsList == null ? DetectorFactoryCollection.instance() : DetectorCollectionProvider.getTemporaryCollection(modifiedPluginsList);

        for (BugPattern bp : dfc.getBugPatterns()) {
            BugCategory c = dfc.getBugCategory(bp.getCategory());

            if (c.isHidden()) continue;

            List<BugPattern> bugs = categorizedBugs.get(c);

            if (bugs == null) {
                categorizedBugs.put(c, bugs = new ArrayList<BugPattern>());
            }

            bugs.add(bp);
        }

        Map<BugCategory, List<BugPattern>> sortedCategorizedBugs = new TreeMap<BugCategory, List<BugPattern>>(new Comparator<BugCategory>() {
            @Override public int compare(BugCategory o1, BugCategory o2) {
                return o1.getShortDescription().compareTo(o2.getShortDescription());
            }
        });

        sortedCategorizedBugs.putAll(this.categorizedBugs);

        for (Entry<BugCategory, List<BugPattern>> categoryEntry : sortedCategorizedBugs.entrySet()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(categoryEntry.getKey());

            root.add(categoryNode);
            Collections.sort(categoryEntry.getValue(), new Comparator<BugPattern>() {
                @Override public int compare(BugPattern o1, BugPattern o2) {
                    return o1.getShortDescription().compareTo(o2.getShortDescription());
                }
            });

            for (BugPattern bug : categoryEntry.getValue()) {
                DefaultMutableTreeNode bugNode = new DefaultMutableTreeNode(bug);

                categoryNode.add(bugNode);
                bug2Path.put(bug.getType(), new TreePath(new Object[] {root, categoryNode, bugNode}));
            }
        }

        return root;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree bugsTree;
    private javax.swing.JButton customPlugins;
    private javax.swing.JTextPane description;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JCheckBox runInEditor;
    // End of variables declaration//GEN-END:variables

    private boolean enabled(BugPattern bp) {
        String setting = settings.get(bp.getType(), null);

        if (setting != null) return Boolean.valueOf(setting);

        return !defaultsToDisabled && RunFindBugs.isEnabledByDefault(bp);
    }

    private State enabled(BugCategory bc) {
        boolean hasEnabled = false;
        boolean hasDisabled = false;
        for (BugPattern bp : this.categorizedBugs.get(bc)) {
            if (enabled(bp)) hasEnabled = true;
            else hasDisabled = true;
        }

        return hasEnabled ? hasDisabled ? State.OTHER : State.SELECTED : State.NOT_SELECTED;
    }

    private static boolean contains(String where, String what) {
        where = where.toLowerCase();
        what = what.toLowerCase();

        return where.contains(what);
    }

    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N

    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }

        return input;
    }

    private final Map<BugPattern, String> filterText = new IdentityHashMap<BugPattern, String>();

    private synchronized String getFilterText(BugPattern bp) {
        String seq = filterText.get(bp);

        if (seq != null) return seq;

        filterText.put(bp, seq = RunFindBugs.computeFilterText(bp));

        return seq;
    }

    private class CheckBoxRenderer implements TreeCellRenderer {

        private final TristateCheckBox renderer = new TristateCheckBox();
        private final DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            renderer.setBackground(selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor());
            renderer.setForeground(selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor());
            renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN));

            Object user = ((DefaultMutableTreeNode) value).getUserObject();

            if (user instanceof BugCategory) {
                renderer.setText(((BugCategory) user).getShortDescription());
                renderer.setState(enabled((BugCategory) user));
            } else if (user instanceof BugPattern) {
                BugPattern bp = (BugPattern) user;
                renderer.setText("<html>" + (bp.isDeprecated() ? "<s>" : "") + translate(bp.getShortDescription()));
                renderer.setSelected(enabled(bp));
            }

            return renderer;
        }
    }

    private enum State {
        SELECTED, NOT_SELECTED, OTHER;
    };

    private static class TristateCheckBox extends JCheckBox {

        private final TristateDecorator model;

        public TristateCheckBox() {
            super(null, null);
            model = new TristateDecorator(getModel());
            setModel(model);
            setState(State.OTHER);
        }

        /** No one may add mouse listeners, not even Swing! */
        @Override
        public void addMouseListener(MouseListener l) { }
        /**
         * Set the new state to either SELECTED, NOT_SELECTED or
         * OTHER.
         */
        public void setState(State state) { model.setState(state); }
        /** Return the current state, which is determined by the
         * selection status of the model. */
        public State getState() { return model.getState(); }
        @Override
        public void setSelected(boolean b) {
            if (b) {
                setState(State.SELECTED);
            } else {
                setState(State.NOT_SELECTED);
            }
        }
        /**
         * Exactly which Design Pattern is this?  Is it an Adapter,
         * a Proxy or a Decorator?  In this case, my vote lies with the
         * Decorator, because we are extending functionality and
         * "decorating" the original model with a more powerful model.
         */
        private class TristateDecorator implements ButtonModel {
            private final ButtonModel other;
            private TristateDecorator(ButtonModel other) {
                this.other = other;
            }
            private void setState(State state) {
                if (state == State.NOT_SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(false);
                } else if (state == State.SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(true);
                } else { // either "null" or OTHER
                    other.setArmed(true);
                    setPressed(true);
                    setSelected(true);
                }
            }
            /**
             * The current state is embedded in the selection / armed
             * state of the model.
             *
             * We return the SELECTED state when the checkbox is selected
             * but not armed, DONT_CARE state when the checkbox is
             * selected and armed (grey) and NOT_SELECTED when the
             * checkbox is deselected.
             */
            private State getState() {
                if (isSelected() && !isArmed()) {
                    // normal black tick
                    return State.SELECTED;
                } else if (isSelected() && isArmed()) {
                    // don't care grey tick
                    return State.OTHER;
                } else {
                    // normal deselected
                    return State.NOT_SELECTED;
                }
            }
            /** Filter: No one may change the armed status except us. */
            public void setArmed(boolean b) {
            }
            /** We disable focusing on the component when it is not
             * enabled. */
            public void setEnabled(boolean b) {
                setFocusable(b);
                other.setEnabled(b);
            }
            /** All these methods simply delegate to the "other" model
             * that is being decorated. */
            public boolean isArmed() { return other.isArmed(); }
            public boolean isSelected() { return other.isSelected(); }
            public boolean isEnabled() { return other.isEnabled(); }
            public boolean isPressed() { return other.isPressed(); }
            public boolean isRollover() { return other.isRollover(); }
            public void setSelected(boolean b) { other.setSelected(b); }
            public void setPressed(boolean b) { other.setPressed(b); }
            public void setRollover(boolean b) { other.setRollover(b); }
            public void setMnemonic(int key) { other.setMnemonic(key); }
            public int getMnemonic() { return other.getMnemonic(); }
            public void setActionCommand(String s) {
                other.setActionCommand(s);
            }
            public String getActionCommand() {
                return other.getActionCommand();
            }
            public void setGroup(ButtonGroup group) {
                other.setGroup(group);
            }
            public void addActionListener(ActionListener l) {
                other.addActionListener(l);
            }
            public void removeActionListener(ActionListener l) {
                other.removeActionListener(l);
            }
            public void addItemListener(ItemListener l) {
                other.addItemListener(l);
            }
            public void removeItemListener(ItemListener l) {
                other.removeItemListener(l);
            }
            public void addChangeListener(ChangeListener l) {
                other.addChangeListener(l);
            }
            public void removeChangeListener(ChangeListener l) {
                other.removeChangeListener(l);
            }
            public Object[] getSelectedObjects() {
                return other.getSelectedObjects();
            }
        }
    }

    private class PlainRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object user = ((DefaultMutableTreeNode) value).getUserObject();

            if (user instanceof BugCategory) {
                value = ((BugCategory) user).getShortDescription();
            } else if (user instanceof BugPattern) {
                value = ((BugPattern) user).getShortDescription();
            }

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            setIcon(null);
            setDisabledIcon(null);

            return this;
        }
    }

    private static class ModifiedPreferences extends AbstractPreferences {

        private Map<String,Object> map = new HashMap<String, Object>();

        public ModifiedPreferences( Preferences node ) {
            super(null, ""); // NOI18N
            try {
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


        public void store( Preferences target ) {

            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        protected void putSpi(String key, String value) {
            map.put(key, value);
        }

        protected String getSpi(String key) {
            return (String)map.get(key);
        }

        protected void removeSpi(String key) {
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

	boolean isEmpty() {
	    return map.isEmpty();
	}
    }
}
