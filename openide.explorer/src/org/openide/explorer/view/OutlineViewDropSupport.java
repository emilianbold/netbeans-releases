/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.openide.explorer.view;

import org.openide.ErrorManager;
import org.openide.explorer.view.NodeRenderer;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;


/** Implementation of drop support for asociated OutlineView.
*
* @author Dafe Simonek, Jiri Rechtacek, David Strupl
*/
final class OutlineViewDropSupport implements DropTargetListener, Runnable {
    final static protected int FUSSY_POINTING = 3;
    final static private int DELAY_TIME_FOR_EXPAND = 1000;
    final static private int SHIFT_DOWN = -1;
    final static private int SHIFT_RIGHT = 0;//10;
    final static private int SHIFT_LEFT = 0;//15;

    /** true if support is active, false otherwise */
    boolean active = false;
    boolean dropTargetPopupAllowed;

    /** Drop target asociated with the table */
    DropTarget dropTarget;

    /** Node area which we were during
    * DnD operation. */
    Rectangle lastNodeArea;
    private int upperNodeIdx = -1;
    private int lowerNodeIdx = -1;

    /** Swing Timer for expand node's parent with delay time. */
    Timer timer;

    /** Glass pane for JTree which is associate with this class. */
    DropGlassPane dropPane;
    private int pointAt = DragDropUtilities.NODE_CENTRAL;

    /** View manager. */
    protected OutlineView view;

    /** The component we are supporting with drop support */
    protected JTable table;

    /** Creates new TreeViewDropSupport */
    public OutlineViewDropSupport(OutlineView view, JTable table, boolean dropTargetPopupAllowed) {
        this.view = view;
        this.table = table;
        this.dropTargetPopupAllowed = dropTargetPopupAllowed;
    }

    public void setDropTargetPopupAllowed(boolean value) {
        dropTargetPopupAllowed = value;
    }

    public boolean isDropTargetPopupAllowed() {
        return dropTargetPopupAllowed;
    }

    /** User is starting to drag over us */
    public void dragEnter(DropTargetDragEvent dtde) {
        log("dragEnter " + dtde); // NOI18N
        checkStoredGlassPane();

        // set a status and cursor of dnd action
        doDragOver(dtde);
    }

    /** User drags over us */
    public void dragOver(DropTargetDragEvent dtde) {
        log("dragOver " + dtde); // NOI18N
        // bugfix #34483; jdk1.4.1 on w2k could calls dragOver() before dragEnter()
        // (jkdbug fixed in 1.4.2)
        // this check make dragOver/Enter more robust
        checkStoredGlassPane();

        // set a status and cursor of dnd action
        doDragOver(dtde);
    }

    private void checkStoredGlassPane() {
        // remember current glass pane to set back at end of dragging over this compoment
        if (!DropGlassPane.isOriginalPaneStored()) {
            Component comp = table.getRootPane().getGlassPane();
            DropGlassPane.setOriginalPane(table, comp, comp.isVisible());

            // set glass pane for paint selection line
            dropPane = DropGlassPane.getDefault(table);
            table.getRootPane().setGlassPane(dropPane);
            dropPane.revalidate();
            dropPane.setVisible(true);
            log("dropPane was set"); // NOI18N
        }
    }

    /** Process events dragEnter or dragOver. */
    private void doDragOver(DropTargetDragEvent dtde) {
        int dropAction = dtde.getDropAction();
        int allowedDropActions = view.getAllowedDropActions();
        dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dropAction, allowedDropActions);

        // 1. test if I'm over any node
        Point p = dtde.getLocation();
        int row = view.getOutline().rowAtPoint(p);
        int column = view.getOutline().columnAtPoint(p);
        log("doDragOver row == " + row + " column == " + column); // NOI18N
        // 2. find node for drop
        Node dropNode = null;
        
        if (row == -1) {
            // #64469: Can't drop into empty explorer area
            dropNode = view.manager.getRootContext ();
            if (canDrop(dropNode, dropAction)) {
                // ok, root accept
                dtde.acceptDrag(dropAction);
            } else {
                dtde.rejectDrag();
            }
            return ;
        } else {
            dropNode = getNodeForDrop(p);
        }
        log("doDragOver dropNode == " + dropNode); // NOI18N

        // if I haven't any node for drop then reject drop
        if (dropNode == null) {
            dtde.rejectDrag();
            removeDropLine();

            return;
        }

