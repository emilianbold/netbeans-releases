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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.view;

import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.event.*;
import javax.swing.tree.*;


/** Displays {@link Node} hierarchy as a tree of all nodes.
 *
 * <p>
 * This class is a <q>view</q>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <q>views</q> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
*/
public class BeanTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3841322840231536380L;

    /** Constructor.
    */
    public BeanTreeView() {
        // we should have no border, window system will provide borders
        setBorder(BorderFactory.createEmptyBorder());
    }

    void initializeTree() {
        super.initializeTree();
    }

    /** Create a new model.
    * The default implementation creates a {@link NodeTreeModel}.
    * @return the model
    */
    protected NodeTreeModel createModel() {
        return new NodeTreeModel();
    }

    /** Can select any nodes.
    */
    protected boolean selectionAccept(Node[] nodes) {
        return true;
    }

    /* Synchronizes selected nodes from the manager of this Explorer.
    */
    protected void showSelection(TreePath[] treePaths) {
        tree.getSelectionModel().setSelectionPaths(treePaths);

        if (treePaths.length == 1) {
            showPathWithoutExpansion(treePaths[0]);
        }
    }

    /* Called whenever the value of the selection changes.
    * @param nodes nodes
    * @param em explorer manager
    */
    protected void selectionChanged(Node[] nodes, ExplorerManager em)
    throws PropertyVetoException {
        if (nodes.length > 0) {
            Node context = nodes[0].getParentNode();

            for (int i = 1; i < nodes.length; i++) {
                if (context != nodes[i].getParentNode()) {
                    em.setSelectedNodes(nodes);

                    return;
                }
            }

            // May not set explored context above the root context:
            if (em.getRootContext().getParentNode() == context) {
                em.setExploredContextAndSelection(null, nodes);
            } else {
                em.setExploredContextAndSelection(context, nodes);
            }
        } else {
            em.setSelectedNodes(nodes);
        }
    }

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath(TreePath path) {
        tree.expandPath(path);
        showPathWithoutExpansion(path);
    }

    /** Make a path visible.
    * @param path the path
    */
    private void showPathWithoutExpansion(TreePath path) {
        Rectangle rect = tree.getPathBounds(path);

        if (rect != null) { //PENDING
            tree.scrollRectToVisible(rect);
        }
    }

    /** Delegate the setEnable method to Jtree
     *  @param enabled whether to enable the tree
     */
    public void setEnabled(boolean enabled) {
        this.tree.setEnabled(enabled);
    }

    /** Is the tree enabled
     *  @return boolean
     */
    public boolean isEnabled() {
        if (this.tree == null) {
            // E.g. in JDK 1.5 w/ GTK L&F, may be called from TreeView's
            // super (JScrollPane) constructor, so tree is uninitialized
            return true;
        }

        return this.tree.isEnabled();
    }
}
