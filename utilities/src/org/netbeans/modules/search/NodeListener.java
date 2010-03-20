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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON1_MASK;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    /**
     * Checks whether all given tree paths are of the expected length.
     * @param  paths  paths to be checked
     * @param  expLength  expected length of the paths
     * @return  {@code true} if all the paths are of the expected length,
     *          {@code false} otherwise
     */
    private static boolean checkPathCounts(TreePath[] paths, int expLength) {
        for (TreePath path : paths) {
            if (path.getPathCount() != expLength) {
                return false;
            }
        }
        return true;
    }
    
    private void popupTriggerEventFired(MouseEvent e) {
        final JTree tree = (JTree) e.getSource();
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        if (!isPathSelected(path, tree)) {
            tree.setSelectionPath(path);
        }

        int selCount = tree.getSelectionCount();
        if (selCount >= 1) {
            final ResultModel resultModel = getResultModel(tree);
            if (selCount > 1) {
                showPopup(tree, null, resultModel, e);
            } else if (!isInsideCheckBox(tree, path, resultModel, e)) {
                showPopup(tree, path, resultModel, e);
            }
        }
    }

    /**
     * Checks whether the given tree path is among the paths currently selected
     * in the given tree.
     * @param  path  the path whose selection is to be probed
     * @param  tree  tree whose selection is to be probed
     * @return  {@code true} if the given path is selected in the given tree,
     *          {@code false} otherwise
     */
    private static boolean isPathSelected(final TreePath path,
                                          final JTree tree) {
        int selCount = tree.getSelectionCount();
        if (selCount == 0) {
            return false;
        }

        if (selCount == 1) {
            return tree.getSelectionPath().equals(path);
        }

        for (TreePath tp : tree.getSelectionPaths()) {
            if (tp.equals(path)) {
                return true;
            }
        }

        return false;
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
     * Displays a pop-up menu (context menu) in the given tree, for a node
     * given by a tree path.
     * 
     * @param  tree  tree in which the menu should be displayed
     * @param  path  tree path specification of a node,
     *               or {@code null} if there are multiple nodes selected
     * @param  resultModel  data model of the tree
     * @param  e  mouse event which triggered display of the pop-up menu,
     *            or {@code null} if it was something else than a mouse
     *            what triggered it
     */
    private void showPopup(final JTree tree,
                           final TreePath path,
                           final ResultModel resultModel,
                           final MouseEvent e) {
        final int pathCount;
        final TreePath[] paths;
        if (path != null) {
            paths = null;
            pathCount = path.getPathCount();
        } else {
            paths = tree.getSelectionPaths();
            assert paths.length > 1;
            if (checkPathCounts(paths, 2)) {
                pathCount = 2;
            } else {
                // no popup-menu for multiple selection of various depth
                return;
            }
        }

        if (pathCount == 1) {               //root node
            //no popup-menu for the root node
        } else if (pathCount == 2) {
            JPopupMenu popup = null;
            if (tree.getSelectionCount() > 1) {
                // prepare "best effort" popup menu for multiple selected nodes
                StringBuilder wholeNameBuf = new StringBuilder(30);
                Set<String> labels = new HashSet<String>();
                final List<Node> nbNodes = new ArrayList<Node>(paths.length);
                for (TreePath tp : paths) {
                    Node nbNode = getNbNode(tp, resultModel);
                    if (nbNode != null) {
                        nbNodes.add(nbNode);
                        Action action = getDefaultAction(nbNode);
                        if (action == null) {
                            continue;
                        }
                        String partName = getMenuItemLabel(action);
                        if ((partName.length() != 0) && labels.add(partName)) {
                            wholeNameBuf.append('/').append(partName);
                        }
                    }
                }
                if (labels.isEmpty()) {
                    //no named popup actions collected
                    return;
                }

                JMenuItem menuItem = new JMenuItem(
                        wholeNameBuf.toString().substring(1)); //strip initial '/'
                menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            callResultNodesDefaultActions(nbNodes, e);
                        }

                    }
                );
                popup = new JPopupMenu();
                popup.add(menuItem);
            } else {
                Node nbNode = getNbNode(path, resultModel);
                if (nbNode != null) {
                    popup = createFileNodePopupMenu(nbNode);
                }
            }
            if (popup != null) {
                Point location = getPopupMenuLocation(tree, path, e);
                popup.show(tree, location.x, location.y);
            }
        } else if (pathCount == 3) {        //detail node
            if (tree.getSelectionCount() == 1) {
                // show popup only when single node has been selected
                Node nbNode = getNbNode(path, resultModel);
                if (nbNode != null) {
                    Point location = getPopupMenuLocation(tree, path, e);
                    nbNode.getContextMenu().show(tree, location.x, location.y);
                }
            }
        } else {
            assert false;
        }
    }

    /**
     * Performs default action on all file nodes selected in
     * @param e  ActionEvent performed
     */
    private void callResultNodesDefaultActions(Collection<Node> nbNodes,
                                               ActionEvent e) {
        assert (nbNodes != null) && !nbNodes.isEmpty();

        Object eSource = e.getSource();
        int eID = e.getID();
        for (Node nbNode : nbNodes) {
            callDefaultAction(nbNode, eSource, eID, "click");           //NOI18N
        }
    }
    
    /**
     * Determines location where the pop-up menu for a node should be displayed.
     * @param  tree  tree in which the pop-up menu should be displayed
     * @param  path  currently selected tree path
     * @param  e  mouse event which caused the pop-up menu to be displayed,
     *            or {@code null} of it was not a mouse event what triggered
     *            the pop-up menu
     * @return  point at which the pop-up menu should be displayed
     */
    private static Point getPopupMenuLocation(JTree tree,
                                              TreePath path,
                                              MouseEvent e) {
        if (e != null) {
            return new Point(e.getX(), e.getY());
        } else if (path != null) {
            return tree.getPathBounds(path).getLocation();
        } else {
            Point pos = tree.getMousePosition();
            if (pos == null) {
                java.awt.Rectangle r = tree.getVisibleRect();
                pos = new Point(r.x + r.width / 2,
                                r.y + r.height / 2);
            }
            return pos;
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
        
        // issue 147983 fix - no search executed yet,
        // only result window was opended and spacebar was pressed
        if (resultModel == null)
            return;

        final List<MatchingObject> matchingObjects
                                    = resultModel.getMatchingObjects();

        int[] toggledIndices = null;
        MatchingObject[] toggledObjects = null;
        List<MatchingObject> expandedToggled = null;
        int toggledCount = 0;
        int i = 0;
        for(final MatchingObject matchingObj: matchingObjects) {

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
                int arrayLength = matchingObjects.size() - i;
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
            i++;
        }
        if (toggledCount != 0 && toggledCount != matchingObjects.size()) {

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
        resultTreeModel.setRootNodeSelected(willBeSelected);

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
        
        autocollapseFileNodeIfNeeded(tree, treePath, matchingObj, selected);
        matchingObj.setSelected(selected);
        getResultTreeModel(treePath).fileNodeSelectionChanged(matchingObj, true);
    }
    
    /**
     */
    private void toggleDetailNodeSelection(JTree tree,
                                           ResultModel resultModel, 
                                           MatchingObject matchingObj,
                                           int index) {
        boolean propagateUp = matchingObj.toggleSubnodeSelection(resultModel, index);

        ResultTreeModel resultTreeModel = getResultTreeModel(tree);
        resultTreeModel.fireDetailNodeSelectionChanged(matchingObj, index);
        if (propagateUp) {
            resultTreeModel.fileNodeSelectionChanged(matchingObj, false);
        }
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
            node = (resultModel==null) ? null : resultModel.getSearchGroup().getNodeForFoundObject(
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
        
        JMenuItem menuItem = getJMenuItem(action);
        JPopupMenu popupMenu = new JPopupMenu();
        if (menuItem != null) {
            popupMenu.add(menuItem);
        } else {
            popupMenu.add(action);
        }
        return popupMenu;
    }

    private static String getMenuItemLabel(Action action) {
        JMenuItem menuItem = getJMenuItem(action);
        return (menuItem != null) ? menuItem.getText()
                                  : (String) action.getValue(Action.NAME);

    }

    private static JMenuItem getJMenuItem(Action action) {
        return (action instanceof Presenter.Popup)
               ? ((Presenter.Popup) action).getPopupPresenter()
               : null;
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
        final JTree tree = (JTree) e.getSource();
        ResultModel resultModel = getResultModel(tree);
        if (e.getKeyChar() == ' ') {
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                if (resultModel.searchAndReplace)
                    toggleSelection(tree, path);
                else {
                    if (tree.isCollapsed(path))
                        tree.expandPath(path);
                    else
                        tree.collapsePath(path);
                }
            }
         } else if ((e.getKeyCode() == KeyEvent.VK_ENTER)
                   && (e.getModifiersEx() == 0))  {
            final TreeSelectionModel selectionModel = tree.getSelectionModel();
            if (selectionModel.getSelectionCount() == 0) {
                return;
            }

            List<TreePath> mainNodes;
            if (selectionModel.getSelectionCount() == 1) {
                final TreePath selectedPath = selectionModel.getLeadSelectionPath();
                if ((selectedPath == null) || (selectedPath.getParentPath() == null)) {
                    // empty selection or root node selected
                    return;
                }
                mainNodes = Collections.<TreePath>singletonList(selectedPath);
            } else {
                TreePath[] selectedPaths = selectionModel.getSelectionPaths();
                if ((selectedPaths == null) || (selectedPaths.length < 2)) {
                    // this should not happen because (selectionCount >= 2)
                    assert false;
                    return;
                }
                mainNodes = NodeSelector.selectMainNodes(
                                                selectedPaths,
                                                resultModel.canHaveDetails());
            }
            for (TreePath mainNode : mainNodes) {
                Node nbNode = getNbNode(mainNode, resultModel);
                callDefaultAction(nbNode, e.getSource(), e.getID(), "enter");   //NOI18N
            }
        } else if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
                   && (e.getModifiersEx() == 0)) {
            e.consume();
            int selCount = tree.getSelectionCount();
            if (selCount >= 1) {
                if (selCount > 1) {
                    showPopup(tree, null, resultModel, null);
                } else {
                    TreePath path = tree.getSelectionPath();
                    showPopup(tree, path, resultModel, null);
                }
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