        Rectangle nodeArea = table.getCellRect(row, column, false);
        log("nodeArea == " + nodeArea); // NOI18N
        int endPointX = nodeArea.x + nodeArea.width;
        if (nodeArea != null) {
            pointAt = DragDropUtilities.NODE_CENTRAL;

            if (p.y <= (nodeArea.y + FUSSY_POINTING)) {
                // don't get line above root
                if (row != 0) {
                    // point above node
                    pointAt = DragDropUtilities.NODE_UP;
                    // drop candidate is parent
                    if (dropNode.getParentNode() != null) {
                        log("dropNode is parent 1"); // NOI18N
                        dropNode = dropNode.getParentNode();
                    }
                }
            } else if (p.y >= ((nodeArea.y + nodeArea.height) - FUSSY_POINTING)) {
                // exclude expanded folder
                TreePath tp = view.getOutline().getLayoutCache().getPathForRow(
                        view.getOutline().convertRowIndexToModel(row));
                log("tp == " + tp); //NOI18N
                if (!view.getOutline().getLayoutCache().isExpanded(tp)) {
                    log("tree path is not expanded"); // NOI18N
                    // point bellow node
                    pointAt = DragDropUtilities.NODE_DOWN;

                    TreePath downPath = view.getOutline().getLayoutCache().getPathForRow(
                        view.getOutline().convertRowIndexToModel(row+1));
                    // drop candidate is parent
                    if (dropNode.getParentNode() != null) {
                        log("dropNode is parent 2"); // NOI18N
                        dropNode = dropNode.getParentNode();
                    }
                }
            }
        }

        endPointX = endPointX + SHIFT_RIGHT;

        // 2.b. check index cookie
        Index indexCookie = (Index) dropNode.getCookie(Index.class);
        log("indexCookie == " + indexCookie); // NOI18N
        if (indexCookie != null) {
            if (pointAt == DragDropUtilities.NODE_UP) {
                lowerNodeIdx = indexCookie.indexOf(getNodeForDrop(p));
                upperNodeIdx = lowerNodeIdx - 1;
            } else if (pointAt == DragDropUtilities.NODE_DOWN) {
                upperNodeIdx = indexCookie.indexOf(getNodeForDrop(p));
                lowerNodeIdx = upperNodeIdx + 1;
            }
        }

        // 3. expand with a delay
        final TreePath path = view.getOutline().getLayoutCache().getPathForRow(
                view.getOutline().convertRowIndexToModel(row));
        boolean expanded = view.getOutline().getLayoutCache().isExpanded(path);
        if (
            ((timer == null) || !timer.isRunning()) && (dropNode != null) && !dropNode.isLeaf() 
            && !expanded
        ) {
            // ok, let's expand in a while
            // node is candidate for expand
            final Node cn = dropNode;

            // remove old timer
            removeTimer();

            // create new timer
            timer = new Timer(
                    DELAY_TIME_FOR_EXPAND,
                    new ActionListener() {
                        final public void actionPerformed(ActionEvent e) {
                            log("should expand " + path); // NOI18N
                            view.getOutline().expandPath(path);
                        }
                    }
                );
            timer.setRepeats(false);
            timer.start();
        }

        // 4. present node for drop
        // prepare selection or line
        if (pointAt == DragDropUtilities.NODE_CENTRAL) {
            // no line
            dropPane.setDropLine(null);
        } else {
            // line and selection of parent if any
            if (pointAt == DragDropUtilities.NODE_UP) {
                Line2D line = new Double(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + SHIFT_DOWN, endPointX, nodeArea.y + SHIFT_DOWN
                    );
                convertBoundsAndSetDropLine(line);

                // enlagre node area with area for line
                Rectangle lineArea = new Rectangle(
                        nodeArea.x - SHIFT_LEFT, (nodeArea.y + SHIFT_DOWN) - 3, endPointX - nodeArea.x + SHIFT_LEFT, 5
                    );
                nodeArea = (Rectangle) nodeArea.createUnion(lineArea);
            } else {
                Line2D line = new Double(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + nodeArea.height + SHIFT_DOWN, endPointX,
                        nodeArea.y + nodeArea.height + SHIFT_DOWN
                    );
                convertBoundsAndSetDropLine(line);

                // enlagre node area with area for line
                Rectangle lineArea = new Rectangle(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + nodeArea.height, endPointX - nodeArea.x + SHIFT_LEFT,
                        SHIFT_DOWN + 3
                    );
                nodeArea = (Rectangle) nodeArea.createUnion(lineArea);
            }
        }

        // back normal view w/o any selecetion nor line
        if ((lastNodeArea != null) && (!lastNodeArea.equals(nodeArea))) {
            repaint(lastNodeArea);
        }

