/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.versioning.util.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreePath;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.awt.MouseUtils;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * Treetable to show diff/status nodes.
 * 
 * @author Ondra Vrabec
 */
public abstract class FileTreeView<T extends Node> implements FileViewComponent<T>, AncestorListener, PropertyChangeListener, MouseListener {

    protected final OutlineView view;
    private final ExplorerManager em;
    private boolean displayed;
    private EditorCookie[] editorCookies;
    private final ViewContainer viewComponent;
    private T[] nodes;
    
    private static class ViewContainer extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager em;
        
        private ViewContainer (ExplorerManager em) {
            this.em = em;
            setLayout(new BorderLayout());
        }
        
        @Override
        public ExplorerManager getExplorerManager () {
            return em;
        }
        
    }
    
    @NbBundle.Messages({
        "CTL_FileTree.treeColumn.Name=File"
    })
    public FileTreeView () {
        em = new ExplorerManager();
        view = new OutlineView(Bundle.CTL_FileTree_treeColumn_Name());
        view.getOutline().setShowHorizontalLines(true);
        view.getOutline().setShowVerticalLines(false);
        view.getOutline().setRootVisible(false);
        view.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        view.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        view.setPopupAllowed(false);
        view.getOutline().addMouseListener(this);
        view.getOutline().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        view.getOutline().getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(view.getOutline()));
            }
        });
        viewComponent = new ViewContainer(em);
        viewComponent.add(view, BorderLayout.CENTER);
        viewComponent.addAncestorListener(this);
        em.addPropertyChangeListener(this);
    }

    @Override
    public int getPreferredHeaderHeight () {
        return view.getOutline().getTableHeader().getPreferredSize().height;
    }

    @Override
    public JComponent getComponent () {
        return viewComponent;
    }

    @Override
    public int getPreferredHeight () {
        return view.getOutline().getPreferredSize().height;
    }
    
    protected final Node getNodeAt( int rowIndex ) {
        Node result = null;
        TreePath path = view.getOutline().getOutlineModel().getLayout().getPathForRow(rowIndex);
        if (path != null) {
            result = Visualizer.findNode(path.getLastPathComponent());
        }
        return result;
    }
    
    @Override
    public void setModel (T[] nodes, EditorCookie[] editorCookies, Object modelData) {
        this.editorCookies = editorCookies;
        this.nodes = nodes;
        em.setRootContext((Node) modelData);
        for (T n : nodes) {
            view.expandNode(n);
        }
    }
    
    protected abstract void setDefaultColumnSizes ();

    @Override
    public void ancestorAdded(AncestorEvent event) {
        if (!displayed) {
            displayed = true;
            setDefaultColumnSizes();
        }
    }

    @Override
    public void ancestorRemoved (AncestorEvent event) {
    }

    @Override
    public void ancestorMoved (AncestorEvent event) {
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] selectedNodes = em.getSelectedNodes();
            if (selectedNodes.length == 1) {
                // single selection
                T node = convertToAcceptedNode(em.getSelectedNodes()[0]);
                if (node != null) {
                    nodeSelected(node);
                    return;
                }
            }
            nodeSelected(null);
            final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, view);
            if (tc != null) {
                tc.setActivatedNodes(em.getSelectedNodes());
            }
        }
    }
    
    private void showPopup (final MouseEvent e) {
        int row = view.getOutline().rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = view.getOutline().getSelectedRows();

            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                view.getOutline().getSelectionModel().setSelectionInterval(row, row);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // invoke later so the selection on the table will be set first
                JPopupMenu menu = getPopup();
                if (menu != null) {
                    menu.show(view.getOutline(), e.getX(), e.getY());
                }
            }
        });
    }

    private void showPopup(Point p) {
        JPopupMenu menu = getPopup();
        if (menu != null) {
            menu.show(view.getOutline(), p.x, p.y);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = view.getOutline().rowAtPoint(e.getPoint());
            if (row == -1) return;
            T n = convertToAcceptedNode(getNodeAt(view.getOutline().convertRowIndexToModel(row)));
            if (n != null) {
                Action action = n.getPreferredAction();
                if (action != null && action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            }
        }
    }

    protected abstract T convertToAcceptedNode (Node node);

    protected abstract void nodeSelected (T node);
    
    protected abstract JPopupMenu getPopup ();
    
    @Override
    public T getSelectedNode () {
        T node = null;
        Node[] selectedNodes = em.getSelectedNodes();
        if (selectedNodes.length == 1) {
            node = convertToAcceptedNode(selectedNodes[0]);
        }
        return node;
    }

    @Override
    public void setSelectedNode (T toSelect) {
        try {
            em.setSelectedNodes(new Node[] { toSelect });
        } catch (PropertyVetoException ex) {
            Logger.getLogger(FileTreeView.class.getName()).log(Level.FINE, null, ex);
        }
    }

    @Override
    public T getNodeAtPosition (int position) {
        for (int i = 0; i < view.getOutline().getRowCount(); ++i) {
            Node n = getNodeAt(view.getOutline().convertRowIndexToModel(i));
            T converted = convertToAcceptedNode(n);
            if (converted != null) {
                if (position-- == 0) {
                    return converted;
                }
            }
        }
        return null;
    }

    @Override
    public T[] getNeighbouringNodes (T node, int boundary) {
        assert EventQueue.isDispatchThread();
        Set<T> neighbours = new LinkedHashSet<T>(5);
        neighbours.add(node);
        for (int i = 1; i < boundary; ++i) {
            T next = convertToAcceptedNode(findShiftNode(node, i, false));
            if (next != null) {
                neighbours.add(next);
            }
            T prev = convertToAcceptedNode(findShiftNode(node, -i, false));
            if (prev != null) {
                neighbours.add(prev);
            }
        }
        return neighbours.toArray((T[]) Array.newInstance(
                                    node.getClass(),
                                    neighbours.size()));
    }

    @Override
    public T getNextNode (T node) {
        Node nextNode = findShiftNode(node, 1, true);
        return convertToAcceptedNode(nextNode);
    }

    @Override
    public T getPreviousNode (T node) {
        Node prevNode = findShiftNode(node, -1, true);
        return convertToAcceptedNode(prevNode);
    }

    @Override
    public boolean hasNextNode (T node) {
        return convertToAcceptedNode(findShiftNode(node, 1, false)) != null;
    }

    @Override
    public boolean hasPreviousNode (T node) {
        return convertToAcceptedNode(findShiftNode(node, -1, false)) != null;
    }
    
    private Node findShiftNode (Node startingNode, int direction, boolean canExpand) {
        return startingNode == null ? null : findDetailNode(startingNode, direction, view, canExpand);
    }

    private Node findDetailNode(Node fromNode, int direction,
            OutlineView outlineView, boolean canExpand) {
        return findUp(fromNode, direction,
                convertToAcceptedNode(fromNode) != null || direction < 0 ? direction : 0,
                outlineView, canExpand);
    }
    
    /**
     * Start finding for next or previous occurance, from a node or its previous
     * or next sibling of node {@code node}
     *
     * @param node reference node
     * @param offset 0 to start from node {@code node}, 1 to start from its next
     * sibling, -1 to start from its previous sibling.
     * @param dir Direction: 1 for next, -1 for previous.
     */
    private Node findUp(Node node, int dir, int offset, OutlineView outlineView,
            boolean canExpand) {
        if (node == null) {
            return null;
        }
        Node parent = node.getParentNode();
        Node[] siblings;
        if (parent == null) {
            siblings = new Node[]{node};
        } else {
            siblings = getChildren(parent, outlineView, canExpand);
        }
        int nodeIndex = findChildIndex(node, siblings);
        if (nodeIndex + offset < 0 || nodeIndex + offset >= siblings.length) {
            return findUp(parent, dir, dir, outlineView, canExpand);
        }
        for (int i = nodeIndex + offset;
                i >= 0 && i < siblings.length; i += dir) {
            Node found = findDown(siblings[i], siblings, i, dir, outlineView,
                    canExpand);
            return found;
        }
        return findUp(parent, dir, offset, outlineView, canExpand);
    }

    /**
     * Find Depth-first search to find a detail node in the subtree.
     */
    private Node findDown(Node node, Node[] siblings, int nodeIndex,
            int dir, OutlineView outlineView, boolean canExpand) {

        Node[] children = getChildren(node, outlineView, canExpand);
        for (int i = dir > 0 ? 0 : children.length - 1;
                i >= 0 && i < children.length; i += dir) {
            Node found = findDown(children[i], children, i, dir, outlineView,
                    canExpand);
            if (found != null) {
                return found;
            }
        }
        for (int i = nodeIndex; i >= 0 && i < siblings.length; i += dir) {
            Node converted = convertToAcceptedNode(siblings[i]);
            if (converted != null) {
                return converted;
            }
        }
        return null;
    }

    private static int findChildIndex(Node selectedNode, Node[] siblings) {
        int pos = -1;
        for (int i = 0; i < siblings.length; i++) {
            if (siblings[i] == selectedNode) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private static Node[] getChildren(Node n, OutlineView outlineView,
            boolean canExpand) {
        if (outlineView != null) {
            if (!outlineView.isExpanded(n)) {
                if (canExpand) {
                    outlineView.expandNode(n);
                } else {
                    return n.getChildren().getNodes(true);
                }
            }
            return getChildrenInDisplayedOrder(n, outlineView);
        } else {
            return n.getChildren().getNodes(true);
        }
    }

    private static Node[] getChildrenInDisplayedOrder(Node parent,
            OutlineView outlineView) {

        Outline outline = outlineView.getOutline();
        Node[] unsortedChildren = parent.getChildren().getNodes(true);
        int rows = outlineView.getOutline().getRowCount();
        int start = findRowIndexInOutline(parent, outline, rows);
        if (start == -1 && parent != ExplorerManager.find(outlineView).getRootContext()) {
            return unsortedChildren;
        }
        List<Node> children = new LinkedList<Node>();
        for (int j = start + 1; j < rows; j++) {
            int childModelIndex = outline.convertRowIndexToModel(j);
            if (childModelIndex == -1) {
                continue;
            }
            Object childObject = outline.getModel().getValueAt(
                    childModelIndex, 0);
            Node childNode = Visualizer.findNode(childObject);
            if (childNode.getParentNode() == parent) {
                children.add(childNode);
            } else if (children.size() == unsortedChildren.length) {
                break;
            }
        }
        return children.toArray(new Node[children.size()]);
    }

    private static int findRowIndexInOutline(Node node, Outline outline,
            int rows) {

        int startRow = Math.max(outline.getSelectedRow(), 0);
        int offset = 0;
        while (startRow + offset < rows || startRow - offset >= 0) {
            int up = startRow + offset + 1;
            int down = startRow - offset;

            if (up < rows && testNodeInRow(outline, node, up)) {
                return up;
            } else if (down >= 0 && testNodeInRow(outline, node, down)) {
                return down;
            } else {
                offset++;
            }
        }
        return -1;
    }

    private static boolean testNodeInRow(Outline outline, Node node, int i) {
        int modelIndex = outline.convertRowIndexToModel(i);
        if (modelIndex != -1) {
            Object o = outline.getModel().getValueAt(modelIndex, 0);
            Node n = Visualizer.findNode(o);
            if (n == node) {
                return true;
            }
        }
        return false;
    }

    protected abstract class AbstractRenderDataProvider implements RenderDataProvider {
        
        @Override
        public String getDisplayName (Object o) {
            Node n = Visualizer.findNode(o);
            String value = n.getDisplayName();
            T leafNode = convertToAcceptedNode(n);
            if (leafNode != null) {
                String htmlDisplayName = DiffUtils.getHtmlDisplayName(leafNode, isModified(leafNode), Arrays.asList(em.getSelectedNodes()).contains(n));
                htmlDisplayName = annotateName(leafNode, htmlDisplayName);
                if (htmlDisplayName != null) {
                    value = "<html>" + htmlDisplayName; //NOI18N
                }
            }
            return value;
        }

        @Override
        public boolean isHtmlDisplayName (Object o) {
            return true;
        }

        @Override
        public Color getBackground (Object o) {
            return null;
        }

        @Override
        public Color getForeground (Object o) {
            return null;
        }

        @Override
        public String getTooltipText (Object o) {
            Node n = Visualizer.findNode(o);
            File file = n.getLookup().lookup(File.class); 
            return file != null ? file.getAbsolutePath() : n.getShortDescription();
        }

        @Override
        public Icon getIcon (Object o) {
            Node n = Visualizer.findNode(o);
            return new ImageIcon(n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        }

        private boolean isModified (T node) {
            int index = Arrays.asList(nodes).indexOf(node);
            EditorCookie editorCookie = index >= 0 ? editorCookies[index] : null;
            return (editorCookie != null) ? editorCookie.isModified() : false;
        }

        protected abstract String annotateName (T leafNode, String htmlDisplayName);
    }
}
