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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.spi.viewmodel.TreeExpansionModel;

import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author Dan
 */
public class DebugTreeView extends BeanTreeView implements TreeExpansionListener {

    private int thickness;
    private Color highlightColor = new Color(233, 239, 248); // new Color(234, 234, 250);
    private Color currentThreadColor = new Color(233, 255, 230); // new Color(234, 234, 250);
    private Color whiteColor = javax.swing.UIManager.getDefaults().getColor("Tree.background"); // NOI18N
    
    private JPDAThread focusedThread;
    
    DebugTreeView() {
        super();
//        tree.setPreferredSize(new Dimension(5, 5)); // [TODO]
//        tree.setMinimumSize(new Dimension(5, 5));
//        setPreferredSize(new Dimension(5, 5));
//        setMinimumSize(new Dimension(5, 5));
        setBackground(whiteColor);
        tree.setOpaque(false);
        ((JComponent)tree.getParent()).setOpaque(false);
        
        tree.addTreeExpansionListener(this);
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

    public Object getJPDAObject(TreePath path) {
        Node node = Visualizer.findNode(path.getLastPathComponent());
        JPDAThread jpdaThread = node.getLookup().lookup(JPDAThread.class);
        if (jpdaThread != null) {
            return jpdaThread;
        }
        JPDAThreadGroup jpdaThreadGroup = node.getLookup().lookup(JPDAThreadGroup.class);
        return jpdaThreadGroup;
    }
    
    public void addTreeExpansionListener(TreeExpansionListener listener) {
        tree.addTreeExpansionListener(listener);
    }
    
    public void removeTreeExpansionListener(TreeExpansionListener listener) {
        tree.removeTreeExpansionListener(listener);
    }

    void setExpansionModel(TreeExpansionModel model) {
        // [TODO] ???
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintStripes(g, this);
    }

    // [TODO] optimize paintStripes() method
    void paintStripes(Graphics g, JComponent comp) {
        List<TreePath> paths = getVisiblePaths(); // [TODO] do not call getVisiblePaths()
        int linesNumber = paths.size();
        int zebraHeight = linesNumber * thickness;
        Rectangle rect = paths.size() > 0 ? tree.getRowBounds(tree.getRowForPath(paths.get(0))) : null;
        thickness = rect != null ? (int) Math.round(rect.getHeight()) : 18; // [TODO] compute height for each particular row
        
        int width = getWidth();
        int height = getHeight();
        
        if ((width <= 0) || (height <= 0)) {
            return;
        }

        Rectangle clipRect = g.getClipBounds();
        int clipX;
        int clipY;
        int clipW;
        int clipH;
        if (clipRect == null) {
            clipX = clipY = 0;
            clipW = width;
            clipH = height;
        }
        else {
            clipX = clipRect.x;
            clipY = clipRect.y;
            clipW = clipRect.width;
            clipH = clipRect.height;
        }

        if(clipW > width) {
            clipW = width;
        }
        if(clipH > height) {
            clipH = height;
        }

        Color origColor = g.getColor();
        JPDADebugger debugger = ThreadsListener.getDefault().getDebugger();
        JPDAThread currentThread = (debugger != null) ? debugger.getCurrentThread() : null;
        if (currentThread != null && !currentThread.isSuspended()) {
            currentThread = null;
        }
        boolean isHighlighted = false;
        boolean isCurrent = false;
        Iterator<TreePath> iter = paths.iterator();
        int firstGroupNumber = clipY / thickness;
        for (int x = 0; x <= firstGroupNumber && iter.hasNext(); x++) {
            Node node = Visualizer.findNode(iter.next().getLastPathComponent());
            JPDAThread thread = node.getLookup().lookup(JPDAThread.class);
            isHighlighted = focusedThread != null && thread == focusedThread;
            if (thread != null) {
                isCurrent = currentThread == thread;
            }
        }
        
        int sy = (clipY / thickness) * thickness;
        int limit = Math.min(clipY + clipH - 1, zebraHeight);
        while (sy < limit) {
            int y1 = Math.max(sy, clipY);
            int y2 = Math.min(clipY + clipH, y1 + thickness);
            if (isHighlighted || isCurrent) {
                //g.setColor(isHighlighted ? highlightColor : (isCurrent ? currentThreadColor : whiteColor));
                g.setColor(isHighlighted ? highlightColor : currentThreadColor);
                g.fillRect(clipX, y1, clipW, y2 - y1);
            }
            sy += thickness;
            if (iter.hasNext()) {
                Node node = Visualizer.findNode(iter.next().getLastPathComponent());
                JPDAThread thread = node.getLookup().lookup(JPDAThread.class);
                isHighlighted = focusedThread != null && thread == focusedThread;
                if (thread != null) {
                    isCurrent = currentThread == thread;
                }
            } else {
                isHighlighted = false;
                isCurrent = false;
            }
        }
//        if (sy < clipY + clipH - 1) {
//            g.setColor(whiteColor);
//            g.fillRect(clipX, sy, clipW, clipH + clipY - sy);
//        }
        g.setColor(origColor);
    }

    boolean threadFocuseGained(JPDAThread jpdaThread) {
        if (jpdaThread != null && focusedThread != jpdaThread) {
            focusedThread = jpdaThread;
            repaint();
            return true;
        }
        return false;
    }

    boolean threadFocuseLost(JPDAThread jpdaThread) {
        if (jpdaThread != null && focusedThread == jpdaThread) {
            focusedThread = null;
            repaint();
            return true;
        }
        return false;
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

    public void treeExpanded(TreeExpansionEvent event) {
        repaint(); // [TODO]
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        repaint(); // [TODO]
    }
    
}
