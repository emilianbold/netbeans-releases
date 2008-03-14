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

import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.CopyOf;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.ValueOf;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslComponentFactory;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;

/**
 * Visitor to extract or update xpath expressions stored in different types of
 * XSL elements
 *
 */
public class SetExpressionVisitor extends XslVisitorAdapter{
    
    
    
    
    private XPathExpression expression;
    
    
    public SetExpressionVisitor(XPathExpression expression) {
        this.expression = expression;
        
    }
    
    
    public String getExpressionString() {
        return (expression != null) ? expression.getExpressionString() : "";
    }
    
    
    public void visit(ValueOf vof){
        vof.setSelect(getExpressionString());
    }
    
    
    public void visit(If iff) {
        iff.setTest(getExpressionString());
    }
    
    public void visit(CopyOf cof){
        cof.setSelect(getExpressionString());
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
        ValueOf vof = GetExpressionVisitor.isValueOfContainer(component);
        if (vof == null){
            XslComponentFactory factory = component.getModel().getFactory();
            vof = factory.createValueOf();
            ((SequenceConstructor) component).appendSequenceChild(vof);
            
        }
        vof.setSelect(getExpressionString());
        
    }
    
    public void visit(ForEach forEach) {
            forEach.setSelect(getExpressionString());
       
    }
    
    public void visit(When when) {
            when.setTest(getExpressionString());
        
    }
    
    
}
