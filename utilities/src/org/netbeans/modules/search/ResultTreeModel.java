/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.openide.nodes.Node;

/**
 *
 *
 * @author  Marian Petras
 */
final class ResultTreeModel implements TreeModel {
    
    /** */
    final ResultModel resultModel;
    /** */
    private final TreePath rootPath;
    /** */
    private String rootDisplayName;
    /** */
    private boolean selected = true;
    /** */
    private int objectsCount;
    /** */
    private int selectedObjectsCount;
    /** */
    private List<TreeModelListener> treeModelListeners;
    
    /**
     * 
     * @param  resultModel  result model, or {@code null} for empty tree model
     */
    ResultTreeModel(ResultModel resultModel) {
        this.resultModel = resultModel;
        this.rootPath = new TreePath(this);
        
        if (resultModel != null) {
            resultModel.setObserver(this);
        }
    }

    @Override
    public Object getRoot() {
        return this;
    }
    
    @Override
    public Object getChild(Object parent, int index) {
        assert EventQueue.isDispatchThread();
        
        if ((resultModel == null) || (index < 0)) {
            return null;
        }
        
        Object ret;
        if (parent == getRoot()) {
            if (index >= objectsCount) {
                ret = null;
            } else {
                try {
                    //PENDING - threading:
                    ret = resultModel.getMatchingObjects().get(index);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    assert false;
                    ret = null;
                }
            }
        } else if (parent.getClass() == MatchingObject.class) {
            if (resultModel.canHaveDetails() == Boolean.FALSE) {
                ret = null;
            } else {
                MatchingObject mo = (MatchingObject) parent;
                Node[] detailNodes = resultModel.searchAndReplace ? 
                    resultModel.basicCriteria.getDetails(mo.object) :
                    resultModel.getDetails(mo);
                if ((detailNodes == null) || (index >= detailNodes.length)) {
                    ret = null;
                } else {
                    ret = detailNodes[index];
                }
            }
        } else {            //detail node
            ret = null;
        }
        return ret;
    }
    
    @Override
    public int getChildCount(Object parent) {
        assert EventQueue.isDispatchThread();
        
        if (resultModel == null) {
            return 0;
        }
        
        int ret;
        if (parent == getRoot()) {
            ret = objectsCount;
        } else if (parent.getClass() == MatchingObject.class) {
            if (resultModel.searchAndReplace) {
                ret = resultModel.basicCriteria.getDetailsCount(
                                            ((MatchingObject) parent).object);
            } else if (resultModel.canHaveDetails() == Boolean.FALSE) {
                ret = 0;
            } else {
                ret = resultModel.getDetailsCount((MatchingObject) parent);
            }
        } else {            //detail node
            ret = 0;
        }
        return ret;
    }

