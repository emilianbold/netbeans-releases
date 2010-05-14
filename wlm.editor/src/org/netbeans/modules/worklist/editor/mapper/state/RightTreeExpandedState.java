/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.state;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.RightTree;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.worklist.editor.mapper.model.WlmMapperModel;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperTreeNode;

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
        assert mapperModel instanceof WlmMapperModel;
        return mapperModel != null
                ? ((WlmMapperModel)mapperModel).getRightTreeModel() : null;
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
