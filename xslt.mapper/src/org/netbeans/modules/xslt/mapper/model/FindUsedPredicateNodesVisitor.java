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
