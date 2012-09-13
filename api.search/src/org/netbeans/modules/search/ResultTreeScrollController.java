/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

/**
 * This class sets custom scroll behavior on JScrollPane and JTree.
 *
 * Without this class, JScrollPane auto-scrolls JTree in both vertical and
 * horizontal direction, which hides small icons to expand and colapse nodes
 * (on Linux).
 *
 * See bug #197703.
 *
 * @author jhavlin
 */
class ResultTreeScrollController implements TreeWillExpandListener,
        TreeExpansionListener {

    /**
     * Last horizontal offset.
     */
    private int lastX = -1;
    private JScrollPane scrollPane;
    private JTree tree;
    private boolean enabled = true;

    private ResultTreeScrollController(JScrollPane scrollPane, JTree tree) {
        this.scrollPane = scrollPane;
        this.tree = tree;
    }

    @Override
    public synchronized void treeWillExpand(TreeExpansionEvent event)
            throws ExpandVetoException {

        if (enabled) {
            lastX = (int) scrollPane.getViewport().getViewPosition().getX();
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event)
            throws ExpandVetoException {
    }

    @Override
    public void treeExpanded(final TreeExpansionEvent event) {

        if (!enabled) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                Rectangle r = tree.getPathBounds(event.getPath());

                if (r != null) {
                    Object treeNode = event.getPath().getLastPathComponent();
                    int childCount = tree.getModel().getChildCount(treeNode);

                    // View height needed to show node and its children.
                    int height = (int) r.getHeight() * (childCount + 1);

                    Rectangle rectToShow = new Rectangle(lastX, (int) r.getY(),
                            scrollPane.getWidth() - 20, height);

                    tree.scrollRectToVisible(rectToShow);
                }
            }
        });
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent tee) {
    }

    /**
     * Enable or disable the scroll controller.
     *
     * Automatic scrolling should be disabled when tree nodes are expanded
     * programatically, e.g. from method ResultTreeModel.fixTreeExpansionState.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Register listeners to a JTree and its containing JScrollPane,
     * and disables tree auto-scolling on node expansion.
     * 
     * @param scrollPane
     * @param tree 
     */
    static ResultTreeScrollController register(JScrollPane scrollPane,
            JTree tree) {

        ResultTreeScrollController h =
                new ResultTreeScrollController(scrollPane, tree);

        tree.addTreeWillExpandListener(h);
        tree.addTreeExpansionListener(h);
        tree.setScrollsOnExpand(false);
        return h;
    }
}
