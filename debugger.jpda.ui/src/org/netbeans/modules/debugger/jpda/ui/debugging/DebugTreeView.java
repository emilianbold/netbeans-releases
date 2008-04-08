/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.Renderer;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author Dan
 */
public class DebugTreeView extends BeanTreeView implements TreeCellRenderer {

    private TreeCellRenderer origCellRenderer;
    
    DebugTreeView() {
        super();
        tree.setPreferredSize(new Dimension(5, 5));
        tree.setMinimumSize(new Dimension(5, 5));
        setPreferredSize(new Dimension(5, 5));
        setMinimumSize(new Dimension(5, 5));
        tree.setOpaque(true);
        setOpaque(true);
        
        origCellRenderer = tree.getCellRenderer();
        // tree.setCellRenderer(this);
    }
    
    public JTree getTree() {
        return tree;
    }

    public List<TreePath> getVisiblePaths() {
        synchronized(tree) {
            List<TreePath> result = new ArrayList<TreePath>();
//            collectVisiblePaths(result, tree.getPathForRow(0));
            int count = tree.getRowCount();
            for (int x = 0; x < count; x++) {
                TreePath path = tree.getPathForRow(x);
                if (tree.isVisible(path)) {
                    result.add(path);
                }
            }
            return result;
        }
    }

    public void addTreeExpansionListener(TreeExpansionListener listener) {
        tree.addTreeExpansionListener(listener);
    }
    
    public void removeTreeExpansionListener(TreeExpansionListener listener) {
        tree.removeTreeExpansionListener(listener);
    }

    private void collectVisiblePaths(List<TreePath> result, TreePath path) {
        result.add(path);
        Enumeration<TreePath> paths = tree.getExpandedDescendants(path);
        if (paths != null) {
            while (paths.hasMoreElements()) {
                path = paths.nextElement();
                collectVisiblePaths(result, path);
            }
        }
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component comp = origCellRenderer.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
        if (row % 2 == 0 && comp instanceof Renderer) {
            comp.setBackground(Color.LIGHT_GRAY);
        }
        return comp;
    }
    
}
