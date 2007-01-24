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

    public Object getRoot() {
        return this;
    }
    
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
                    ret = resultModel.matchingObjects.get(index);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    assert false;
                    ret = null;
                }
            }
        } else if (parent.getClass() == MatchingObject.class) {
            if (!resultModel.canHaveDetails()) {
                ret = null;
            } else {
                MatchingObject matchingObject = (MatchingObject) parent;
                Node[] detailNodes
                        = resultModel.searchAndReplace
                          ? resultModel.fullTextSearchType.getDetails(
                                                    matchingObject.object)
                          : resultModel.getDetails(matchingObject);
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
                ret = resultModel.fullTextSearchType.getDetailsCount(
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

    public boolean isLeaf(Object node) {
        assert EventQueue.isDispatchThread();
        
        boolean ret;
        if (node == getRoot()) {
            ret = false;
        } else if (node.getClass() == MatchingObject.class) {
            Boolean hasDetails = resultModel.canHaveDetails();
            if (hasDetails != null) {
                ret = !hasDetails.booleanValue();
            } else {
                ret = !resultModel.hasDetails((MatchingObject) node);
            }
        } else {        //detail node
            ret = true;
        }
        return ret;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        assert EventQueue.isDispatchThread();
        
        /* This should never be called. None of the nodes is editable. */
        assert false;
    }

    public int getIndexOfChild(Object parent, Object child) {
        assert EventQueue.isDispatchThread();
        
        if ((resultModel == null) || (parent == null) || (child == null)) {
            return -1;
        }
        
        int ret;
        if (parent == getRoot()) {
            ret = (child.getClass() == MatchingObject.class)
                  ? resultModel.matchingObjects.indexOf(child)
                  : -1;
        } else {
            ret = -1;
            if ((parent.getClass() == MatchingObject.class)
                    && resultModel.canHaveDetails()
                    && (child instanceof Node)) {
                MatchingObject matchingObject = (MatchingObject) parent;
                Node[] detailNodes
                        = resultModel.searchAndReplace
                          ? resultModel.fullTextSearchType.getDetails(
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

    public void addTreeModelListener(TreeModelListener l) {
        assert EventQueue.isDispatchThread();
        
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        if (treeModelListeners == null) {
            treeModelListeners = new ArrayList<TreeModelListener>(4);
        }
        treeModelListeners.add(l);
    }

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
     */
    void setSelected(boolean selected) {
        if (selected == this.selected) {
            return;
        }
        
        this.selected = selected;
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
                } else {
                    fireNodeChanged(foundObject);
                }
            } else {
                fireRootNodeChanged();
            }
        }
    }
    
    /**
     */
    private void fireNodeAdded(int index, MatchingObject object) {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }
        
        TreeModelEvent event = new TreeModelEvent(this,
                                                  rootPath,
                                                  new int[] { index },
                                                  new Object[] { object });
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesInserted(event);
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
     * @see  MatchingObj#markChildrenSelectionDirty()
     */
    void fireFileNodeSelectionChanged(MatchingObject matchingObj,
                                      boolean includingSubnodes) {
        assert EventQueue.isDispatchThread();
        
        if ((treeModelListeners == null) || treeModelListeners.isEmpty()) {
            return;
        }

        final int index = resultModel.matchingObjects.indexOf(matchingObj);
        
        /* Notify that the file node itself has changed... */
        TreeModelEvent event = new TreeModelEvent(this,
                                                  rootPath,
                                                  new int[] { index },
                                                  new Object[] { matchingObj });
        for (TreeModelListener l : treeModelListeners) {
            l.treeNodesChanged(event);
        }
        
        if (includingSubnodes) {
            /* ... and also all its children need to be updated: */
            fireFileNodeChildrenSelectionChanged(matchingObj);
        }
    }
    
    /**
     */
    void fireFileNodeChildrenSelectionChanged(MatchingObject matchingObj) {
        Node[] children = resultModel.fullTextSearchType
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
        Node[] detailNodes = resultModel.fullTextSearchType
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
    public String toString() {
        return super.toString() + "[" + rootDisplayName + "]"; // NOI18N
    }
}
