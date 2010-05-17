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

package org.netbeans.modules.soa.ui.tree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.DataObjectHolder;
import org.netbeans.modules.soa.ui.tree.ExtTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder.FindResult;

/**
 * The processor, which looks for a tree item inside of TreeModel by 
 * a list of finders. The result of looking for is a TreePath. 
 * 
 * @author nk160297
 */
public class TreeFinderProcessor {
    
    private ExtTreeModel mTreeModel;

    public TreeFinderProcessor(ExtTreeModel treeModel) {
        mTreeModel = treeModel;
    }
    
    /**
     * Looks for the first node, which satisfies the search conditions, 
     * which are specified by the finderList argument.
     * The finderList contains the list of TreeItemFinder objects which 
     * have to be applied sequentially.
     * 
     * @param helper
     * @return TreePath of the found tree item. 
     */
    public TreePath findFirstNode(List<TreeItemFinder> finderList) {
        if (finderList == null || finderList.isEmpty()) {
            return null;
        }
        //
        Object rootNode = mTreeModel.getRoot();
        Stack<Object> locationStack = new Stack<Object>();
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
    
    public boolean findFirstChild(Stack<Object> locationStack,
            TreeItemFinder finder, int maxDepth) {
        //
        Object parentNode = locationStack.peek();
        List<Object> children = mTreeModel.getChildren(parentNode);
        if (children != null && children.size() != 0) {
            maxDepth--;
            for (Object child : children) {
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
     * Looks for the nodes, which satisfies the search conditions, 
     * which are specified by the finderList argument.
     * Then the data object is taken from each node and collected in the 
     * result list.
     * 
     * @param list of finders
     * @return List of data objects of the found tree item. 
     */
    public List<Object> findAllDataObjects(List<TreeItemFinder> finderList) {
        if (finderList == null || finderList.isEmpty()) {
            return null;
        }
        //
        ArrayList<List<Object>> foundLocations = new ArrayList<List<Object>>();
        //
        Object rootNode = mTreeModel.getRoot();
        Stack<Object> locationStack = new Stack<Object>();
        locationStack.push(rootNode);
        //
        for (TreeItemFinder finder : finderList) {
            fillNodesList(foundLocations, locationStack, finder, -1, false);
        }
        //
        ArrayList<Object> result = new ArrayList<Object>();
        for (List<Object> location : foundLocations) {
            Object tailObject = location.get(location.size() - 1); 
            assert tailObject instanceof DataObjectHolder;
            Object dataObject = ((DataObjectHolder)tailObject).getDataObject();
            result.add(dataObject);
        }
        return result;
    }

    
    /**
     * Looks for the tree pathes which tail satisfies the search conditions, 
     * which are specified by the finder.
     * 
     * @param list of finders
     * @return List of tree items 
     */
    public List<TreePath> findAllTreePaths(TreeItemFinder finder) {
        List<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>(1);
        finderList.add(finder);
        return findAllTreePaths(finderList);
    }
    
    
    /**
     * Looks for the tree pathes which tail satisfies the search conditions, 
     * which are specified by the finderList argument.
     * 
     * @param list of finders
     * @return List of tree items 
     */
    public List<TreePath> findAllTreePaths(List<TreeItemFinder> finderList) {
        if (finderList == null || finderList.isEmpty()) {
            return null;
        }
        //
        ArrayList<List<Object>> foundLocations = new ArrayList<List<Object>>();
        //
        Object rootNode = mTreeModel.getRoot();
        Stack<Object> locationStack = new Stack<Object>();
        locationStack.push(rootNode);
        //
        for (TreeItemFinder finder : finderList) {
            fillNodesList(foundLocations, locationStack, finder, -1, false);
        }
        //
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        for (List<Object> location : foundLocations) {
            TreePath treePath = new TreePath(location.toArray());
            result.add(treePath);
        }
        return result;
    }
    
    /**
     * An auxiliary method is intended to help search nodes recursively.
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
    public boolean checkNode(Stack<Object> locationStack, 
            TreeItemFinder finder, int maxDepth) {
        //
        Object parentNode = locationStack.peek();
        Object dataObject = getDataObject(parentNode);
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
    
    /**
     * Takes a tree item and returns an associated data object. 
     * The data objec here is the object, which will be analysed by finders. 
     * This method is intended to be overriden if necessary. 
     * Now it has the default implementation. 
     * 
     * @param treeItem
     * @return
     */
    protected Object getDataObject(Object treeItem) {
        if (treeItem instanceof DataObjectHolder) {
            return ((DataObjectHolder)treeItem).getDataObject();
        } else {
            return treeItem;
        }
    }

    //-------------------------------------------------------------------------
    
    /**
     * An auxiliary method is intended to help search nodes recursively.
     * See description of the findFirstNode method. 
     * Unlike the findFirstNode it can find more then one node.
     */
    public void fillNodesList(
            List<List<Object>> foundLocationsList,
            Stack<Object> locationStack,
            TreeItemFinder finder,
            int maxDepth,
            boolean lookDeeperIfFound) {
        //
        Object parentNode = locationStack.peek();
        Object dataObject = getDataObject(parentNode);
        //
        FindResult fr = finder.process(dataObject, null);
        //
        if (fr.isFit()) {
            // Copy location stack content to separate list and save it to result list.
            ArrayList<Object> foundLocation = 
                    new ArrayList<Object>(locationStack);
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
            List<Object> children = mTreeModel.getChildren(parentNode);
            maxDepth--;
            for (Object child : children) {
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
        assert parentObj instanceof TreeItem;
        //
        List<Object> children = mTreeModel.getChildren((Object)parentObj);
        for (Object childNode : children) {
            Object childDo = getDataObject(childNode);
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
        assert parentObj instanceof TreeItem;
        //
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        List<Object> children = mTreeModel.getChildren(parentObj);
        for (Object childNode : children) {
            Object childDo = getDataObject(childNode);
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
        assert parentObj instanceof TreeItem;
        //
        List<Object> children = mTreeModel.getChildren(parentObj);
        for (Object childNode : children) {
            Object childDo = getDataObject(childNode);
            assert childDo != null;
            //
            if (childDo.equals(dataObject)) {
                TreePath result = parentPath.pathByAddingChild(childNode);
                return result;
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
        assert parentObj instanceof TreeItem;
        //
        List<Object> children = mTreeModel.getChildren(parentObj);
        if (index >= children.size()) {
            return null;
        }
        //
        Object childNode = children.get(index);
        TreePath result = parentPath.pathByAddingChild(childNode);
        return result;
    }

}
