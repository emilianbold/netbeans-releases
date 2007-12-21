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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.OperationOrFunctionCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.AbstractNodeVisitor;
import org.netbeans.modules.xslt.mapper.model.targettree.AttributeDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.ElementDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.PredicatedSchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.RuleNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;

/**
 * This visitor collects a list of the predicated tree nodes located under 
 * the source tree, which are used by XSLT.
 * It is intended to delete unused predicates from the source tree.
 * 
 * The visitor should be applied to the root element of the target tree!
 * 
 * @author nk160297
 */
public class FindUsedPredicateNodesVisitor extends AbstractNodeVisitor {
    
    private HashSet<PredicatedSchemaNode> myResultNodesList = 
            new HashSet<PredicatedSchemaNode>();
    
    /**
     * Returns the set of used predicated nodes.
     */ 
    public Set<PredicatedSchemaNode> getResultList() {
        return myResultNodesList;
    }
    
    public void visit(ElementDeclarationNode node){
        acceptUpstream(node);
        //
        acceptDownTree(node);
    }
    
    public void visit(AttributeDeclarationNode node){
        acceptUpstream(node);
    }
    
    public void visit(RuleNode node){
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
            if (node instanceof PredicatedSchemaNode) {
                myResultNodesList.add((PredicatedSchemaNode)node);
            }
            acceptUpTree(node);
        } 
    }
    
}
