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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.RuleId;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;

/**
 * Selection section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesSelectionPanel extends JPanel {
    /** Request processor used by this class. */
    static final RequestProcessor RP = new RequestProcessor(CSSStylesSelectionPanel.class);
    /** Lookup of this panel. */
    private Lookup lookup;
    /** The current inspected page. */
    private WebKitPageModel pageModel;
    /** Page model listener. */
    private Listener listener;
    /** Property Summary view. */
    private TreeTableView propertyPane;
    /** Explorer manager for Property Summary. */
    private ExplorerManager propertyPaneManager;
    /** Style Cascade view. */
    private TreeTableView rulePane;
    /** Explorer manager for Style Cascade. */
    private ExplorerManager rulePaneManager;
    /** Label for messages. */
    private JLabel messageLabel;
    /** Component showing the style information for the current selection. */
    private JComponent selectionView;

    /**
     * Creates a new {@code CSSStylesSelectionPanel}.
     */
    CSSStylesSelectionPanel() {
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(initPropertyPane());
        splitPane.setBottomComponent(initRulePane());
        splitPane.setDividerLocation(100);
        selectionView = splitPane;
        initMessageLabel();
        initSelectionOfOwningRule();
        add(splitPane, BorderLayout.CENTER);
        updateContent(null, false);
    }

    /**
     * Initializes Property Summary section.
     *
     * @return Property Summary panel.
     */
    private JPanel initPropertyPane() {
        propertyPane = new CustomTreeTableView();
        propertyPane.setProperties(new Node.Property[] {
            new PropertySupport.ReadOnly<String>(MatchedPropertyNode.PROPERTY_VALUE, String.class, "", null) { // NOI18N
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return null;
                }
            }
        });
        ExplorerManagerProviderPanel propertyPanePanel = new ExplorerManagerProviderPanel();
        propertyPanePanel.setLayout(new BorderLayout());
        propertyPanePanel.add(propertyPane, BorderLayout.CENTER);
        propertyPaneManager = propertyPanePanel.getExplorerManager();
        return propertyPanePanel;
    }

    /**
     * Initializes Style Cascade section.
     *
     * @return Style Cascade section.
     */
    private JPanel initRulePane() {
        rulePane = new CustomTreeTableView();
        rulePane.setProperties(new Node.Property[] {
            new PropertySupport.ReadOnly<String>(MatchedRuleNode.PROPERTY_NODE, String.class, "", null) { // NOI18N
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return null;
                }
            }
        });
        rulePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ExplorerManagerProviderPanel rulePanePanel = new ExplorerManagerProviderPanel();
        rulePanePanel.setLayout(new BorderLayout());
        rulePanePanel.add(rulePane, BorderLayout.CENTER);
        rulePaneManager = rulePanePanel.getExplorerManager();
        lookup = ExplorerUtils.createLookup(rulePaneManager, getActionMap());
        return rulePanePanel;
    }

    /**
     * Initializes the listener responsible for selection of the owning
     * rule (in Style Cascade) when the selection in Property Summary changes.
     */
    private void initSelectionOfOwningRule() {
        propertyPaneManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propertyName)) {
                    Node[] selection = propertyPaneManager.getSelectedNodes();
                    if (selection.length == 1) {
                        Lookup lookup = selection[0].getLookup();
                        Rule rule = lookup.lookup(Rule.class);
                        if (rule != null) {
                            selectRule(rule, rulePaneManager.getRootContext());
                        }
                    }
                }
            }
            /**
             * Attempts to select the specified rule in the given sub-tree
             * of Style Cascade view.
             * 
             * @param rule rule to select.
             * @param root root of a sub-tree in Style Cascade to search.
             * @return {@code true} when the rule was selected successfully,
             * returns {@code false} otherwise.
             */
            private boolean selectRule(Rule rule, Node root) {
                RuleId id = rule.getId();
                Lookup lookup = root.getLookup();
                Rule otherRule = lookup.lookup(Rule.class);
                if (otherRule != null && otherRule.getId().equals(id)) {
                    try {
                        rulePaneManager.setSelectedNodes(new Node[] { root });
                    } catch (PropertyVetoException ex) {}
                    return true;
                }
                for (Node subNode : root.getChildren().getNodes()) {
                    if (selectRule(rule, subNode)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Initializes the label used to display messages.
     */
    private void initMessageLabel() {
        messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(false);
        messageLabel.setBackground(new BeanTreeView().getViewport().getView().getBackground());
        messageLabel.setOpaque(true);
    }

    /**
     * Updates the content of and sets a new page model to this panel.
     *
     * @param pageModel page model to use by this panel.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    final void updateContent(WebKitPageModel pageModel, boolean keepSelection) {
        if (this.pageModel != null) {
            this.pageModel.removePropertyChangeListener(getListener());
        }
        this.pageModel = pageModel;
        if (this.pageModel != null) {
            this.pageModel.addPropertyChangeListener(getListener());
        }
        updateContentImpl(pageModel, keepSelection);
    }

    /**
     * Updates the content of this panel.
     *
     * @param pageModel page model to use by this panel.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    void updateContentImpl(final WebKitPageModel pageModel, final boolean keepSelection) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateContentImpl(pageModel, keepSelection);
                }
            });
            return;
        }
        if (pageModel == null) {
            setDummyRoots();
        } else {
            List<Node> selection = pageModel.getSelectedNodes();
            int selectionSize = selection.size();
            if (selectionSize == 0) {
                setDummyRoots();
                showLabel("CSSStylesSelectionPanel.noElementSelected"); // NOI18N
            } else if (selectionSize > 1) {
                setDummyRoots();
                showLabel("CSSStylesSelectionPanel.multipleElementsSelected"); // NOI18N
            } else {
                final Node selectedNode = selection.get(0);
                final org.netbeans.modules.web.webkit.debugging.api.dom.Node node =
                    selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                if (node.getNodeType() == Document.ELEMENT_NODE) {
                    showLabel(null);
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            WebKitDebugging webKit = pageModel.getWebKit();
                            CSS css = webKit.getCSS();
                            MatchedStyles matchedStyles;
                            try {
                                matchedStyles = css.getMatchedStyles(node, null, false, true);
                            } catch (TransportStateException tsex) {
                                matchedStyles = null;
                            }
                            if (matchedStyles != null) {
                                final Node[] selectedRules = rulePaneManager.getSelectedNodes();
                                final Node[] selectedProperties = propertyPaneManager.getSelectedNodes();
                                Project project = pageModel.getProject();
                                final Node rulePaneRoot = new MatchedRulesNode(project, selectedNode, matchedStyles);
                                rulePaneManager.setRootContext(rulePaneRoot);
                                final Node propertyPaneRoot = new MatchedPropertiesNode(project, matchedStyles);
                                propertyPaneManager.setRootContext(propertyPaneRoot);
                                if (keepSelection) {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (selectedProperties.length > 0) {
                                                Node selectedProperty = selectedProperties[0];
                                                Property property = selectedProperty.getLookup().lookup(Property.class);
                                                if (property != null) {
                                                    String propertyName = property.getName();
                                                    for (Node candidate : propertyPaneRoot.getChildren().getNodes()) {
                                                        Property candProperty = candidate.getLookup().lookup(Property.class);
                                                        if (candProperty != null && propertyName.equals(candProperty.getName())) {
                                                            try {
                                                                propertyPaneManager.setSelectedNodes(new Node[] {candidate});
                                                            } catch (PropertyVetoException pvex) {}
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            if (selectedRules.length > 0) {
                                                Node selectedRuleNode = selectedRules[0];
                                                Rule selectedRule = selectedRuleNode.getLookup().lookup(Rule.class);
                                                if (selectedRule != null) {
                                                    Node newSelectedRuleNode = Utilities.findRule(rulePaneRoot, selectedRule);
                                                    if (newSelectedRuleNode != null) {
                                                        try {
                                                            rulePaneManager.setSelectedNodes(new Node[] {newSelectedRuleNode});
                                                        } catch (PropertyVetoException pvex) {}
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    setDummyRoots();
                    showLabel("CSSStylesSelectionPanel.noElementSelected"); // NOI18N
                }
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Shows a label with the message that corresponds to the given bundle key.
     *
     * @param key key that corresponds to the message to show. If it is
     * {@code null} then the message label is hidden and the regular selection
     * view is shown instead.
     */
    private void showLabel(String key) {
        if ((key == null) != (selectionView.getParent() != null)) {
            if (key == null) {
                remove(messageLabel);
                add(selectionView);
            } else {
                remove(selectionView);
                add(messageLabel);
            }
        }
        if (key != null) {
            String message = NbBundle.getMessage(CSSStylesSelectionPanel.class, key);
            messageLabel.setText(message);
        }
    }

    /**
     * Sets dummy roots to tree views to release the currently displayed nodes.
     */
    private void setDummyRoots() {
        Node rulePaneRoot = new AbstractNode(Children.LEAF);
        Node propertyPaneRoot = new AbstractNode(Children.LEAF);
        // Workaround for a bug in TreeTableView
        rulePaneRoot.setDisplayName(NbBundle.getMessage(CSSStylesSelectionPanel.class, "MatchedRulesNode.displayName")); // NOI18N
        propertyPaneRoot.setDisplayName(NbBundle.getMessage(CSSStylesSelectionPanel.class, "MatchedPropertiesNode.displayName")); // NOI18N
        rulePaneManager.setRootContext(rulePaneRoot);
        propertyPaneManager.setRootContext(propertyPaneRoot);
    }

    /**
     * Returns the lookup of this panel.
     *
     * @return lookup of this panel.
     */
    Lookup getLookup() {
        return lookup;
    }

    /**
     * Returns a node selection listener.
     *
     * @return node selection listener.
     */
    private synchronized Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }

    /**
     * Node selection listener.
     */
    class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PageModel.PROP_SELECTED_NODES.equals(propertyName)) {
                updateContentImpl(pageModel, false);
            }
        }

    }

    /**
     * Panel that provides explorer manager and the corresponding lookup.
     */
    static class ExplorerManagerProviderPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
        /** Explorer manager provided by this panel. */
        private ExplorerManager manager = new ExplorerManager();
        /** Lookup provided by this panel. */
        private Lookup lookup = ExplorerUtils.createLookup(manager, getActionMap());

        @Override
        public final ExplorerManager getExplorerManager() {
            return manager;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }
    }

    /**
     * Custom {@code TreeTableView} used to display style information
     * for the selected element.
     */
    static class CustomTreeTableView extends TreeTableView {

        /**
         * Creates a new {@code CustomTreeTableView}.
         */
        CustomTreeTableView() {
            setRootVisible(false);
            setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            final TreeCellRenderer renderer = tree.getCellRenderer();
            tree.setCellRenderer(new TreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(JTree tree,
                        Object value, boolean selected, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {
                    Component component = renderer.getTreeCellRendererComponent(
                            tree, value, selected, expanded, leaf, row, hasFocus);
                    if (component instanceof JLabel) {
                        ((JLabel)component).setIcon(null);
                    }
                    return component;
                }
            });
            hideTreeLines();
            final TableCellRenderer defaultRenderer = HtmlRenderer.createRenderer();
            treeTable.setDefaultRenderer(Node.Property.class, new TableCellRenderer() {
                // Text rendered in the first column of tree-table (i.e. in the tree)
                // is not baseline-aligned with the text in the other columns for some reason.
                // This border attempts to work around this problem.
                private Border border = BorderFactory.createEmptyBorder(1,0,0,0);
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof Node.Property) {
                        Node.Property property = (Node.Property)value;
                        try {
                            value = property.getValue();
                        } catch (IllegalAccessException ex) {
                        } catch (InvocationTargetException ex) {
                        }
                    }
                    Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (component instanceof JComponent) {
                        ((JComponent)component).setBorder(border);
                    }
                    return component;
                }
            });
        }

        /**
         * A hack that diables painting of tree lines.
         */
        private void hideTreeLines() {
            TreeUI treeUI = tree.getUI();
            if (treeUI instanceof BasicTreeUI) {
                try {
                    // The following code is equivalent to
                    // ((BasicTreeUI)tree.getUI()).paintLines = false;
                    Field paintLines = BasicTreeUI.class.getDeclaredField("paintLines"); // NOI18N
                    paintLines.setAccessible(true);
                    paintLines.setBoolean(treeUI, false);
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                } catch (NoSuchFieldException ex) {
                } catch (SecurityException ex) {
                }
            }
        }

    }

}
