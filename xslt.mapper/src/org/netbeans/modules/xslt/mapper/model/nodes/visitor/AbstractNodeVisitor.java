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

package org.netbeans.modules.xslt.mapper.model.nodes.visitor;

import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.OperationOrFunctionCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.AttributeDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.ElementDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.RuleNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;


/**
 *
 * @author radval
 *
 */
public abstract class AbstractNodeVisitor implements NodeVisitor {

    public void visitNode(Node node) {
    }
    {
    }

    public void visit(SchemaNode node) {
        visitNode(node);
    }

    public void visit(ElementDeclarationNode node) {
        visitNode(node);
    }

    public void visit(AttributeDeclarationNode node) {
        visitNode(node);
    }

    public void visit(RuleNode node) {
        visitNode(node);
    }

    public void visit(OperationOrFunctionCanvasNode node) {
        visitNode(node);
    }

    public void visit(LiteralCanvasNode node) {
        visitNode(node);
    }



    public void acceptUpstream(Node node) {
        for (Node n : node.getPreviousNodes()) {
            if (n != null) {
                n.accept(this);
            }
        }
    }

    public void acceptDowntream(Node node) {
        for (Node n : node.getNextNodes()) {
            if (n != null) {
                n.accept(this);
            }
        }
    }

    /**
     * Move from child to parent.
     */
    public void acceptUpTree(TreeNode node) {
        TreeNode parent = node.getParent();
        if (parent != null) {
            parent.accept(this);
        }
    }

    /**
     * Move from paretn to child.
     */
    public void acceptDownTree(TreeNode node) {
        for (TreeNode child : node.getChildren()) {
            child.accept(this);
        }
    }
}
