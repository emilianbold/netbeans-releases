/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.tree.TreeSelectionModel;
import org.openide.DialogDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * GUI panel for browsing for JNDI name
 *
 * @author echou
 */
public class JndiBrowserPanel extends TopComponent implements ExplorerManager.Provider, Lookup.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private String serverInstanceDisplayName;
    private JndiBrowser.Category browseCategory;

    public JndiBrowserPanel(Node rootNode, String serverInstanceDisplayName, JndiBrowser.Category browseCategory) {
        this.serverInstanceDisplayName = serverInstanceDisplayName;
        this.browseCategory = browseCategory;

        associateLookup(ExplorerUtils.createLookup (manager, this.getActionMap()));

        BeanTreeView btv = new BeanTreeView();
        btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        NodeProxy proxyNode = new NodeProxy(rootNode, 0);
        manager.setRootContext(proxyNode);

        setLayout(new BorderLayout());
        add(btv, BorderLayout.CENTER);
        expandAll(btv, proxyNode);
    }

    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }


    public Node[] getSelectedNodes() {
        return manager.getSelectedNodes();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void setDialogDescriptor(final DialogDescriptor d) {
        d.setValid(isDescriptorValid());

        manager.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    d.setValid(isDescriptorValid());
                }
        });
    }

    private boolean isDescriptorValid() {
        Node[] nodes = manager.getSelectedNodes();
        if (nodes.length == 0) {
            return false;
        }
        Node node = nodes[0];
        Action[] actions = node.getActions(true);
        boolean hasDeleteResourceAction = false;
        boolean hasPropertiesAction = false;
        for (Action action : actions) {
            if (action == null) {
                continue;
            }
            if (action.getClass().getName().equals("org.netbeans.modules.j2ee.sun.ide.runtime.actions.DeleteResourceAction")) { // NOI18N
                hasDeleteResourceAction = true;
            } else if (action.getClass().getName().equals("org.openide.actions.PropertiesAction")) { // NOI18N
                hasPropertiesAction = true;
            }
        }

        return hasDeleteResourceAction && hasPropertiesAction;
    }

    private void expandAll(BeanTreeView btv, Node node) {
        Children children = node.getChildren();
        children.findChild(null);
        Node[] nodes = children.getNodes(true);
        for (Node n : nodes) {
            expandAll(btv, n);
        }
        btv.expandNode(node);
    }

    class NodeProxy extends FilterNode {

        public NodeProxy(Node original, int depth) {
            super(original, new ProxyChildren(original, depth + 1));
        }

        // add your specialized behavior here...
    }

    class ProxyChildren extends FilterNode.Children {

        private int depth;

        public ProxyChildren (Node owner, int depth)  {
            super(owner);
            this.depth = depth;
        }

        @Override
        protected Node copyNode (Node original){
            return new NodeProxy(original, depth);
        }

        @Override
        protected Node[] createNodes(Node object) {
            List<Node> result = new ArrayList<Node>();

            for (Node node : super.createNodes(object)) {
                if (accept(node, depth)) {
                    result.add(node);
                }
            }

            return result.toArray(new Node[0]);
        }

        private boolean accept(Node node, int depth) {
            switch (depth) {
                case 0:
                    return true;
                case 1:
                    if ("Resources".equals(node.getDisplayName())) { // NOI18N
                        return true;
                    } else {
                        return false;
                    }
                case 2:
                    if ("Connectors".equals(node.getDisplayName())) { // NOI18N
                        return true;
                    } else {
                        return false;
                    }
                case 3:
                    if (browseCategory == JndiBrowser.Category.CONNECTOR_RESOURCE) {
                        if ("Connector Resources".equals(node.getDisplayName())) { // NOI18N
                            return true;
                        } else {
                            return false;
                        }
                    } else if (browseCategory == JndiBrowser.Category.ADMIN_OBJECT) {
                        if ("Admin Object Resources".equals(node.getDisplayName())) { // NOI18N
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                default:
                    return true;
            }
        }

    }

}
