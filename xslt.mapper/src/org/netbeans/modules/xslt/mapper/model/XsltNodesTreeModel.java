/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xslt.mapper.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author nk160297
 */
public abstract class XsltNodesTreeModel implements TreeModel {
    
    private TreeNode myRootNode;
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    private XsltMapper mapper;
    public XsltNodesTreeModel(XsltMapper mapper) {
        this.mapper = mapper;
        resetRoot();
    }
    
   
    public abstract TreeNode loadRoot();
    
    public XsltMapper getMapper(){
        return this.mapper;
    }
    
    public Object getRoot() {
        return myRootNode;
    }
    
    public void resetRoot(){
        TreeNode newRootNode = loadRoot();
        if (newRootNode != null && 
            myRootNode!=null &&
            newRootNode.getDataObject() == myRootNode.getDataObject()){
            return;
        }
        
        if (myRootNode != null){
            myRootNode.removeFromTree();
        }
        
        myRootNode = newRootNode;
    }
    public int getIndexOfChild(Object parent, Object requiredChild) {
        assert parent instanceof TreeNode;
        assert requiredChild instanceof TreeNode;
        //
        List<TreeNode> children = ((TreeNode)parent).getChildren();
        
        return children.indexOf(requiredChild);
        
    }
    
    public Object getChild(Object parent, int index) {
        assert parent instanceof TreeNode;
        
        //
        List<TreeNode> children = ((TreeNode)parent).getChildren();
        return children.get(index);
    }
    
    public boolean isLeaf(Object node) {
        assert node instanceof TreeNode;
        //
        return ((TreeNode) node).getChildren().isEmpty();
    }
    
    public int getChildCount(Object parent) {
        assert parent instanceof TreeNode;
        //
        return ((TreeNode) parent).getChildren().size();
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing for a while
    }

    public void fireTreeChanged(TreePath tp) {
        TreeModelEvent event = new TreeModelEvent(this, tp);
        for(TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }


    
}
