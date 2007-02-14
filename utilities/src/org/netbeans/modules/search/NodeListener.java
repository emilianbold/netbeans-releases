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

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.awt.Toolkit;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON1_MASK;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.actions.EditAction;
import org.openide.cookies.EditCookie;
import org.openide.nodes.Node;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Marian Petras
 */
final class NodeListener implements MouseListener, KeyListener,
                                    TreeWillExpandListener,
                                    TreeExpansionListener {
    
    /** */
    private static final boolean COLLAPSE_FILE_ON_SELECTION = false;
    /** */
    private static final boolean COLLAPSE_FILE_ON_UNSELECTION = true;
    /** */
    private boolean selectionChangeEnabled = true;
    
    NodeListener() {
    }
    
    public void mouseClicked(MouseEvent e) {
        // todo (#pf): we need to solve problem between click and double
        // click - click should be possible only on the check box area
        // and double click should be bordered by title text.
        // we need a test how to detect where the mouse pointer is
        final JTree tree = (JTree) e.getSource();

        final int clickCount = e.getClickCount();
        if ((clickCount != 1) && (clickCount != 2)) {
            return;
        }
        
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        final ResultModel resultModel = getResultModel(tree);
        final boolean insideCheckBox = isInsideCheckBox(tree,
                                                        path,
                                                        resultModel,
                                                        e);
        
        final int modifiers = e.getModifiersEx();
        if ((modifiers == 0) || (modifiers == BUTTON1_MASK)
                             || (modifiers == BUTTON1_DOWN_MASK)) {
            if (clickCount == 1) {
                if (insideCheckBox) {
                    assert path != null;
                    toggleSelection(tree, path);
                }
            }
            if (clickCount == 2) {
                if (!insideCheckBox) {
                    if (path.getPathCount() == 1) {     //root node
                        toggleExpansionState(tree, path);
                    } else {
                        Node nbNode = getNbNode(path, resultModel);
                        callDefaultAction(nbNode,
                                          e.getSource(),
                                          e.getID(),
                                          "double-click");              //NOI18N
                    }
                }
            }
        }
    }
    
    private void popupTriggerEventFired(MouseEvent e) {
        final JTree tree = (JTree) e.getSource();
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        final ResultModel resultModel = getResultModel(tree);
        tree.setSelectionPath(path);
        if (!isInsideCheckBox(tree, path, resultModel, e)) {
            showPopup(tree, path, resultModel, e);
        }
    }
    
    /**
     */
    private boolean isInsideCheckBox(final JTree tree,
                                     final TreePath path,
                                     final ResultModel resultModel,
                                     final MouseEvent mouseEvent) {
        if ((resultModel == null) || !resultModel.searchAndReplace) {
            return false;
        }
        
        Rectangle rowRect = tree.getPathBounds(path);
        Rectangle chRect = NodeRenderer.getCheckBoxRectangle();
        chRect.setLocation(rowRect.x + chRect.x, rowRect.y + chRect.y);
        return chRect.contains(mouseEvent.getPoint());
    }
    
    /**
     */
    private void showPopup(final JTree tree,
                           final TreePath path,
                           final ResultModel resultModel,
                           final MouseEvent e) {
        final int pathCount = path.getPathCount();
        if (pathCount == 1) {               //root node
            //no popup-menu for the root node
        } else if (pathCount == 2) {
            Node nbNode = getNbNode(path, resultModel);
            if (nbNode != null) {
                JPopupMenu popup = createFileNodePopupMenu(nbNode);
                if (popup != null) {
                    popup.show(tree, e.getX(), e.getY());
                }
            }
        } else if (pathCount == 3) {        //detail node
            Node nbNode = getNbNode(path, resultModel);
            if (nbNode != null) {
                nbNode.getContextMenu().show(tree, e.getX(), e.getY());
            }
        } else {
            assert false;
        }
    }
    
    /**
     * Auto-collapses a given file node if appropriate after
     * the select or unselect operation. The behaviour is given by constants
     * {@link #COLLAPSE_FILE_ON_SELECTION}
     * and {@link #COLLAPSE_FILE_ON_UNSELECTION}.
     * 
     * @param  tree  tree in which the node resides
     * @param  treePath  path to the file node
     * @param  matchingObj  object containing the file
     * @param  selected  determines the event that caused this method
     *                   to be called
     *                   - {@code true} for selection,
     *                     {@code false} for unselection
     * @return  final expansion state of the node
     *          - {@code true} if the node is now collapsed,
     *            {@code false} if the node is now expanded
     */
    private boolean autocollapseFileNodeIfNeeded(JTree tree,
                                                 TreePath treePath,
                                                 MatchingObject matchingObj,
                                                 boolean selected) {
        assert treePath.getPathCount() == 2
               && treePath.getLastPathComponent() == matchingObj;
        
        final boolean autocollapse = selected ? COLLAPSE_FILE_ON_SELECTION
                                              : COLLAPSE_FILE_ON_UNSELECTION;
        boolean isCollapsed;
        if (autocollapse) {
            if (matchingObj.isExpanded()) {
                tree.collapsePath(treePath);
            }
            isCollapsed = true;
        } else {
            isCollapsed = !tree.isExpanded(treePath);
        }
        return isCollapsed;
    }
    
    /**
     */
    private void toggleRootNodeSelection(final JTree tree,
                                         final TreePath path) {
        final ResultTreeModel resultTreeModel = getResultTreeModel(path);
        final ResultModel resultModel = resultTreeModel.resultModel;
        final boolean isSelected = resultTreeModel.isSelected();
        final boolean willBeSelected = !isSelected;
        final boolean autocollapse = willBeSelected
                                     ? COLLAPSE_FILE_ON_SELECTION
                                     : COLLAPSE_FILE_ON_UNSELECTION;

        final MatchingObject[] matchingObjects
                                    = resultModel.getMatchingObjects();

        int[] toggledIndices = null;
        MatchingObject[] toggledObjects = null;
        List<MatchingObject> expandedToggled = null;
        int toggledCount = 0;
        for (int i = 0; i < matchingObjects.length; i++) {
            final MatchingObject matchingObj = matchingObjects[i];

            boolean collapsed = !matchingObj.isExpanded();
            if (autocollapse && !collapsed) {
                tree.collapsePath(path.pathByAddingChild(matchingObj));
                collapsed = true;
            }

            if (matchingObj.isSelected() == willBeSelected) {
                continue;
            }

            matchingObj.setSelected(willBeSelected);
            if (toggledCount == 0) {
                int arrayLength = matchingObjects.length - i;
                toggledIndices = new int[arrayLength];
                toggledObjects = new MatchingObject[arrayLength];
            }
            toggledIndices[toggledCount] = i;
            toggledObjects[toggledCount] = matchingObj;
            toggledCount++;

            if (collapsed) {
                matchingObj.markChildrenSelectionDirty();
            } else {
                if (expandedToggled == null) {
                    expandedToggled = new ArrayList<MatchingObject>(6);
                }
                expandedToggled.add(matchingObj);
            }
        }
        if (toggledCount != 0 && toggledCount != matchingObjects.length) {

            int[] newToggledIndices = new int[toggledCount];
            System.arraycopy(toggledIndices, 0,
                             newToggledIndices, 0,
                             toggledCount);
            toggledIndices = newToggledIndices;

            MatchingObject[] newToggledObjects
                                    = new MatchingObject[toggledCount];
            System.arraycopy(toggledObjects, 0,
                             newToggledObjects, 0,
                             toggledCount);
            toggledObjects = newToggledObjects;
        }

        /* Update selection of the root node: */
        resultTreeModel.setSelected(willBeSelected);
        resultTreeModel.fireRootNodeChanged();

        /* Update selection of file nodes: */
        if (toggledCount != 0) {
            assert toggledIndices.length == toggledCount;
            assert toggledObjects.length == toggledCount;
            resultTreeModel.fireFileNodesSelectionChanged(toggledIndices,
                                                          toggledObjects);
        }

        /* Update selection of visible detail nodes: */
        if (expandedToggled != null) {
            for (MatchingObject obj : expandedToggled) {
                resultTreeModel.fireFileNodeChildrenSelectionChanged(obj);
            }
        }
    }
    
    /**
     */
    private void setFileNodeSelected(JTree tree,
                                     TreePath treePath,
                                     MatchingObject matchingObj,
                                     boolean selected) {
        assert treePath.getPathCount() == 2;
        
        boolean collapsed = autocollapseFileNodeIfNeeded(tree,
                                                         treePath,
                                                         matchingObj,
                                                         selected);
        boolean deferChildrenSelection = collapsed;
        matchingObj.setSelected(selected);
        getResultTreeModel(treePath).fireFileNodeSelectionChanged(
                                                matchingObj,
                                                !deferChildrenSelection);
        if (deferChildrenSelection) {
            matchingObj.markChildrenSelectionDirty();
        }
    }
    
    /**
     */
    private void toggleDetailNodeSelection(JTree tree,
                                           ResultModel resultModel, 
                                           MatchingObject matchingObj,
                                           int index) {
        matchingObj.toggleSubnodeSelection(resultModel, index);
        getResultTreeModel(tree).fireDetailNodeSelectionChanged(matchingObj,
                                                                index);
    }
    
    /**
     */
    private ResultTreeModel getResultTreeModel(TreePath path) {
        return (ResultTreeModel) path.getPathComponent(0);
    }
    
    /**
     */
    private ResultTreeModel getResultTreeModel(JTree tree) {
        return (ResultTreeModel) tree.getPathForRow(0).getPathComponent(0);
    }
    
    /**
     */
    private ResultModel getResultModel(JTree tree) {
        return ((ResultTreeModel) tree.getPathForRow(0).getPathComponent(0))
               .resultModel;
    }
    
    /**
     * Returns a NetBeans explorer node corresponding to the given object.
     * 
     * @param  path  identifies the object for which a NetBeans explorer node
     *               should be returned
     * @param  resultModel  model of search results
     * @return  node corresponding to the given object,
     *          or {@code null} if the {@link MatchingObject} representing
     *          the given object is not {@link MatchingObject#isValid}
     */
    private static Node getNbNode(TreePath path, ResultModel resultModel) {
        Node node;
        
        Object obj = path.getLastPathComponent();
        
        MatchingObject matchingObj;
        boolean isFileNode;
        
        if (obj.getClass() == MatchingObject.class) {
            matchingObj = (MatchingObject) obj;
            
            isFileNode = true;
        } else {
            Object parentObj = path.getParentPath().getLastPathComponent();
            assert parentObj.getClass() == MatchingObject.class;
            matchingObj = (MatchingObject) parentObj;
            
            isFileNode = false;
        }
        if (!matchingObj.isObjectValid()) {
            return null;
        }
        
        if (isFileNode) {
            node = resultModel.getSearchGroup().getNodeForFoundObject(
                                                            matchingObj.object);
        } else {
            assert obj instanceof Node;         //detail node
            node = (Node) obj;
        }
        return node;
    }
    
    /**
     * Creates a popup-menu for the given file node.
     * 
     * @param  fileNode  file node for which a popup-menu should be built
     * @return  created popup-menu; or {@code null} if no popup-menu
     *          is available for the given file node
     */
    private JPopupMenu createFileNodePopupMenu(Node fileNode) {
        Action action = getDefaultAction(fileNode);
        if (action == null) {
            return null;
        }
        
        assert action.isEnabled();
        
        JMenuItem menuItem = (action instanceof Presenter.Popup)
                             ? ((Presenter.Popup) action).getPopupPresenter()
                             : null;
        JPopupMenu popupMenu = new JPopupMenu();
        if (menuItem != null) {
            popupMenu.add(menuItem);
        } else {
            popupMenu.add(action);
        }
        return popupMenu;
    }
    
    /**
     */
    private Action getDefaultAction(Node node) {
        EditAction editAction = SharedClassObject.findObject(EditAction.class, true);
        Action action;
        if (editAction != null) {
            action = editAction.createContextAwareInstance(
                                                    Lookups.singleton(node));
            if (action.isEnabled()) {
                return action;
            }
        }
        
        Action preferredAction = node.getPreferredAction();
        if (preferredAction == null) {
            return null;
        }
        
        action = preferredAction;
        if (action instanceof NodeAction) {
            action = ((NodeAction) action).createContextAwareInstance(
                                                       Lookups.singleton(node));
        }
        return ((action != null) && action.isEnabled()) ? action : null;
    }
    
    /**
     * @param  node  node on which the default action should be called,
     *               or {@code null} if the user action was called on an invalid
     *               object
     * @see  MatchingObject#isValid
     */
    private void callDefaultAction(Node node,
                                   Object eventSource,
                                   int eventId,
                                   String command) {
        if (node == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        /*
         * Before trying the actual default action, try EditCookie first.
         * This changes the behaviour such that forms are opened in the edit
         * mode instead of the visual form editor.
         */
        EditCookie editCookie = node.getCookie(EditCookie.class);
        if (editCookie != null) {
            editCookie.edit();
            return;
        }
        
        /* No EditCookie? So try the default action now. */
        Action action = node.getPreferredAction();
        if (action == null) {
            return;
        }
        if (action instanceof NodeAction) {
            action = ((NodeAction) action).createContextAwareInstance(
                                                       Lookups.singleton(node));
        }
        if ((action != null) && action.isEnabled()) {
            action.actionPerformed(new ActionEvent(eventSource,
                                                   eventId,
                                                   command));
        }
    }
    
    /**
     */
    private void toggleExpansionState(JTree tree, TreePath path) {
        if (tree.isCollapsed(path)) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }
    
    /**
     */
    private void toggleSelection(final JTree tree,
                                 final TreePath path) {
        assert EventQueue.isDispatchThread();
        
        if (!selectionChangeEnabled) {
            return;
        }
        
        final int pathCount = path.getPathCount();
        if (pathCount == 1) {
            toggleRootNodeSelection(tree, path);

        } else if (pathCount == 2) {
            MatchingObject matchingObj
                    = (MatchingObject) path.getPathComponent(1);
            setFileNodeSelected(tree,
                                path,
                                matchingObj,
                                !matchingObj.isSelected());

        } else {
            assert pathCount == 3;
            MatchingObject matchingObj
                    = (MatchingObject) path.getPathComponent(1);
            int parentPathRow = tree.getRowForPath(path.getParentPath());
            int row = tree.getRowForPath(path);
            int index = row - parentPathRow - 1;
            toggleDetailNodeSelection(tree,
                                      getResultModel(tree),
                                      matchingObj,
                                      index);
        }
    }
    
    /**
     */
    void setSelectionChangeEnabled(boolean enabled) {
        assert EventQueue.isDispatchThread();
        
        this.selectionChangeEnabled = enabled;
    }

    public void keyTyped(KeyEvent e) {
    }
    
    public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode() != KeyEvent.VK_ENTER)
                || (e.getModifiersEx() != 0)) {
            return;
        }
        
        final JTree tree = (JTree) e.getSource();
        final TreeSelectionModel selectionModel = tree.getSelectionModel();
        if (selectionModel.getSelectionCount() != 1) {
            return;
        }
        final TreePath selectedPath = selectionModel.getLeadSelectionPath();
        if ((selectedPath == null) || (selectedPath.getParentPath() == null)) {
            // empty selection or root node selected
            return;
        }
        Node nbNode = getNbNode(selectedPath, getResultModel(tree));
        callDefaultAction(nbNode, e.getSource(), e.getID(), "enter");   //NOI18N
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupTriggerEventFired(e);
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupTriggerEventFired(e);
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                toggleSelection(tree, path);
            }
        }
    }

    public void treeWillExpand(TreeExpansionEvent event)
                                            throws ExpandVetoException {
        final TreePath path = event.getPath();
        if (path.getPathCount() == 2) {         //file node
            MatchingObject matchingObj = (MatchingObject)
                                         path.getLastPathComponent();
            if (matchingObj.isChildrenSelectionDirty()) {
                getResultTreeModel(path)
                        .fireFileNodeChildrenSelectionChanged(matchingObj);
                matchingObj.markChildrenSelectionClean();
            }
        }
    }

    public void treeWillCollapse(TreeExpansionEvent event)
                                            throws ExpandVetoException {
    }

    public void treeExpanded(TreeExpansionEvent event) {
        final TreePath path = event.getPath();
        if (path.getPathCount() == 2) {
            ((MatchingObject) path.getLastPathComponent()).markExpanded(true);
        }
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        final TreePath path = event.getPath();
        if (path.getPathCount() == 2) {
            ((MatchingObject) path.getLastPathComponent()).markExpanded(false);
        }
    }
    
}
