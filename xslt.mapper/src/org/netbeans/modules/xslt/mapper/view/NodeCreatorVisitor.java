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

package org.netbeans.modules.xslt.mapper.view;

import java.util.Iterator;
import org.netbeans.modules.soa.mapper.util.Duration;


import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.methoid.MethoidLoader;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.SourceTypeFinder;

/**
 *
 * @author radval
 *
 */
public class NodeCreatorVisitor extends AbstractXPathVisitor {
    
    private XsltMapper mapper;
    
    private Node result;
    
    public NodeCreatorVisitor(XsltMapper mapper) {
        this.mapper = mapper;
    }
    
    public Node getResult(){
        return result;
    }
    
    private void setResult(Node result){
        this.result = result;
    }
    
    public void visit(XPathStringLiteral expr) {
        IMethoidNode node = null;
        try {
            Duration.parse(expr.getExpressionString());
            setResult(createMethoidNode(expr, Constants.DURATION_LITERAL));
        } catch (Exception e) {
            // Not a duration literal
            setResult(createLiteralNode(expr, Constants.STRING_LITERAL));
        }
    }
    
    public void visit(XPathNumericLiteral expr) {
        setResult(createLiteralNode(expr, Constants.NUMBER_LITERAL));
    }
    
    public void visit(XPathCoreOperation expr) {
        visitXPathOperatorOrFunction(expr);
    }
    
    public void visit(XPathCoreFunction expr) {
        visitXPathOperatorOrFunction(expr);
    }
    
    private void visitXPathOperatorOrFunction(XPathOperationOrFuntion operator) {
        Node node = createMethoidNode(operator, operator.getName());
        if (node != null) {
            setResult(node);
        } else {
            setResult(createLiteralNode(operator, Constants.XPATH_LITERAL));
        }
    }
    
    public void visit(XPathExtensionFunction expr) {
        setResult(createLiteralNode(expr, Constants.XPATH_LITERAL));
    }
    
    public void visit(XPathLocationPath expr) {
        Node result = (new SourceTypeFinder(mapper).findNode(expr));
        if (result != null){
            setResult(result);
        } else {
            setResult(createLiteralNode(expr, Constants.XPATH_LITERAL));
        } 
    }
    
    public void visit(XPathVariableReference vReference) {
        setResult(createLiteralNode(vReference, Constants.XPATH_LITERAL));
    }
    
    private Node createLiteralNode(XPathExpression expr, String name){
        Node node = createMethoidNode(expr, name);
        if (node != null){
            IFieldNode out_field = (IFieldNode) node.getOutputNode();
            if (out_field != null){
                out_field.setLiteralName(expr.getExpressionString());
            }
        }
        return node;
    }
    
    private Node createMethoidNode(XPathExpression expr, String name) {
        IMethoid methoid = MethoidLoader.loadMethoid(name);
        IMethoidNode node = mapper.createMethoidNode(methoid);
        
        Node operatorNode = NodeFactory.createNode(expr, (XsltMapper) mapper);
        
        node.setNodeObject(operatorNode);
        operatorNode.setMapperNode(node);
        
        return operatorNode;
    }
    
}
