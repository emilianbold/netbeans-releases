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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.clazz;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Method;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.07.15
 */
final class ClazzTree extends JTree {

    ClazzTree(List<ClazzElement> elements) {
        super(new DefaultTreeModel(new DefaultMutableTreeNode()));
        myRoot = (DefaultMutableTreeNode) getModel().getRoot();
        myRoot.setUserObject(new ClazzElement(null, "", null, null)); // NOI18N
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(this);
        setCellRenderer(new TreeRenderer());
        setShowsRootHandles(true);
        setRootVisible(false);
        addElements(elements);
    }

    Method getSelectedMethod() {
        DefaultMutableTreeNode node = getSelectedNode();

        if (node == null) {
            return null;
        }
        ClazzElement element = getUserObject(node);

        if (element == null) {
            return null;
        }
        Object source = element.getSource();

        if ( !(source instanceof Method)) {
            return null;
        }
        return (Method) source;
    }

    private DefaultMutableTreeNode getSelectedNode() {
        TreePath[] paths = getSelectionPaths();

        if (paths == null || paths.length == 0) {
            return myRoot;
        }
        TreePath path = paths[0];

        if (path == null) {
            return myRoot;
        }
        return getNode(path);
    }

    private void addElements(List<ClazzElement> elements) {
        for (ClazzElement element : elements) {
            addElement(myRoot, element, getParents(element));
        }
        expandPath(new TreePath(myRoot.getPath()));

        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent event) {}

            public void treeExpanded(TreeExpansionEvent event) {
                expandChildren(getNode(event.getPath()));
            }
        });
    }

    private void addElement(MutableTreeNode root, ClazzElement element, Iterator<ClazzElement> parents) {
//out("ADD element: " + element);
        ClazzElement next = null;

        if (parents.hasNext()) {
            next = parents.next();
        }
        if (next == null) { // leaf
//out("add leaf: " + element);
            root.insert(new DefaultMutableTreeNode(element), 0);
            return;
        }
        // try to find node among children
        Enumeration children = root.children();

//out("try to add child: " + next);
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

            if (child.isLeaf()) {
//out("skip leaf: " + child);
                continue;
            }
            if (getUserObject(child) == next) {
                // go to the next level
//out("next level");
                addElement(child, element, parents);
                return;
            }
        }
        // cannot find the same node, add new node
        MutableTreeNode node = new DefaultMutableTreeNode(next);
//out("add internal: " + next);
        root.insert(node, 0);
        addElement(node, element, parents);
    }

    private Iterator<ClazzElement> getParents(ClazzElement element) {
        List<ClazzElement> elements = new ArrayList<ClazzElement>();
        ClazzElement parent = element.getParent();

        while (parent != null) {
            elements.add(0, parent);
//out("     parent: " + parent);
            parent = parent.getParent();
        }
        return elements.iterator();
    }

    private static ClazzElement getUserObject(DefaultMutableTreeNode node) {
        return (ClazzElement) node.getUserObject();
    }

    private void expandChildren(TreeNode node) {
        Enumeration children = node.children();

        if (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();

            if (!children.hasMoreElements()) {
                TreePath path = new TreePath(child.getPath());
                expandPath(path);

                if (isExpanded(path)) {
                    expandChildren(child);
                }
            }
        }
    }

    private DefaultMutableTreeNode getNode(TreePath path) {
        return (DefaultMutableTreeNode) path.getLastPathComponent();
    }

    // ----------------------------------------------------------------------
    private static final class TreeRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean select, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, select, expanded, leaf, row, focus);
            ClazzElement element = getUserObject((DefaultMutableTreeNode) value);
            setText(getHtml(getHtmlName(element.getName(), leaf, row)));
            setToolTipText(element.getToolTip());
            setIcon(element.getIcon());
            return this;
        }

        private String getHtmlName(String name, boolean leaf, int row) {
            if ( !leaf) {
                return name;
            }
            int k2 = name.indexOf("(");

            if (k2 == -1) {
                return name;
            }
            int k1 = k2 - 1;

            while (k1 > 0) {
                if (name.charAt(k1) == ' ') {
                    break;
                }
                k1--;
            }
            return name.substring(0, k1) + "<b>" + name.substring(k1, k2) + "</b>" + name.substring(k2); // NOI18N
        }
    }

    private DefaultMutableTreeNode myRoot;
}
