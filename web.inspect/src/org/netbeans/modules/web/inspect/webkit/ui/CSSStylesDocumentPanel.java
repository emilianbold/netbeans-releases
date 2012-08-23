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
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.ui.FakeRootNode;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Document section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesDocumentPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(CSSStylesDocumentPanel.class);
    /** Tree view showing the style sheet information. */
    private BeanTreeView treeView;
    /** Explorer manager provided by this panel. */
    private ExplorerManager manager = new ExplorerManager();
    /** Lookup of this panel. */
    private Lookup lookup = ExplorerUtils.createLookup(getExplorerManager(), getActionMap());
    /** Filter for the tree displayed in this panel. */
    private Filter filter = new Filter();

    /**
     * Creates a new {@code CSSStylesDocumentPanel}.
     */
    CSSStylesDocumentPanel() {
        setLayout(new BorderLayout());
        initTreeView();
        initFilter();
        updateContent(null, true);
    }

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView() {
            @Override
            public void expandAll() {
                // The original expandAll() doesn't work for us as it doesn't
                // seem to wait for the calculation of sub-nodes.
                Node root = manager.getRootContext();
                expandAll(root);
                // The view attempts to scroll to the expanded node
                // and it does it with a delay. Hence, simple calls like
                // tree.scrollRowToVisible(0) have no effect (are overriden
                // later) => the dummy collapse and expansion attempts
                // to work around that and keep the root node visible.
                collapseNode(root);
                expandNode(root);
            }
            /**
             * Expands the whole sub-tree under the specified node.
             *
             * @param node root node of the sub-tree that should be expanded.
             */
            private void expandAll(Node node) {
                treeView.expandNode(node);
                for (Node subNode : node.getChildren().getNodes(true)) {
                    if (!subNode.isLeaf()) {
                        expandAll(subNode);
                    }
                }
            }
        };
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
        add(treeView, BorderLayout.CENTER);
    }

    /**
     * Initializes the filter section of this panel.
     */
    private void initFilter() {
        JPanel panel = new JPanel();
        Color background = treeView.getViewport().getView().getBackground();
        panel.setBackground(background);

        // "Find" label
        JLabel label = new JLabel(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/web/inspect/resources/find.png", true)); // NOI18N
        label.setVerticalAlignment(SwingConstants.CENTER);

        // Pattern text field
        final JTextField field = new JTextField();
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                filter.setPattern(field.getText());
            }
        });

        // Clear pattern button
        JButton button = new JButton(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/web/inspect/resources/cancel.png", true)); // NOI18N
        button.setBackground(background);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0,0,0,0));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.setText(""); // NOI18N
            }
        });

        // Layout
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(2)
            .addComponent(label)
            .addComponent(field)
            .addComponent(button)
            .addGap(2));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(label)
                .addComponent(field)
                .addComponent(button)));
        add(panel, BorderLayout.PAGE_START);
    }

    /**
     * Updates the content of this panel.
     *
     * @param webKit WebKit debugging.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    final void updateContent(final WebKitPageModel pageModel, final boolean keepSelection) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Node root;
                if (pageModel == null) {
                    // Using dummy node as the root to release the old root
                    root = new AbstractNode(Children.LEAF);
                } else {
                    WebKitDebugging webKit = pageModel.getWebKit();
                    Project project = pageModel.getProject();
                    CSS css = webKit.getCSS();
                    filter.removePropertyChangeListeners();
                    DocumentNode documentNode = new DocumentNode(project, css, filter);
                    root = new FakeRootNode<DocumentNode>(documentNode,
                            new Action[] { new RefreshAction() });
                }
                final Node[] oldSelection = manager.getSelectedNodes();
                manager.setRootContext(root);
                treeView.expandAll();
                if (keepSelection) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            List<Node> selection = new ArrayList<Node>(oldSelection.length);
                            for (Node oldSelected : oldSelection) {
                                Rule rule = oldSelected.getLookup().lookup(Rule.class);
                                if (rule != null) {
                                    Node newSelected = Utilities.findRule(root, rule);
                                    if (newSelected != null) {
                                        selection.add(newSelected);
                                    }
                                }
                            }
                            try {
                                manager.setSelectedNodes(selection.toArray(new Node[selection.size()]));
                            } catch (PropertyVetoException pvex) {}
                        }
                    });
                }
            }
        });
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Action that refreshes the content of the document section of CSS Styles view.
     */
    private class RefreshAction extends AbstractAction {

        private RefreshAction() {
            String name = NbBundle.getMessage(RefreshAction.class,
                    "CSSStylesDocumentPanel.RefreshAction.displayName"); // NOI18N
            putValue(Action.NAME, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PageModel pageModel = PageInspectorImpl.getDefault().getPage();
            updateContent(pageModel instanceof WebKitPageModel ? (WebKitPageModel)pageModel : null, false);
        }

    }

}
