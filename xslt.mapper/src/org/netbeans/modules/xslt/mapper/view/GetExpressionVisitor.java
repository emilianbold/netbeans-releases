/*
 * GetExpressionVisitor.java
 *
 * Created on 21 январь 2007 г., 18:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
 *
 * @author Alexey
 */
public class GetExpressionVisitor extends XslVisitorAdapter{
    
    private String result;
    private boolean isContainer = true;
    
    public GetExpressionVisitor() {
    }
    
    public XPathExpression getResult(){
        return this.result != null ?
            getExpression(result) :
            null;
    }
    
    public boolean isContaineer(){
        return isContainer;
    }
    
    
    public void visit(ValueOf vof){
        result = vof.getSelect();
    }
    
    
    
    private XPathExpression getExpression(String str){
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        try {
            return xpImpl.parseExpression(str);
        } catch (XPathException ex) {
            
        }
        
        return null;
    }
    
    public void visit(If iff) {
        result = iff.getTest();
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
            result = vof.getSelect();
            isContainer = false;
        }
    }
    
    public void visit(ForEach forEach) {
        result = forEach.getSelect();
    }
    
    public void visit(When when) {
        result = when.getTest();
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