        // paint new state
        if (!nodeArea.equals(lastNodeArea)) {
            repaint(nodeArea);
            lastNodeArea = nodeArea;
            removeTimer();
        }

        // 5. show to cursor belong to state
        if (canDrop(dropNode, dropAction)) {
            // ok, can accept
            dtde.acceptDrag(dropAction);
        } else {
            // can only reorder?
            if (canReorder(dropNode, ExplorerDnDManager.getDefault().getDraggedNodes())) {
                // ok, can accept only reoder
                dtde.acceptDrag(dropAction);
            } else {
                dtde.rejectDrag();
            }
        }
    }

    /** Repaints TreeView, the given rectangle is enlarged for 5 pixels
     * because some parts was not repainted correctly.
     * @param Rectangle r rectangle which will be repainted.*/
    private void repaint(Rectangle r) {
        table.repaint(r.x - 5, r.y - 5, r.width + 10, r.height + 10);
    }

    /** Converts line's bounds by the bounds of the root pane. Drop glass pane
     * is over this root pane. After covert a given line is set to drop glass pane.
     * @param line line for show in drop glass pane */
    private void convertBoundsAndSetDropLine(final Line2D line) {
        int x1 = (int) line.getX1();
        int x2 = (int) line.getX2();
        int y1 = (int) line.getY1();
        int y2 = (int) line.getY2();
        Point p1 = SwingUtilities.convertPoint(table, x1, y1, table.getRootPane());
        Point p2 = SwingUtilities.convertPoint(table, x2, y2, table.getRootPane());
        line.setLine(p1, p2);
        dropPane.setDropLine(line);
    }

    /** Removes timer and all listeners. */
    private void removeTimer() {
        if (timer != null) {
            ActionListener[] l = (ActionListener[]) timer.getListeners(ActionListener.class);

            for (int i = 0; i < l.length; i++) {
                timer.removeActionListener(l[i]);
            }

            timer.stop();
            timer = null;
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        // check if the nodes are willing to do selected action
        Node[] nodes = ExplorerDnDManager.getDefault().getDraggedNodes();
        int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dtde.getDropAction(), view.getAllowedDropActions()
            );

        for (int i = 0; i < nodes.length; i++) {
            if (
                ((view.getAllowedDropActions() & dropAction) == 0) ||
                    !DragDropUtilities.checkNodeForAction(nodes[i], dropAction)
            ) {
                // this action is not supported
                dtde.rejectDrag();

                return;
            }
        }

        return;
    }

    /** User exits the dragging */
    public void dragExit(DropTargetEvent dte) {
        stopDragging();
    }

    private void removeDropLine() {
        dropPane.setDropLine(null);

        if (lastNodeArea != null) {
            repaint(lastNodeArea);
            lastNodeArea = null;
        }
    }

    private void stopDragging() {
        removeDropLine();
        removeTimer();

        // set back the remembered glass pane
        if (DropGlassPane.isOriginalPaneStored()) {
            DropGlassPane.putBackOriginal();
        }
    }

    /** Get a node on given point or null if there none*/
    private Node getNodeForDrop(Point p) {
        int row = view.getOutline().rowAtPoint(p);
        return view.getNodeFromRow(row);
    }

    private boolean canReorder(Node folder, Node[] dragNodes) {
        if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_MOVE) == 0) {
            log("canReorder returning false 1");
            return false;
        }

        if ((folder == null) || (dragNodes == null) || (dragNodes.length == 0)) {
            log("canReorder returning false 2");
            return false;
        }

        // has folder a index cookie?
        Index ic = (Index) folder.getCookie(Index.class);

        if (ic == null) {
            log("canReorder returning false 3");
            return false;
        }

        // folder has index cookie
        // check if all dragNodes are from same folder
        for (int i = 0; i < dragNodes.length; i++) {
            // bugfix #23988, check if dragNodes[i] isn't null
            if (dragNodes[i] == null) {
                log("canReorder returning false 4");
                return false;
            }

            if (dragNodes[i].getParentNode() == null) {
                log("canReorder returning false 5");
                return false;
            }

            if (!dragNodes[i].getParentNode().equals(folder)) {
                log("canReorder returning false 6");
                return false;
            }
        }
        log("canReorder returning true");
        return true;
    }

    private void performReorder(final Node folder, Node[] dragNodes, int lNode, int uNode) {
        try {
            Index indexCookie = (Index) folder.getCookie(Index.class);
            log("performReorder indexCookie == " + indexCookie);

            if (indexCookie != null) {
                int[] perm = new int[indexCookie.getNodesCount()];
                int[] indexes = new int[dragNodes.length];
                int indexesLength = 0;

                for (int i = 0; i < dragNodes.length; i++) {
                    int idx = indexCookie.indexOf(dragNodes[i]);

                    if ((idx >= 0) && (idx < perm.length)) {
                        indexes[indexesLength++] = idx;
                    }
                }

                // XXX: normally indexes of dragged nodes should be in ascending order, but
                // it seems that Tree.getSelectionPaths doesn't keep this order
                Arrays.sort(indexes);

                if ((lNode < 0) || (uNode >= perm.length) || (indexesLength == 0)) {
                    return;
                }

                int k = 0;

                for (int i = 0; i < perm.length; i++) {
                    if (i <= uNode) {
                        if (!containsNumber(indexes, indexesLength, i)) {
                            perm[i] = k++;
                        }

                        if (i == uNode) {
                            for (int j = 0; j < indexesLength; j++) {
                                if (indexes[j] <= uNode) {
                                    perm[indexes[j]] = k++;
                                }
                            }
                        }
                    } else {
                        if (i == lNode) {
                            for (int j = 0; j < indexesLength; j++) {
                                if (indexes[j] >= lNode) {
                                    perm[indexes[j]] = k++;
                                }
                            }
                        }

                        if (!containsNumber(indexes, indexesLength, i)) {
                            perm[i] = k++;
                        }
                    }
                }

                // check for identity permutation
                for (int i = 0; i < perm.length; i++) {
                    if (perm[i] != i) {
                        indexCookie.reorder(perm);

                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Pending: add annotation or remove try/catch block
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    private boolean containsNumber(int[] arr, int arrLength, int n) {
        for (int i = 0; i < arrLength; i++) {
            if (arr[i] == n) {
                return true;
            }
        }

        return false;
    }

    private Node[] findDropedNodes(Node folder, Node[] dragNodes) {
        if ((folder == null) || (dragNodes.length == 0)) {
            return null;
        }

        Node[] dropNodes = new Node[dragNodes.length];
        Children children = folder.getChildren();

        for (int i = 0; i < dragNodes.length; i++) {
            dropNodes[i] = children.findChild(dragNodes[i].getName());
        }

        return dropNodes;
    }

    /** Can node recieve given drop action? */

    // XXX canditate for more general support
    private boolean canDrop(Node n, int dropAction) {
        log("canDrop " + n); // NOI18N
        if (n == null) {
            return false;
        }

        // Test to see if the target node supports the drop action
        if ((view.getAllowedDropActions() & dropAction) == 0) {
            return false;
        }

        // test if a parent of the dragged nodes isn't the node over
        // only for MOVE action
        if ((DnDConstants.ACTION_MOVE & dropAction) != 0) {
            Node[] nodes = ExplorerDnDManager.getDefault().getDraggedNodes();

            if (nodes == null) {
                return false;
            }

            for (int i = 0; i < nodes.length; i++) {
                if (n.equals(nodes[i].getParentNode())) {
                    return false;
                }
            }
        }

        Transferable trans = ExplorerDnDManager.getDefault().getDraggedTransferable(
                (DnDConstants.ACTION_MOVE & dropAction) != 0
            );
        log("transferable == " + trans); // NOI18N
        if (trans == null) {
            return false;
        }

        // get paste types for given transferred transferable
        PasteType pt = null;//TODO DragDropUtilities.getDropType(n, trans, dropAction);

        return (pt != null);
    }

    /** Performs the drop action, if we are dropping on
    * right node and target node agrees.
    */
    public void drop(DropTargetDropEvent dtde) {
        boolean dropResult = true;
        try {
            log("drop");
            stopDragging();

            // find node for the drop perform
            Node dropNode = getNodeForDrop(dtde.getLocation());
            log("drop dropNode == " + dropNode);

            // #64469: Can't drop into empty explorer area
            if (dropNode == null) {
                dropNode = view.manager.getRootContext ();
            } else if (pointAt != DragDropUtilities.NODE_CENTRAL) {
                dropNode = dropNode.getParentNode();
            }

            Node[] dragNodes = ExplorerDnDManager.getDefault().getDraggedNodes();
            int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                    dtde.getDropAction(), view.getAllowedDropActions()
                );

            if (!canDrop(dropNode, dropAction)) {
                if (canReorder(dropNode, dragNodes)) {
                    performReorder(dropNode, dragNodes, lowerNodeIdx, upperNodeIdx);
                    dtde.acceptDrop(dropAction);
                } else {
                    dtde.rejectDrop();
                }

                return;
            }

            // finally perform the drop
            dtde.acceptDrop(dropAction);

            if (DnDConstants.ACTION_LINK == dropAction) {
                // construct all paste types
                PasteType[] ptCut = new PasteType[] {  };

                // construct all paste types
                PasteType[] ptCopy = new PasteType[] {  };

                // do not try get paste types for move if MOVE is not allowed
                if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_MOVE) != 0) {
                    ptCut = DragDropUtilities.getPasteTypes(
                            dropNode, ExplorerDnDManager.getDefault().getDraggedTransferable(true)
                        );
                }

                // do not try get paste types for copy if COPY is not allowed
                if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_COPY) != 0) {
                    ptCopy = DragDropUtilities.getPasteTypes(
                            dropNode, ExplorerDnDManager.getDefault().getDraggedTransferable(false)
                        );
                }

                TreeSet setPasteTypes = new TreeSet(
                        new Comparator() {
                            public int compare(Object obj1, Object obj2) {
                                // have to fix: the different actions can have same name!!!

                                int res = ((PasteType)obj1).getName ().compareTo (((PasteType)obj2).getName ());
                                log("res1: "+res); // NOI18N
                                if (res == 0) {
                                    res = System.identityHashCode(obj1)-System.identityHashCode(obj2);
                                }
                                log("res2: "+res); // NOI18N
                                return res;
                            }
                        }
                    );

                for (int i = 0; i < ptCut.length; i++) {
                    log(ptCut[i].getName()+", "+System.identityHashCode(ptCut[i]));
                    setPasteTypes.add(ptCut[i]);
                }

                for (int i = 0; i < ptCopy.length; i++) {
                    log(ptCopy[i].getName()+", "+System.identityHashCode(ptCopy[i]));
                    setPasteTypes.add(ptCopy[i]);
                }

                DragDropUtilities.createDropFinishPopup(setPasteTypes).show(
                    table, Math.max(dtde.getLocation().x - 5, 0), Math.max(dtde.getLocation().y - 5, 0)
                );

                // reorder have to be perform
                if (canReorder(dropNode, dragNodes)) {
                    final Node tempDropNode = dropNode;
                    final int tmpUpper = upperNodeIdx;
                    final int tmpLower = lowerNodeIdx;
                    final Node[] tempDragNodes = dragNodes;
                    DragDropUtilities.setPostDropRun(
                        new Runnable() {
                            public void run() {
                                performReorder(
                                    tempDropNode, findDropedNodes(tempDropNode, tempDragNodes), tmpLower, tmpUpper
                                );
                            }
                        }
                    );
                }
            } else {
                // get correct paste type
                PasteType pt = DragDropUtilities.getDropType(
                        dropNode,
                        ExplorerDnDManager.getDefault().getDraggedTransferable(
                            (DnDConstants.ACTION_MOVE & dropAction) != 0
                        ), dropAction, -1 //TODO dropIndex!
                    );

                Node[] diffNodes = DragDropUtilities.performPaste(pt, dropNode);
                ExplorerDnDManager.getDefault().setDraggedNodes(diffNodes);

                // check canReorder or optionally perform it
                if (canReorder(dropNode, diffNodes)) {
                    performReorder(dropNode, diffNodes, lowerNodeIdx, upperNodeIdx);
                }
            }
        } finally {
            // finished
            dtde.dropComplete(dropResult);
        }
    }

    /** Activates or deactivates Drag support on asociated JTree
    * component
    * @param active true if the support should be active, false
    * otherwise
    */
    public void activate(boolean active) {
        if (this.active == active) {
            return;
        }

        this.active = active;
        getDropTarget().setActive(active);
    }

    /** Implementation of the runnable interface.
    * Notifies user in AWT thread. */
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);

            return;
        }

        DragDropUtilities.dropNotSuccesfull();
    }

    /** Safe accessor to the drop target which is asociated
    * with the table */
    DropTarget getDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(table, view.getAllowedDropActions(), this, false);
        }

        return dropTarget;
    }
    
    //
    // Logging:
    //
    
    /** Using the NetBeans error manager for logging. */
    private static ErrorManager err = ErrorManager.getDefault().getInstance(
            OutlineViewDropSupport.class.getName());
    /** Settable from the system property */
    private static boolean LOGABLE = err.isLoggable(ErrorManager.INFORMATIONAL);
    /**
     * Logs the string only if logging is turned on.
     */
    private static void log(String s) {
        if (LOGABLE) {
            err.log(s);
        }
    }

}
