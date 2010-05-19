/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.print.PrintManager;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.ui.tree.ExtTreeModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.impl.TreePathComparator;
import org.netbeans.modules.worklist.editor.mapper.DesignContextController;
import org.netbeans.modules.worklist.editor.mapper.MapperTcContext;
import org.netbeans.modules.worklist.editor.mapper.model.MapperTreeContext;
import org.netbeans.modules.worklist.editor.mapper.tree.model.MapperConnectabilityProvider;

/**
 * An internal tree model based on the Swing Tree Model. 
 * It performs caching of data is obtained from another tree models,
 * which are described by the MapperTreeModel and TreeItemInfoProvider 
 * interfaces. 
 * 
 * @author nk160297
 */
public class MapperSwingTreeModel implements ExtTreeModel<MapperTreeNode>, 
        MapperTcContext.Provider {
    
    protected EventListenerList listenerList = new EventListenerList();
    private SoaTreeModel mSourceModel;
    private MapperTcContext mMapperTcContext;
    private MapperTreeNode mRootNode;
    
    
    public MapperSwingTreeModel(MapperTcContext mapperTcContext, 
            SoaTreeModel sourceModel) {
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
        //
        Boolean isLeafObj = mNode.isLeaf();
        if (isLeafObj == null) {
            isLeafObj = mSourceModel.getTreeStructureProvider().isLeaf(mNode);
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
    
    public SoaTreeModel getSourceModel() {
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
                    displayName = infoProvider.getDisplayName(mNode);
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
                toolTipText = infoProvider.getToolTipText(mNode);
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
                    icon = infoProvider.getIcon(mNode);
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
        //
        if (mSourceModel instanceof MapperConnectabilityProvider) {
            return ((MapperConnectabilityProvider)mSourceModel).
                    isConnectable(mNode) == Boolean.TRUE;
        }
        //
        return false;
    }
    
    public JPopupMenu getPopupMenu(Object node) {
        assert node instanceof MapperTreeNode;
        MapperTreeNode mNode = (MapperTreeNode)node;
        //
        TreePath treePath = mNode.getTreePath();
        //
        TreeItemActionsProvider actionProvider = 
                mSourceModel.getTreeItemActionsProvider();
        if (actionProvider != null) {
            //
            // Determine if the model is the left one
            TreeModel leftTreeModel = 
                    mMapperTcContext.getMapper().getModel().getLeftTreeModel();
            boolean isLeft = (leftTreeModel == this);
            //
            MapperTreeContext context = new MapperTreeContext(mMapperTcContext, isLeft);
            List<Action> menuActionList = actionProvider.getMenuActions(mNode,
                    context, treePath);
            //
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
    
    public JPopupMenu getCanvasPopupMenu(GraphItem item) {
        JPopupMenu newMenu = new JPopupMenu();

        // vlv: print
        Action action = PrintManager.
                printAction(mMapperTcContext.getMapper());
        JMenuItem newItem = new JMenuItem(action);
        
        newItem.setText(action.getValue(Action.SHORT_DESCRIPTION).toString());
//        newItem.setMnemonic(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK + 
//                KeyEvent.SHIFT_DOWN_MASK + KeyEvent.ALT_DOWN_MASK).getKeyCode());
//        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK + 
//                KeyEvent.SHIFT_DOWN_MASK + KeyEvent.ALT_DOWN_MASK));
        newMenu.add(newItem);

        return newMenu;
    }
    
    public List<MapperTreeNode> getChildren(MapperTreeNode parent) {
        List<MapperTreeNode> childrenList = parent.getChildren();
        if (childrenList == null) {
            //
            // Construct children nodes here
            childrenList = new ArrayList<MapperTreeNode>();
            List<Object> childrenDataObjectList = 
                    mSourceModel.getTreeStructureProvider().getChildren(parent);
            
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
        for (int i = listeners.length - 2; i >= 0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    //
                    e = new TreeModelEvent(source, parentTreePath, 
                            new int[] {childIndex}, 
                            new Object[] {removedObj});
                }
                ((TreeModelListener)listeners[i + 1]).treeNodesRemoved(e);
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
    
    public List<TreePath> sortByLocation(Collection<TreePath> unsorted) {
        //
        ArrayList<TreePath> sorted = new ArrayList<TreePath>(unsorted.size());
        sorted.addAll(unsorted);
        
        Collections.sort(sorted, new TreePathComparator(this));
        //
        return sorted;
    }
    
    //-------------------------------------------------------------------------
    // Utility functions
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
    
    public static TreeItem getTreeItem(TreePath treePath) {
        Object obj = treePath.getLastPathComponent();
        if (obj instanceof TreeItem) {
            return (TreeItem)obj;
        }
        //
        return null;
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
    
}
