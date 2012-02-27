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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.findbugs.RunFindBugs;
import org.netbeans.modules.findbugs.RunInEditor;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.modules.options.editor.spi.OptionsFilter.Acceptor;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public final class FindBugsPanel extends javax.swing.JPanel {

    private Preferences settings;
    private final boolean defaultsToDisabled;
    private final Map<BugCategory, List<BugPattern>> categorizedBugs = new HashMap<BugCategory, List<BugPattern>>();
    private final DefaultTreeModel treeModel;

    public FindBugsPanel(OptionsFilter filter, final CustomizerContext<?, ?> cc) {
        this.settings = new ModifiedPreferences(NbPreferences.forModule(FindBugsPanel.class).node("global-settings"));
        initComponents();
        this.treeModel = new DefaultTreeModel(createRootNode());
        if (filter != null) {
            filter.installFilteringModel(bugsTree, treeModel, new Acceptor() {
                @Override public boolean accept(Object originalTreeNode, String filterText) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) originalTreeNode;
                    Object user = node.getUserObject();

                    if (user instanceof BugPattern) {
                        BugPattern bp = (BugPattern) user;
                        return    contains(bp.getShortDescription(), filterText)
                               || contains(bp.getLongDescription(), filterText)
                               || contains(bp.getCategory(), filterText)
                               || contains(bp.getDetailPlainText(), filterText);
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
        defaultsToDisabled = cc != null;
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
            treeModel.nodeChanged(node);
            treeModel.nodeChanged(node.getParent());
        }
        else if ( user instanceof BugCategory ) {
            boolean value = !enabled((BugCategory) user);

            for ( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
                Object cho = ch.getUserObject();
                if (cho instanceof BugPattern) {
                    BugPattern pattern = (BugPattern)cho;
                    boolean cv = enabled(pattern);
                    if ( cv != value ) {
                        settings.putBoolean(pattern.getType(), value);
                        treeModel.nodeChanged( ch );
                    }
                }
            }
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

        jSplitPane1.setDividerLocation(200);

        jScrollPane1.setViewportView(bugsTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        description.setContentType(org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.description.contentType")); // NOI18N
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
                .addGap(0, 153, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        org.openide.awt.Mnemonics.setLocalizedText(runInEditor, org.openide.util.NbBundle.getMessage(FindBugsPanel.class, "FindBugsPanel.runInEditor.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(runInEditor)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(runInEditor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSplitPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        this.runInEditor.setSelected(NbPreferences.forModule(FindBugsPanel.class).getBoolean(RunInEditor.RUN_IN_EDITOR, RunInEditor.RUN_IN_EDITOR_DEFAULT));
    }

    void store() {
        ((ModifiedPreferences) this.settings).store(NbPreferences.forModule(FindBugsPanel.class).node("global-settings"));
        NbPreferences.forModule(RunInEditor.class).putBoolean(RunInEditor.RUN_IN_EDITOR, this.runInEditor.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    public void setSettings(Preferences settings) {
        this.settings = settings;
        bugsTree.repaint();
    }

    private TreeNode createRootNode() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

        for (BugPattern bp : dfc.getBugPatterns()) {
            BugCategory c = dfc.getBugCategory(bp.getCategory());
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
            }
        }

        return root;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree bugsTree;
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

    private boolean enabled(BugCategory bc) {
        for (BugPattern bp : this.categorizedBugs.get(bc)) {
            if (enabled(bp)) return true;
        }

        return false;
    }

    private static boolean contains(String where, String what) {
        where = where.toLowerCase();
        what = what.toLowerCase();

        return where.contains(what);
    }

    private class CheckBoxRenderer implements TreeCellRenderer {

        private JCheckBox renderer = new JCheckBox();
        private DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            renderer.setBackground(selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor());
            renderer.setForeground(selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor());
            renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN));

            Object user = ((DefaultMutableTreeNode) value).getUserObject();

            if (user instanceof BugCategory) {
                renderer.setText(((BugCategory) user).getShortDescription());
                renderer.setSelected(enabled((BugCategory) user));
            } else if (user instanceof BugPattern) {
                renderer.setText(((BugPattern) user).getShortDescription());
                renderer.setSelected(enabled((BugPattern) user));
            }

            return renderer;
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
