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

package org.netbeans.modules.bpel.mapper.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextController;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder.FindResult;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;

/**
 * An internal tree model based on the Swing Tree Model. 
 * It performs caching of data is obtained from another tree models,
 * which are described by the MapperTreeModel and TreeItemInfoProvider 
 * interfaces. 
 * 
 * @author nk160297
 */
public class MapperSwingTreeModel implements TreeModel, MapperTcContext.Provider {
    
    protected EventListenerList listenerList = new EventListenerList();
    private MapperTreeModel mSourceModel;
    private MapperTcContext mMapperTcContext;
    private MapperTreeNode mRootNode;
    
    
    public MapperSwingTreeModel(MapperTcContext mapperTcContext, 
            MapperTreeModel sourceModel) {
        //
        mMapperTcContext = mapperTcContext;
        mSourceModel = sourceModel;
    }
    
    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }
    
    public Object getRoot() {
        if (mRootNode == null) {
            Object dataObject = mSourceModel.getRoot();
            mRootNode = new MapperTreeNode(null, dataObject);
        }
        return mRootNode;
    }

    public Object getChild(Object parent, int index) {
        assert parent instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)parent;
        List<MapperTreeNode> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.get(index);
    }

    public int getChildCount(Object parent) {
        assert parent instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)parent;
        List<MapperTreeNode> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.size();
    }

    public boolean isLeaf(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        Object dataObject = mNode.getDataObject();
        //
        Boolean isLeafObj = mNode.isLeaf();
        if (isLeafObj == null) {
            isLeafObj = mSourceModel.isLeaf(dataObject);
        }
        //
        if (isLeafObj == null) {
            List<MapperTreeNode> childrenNodes = getChildren(mNode);
            isLeafObj = childrenNodes.isEmpty();
        }
        //
        mNode.setIsLeaf(isLeafObj);
        return isLeafObj;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndexOfChild(Object parent, Object child) {
        assert parent instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)parent;
        List<MapperTreeNode> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.indexOf(child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    public void insertChild(TreePath parentPath, int index, Object itemDataObject) {
        Object parentNode = parentPath.getLastPathComponent();
        assert parentNode instanceof MapperTreeNode;
        List<MapperTreeNode> childrenList = getChildren((MapperTreeNode)parentNode);
        MapperTreeNode newChildNode = 
                new MapperTreeNode((MapperTreeNode)parentNode, itemDataObject);
        childrenList.add(index, newChildNode);
        //
        fireTreeNodesInserted(this, parentPath, index, newChildNode);
    }
    
    public void remove(TreePath treePath) {
        TreePath parentTreePath = treePath.getParentPath();
        Object parentComp = parentTreePath.getLastPathComponent();
        assert parentComp instanceof MapperTreeNode;
        //
        Object lastComp = treePath.getLastPathComponent();
        int childIndex = getIndexOfChild(parentComp, lastComp);
        //
        ((MapperTreeNode)parentComp).removeChild(lastComp);
        //
        fireTreeNodesRemoved(this, parentTreePath, childIndex, lastComp);
    }
    
    public MapperTreeModel getSourceModel() {
        return mSourceModel;
    }
    
    public String getDisplayName(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        String displayName = mNode.getDisplayName();
        if (displayName == null) {
            if (mSourceModel != null) {
                TreeItemInfoProvider infoProvider = 
                        mSourceModel.getTreeItemInfoProvider();
                if (infoProvider != null) {
                    Object dataObject = mNode.getDataObject();
                    displayName = infoProvider.getDisplayName(dataObject);
                    mNode.setDisplayName(displayName);
                }
            }
        }
        return displayName;
    }
    
    public String getToolTipText(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        String toolTipText = null;
        if (mSourceModel != null) {
            TreeItemInfoProvider infoProvider =
                    mSourceModel.getTreeItemInfoProvider();
            if (infoProvider != null) {
                Object dataObject = mNode.getDataObject();
                toolTipText = infoProvider.getToolTipText(dataObject);
            }
        }
        return toolTipText;
    }
    public Icon getIcon(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        Icon icon = mNode.getIcon();
        if (icon == null) {
            if (mSourceModel != null) {
                TreeItemInfoProvider infoProvider = 
                        mSourceModel.getTreeItemInfoProvider();
                if (infoProvider != null) {
                    Object dataObject = mNode.getDataObject();
                    icon = infoProvider.getIcon(dataObject);
                    mNode.setIcon(icon);
                }
            }
        }
        return icon;
    }
    
    public boolean isConnectable(TreePath treePath) {
        Object node = treePath.getLastPathComponent();
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        Object dataObject = mNode.getDataObject();
        //
        return mSourceModel.isConnectable(dataObject) == Boolean.TRUE;
    }
    
    public JPopupMenu getPopupMenu(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        //
        TreePath treePath = mNode.getTreePath();
        RestartableIterator<Object> dataObjectPathItr = 
                getDataObjectsPathIterator(mNode);
        //
        TreeItemInfoProvider infoProvider = mSourceModel.getTreeItemInfoProvider();
        if (infoProvider != null) {
            //
            // Determine if the model is the left one
            TreeModel leftTreeModel = 
                    mMapperTcContext.getMapper().getModel().getLeftTreeModel();
            boolean isLeft = (leftTreeModel == this);
            //
            List<Action> menuActionList = infoProvider.getMenuActions(
                    mMapperTcContext, isLeft, treePath, dataObjectPathItr);
            if (menuActionList != null && !menuActionList.isEmpty()) {
                JPopupMenu newMenu = new JPopupMenu();
                for (Action menuAction : menuActionList) {
                    JMenuItem newItem = new JMenuItem(menuAction);
                    newMenu.add(newItem);
                }
                //
                return newMenu;
            }
        }
        //
        return null;
    }
    
    private List<MapperTreeNode> getChildren(MapperTreeNode parent) {
        List<MapperTreeNode> childrenList = parent.getChildren();
        if (childrenList == null) {
            //
            // Construct children nodes here
            childrenList = new ArrayList<MapperTreeNode>();
            RestartableIterator<Object> dataObjectPathItr = 
                    getDataObjectsPathIterator(parent);
            List<Object> childrenDataObjectList = 
                    mSourceModel.getChildren(dataObjectPathItr);
            
            if (childrenDataObjectList != null) {
                DesignContextController dcc = mMapperTcContext
                        .getDesignContextController();
            
                for (Object childDataObject : childrenDataObjectList) {
                    if (dcc != null) {
                        dcc.processDataObject(childDataObject);
                    }
                
                    MapperTreeNode newNode = 
                            new MapperTreeNode(parent, childDataObject);
                    childrenList.add(newNode);
                    
                    
                }
            }
        }
        //
        parent.setChildren(childrenList);
        return childrenList;
    }
    
    public void fireTreeChanged(Object source, TreePath tPath) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    TreePath parentTreePath = tPath.getParentPath();
                    Object lastComp = tPath.getLastPathComponent();
                    assert lastComp instanceof MapperTreeNode;
                    //
                    // Reload cached data object
                    ((MapperTreeNode)lastComp).discardCachedData();
                    //
                    int childIndex = getIndexOfChild(
                            parentTreePath.getLastPathComponent(), lastComp);
                    //
                    e = new TreeModelEvent(source, parentTreePath, 
                            new int[] {childIndex}, 
                            new Object[] {lastComp});
                }
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }          
        }
    }
    
    protected void fireTreeNodesRemoved(Object source, 
            TreePath parentTreePath, int childIndex, Object removedObj) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    //
                    e = new TreeModelEvent(source, parentTreePath, 
                            new int[] {childIndex}, 
                            new Object[] {removedObj});
                }
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }          
        }
    }
    
    protected void fireTreeNodesInserted(Object source, 
            TreePath parentTreePath, int childIndex, Object inserted) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, parentTreePath, 
                            new int[] {childIndex}, // put next to the base node
                            new Object[] {inserted});
                }
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }          
        }
    }
    
    /**
     * Returns an iterator, which provides a tree path from the specifed node to 
     * the tree root. The path consists not from the MapperTreeNode objects, but 
     * from containing data objects. The first element is get from the result 
     * iterator references the data object of the specified node. 
     * @param node
     * @return
     */
    private RestartableIterator<Object> getDataObjectsPathIterator(
            final MapperTreeNode node) {
        return new RestartableIterator() {

            private MapperTreeNode mNextNode = node;
            
            public boolean hasNext() {
                return mNextNode != null;
            }

            public Object next() {
                assert mNextNode != null;
                Object result = mNextNode.getDataObject();
                mNextNode = mNextNode.getParent();
                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void restart() {
                mNextNode = node;
            }
            
            @Override
            public String toString() {
                return PathConverter.toString(this);
            }
        };
    }
    
    public List<TreePath> sortByLocation(Collection<TreePath> unsorted) {
        //
        ArrayList<TreePath> sorted = new ArrayList<TreePath>(unsorted.size());
        sorted.addAll(unsorted);
        
        Collections.sort(sorted, new TreePathComparator(this));
        //
        return sorted;
    }
    
    //-------------------------------------------------------------------------
    // Search Engine functions
    //-------------------------------------------------------------------------

    /**
     * Converts content of the TreePath to the list of data objects.
     * @param treePath
     * @return
     */
    public static List<Object> convertTreePath(TreePath treePath) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (Object item : treePath.getPath()) {
            if (item instanceof MapperTreeNode) {
                MapperTreeNode treeNode = (MapperTreeNode)item;
                Object dataObject = treeNode.getDataObject();
                result.add(dataObject);
            }
        }
        //
        return result;
    }
    
    public static Object getDataObject(TreePath treePath) {
        Object lastComp = treePath.getLastPathComponent();
        if (lastComp instanceof MapperTreeNode) {
            Object dataObject = ((MapperTreeNode)lastComp).getDataObject();
            return dataObject;
        }
        return null;
    }
    
    /**
     * Returns true if the specified tree path contains a tree item with the 
     * specified data object.
     * @param treePath
     * @param dataObj
     * @return
     */
    public static boolean containsDataObject(TreePath treePath, Object dataObj) {
        while (treePath != null) {
            Object lastComp = treePath.getLastPathComponent();
            if (lastComp instanceof MapperTreeNode) {
                Object pathDataObj = ((MapperTreeNode)lastComp).getDataObject();
                if (pathDataObj.equals(dataObj)) {
                    return true;
                }
            }
            treePath = treePath.getParentPath();
        }
        //
        return false;
    }
    
    /**
     * Looks for the first node, which satisfies the search conditions, 
     * which are specified by the finderList argument.
     * The finderList contains the list of TreeItemFinder objects which 
     * has to be applied sequentially.
     * 
     * @param helper
     * @return TreePath of the found tree item. 
     */
    public TreePath findFirstNode(List<TreeItemFinder> finderList) {
        if (finderList == null || finderList.isEmpty()) {
            return null;
        }
        //
        MapperTreeNode rootNode = (MapperTreeNode)getRoot();
        Stack<MapperTreeNode> locationStack = new Stack<MapperTreeNode>();
        locationStack.push(rootNode);
        //
        for (TreeItemFinder finder : finderList) {
            //
            boolean found = findFirstChild(locationStack, finder, -1);
            //
            if (!found) {
                return null;
            }
        }
        //
        TreePath result = new TreePath(locationStack.toArray());
        return result;
    }
    
    /**
     * An auxiliary method is intended to help seach nodes recursively.
     * The locationStack parameter specifies a chain of MapperTreeNode 
     * objects, which points to the tree node, from which the searching 
     * has to be started. 
     * <p>
     * The finder parameter is an object which makes decision. 
     * It has to be implemented externally.
     * <p>
     * The maxDepth parameter specifies the maximum depth do
     * which the recursive algorithm can go.
     * <p>
     * If it equals to -1, then infinite depth is emplied.
     * <p>
     * if it equals to 0 than it means that it only necessary to check
     * if the top node in the stack satisfies to the searching conditions.
     * <p>
     * if it equals to 1 than it means that searching is requested
     * only among direct children of the source node.
     */
    public boolean checkNode(
            Stack<MapperTreeNode> locationStack, 
            TreeItemFinder finder, int maxDepth) {
        //
        MapperTreeNode parentNode = locationStack.peek();
        Object dataObject = parentNode.getDataObject();
        //
        FindResult fr = finder.process(dataObject, null);
        //
        if (fr.isFit()) {
            return true;
        }
        //
        if (maxDepth == 0) {
            return false;
        }
        //
        if (fr.drillDeeper()) {
            return findFirstChild(locationStack, finder, maxDepth);
        }
        return false;
    }
    
    public boolean findFirstChild(
            Stack<MapperTreeNode> locationStack,
            TreeItemFinder finder, int maxDepth) {
        //
        MapperTreeNode parentNode = locationStack.peek();
        List<MapperTreeNode> children = getChildren(parentNode);
        if (children != null && children.size() != 0) {
            maxDepth--;
            for (MapperTreeNode child : children) {
                locationStack.push(child);
                //
                if (checkNode(locationStack, finder, maxDepth)) {
                    return true;
                }
                //
                locationStack.pop();
            }
        }
        //
        return false;
    }
    
    /**
     * An auxiliary method is intended to help seach nodes recursively.
     * See description of the findFirstNode method. 
     * Unlike the findFirstNode it can find more then one node.
     */
    public void fillNodesList(
            List<List<MapperTreeNode>> foundLocationsList,
            Stack<MapperTreeNode> locationStack,
            TreeItemFinder finder,
            int maxDepth,
            boolean lookDeeperIfFound) {
        //
        MapperTreeNode parentNode = locationStack.peek();
        Object dataObject = parentNode.getDataObject();
        //
        FindResult fr = finder.process(dataObject, null);
        //
        if (fr.isFit()) {
            // Copy location stack content to separate list and save it to result list.
            ArrayList<MapperTreeNode> foundLocation = 
                    new ArrayList<MapperTreeNode>(locationStack);
            foundLocationsList.add(foundLocation);
            if (!lookDeeperIfFound) {
                return;
            }
        }
        //
        if (maxDepth == 0) {
            return;
        }
        //
        if (fr.drillDeeper()) {
            List<MapperTreeNode> children = getChildren(parentNode);
            maxDepth--;
            for (MapperTreeNode child : children) {
                locationStack.push(child);
                //
                fillNodesList(foundLocationsList, locationStack, 
                        finder, maxDepth, lookDeeperIfFound);
                //
                locationStack.pop();
            }
        }
        return;
    }
    
    /**
     * Looks for the first child of the specified parent according to the 
     * finder. 
     * @param parentPath
     * @param finder
     * @return the tree path of the found child or null. 
     */
    public TreePath findChild(TreePath parentPath, TreeItemFinder finder) {
        if (finder == null) {
            return null;
        }
        //
        Object parentObj = parentPath.getLastPathComponent();
        assert parentObj instanceof MapperTreeNode;
        //
        List<MapperTreeNode> children = getChildren((MapperTreeNode)parentObj);
        for (MapperTreeNode childNode : children) {
            Object childDo = childNode.getDataObject();
            assert childDo != null;
            //
            FindResult fr = finder.process(childDo, null);
            //
            if (fr.isFit()) {
                return parentPath.pathByAddingChild(childNode);
            }
        }
        //
        return null;
    }
    
    /**
     * Looks for the set of children of the specified parent according to the 
     * finder. 
     * @param parentPath
     * @param finder
     * @return the tree path of the found child or null. 
     */
    public List<TreePath> findChildren(TreePath parentPath, TreeItemFinder finder) {
        if (finder == null) {
            return null;
        }
        //
        Object parentObj = parentPath.getLastPathComponent();
        assert parentObj instanceof MapperTreeNode;
        //
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        List<MapperTreeNode> children = getChildren((MapperTreeNode)parentObj);
        for (MapperTreeNode childNode : children) {
            Object childDo = childNode.getDataObject();
            assert childDo != null;
            //
            FindResult fr = finder.process(childDo, null);
            //
            if (fr.isFit()) {
                TreePath foundChildPath = parentPath.pathByAddingChild(childNode);
                result.add(foundChildPath);
            }
        }
        //
        return result;
    }
    
    /**
     * Looks for a child node by data object
     * @param parentPath
     * @param dataObject
     * @return
     */
    public TreePath findChildByDataObj(TreePath parentPath, Object dataObject) {
        if (dataObject == null) {
            return null;
        }
        //
        Object parentObj = parentPath.getLastPathComponent();
        assert parentObj instanceof MapperTreeNode;
        //
        List<MapperTreeNode> children = getChildren((MapperTreeNode)parentObj);
        for (MapperTreeNode childNode : children) {
            Object childDo = childNode.getDataObject();
            assert childDo != null;
            //
            if (childDo.equals(dataObject)) {
                return childNode.getTreePath();
            }
        }
        //
        return null;
    }
    
    /**
     * Looks for a child node by index
     * @param parentPath
     * @param dataObject
     * @return
     */
    public TreePath findChildByIndex(TreePath parentPath, int index) {
        if (index < 0) {
            return null;
        }
        //
        Object parentObj = parentPath.getLastPathComponent();
        assert parentObj instanceof MapperTreeNode;
        //
        List<MapperTreeNode> children = getChildren((MapperTreeNode)parentObj);
        if (index >= children.size()) {
            return null;
        }
        //
        MapperTreeNode childNode = children.get(index);
        return childNode.getTreePath();
    }
    
}
