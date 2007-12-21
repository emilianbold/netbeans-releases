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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.mapper.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Alexey
 */
public class TreeExpandedState {

    private JTree tree;
    private MapperSwingTreeModel mTreeModel;
    
    private ArrayList<ArrayList<Step>> expanedNodes = new ArrayList<ArrayList<Step>>();

    private class Step {

        protected String name;
        protected int index;

        public Step(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }
        
        @Override
        public String toString() {
            return "Name: " + name + " Index: " + index;
        }
    }

    public TreeExpandedState(JTree tree) {
        this.tree = tree;
        TreeModel model = tree.getModel();
        assert model instanceof MapperSwingTreeModel;
        mTreeModel = (MapperSwingTreeModel)model;
    }

    public void save() {
        Object root = tree.getModel().getRoot();
        TreePath root_tp = new TreePath(root);
        //
        //save the expanded state
        Enumeration<TreePath> expanded = tree.getExpandedDescendants(root_tp);
        if (expanded != null) {
            while (expanded.hasMoreElements()) {
                TreePath tp = expanded.nextElement();
                ArrayList<Step> path = getStepsByTreePath(tp);
                expanedNodes.add(path);
            }
        }
    }

    public void restore() {

        for (ArrayList<Step> steps : expanedNodes) {
            TreePath tp = getTreePathbySteps(steps);
            if (tp != null) {
                tree.expandPath(tp);
            }
        }
    }

    
    
    private ArrayList<Step> getStepsByTreePath(TreePath tp) {

        ArrayList<Step> result = new ArrayList<Step>();

        for (int n = 0; n < tp.getPathCount(); n++) {
            MapperTreeNode node = (MapperTreeNode)tp.getPathComponent(n);
            String nodeDisplayName = mTreeModel.getDisplayName(node);

            Object parent = null;
                    
            if (n > 0) {
                parent = tp.getPathComponent(n - 1);
            }

            int index = 0;

            //calculate node index
            if (parent != null && parent instanceof MapperTreeNode) {
                List<MapperTreeNode> children = ((MapperTreeNode)parent).getChildren();
                for (MapperTreeNode tn : children) {
                    String name1 = mTreeModel.getDisplayName(tn);
                    if (name1.equals(nodeDisplayName)) {
                        if (node == tn) {
                            break;
                        }
                        index++;
                    }
                }
            }
            result.add(new Step(nodeDisplayName, index));
        }
        return result;
    }
    
    private TreePath getTreePathbySteps(ArrayList<Step> steps) {
        
        MapperTreeNode node = (MapperTreeNode) tree.getModel().getRoot();
        
        if (node == null){
            return null;
        }
        
        if (steps.size() == 0){
            return null;
        }
        
        String nodeDisplayName = mTreeModel.getDisplayName(node);
        if (!steps.get(0).getName().equals(nodeDisplayName)){
            return null;
        }
        
        Iterator it = steps.iterator();
        
        TreePath result = new TreePath(node);
        
        for (int n = 1; n < steps.size(); n++){
            Step step = steps.get(n);
            
            List<MapperTreeNode> children = node.getChildren();
            
            node = getTreeNodeByStep(children, step);
            if (node == null){
                return null;
            }
            
            result = result.pathByAddingChild(node);
        }
        return result;
    }
    
    private MapperTreeNode getTreeNodeByStep(List<MapperTreeNode> children, Step step){
        if (children == null) {
            return null;
        }
        //
        int index = 0;
        for (MapperTreeNode tn : children){
            String nodeDisplayName = mTreeModel.getDisplayName(tn);
            if (step.getName().equals(nodeDisplayName)){
                if (index == step.index){
                    return tn;
                }
                index++;
            }
        }
        return null;
    }
}
