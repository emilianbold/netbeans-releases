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
