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
package org.netbeans.modules.xslt.mapper.view;

import java.util.List;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.CopyOf;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.ValueOf;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;

/**
 * Visitor to extract or update xpath expressions stored in different types of
 * XSL elements
 *
 */
public class GetExpressionVisitor extends XslVisitorAdapter{
    
    
    
    
    
    private XPathExpression expression;
    
    public GetExpressionVisitor() {
    }
    
    
    public void setExpression(XPathExpression expression) {
        this.expression = expression;
    }
    
    public void setExpression(String expression) {
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        try {
            
            setExpression((expression != null) ?
                xpImpl.parseExpression(expression) : null);
        } catch (XPathException ex) {
            setExpression((XPathExpression) null);
        };
    }
    
    public String getExpressionString() {
        return (expression != null) ? expression.toString() : "";
    }
    
    public XPathExpression getExpression(){
        return this.expression;
        
    }
    
    public void visit(ValueOf vof){
        setExpression(vof.getSelect());
    }
    
    
    public void visit(If iff) {
        setExpression(iff.getTest());
    }
    
    public void visit(CopyOf cof){
        setExpression(cof.getSelect());
    }
    
    
    
    public void visit(Attribute attribute) {
        handleElementOrAttribute(attribute);
    }
    
    public void visit(Element element) {
        handleElementOrAttribute(element);
        
    }
    
    public void visit(LiteralResultElement element) {
        handleElementOrAttribute(element);
    }
    
    private void handleElementOrAttribute(XslComponent component){
        ValueOf vof = isValueOfContainer(component);
        if (vof != null){
            setExpression(vof.getSelect());
        }
    }
    
    public void visit(ForEach forEach) {
        
        setExpression(forEach.getSelect());
        
        
    }
    
    public void visit(When when) {
        setExpression(when.getTest());
        
    }
    
    
    
    public static ValueOf isValueOfContainer(XslComponent c){
        
        if(c instanceof Attribute || c instanceof Element || c instanceof LiteralResultElement) {
            List<XslComponent> children = c.getChildren();
            if (children.size() == 1 && children.get(0) instanceof ValueOf){
                return ((ValueOf) children.get(0));
                
            }
        }
        return null;
    }
    
    
    
}
