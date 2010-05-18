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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package com.sun.jsfcl.std.property;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.sun.jsfcl.std.reference.ReferenceDataItem;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseManyOfManyReferenceDataPanel extends ChooseManyReferenceDataPanel implements
    TreeSelectionListener {
    protected static final String ADD_LIST_ACTION = "add list";
    protected static final String DELETE_LIST_ACTION = "delete list";

    protected JButton deleteListJButton;
    protected JTree selectedJTree;
    protected DefaultTreeModel selectedJTreeModel;
    protected DefaultMutableTreeNode selectedRootNode;

    public ChooseManyOfManyReferenceDataPanel(ChooseManyOfManyReferenceDataPropertyEditor
        propertyEditor, DesignProperty liveProperty) {

        super(propertyEditor, liveProperty);
    }

    public void actionPerformed(ActionEvent event) {

        super.actionPerformed(event);
        if (ADD_LIST_ACTION.equals(event.getActionCommand())) {
            handleAddListAction(event);
            return;
        }
        if (DELETE_LIST_ACTION.equals(event.getActionCommand())) {
            handleDeleteListAction(event);
            return;
        }
    }

    protected ChooseManyOfManyReferenceDataPropertyEditor
        getChooseManyOfManyReferenceDataPropertyEditor() {

        return (ChooseManyOfManyReferenceDataPropertyEditor)getPropertyEditor();
    }

    protected void adjustLeftColumnWidthIfNecessary(DefaultMutableTreeNode node) {

        ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer renderer = (
            ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer)selectedJTree.getCellRenderer();
        renderer.getTreeCellRendererComponent(
            selectedJTree,
            node,
            false,
            false,
            false,
            -1,
            false);
        renderer.adjustLeftColumnWidthIfNecessary();
    }

    protected ChooseManyOfManyNodeData getSelectedData() {
        DefaultMutableTreeNode node;
        ChooseManyOfManyNodeData data;

        node = getSelectedNode();
        if (node == null) {
            return null;
        }
        data = (ChooseManyOfManyNodeData)node.getUserObject();
        return data;
    }

    protected DefaultMutableTreeNode getSelectedNode() {
        TreePath paths[], path;
        Object pathObjects[];
        DefaultMutableTreeNode node;

        paths = selectedJTree.getSelectionModel().getSelectionPaths();
        if (paths == null || paths.length == 0) {
            return null;
        }
        path = paths[0];
        return (DefaultMutableTreeNode)path.getLastPathComponent();
    }

    public List getSelectedListOfListOfItems() {

        Enumeration rootChildrenEnum = selectedRootNode.children();
        ArrayList manyOfManyList = new ArrayList();
        while (rootChildrenEnum.hasMoreElements()) {
            ArrayList manyList = new ArrayList(16);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootChildrenEnum.nextElement();
            Enumeration nodeChildrenEnum = node.children();
            while (nodeChildrenEnum.hasMoreElements()) {
                DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode)nodeChildrenEnum.
                    nextElement();
                ChooseManyOfManyNodeData nodeChildData = (ChooseManyOfManyNodeData)nodeChild.
                    getUserObject();
                manyList.add(nodeChildData.getData());
            }
            manyOfManyList.add(manyList);
        }
        // remove the trailing empty groups
        for (int i = manyOfManyList.size() - 1; i >= 0; i--) {
            List list = (List)manyOfManyList.get(i);
            if (list.size() == 0) {
                manyOfManyList.remove(i);
            } else {
                break;
            }
        }
        return manyOfManyList;
    }

    protected String getListNodeNamePrefix() {

        return BundleHolder.bundle.getMessage("GroupPrefix") + " ";
    }

    public Object getPropertyValue() {

        List selectedListOfListOfItems = getSelectedListOfListOfItems();
        if (selectedListOfListOfItems == null || selectedListOfListOfItems.size() == 0) {
            return null;
        } else {
            String string = getChooseManyOfManyReferenceDataPropertyEditor().
                getStringForManyOfManyItems(selectedListOfListOfItems);
            return string;
        }
    }

    protected List getSelected_ItemsList() {
        ChooseManyOfManyNodeData data;

        data = getSelectedData();
        if (data == null) {
            return null;
        }
        return (List)data.getData();
    }

    protected String getTopLabel() {

        return getCompositeReferenceData().getChooseManyOfManyTitle();
    }

    protected void handleAddListAction(ActionEvent event) {
        ArrayList list;
        DefaultMutableTreeNode node;
        ChooseManyOfManyNodeData data;
        TreePath path;

        list = new ArrayList();
        data = new ChooseManyOfManyNodeData(null, list);
        node = new DefaultMutableTreeNode(data, true);
        selectedJTreeModel.insertNodeInto(node, selectedRootNode, selectedRootNode.getChildCount());
        path = new TreePath(node.getPath());
        selectedJTree.scrollPathToVisible(path);
        selectedJTree.setSelectionPath(path);
    }

    protected void handleDeselectAllAction(ActionEvent event) {

        Enumeration parentEnum = selectedRootNode.children();
        while (parentEnum.hasMoreElements()) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)parentEnum.nextElement();
            parent.removeAllChildren();
        }
        selectedJTreeModel.reload();
        updateTreeRelatedButtons();
    }

    protected void handleChoicesJListSelectionChanged(ListSelectionEvent event) {

        super.handleChoicesJListSelectionChanged(event);
        if (event.getValueIsAdjusting()) {
            return;
        }
        updateTreeRelatedButtons();
    }

    protected void handleDeleteListAction(ActionEvent event) {

        TreePath paths[] = selectedJTree.getSelectionPaths();
        if (paths == null) {
            return;
        }
        DefaultMutableTreeNode sibling = null;
        for (int i = 0; i < paths.length; i++) {
            TreePath path = paths[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            sibling = node.getNextSibling();
            if (sibling == null) {
                sibling = node.getPreviousSibling();
            }
            removePath(path);
        }
        if (sibling != null) {
            selectedJTree.setSelectionPath(new TreePath(sibling.getPath()));
        }
    }

    protected void handleDeselectAction(ActionEvent event) {
        TreePath paths[], path;

        paths = selectedJTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            path = paths[i];
            removePath(path);
        }
        selectedJTree.setSelectionPaths(null);
    }

    protected void handleDownAction(ActionEvent event) {

        TreePath[] paths = selectedJTree.getSelectionPaths();
        sortTreePaths(paths);
        for (int i = paths.length - 1; i >= 0; i--) {
            TreePath path = paths[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            int index = parent.getIndex(node);
            if ((index + 1) == parent.getChildCount()) {
                continue;
            }
            DefaultMutableTreeNode removed = (DefaultMutableTreeNode)parent.getChildAt(index);
            boolean wasExpanded = selectedJTree.isExpanded(path);
            selectedJTreeModel.removeNodeFromParent(removed);
            selectedJTreeModel.insertNodeInto(removed, parent, index + 1);
            if (wasExpanded && removed.getChildCount() > 0) {
                DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)removed.getFirstChild();
                selectedJTree.makeVisible(new TreePath(firstChild.getPath()));
            }

        }
        selectedJTree.setSelectionPaths(paths);
    }

    protected void handleSelectAction(ActionEvent event) {
        Object items[];
        ReferenceDataItem item;

        TreePath[] selectionPaths = selectedJTree.getSelectionPaths();
        for (int j = 0; j < selectionPaths.length; j++) {
            TreePath selectionPath = selectionPaths[j];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectionPath.
                getLastPathComponent();
            int insertIndex = -1;
            if (node.getParent() != selectedRootNode) {
                insertIndex = node.getParent().getIndex(node);
                insertIndex++;
                node = (DefaultMutableTreeNode)node.getParent();
            }
            items = choicesJList.getSelectedValues();
            for (int i = 0; i < items.length; i++) {
                item = (ReferenceDataItem)items[i];
                Enumeration children = node.children();
                boolean foundMatch = false;
                if (!getChooseManyReferenceDataPropertyEditor().getAllowDuplicates()) {
                    while (children.hasMoreElements()) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.
                            nextElement();
                        ChooseManyOfManyNodeData data = (ChooseManyOfManyNodeData)childNode.
                            getUserObject();
                        ReferenceDataItem otherItem = (ReferenceDataItem)data.getData();
                        if (item.equals(otherItem)) {
                            foundMatch = true;
                            break;
                        }
                    }
                }
                if (!foundMatch) {
                    DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(new
                        ChooseManyOfManyNodeData(item.getDisplayString(), item), false);
                    int actualInsertIndex;
                    if (insertIndex == -1) {
                        actualInsertIndex = node.getChildCount();
                    } else {
                        actualInsertIndex = insertIndex;
                        insertIndex++;
                    }
                    selectedJTreeModel.insertNodeInto(itemNode, node, actualInsertIndex);
                    selectedJTree.makeVisible(new TreePath(itemNode.getPath()));
                    adjustLeftColumnWidthIfNecessary(itemNode);
                }
            }
        }
