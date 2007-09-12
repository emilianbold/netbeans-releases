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

import java.util.List;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xslt.model.Attribute;
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
