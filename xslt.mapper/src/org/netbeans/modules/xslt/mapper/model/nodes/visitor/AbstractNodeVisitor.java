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
