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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.impl.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.SchemaReferenceImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author rico
 * @author Nam Nguyen
 */
public abstract class WSDLComponentBase extends AbstractDocumentComponent<WSDLComponent> implements WSDLComponent {

    /** Creates a new instance of WSDLComponentImpl */
    public WSDLComponentBase(WSDLModel model, org.w3c.dom.Element e) {
        super((WSDLModelImpl) model, e);
    }
    
    public WSDLModel getModel() {
        return (WSDLModel) super.getModel();
    }
    
    protected void populateChildren(List<WSDLComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    WSDLModel wmodel = getModel();
                    WSDLComponentBase comp = (WSDLComponentBase) wmodel.getFactory().create((Element)n,this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    protected static org.w3c.dom.Element createNewElement(QName qName, WSDLModel model){
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, WSDLModel model){
        String qualified = qName.getPrefix() == null ? qName.getLocalPart() : 
            qName.getPrefix() + ":" + qName.getLocalPart();
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qualified);
    }
    
    public void setDocumentation(Documentation doc) {
        setChildBefore(Documentation.class, DOCUMENTATION_PROPERTY, doc, TypeCollection.ALL.types());
    }
    
    public Documentation getDocumentation() {
        return getChild(Documentation.class);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }
    
    protected <T extends ReferenceableWSDLComponent> NamedComponentReference<T> resolveGlobalReference(
            Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    public WSDLModel getWSDLModel() {
        return getModel();
    }
    
    public <T extends ReferenceableWSDLComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type) {
        return new GlobalReferenceImpl<T>(target, type, this);
    }
    
    protected <T extends ReferenceableSchemaComponent>
            NamedComponentReference<T> resolveSchemaReference(Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new SchemaReferenceImpl<T>(c, this, v);
    }
    
    public <T extends ReferenceableSchemaComponent> 
            NamedComponentReference<T> createSchemaReference(T target, Class<T> type) {
        return new SchemaReferenceImpl<T>( target, type, this);
    }
    
    public String toString(QName qname) {
        return getPrefixedName(qname.getNamespaceURI(), qname.getLocalPart());
    }

    public void removeExtensibilityElement(ExtensibilityElement ee) {
        removeChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }

    public void addExtensibilityElement(ExtensibilityElement ee) {
        appendChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return getChildren(ExtensibilityElement.class);
    }
    
    public <T extends ExtensibilityElement> List<T> getExtensibilityElements(Class<T> type) {
        return getChildren(type);
    }

    public boolean canPaste(Component child) {
        if (child instanceof DocumentComponent) {
            return new ChildComponentUpdateVisitor().canAdd(this, (DocumentComponent) child);
        } else {
            return false;
        }
    }
}
