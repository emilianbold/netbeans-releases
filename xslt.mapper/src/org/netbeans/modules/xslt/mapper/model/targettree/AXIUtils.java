/*
 * AXIUtils.java
 *
 * Created on 16 январь 2007 г., 15:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;

/**
 *
 * @author Alexey
 */
public class AXIUtils {
    
    /**
     * Checks if XSL component this node represents creates an element in output tree of given schema type
     * @returns true if types are the same
     **/
    public static boolean isSameSchemaType(XslComponent xslc, AXIComponent axic){
        TypeCheckVisitor visitor = new TypeCheckVisitor(axic);
        xslc.accept(visitor);
        return visitor.isMatching();
    }

    public static class TypeCheckVisitor extends XslVisitorAdapter{
        private AXIComponent axic;
        private boolean isMatching = false;
        public TypeCheckVisitor(AXIComponent axic){
            this.axic = axic;
        }
        public boolean isMatching(){
            return isMatching;
        }
        
        public void visit(org.netbeans.modules.xslt.model.Attribute attribute) {
            if (axic instanceof org.netbeans.modules.xml.axi.Attribute){
                isMatching =  compareName(attribute.getName().toString());
            }
        }

        public void visit(org.netbeans.modules.xslt.model.Element element) {
            if (axic instanceof org.netbeans.modules.xml.axi.Element){
                isMatching =  compareName(element.getName().toString());
            }
        }


        public void visit(org.netbeans.modules.xslt.model.LiteralResultElement element) {
               if (axic instanceof org.netbeans.modules.xml.axi.Element){
                isMatching = compareName(element.getQName().toString());
            }
        }
        private boolean compareName(String name){
            return name.equals(((AXIType) axic).getName());
        }
    }
    /**
     * Call visitor for all children of type Attribute and Element
     **/
    
    
    public static abstract class ElementVisitor {
        public abstract void visit(AXIComponent component);
        public void visitSubelements(Element element){
            for (AbstractAttribute a : element.getAttributes()){
                if (a instanceof Attribute){
                    visit(a);
                }
            }
            
            for (AbstractElement e : element.getChildElements()){
                if (e instanceof Element){
                    visit(e);
                }
            }
            
            
            
        }
        
    }
    
}
