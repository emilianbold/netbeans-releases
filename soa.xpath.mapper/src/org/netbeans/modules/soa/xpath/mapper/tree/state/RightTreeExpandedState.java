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
package org.netbeans.modules.soa.xpath.mapper.tree.state;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.RightTree;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;

/**
 *
 * @author Vitaly Bychkov
 * @author Alexey
 *
 */
public class RightTreeExpandedState implements TreeExpandedState {

    private Mapper myMapper;
    private ArrayList<ArrayList<Step>> expanedNodes = new ArrayList<ArrayList<Step>>();

    public RightTreeExpandedState(Mapper mapper) {
        myMapper = mapper;
    }

    public void setMapper(Mapper mapper) {
        myMapper = mapper;
    }

    private RightTree getRightTree() {
        return myMapper != null ? myMapper.getRightTree() : null;
    }

    private MapperSwingTreeModel getTreeModel() {
        if (myMapper == null) {
            return null;
        }
        MapperModel mapperModel = myMapper.getModel();
        assert mapperModel instanceof XPathMapperModel;
        return mapperModel != null
                ? ((XPathMapperModel)mapperModel).getRightTreeModel() : null;
    }

    public void save() {
        if (myMapper == null) {
            return;
        }
        List<TreePath> expanded = myMapper.getExpandedPathes();

        if (expanded != null) {
            for (TreePath treePath : expanded) {
                ArrayList<Step> path = getStepsByTreePath(treePath);
                expanedNodes.add(path);
            }
        }
    }

    public void restore() {

        List<TreePath> paths = new ArrayList<TreePath>();
        for (ArrayList<Step> steps : expanedNodes) {
            TreePath tp = getTreePathbySteps(steps);
            if (tp != null) {
                paths.add(tp);
            }
        }

        if (paths.size() < 1) {
            return;
        }

        if (myMapper == null) {
            return;
        }
        myMapper.applyExpandedPathes(paths);
    }



    private ArrayList<Step> getStepsByTreePath(TreePath tp) {

        ArrayList<Step> result = new ArrayList<Step>();

        MapperSwingTreeModel treeModel = getTreeModel();
        if (treeModel == null) {
            return result;
        }

        for (int n = 0; n < tp.getPathCount(); n++) {
            MapperTreeNode node = (MapperTreeNode)tp.getPathComponent(n);
            String nodeDisplayName = treeModel.getDisplayName(node);

            Object parent = null;

            if (n > 0) {
                parent = tp.getPathComponent(n - 1);
            }

            int index = 0;

            //calculate node index
            if (parent != null && parent instanceof MapperTreeNode) {
                List<MapperTreeNode> children = ((MapperTreeNode)parent).getChildren();
                for (MapperTreeNode tn : children) {
                    String name1 = treeModel.getDisplayName(tn);
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
        MapperSwingTreeModel treeModel = getTreeModel();
        if (treeModel == null) {
            return null;
        }

        MapperTreeNode node = (MapperTreeNode) treeModel.getRoot();

        if (node == null){
            return null;
        }

        if (steps.size() == 0){
            return null;
        }

        String nodeDisplayName = treeModel.getDisplayName(node);
        if (!steps.get(0).getName().equals(nodeDisplayName)){
            return null;
        }

        TreePath result = new TreePath(node);

        for (int n = 1; n < steps.size(); n++){
            Step step = steps.get(n);

            List<MapperTreeNode> children = node.getChildren();

            node = getTreeNodeByStep(treeModel, children, step);
            if (node == null){
                return null;
            }

            result = result.pathByAddingChild(node);
        }
        return result;
    }

    private MapperTreeNode getTreeNodeByStep(MapperSwingTreeModel treeModel,
            List<MapperTreeNode> children, Step step)
    {
        if (treeModel == null || children == null) {
            return null;
        }
        //
        int index = 0;
        for (MapperTreeNode tn : children){
            String nodeDisplayName = treeModel.getDisplayName(tn);
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
