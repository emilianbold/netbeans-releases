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
package org.netbeans.modules.xslt.mapper.model.targettree;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils.PathItem;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.Instruction;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
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
                AttributeValueTemplate atv = attribute.getName();
                if (atv != null){
                    isMatching =  compareName(atv.getQName());
                }
            }
        }
        
        public void visit(org.netbeans.modules.xslt.model.Element element) {
            if (axic instanceof org.netbeans.modules.xml.axi.Element){
                AttributeValueTemplate atv = element.getName();
                if (atv != null){
                    isMatching =  compareName(atv.getQName());
                }
            }
        }
        
        
        public void visit(org.netbeans.modules.xslt.model.LiteralResultElement element) {
            if (axic instanceof org.netbeans.modules.xml.axi.Element){
                QName qname = element.getQName();
                isMatching = compareName(qname);
            }
        }
        
        private boolean compareName(QName qname){
            if (qname == null){
                return false;
            }
            //
            if (AxiomUtils.isUnqualified(axic)) {
                return qname.getLocalPart().equals(((AXIType) axic).getName());
            } else {
                return qname.getLocalPart().equals(((AXIType) axic).getName()) &&
                        qname.getNamespaceURI().equals(axic.getTargetNamespace());
            }
        }
        
    }
    
    /**
     * Call visitor for all children of type Attribute and Element
     **/
    public static abstract class ElementVisitor {
        public abstract void visit(AXIComponent component);
        public void visitSubelements(AXIComponent axic){
            if (axic instanceof Element){
                visitSubelements((Element) axic); 
            } else if (axic instanceof AXIDocument){
                visitSubelements((AXIDocument) axic); 
            }
        }
        
        protected void visitSubelements(Element element){
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
        
        protected void visitSubelements(AXIDocument doc){
            
            for (AbstractElement e : doc.getChildElements()){
                if (e instanceof Element){
                    visit(e);
                }
            }
            
            
            
        }
        
    }
    
    
    public static List<AXIComponent> getChildTypes(AXIComponent axic)  {
        
        final List<AXIComponent> result = new ArrayList<AXIComponent>();
        
        if (axic != null) {
            
            new AXIUtils.ElementVisitor(){
                public void visit(AXIComponent c){
                    result.add(c);
                }
            }.visitSubelements(axic);
        }
        
        return result;
        
    }
    
    /**
     * Prepares XPath for the specified Schema node.
     */
    public static List<PathItem> prepareSimpleXPath(final SchemaNode schemaNode) {
        //
        // Collects Path Items first
        ArrayList<PathItem> path = new ArrayList<PathItem>();
        TreeNode currNode = schemaNode;
        SchemaNode lastProcessedSchemaNode = null;
        while (currNode != null && currNode instanceof SchemaNode) {
            lastProcessedSchemaNode = (SchemaNode)currNode;
            if (currNode instanceof PredicatedSchemaNode) {
                PredicatedSchemaNode psn = (PredicatedSchemaNode)currNode;
                String pred = psn.getPredicatedAxiComp().getPredicatesText();
                AxiomUtils.processNode(lastProcessedSchemaNode.getType(), pred, path);
            } else {
                AxiomUtils.processNode(lastProcessedSchemaNode.getType(), null, path);
            }
            //
            currNode = currNode.getParent();
        }
        //
        // Add parent elements to ensure that the XPath would be absolute
        if (lastProcessedSchemaNode != null) {
            AXIComponent axiComponent = lastProcessedSchemaNode.getType();
            if (axiComponent != null) {
                AXIComponent parentAxiComponent = axiComponent.getParent();
                while (true) {
                    if (parentAxiComponent == null) {
                        break;
                    }
                    //
                    AxiomUtils.processNode(parentAxiComponent, null, path);
                    //
                    parentAxiComponent = parentAxiComponent.getParent();
                }
            }
        }
        //
        return path;
    }
    
    public static AXIComponent getType(XslComponent xslc, XsltMapper mapper){
        if (xslc == null){
            return null;
        }
        XslComponent xsl_parent = xslc.getParent();
        
        if (xslc instanceof org.netbeans.modules.xslt.model.Element ||
            xslc instanceof org.netbeans.modules.xslt.model.Attribute ) {
            AXIComponent axi_parent = getType(xsl_parent, mapper);
            if (axi_parent != null){
                for (AXIComponent type: axi_parent.getChildElements()){
                    if (type == null || 
                        type.getPeer() == null || 
                        type.getPeer().getModel() == null) {
                        continue;
                    }
                    if (AXIUtils.isSameSchemaType(xslc, type)){
                        return type;
                    }
                }
            }
        } else if (xslc instanceof org.netbeans.modules.xslt.model.Template){ //no declaration nodes fond downtree
            AXIComponent targetType = mapper.getContext().getTargetType();
            return targetType != null ? targetType.getModel().getRoot() : null;
        }  else if (xsl_parent != null) {
            return getType(xsl_parent, mapper);
        }
        return null;
    }
    
}
