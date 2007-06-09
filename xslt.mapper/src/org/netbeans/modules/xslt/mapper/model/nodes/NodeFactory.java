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

package org.netbeans.modules.xslt.mapper.model.nodes;


import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xslt.mapper.model.PredicatedAxiComponent;
import org.netbeans.modules.xslt.mapper.model.targettree.AttributeDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.ElementDeclarationNode;
import org.netbeans.modules.xslt.mapper.model.targettree.PredicatedSchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.RuleNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.TemplateNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author radval
 *
 */
public class NodeFactory {

   
    public static Node createNode(Object obj, XsltMapper mapper){
        if (obj instanceof AXIComponent){
            return new SchemaNode((AXIComponent)obj, mapper);
        } else if (obj instanceof PredicatedAxiComponent) {
            return new PredicatedSchemaNode((PredicatedAxiComponent)obj, mapper);
        } else if (obj instanceof Element || obj instanceof LiteralResultElement) {
            return new ElementDeclarationNode((XslComponent)obj, mapper); 
        } else if (obj instanceof Attribute) {
            return new AttributeDeclarationNode((XslComponent)obj, mapper); 
        } else if(obj instanceof Template) {
            return new TemplateNode((XslComponent)obj, mapper); 
        } else if (obj instanceof XslComponent) {
            return new RuleNode((XslComponent)obj, mapper); 
        } else if (obj instanceof XPathOperationOrFuntion) {
            return new OperationOrFunctionCanvasNode((XPathExpression)obj, mapper);
        } else if (obj instanceof XPathExpression) {
            return new LiteralCanvasNode((XPathExpression)obj, mapper);
        }
        return null;
    }

}