    @Override
    public boolean isLeaf(Object node) {
        boolean ret;
        if (node == getRoot()) {
            ret = false;
        } else if (node.getClass() == MatchingObject.class) {
            Boolean hasDetails = resultModel.canHaveDetails();
            if (hasDetails != null) {
                ret = !hasDetails.booleanValue();
            } else {
                ret = resultModel.getDetails((MatchingObject) node) == null ?
                    true : false;
            }
        } else {        //detail node
            ret = true;
        }
        return ret;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        assert EventQueue.isDispatchThread();
        
        /* This should never be called. None of the nodes is editable. */
        assert false;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        assert EventQueue.isDispatchThread();
        
        if ((resultModel == null) || (parent == null) || (child == null)) {
            return -1;
        }
        
        int ret;
        if (parent == getRoot()) {
            ret = (child.getClass() == MatchingObject.class)
                  ? resultModel.getMatchingObjects().indexOf(child)
                  : -1;
        } else {
            ret = -1;
            if ((parent.getClass() == MatchingObject.class)
                    && resultModel.canHaveDetails()
                    && (child instanceof Node)) {
                MatchingObject matchingObject = (MatchingObject) parent;
                Node[] detailNodes
                        = resultModel.searchAndReplace
                          ? resultModel.basicCriteria.getDetails(
                                        matchingObject.object)
                          : resultModel.getDetails(matchingObject);
                if (detailNodes != null) {
                    for (int i = 0; i < detailNodes.length; i++) {
                        if (detailNodes[i].equals(child)) {
                            ret = i;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        if (treeModelListeners == null) {
            treeModelListeners = new ArrayList<TreeModelListener>(4);
        }
        treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        assert EventQueue.isDispatchThread();
        
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        if (treeModelListeners != null) {
            treeModelListeners.remove(l);
        }
    }
    
    /**
     */
    void objectFound(MatchingObject object, int objectIndex) {
        if (resultModel == null) {
            throw new IllegalStateException("resultModel is null");     //NOI18N
        }
        new Task(object, objectIndex).run();//fireNodeAdded(objectIndex, object);
    }
    
    /**
     */
    void objectBecameInvalid(MatchingObject object) {
        if (resultModel == null) {
            throw new IllegalStateException("resultModel is null");     //NOI18N
        }
        new Task(object).run();
    }
    
    /**
     */
    String getRootDisplayName() {
        assert EventQueue.isDispatchThread();
        
        return rootDisplayName;
    }
    
    /** */
    void setRootDisplayName(String displayName) {
        assert EventQueue.isDispatchThread();
        
        this.rootDisplayName = displayName;
        UPDATE_NAME_TASK.run();                   //fireRootNodeChanged();
    }
    
    /**
     */
    boolean isSelected() {
        return selected;
    }
    
    /**
     * 
     * @return  {@code true} if the selection changed, {@code false} otherwise
     */
    private boolean setSelected(boolean selected) {
        if (selected == this.selected) {
            return false;
        }
        
        this.selected = selected;
        return true;
    }
    
    private final Task UPDATE_NAME_TASK = new Task();
    /**
     * Single class for sending various asynchronous tasks to the event queue.
     */
    private final class Task implements Runnable {
        private final MatchingObject foundObject;
        private final int foundObjectIndex;
        private Task() {
            this.foundObject = null;
            this.foundObjectIndex = -1;
        }
        private Task(MatchingObject object) {
            this.foundObject = object;
            this.foundObjectIndex = -1;
        }
        private Task(MatchingObject foundObject, int foundObjectIndex) {
            assert (foundObject != null) && (foundObjectIndex >= 0);
            this.foundObject = foundObject;
            this.foundObjectIndex = foundObjectIndex;
        }
        @Override
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(this);
                return;
            }
            
            assert EventQueue.isDispatchThread();
            if (foundObject != null) {
                if (foundObjectIndex != -1) {
                    objectsCount++;
                    fireNodeAdded(foundObjectIndex, foundObject);
                    updateRootNodeSelection(true);
                } else {
                    /* file became invalid */
                    assert !foundObject.isObjectValid();
                    boolean wasSelected = foundObject.isSelected();
                    foundObject.setSelected(false);
                    fireNodeChanged(foundObject);
                    if (wasSelected) {
                        updateRootNodeSelection(false);
                    }
                }
            } else {
                fireRootNodeChanged();
            }
        }
    }

    void setRootNodeSelected(boolean selected) {
        if (selected) {
            selectedObjectsCount = objectsCount;
        } else {
            selectedObjectsCount = 0;
        }
        updateRootNodeSelection();
    }
    
    /**
     */
    void fileNodeSelectionChanged(MatchingObject matchingObj,
                                  boolean includingChildren) {
        assert EventQueue.isDispatchThread();

        fireFileNodeSelectionChanged(matchingObj, includingChildren);
        updateRootNodeSelection(matchingObj.isSelected());
    }
    
    /**
     * 
     * @param  selectionAdded  {@code true} if a file node was selected
     *                                 or if a selected file node was added;
     *                         {@code false} if a file node was unselected
     *                                 or if a selected file node was removed
     *                                 or if a selected file node became invalid
     */
    private void updateRootNodeSelection(boolean selectionAdded) {
        if (selectionAdded) {
            selectedObjectsCount++;
        } else {
            selectedObjectsCount--;
        }

        updateRootNodeSelection();
    }

    private void updateRootNodeSelection() {
        assert (selectedObjectsCount >= 0) &&
                (selectedObjectsCount <= objectsCount);
        if (setSelected(selectedObjectsCount != 0)) {
            fireRootNodeChanged();
        }
    }

    /**
     */
    private void fireNodeAdded(int index, MatchingObject object) {
        assert EventQueue.isDispatchThread();
        assert object != null;
        assert index >= 0;
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }
        
//        TreeModelEvent event = new TreeModelEvent(this,
//                                                  rootPath,
//                                                  new int[] { index },
//                                                  new Object[] { object });
        TreeModelEvent event = new TreeModelEvent(this,
                                                  rootPath);
        for (TreeModelListener l : treeModelListeners) {
//            l.treeNodesInserted(event);
            l.treeStructureChanged(event);
        }
    }
    
    /**
     */
    private void fireNodeChanged(MatchingObject object) {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }
        
        TreePath path = rootPath.pathByAddingChild(object);
        TreeModelEvent event = new TreeModelEvent(this, path);
        for (TreeModelListener l : treeModelListeners) {
            l.treeStructureChanged(event);
        }
    }

    /**
     */
    void fireRootNodeChanged() {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }
        
        TreeModelEvent event = new TreeModelEvent(this, rootPath, null, null);
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
    }
    