//        if (firstNode != null) {
//            selectedJTree.scrollPathToVisible(new TreePath(firstNode.getPath()));
//        }
        updateTreeRelatedButtons();
    }

    protected void handleSelectedJTreeSelectionChanged(TreeSelectionEvent event) {

        updateTreeRelatedButtons();
    }

    protected void handleUpAction(ActionEvent event) {

        TreePath paths[] = selectedJTree.getSelectionPaths();
        sortTreePaths(paths);
        for (int i = 0; i < paths.length; i++) {
            TreePath path = paths[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            int index = parent.getIndex(node);
            if (index == 0) {
                continue;
            }
            DefaultMutableTreeNode removed = (DefaultMutableTreeNode)parent.getChildAt(index);
            boolean wasExpanded = selectedJTree.isExpanded(path);
            selectedJTreeModel.removeNodeFromParent(removed);
            selectedJTreeModel.insertNodeInto(removed, parent, index - 1);
            if (wasExpanded && removed.getChildCount() > 0) {
                DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)removed.getFirstChild();
                selectedJTree.makeVisible(new TreePath(firstChild.getPath()));
            }
        }
        selectedJTree.setSelectionPaths(paths);
    }

    protected void initializeComponents() {
        JButton addListJButton;
        GridBagConstraints gridBagConstraints;

        super.initializeComponents();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        // Add add/remove buttons
        addListJButton = new JButton(BundleHolder.bundle.getMessage("NewGroup")); //NOI18N
        addListJButton.setActionCommand(ADD_LIST_ACTION);
        addListJButton.addActionListener(this);
        buttonPanel.add(addListJButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        deleteListJButton = new JButton(BundleHolder.bundle.getMessage("RemoveGroup")); //NOI18N
        deleteListJButton.setActionCommand(DELETE_LIST_ACTION);
        deleteListJButton.addActionListener(this);
        deleteListJButton.setEnabled(false);
        buttonPanel.add(deleteListJButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_END;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(buttonPanel, gridBagConstraints);
        updateTreeRelatedButtons();
    }

    protected Component initializeSelectedListComponent() {

        ChooseManyOfManyNodeData data = new ChooseManyOfManyNodeData("root", null);
        selectedRootNode = new DefaultMutableTreeNode(data, true);
        selectedJTreeModel = new DefaultTreeModel(selectedRootNode);
        selectedJTree = new JTree();
        ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer renderer = new
            ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer(getListNodeNamePrefix());
        selectedJTree.setCellRenderer(renderer);
        populateSelectedJTreeModel();
        selectedJTree.setModel(selectedJTreeModel);
        selectedJTree.setRootVisible(false);
        selectedJTree.setShowsRootHandles(true);
        selectedJTree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        selectedJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.
            DISCONTIGUOUS_TREE_SELECTION);
        selectedJTree.getSelectionModel().addTreeSelectionListener(this);
        // expand all nodes
        for (int i = 0; i < selectedJTree.getRowCount(); i++) {
            selectedJTree.expandRow(i);
        }
        return selectedJTree;
    }

    protected void populateSelectedJTreeModel() {
        List manyOfManyList, manyList;
        Iterator manyOfManyIterator, manyIterator;
        ReferenceDataItem item;
        DefaultMutableTreeNode listNode, itemNode;
        int i;

        selectedRootNode.removeAllChildren();
        selectedJTreeModel.reload();
        ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer renderer = (
            ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer)selectedJTree.getCellRenderer();
        renderer.resetLeftColumnWidth();
        manyOfManyList = getChooseManyOfManyReferenceDataPropertyEditor().
            getValueListOfManyOfManyReferenceDataItems();
        if (manyOfManyList == null) {
            return;
        }
        if (manyOfManyList.size() == 0) {
            manyOfManyList.add(new ArrayList());
        }
        manyOfManyIterator = manyOfManyList.iterator();
        i = 0;
        while (manyOfManyIterator.hasNext()) {
            manyList = (List)manyOfManyIterator.next();
            listNode = new DefaultMutableTreeNode(new ChooseManyOfManyNodeData(null, manyList), true);
            selectedJTreeModel.insertNodeInto(listNode, selectedRootNode,
                selectedRootNode.getChildCount());
            manyIterator = manyList.iterator();
            while (manyIterator.hasNext()) {
                item = (ReferenceDataItem)manyIterator.next();
                itemNode = new DefaultMutableTreeNode(new ChooseManyOfManyNodeData(item.
                    getDisplayString(), item), false);
                selectedJTreeModel.insertNodeInto(itemNode, listNode, listNode.getChildCount());
                renderer.getTreeCellRendererComponent(
                    selectedJTree,
                    itemNode,
                    false,
                    false,
                    false,
                    -1,
                    false);
                renderer.adjustLeftColumnWidthIfNecessary();
            }
            i++;
        }
    }

    protected void removePath(TreePath path) {
        DefaultMutableTreeNode node, parent, removed;
        int index;

        node = (DefaultMutableTreeNode)path.getLastPathComponent();
        parent = (DefaultMutableTreeNode)node.getParent();
        index = parent.getIndex(node);
        removed = (DefaultMutableTreeNode)parent.getChildAt(index);
        selectedJTreeModel.removeNodeFromParent(removed);
    }

    protected void sortTreePaths(TreePath[] paths) {

        IdentityHashMap selected = new IdentityHashMap();
        for (int i = 0; i < paths.length; i++) {
            TreePath path = paths[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            selected.put(node, path);
        }
        sortTreePaths(paths, selected, selectedRootNode, 0);
    }

    protected int sortTreePaths(TreePath[] paths, IdentityHashMap pathsNodes,
        DefaultMutableTreeNode currentNode, int index) {

        if (pathsNodes.containsKey(currentNode)) {
            paths[index] = (TreePath)pathsNodes.get(currentNode);
            index++;
        }
        Enumeration enumer = currentNode.children();
        while (enumer.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)enumer.nextElement();
            index = sortTreePaths(paths, pathsNodes, child, index);
        }
        return index;
    }

    protected void updateTreeRelatedButtons() {

        TreePath[] selected = selectedJTree.getSelectionPaths();
        int selectedCount = selected == null ? 0 : selected.length;
        if (selectedCount == 0 && !selectedRootNode.isLeaf()) {
            selectedJTree.setSelectionRow(0);
            return;
        }
        int groupCount = 0;
        int itemCount = 0;
        Enumeration enumer = selectedRootNode.breadthFirstEnumeration();
        while (enumer.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumer.nextElement();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if (parent == null) {

            } else if (parent == selectedRootNode) {
                groupCount++;
            } else {
                itemCount++;
            }
        }
        int groupsSelectedCount = 0;
        boolean onlyGroupsSelected = true;
        boolean anyChildAtTop = false;
        boolean anyChildAtBottom = false;
        for (int i = 0; i < selectedCount; i++) {
            TreePath path = selected[i];
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node.getParent() == selectedRootNode) {
                groupsSelectedCount++;
            } else {
                onlyGroupsSelected = false;
            }
            int childIndex = node.getParent().getIndex(node);
            if (childIndex == 0) {
                anyChildAtTop = true;
            }
            if (childIndex == (node.getParent().getChildCount() - 1)) {
                anyChildAtBottom = true;
            }
        }
        boolean choicesSelected = choicesJList.getSelectedIndices().length > 0;
        selectJButton.setEnabled(selectedCount > 0 && choicesSelected);
        deselectJButton.setEnabled(groupsSelectedCount == 0 && selectedCount > 0);
        deleteListJButton.setEnabled(selectedCount > 0 && onlyGroupsSelected);
        if (getCompositeReferenceData().canOrderItems()){
            upJButton.setEnabled(selectedCount > 0 && !anyChildAtTop);
            downJButton.setEnabled(selectedCount > 0 && !anyChildAtBottom);
        }
        deselectAllJButton.setEnabled(itemCount > 0);
    }

    public void valueChanged(TreeSelectionEvent event) {

        if (event.getSource() == selectedJTree.getSelectionModel()) {
            handleSelectedJTreeSelectionChanged(event);
        }
    }

}
