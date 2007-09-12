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

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.OperationOrFunctionCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.AbstractNodeVisitor;
import org.netbeans.modules.xslt.mapper.model.targettree.AttributeDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.ElementDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.RuleNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;

/**
 * This visitor collects a list of tree nodes located under the target tree, 
 * which can be affected by a change of a node under the source tree. 
 * 
 * This visitor is intended to track and react to changes in the source tree. 
 * Such quite complex approach is required because the source schema 
 * can has infinit depth in case of recursive schema definition. 
 * So if the changes are caused by modifications in the source tree, 
 * predicates for example, then the relevant nodes of the target tree 
 * can be calculated only from the target tree side (in order to prevent 
 * infinit loop in case of recursive schema difinition).
 * 
 * The visitor should be applied to the root element of the target tree!
 * 
 * @author nk160297
 */
public class FindAffectedTargetNodesVisitor extends AbstractNodeVisitor {
    
    private TreeNode myModifiedSourceNode;
    
    private HashSet<StylesheetNode> myResultNodesList = new HashSet<StylesheetNode>();
    
    private StylesheetNode lastStylesheetNode;
    
    
    public FindAffectedTargetNodesVisitor(TreeNode modifiedSourceNode) {
        myModifiedSourceNode = modifiedSourceNode;
        //
        assert myModifiedSourceNode != null;
        assert myModifiedSourceNode.isSourceViewNode();
    }
    
    /**
     * Returns the collection of target tree nodes which can be affected. 
     */ 
    public Collection<StylesheetNode> getResultList() {
        return myResultNodesList;
    }
    
    public void visit(ElementDeclarationNode node){
        lastStylesheetNode = node;
        acceptUpstream(node);
        //
        acceptDownTree(node);
    }
    
    public void visit(AttributeDeclarationNode node){
        lastStylesheetNode = node;
        acceptUpstream(node);
    }
    
    public void visit(RuleNode node){
        lastStylesheetNode = node;
        acceptUpstream(node);
        //
        acceptDownTree(node);
    }
    
    public void visit(OperationOrFunctionCanvasNode node) {
        acceptUpstream(node);
    }
    
    public void visit(LiteralCanvasNode node) {
        // Nothing to do here.
    }
    
    public void visit(SchemaNode node) {
        if (node.isSourceViewNode()) {
            if (node.equals(myModifiedSourceNode)) {
                myResultNodesList.add(lastStylesheetNode);
            } else {
                acceptUpTree(node);
            }
        } 
    }
    
}