    /**
     */
    void fireFileNodesSelectionChanged(int[] indices,
                                       MatchingObject[] matchingObjects) {
        assert EventQueue.isDispatchThread();
        assert matchingObjects != null;
        assert indices != null;
        assert matchingObjects.length == indices.length;
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }

        TreeModelEvent event = new TreeModelEvent(this,
                                                  rootPath,
                                                  indices,
                                                  matchingObjects);
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
    }
    
    /**
     * Notifies the listeners that selection of the given
     * {@code MatchingObject}'s has changed.
     * 
     * @param  matchingObj  object's whose node's selection has changed
     * @param  includingSubnodes  whether listeners should be notified also
     *                            about change of the node's children's
     *                            selection
     * @return  {@code true} if the file node's selection change caused change
     *          of the root node's selection, {@code false} otherwise
     * @see  MatchingObj#markChildrenSelectionDirty()
     */
    private void fireFileNodeSelectionChanged(MatchingObject matchingObj,
                                              boolean includingChildren) {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }

        final int index = resultModel.getMatchingObjects().indexOf(matchingObj);
        
        /* Notify that the file node itself has changed... */
        TreeModelEvent event = new TreeModelEvent(this,
                                                  rootPath,
                                                  new int[] { index },
                                                  new Object[] { matchingObj });
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
        
        if (includingChildren) {
            if (matchingObj.isExpanded()) {
                fireFileNodeChildrenSelectionChanged(matchingObj);
            } else {
                matchingObj.markChildrenSelectionDirty();
            }
        }
    }

    /**
     */
    void fireFileNodeChildrenSelectionChanged(MatchingObject matchingObj) {
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }

        Node[] children = resultModel.basicCriteria
                          .getDetails(matchingObj.object);
        int[] indices = new int[children.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        final TreeModelEvent event = new TreeModelEvent(
                                        this,
                                        new Object[] { getRoot(), matchingObj },
                                        indices,
                                        children);
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
    }
    
    /**
     */
    void fireDetailNodeSelectionChanged(MatchingObject matchingObj,
                                        int index) {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }

        int[] changedIndices = new int[] { index };
        Node[] detailNodes = resultModel.basicCriteria
                             .getDetails(matchingObj.object);
        Node[] changedNodes = (detailNodes.length == 1)
                              ? detailNodes
                              : new Node[] { detailNodes[index] };
        TreeModelEvent event = new TreeModelEvent(
                                        this,
                                        new Object[] { getRoot(), matchingObj },
                                        changedIndices,
                                        changedNodes);
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
    }
    
    /** Returns display name of the root node.
     * @return display name of the root node.
     */
    @Override
    public String toString() {
        return super.toString() + "[" + rootDisplayName + "]"; // NOI18N
    }
}
