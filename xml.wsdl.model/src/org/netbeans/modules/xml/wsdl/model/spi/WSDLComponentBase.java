/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.impl.*;
import org.netbeans.modules.xml.xam.AbstractComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.SchemaReferenceImpl;
import org.netbeans.modules.xml.xam.Attribute;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.visitor.PositionFinderVisitor;
import org.w3c.dom.NodeList;

/**
 * @author rico
 * @author Name Nguyen
 */
public abstract class WSDLComponentBase extends AbstractComponent<WSDLComponent, WSDLModel> implements WSDLComponent {

    /** Creates a new instance of WSDLComponentImpl */
    public WSDLComponentBase(WSDLModel model, org.w3c.dom.Element e) {
        super(model, e);
    }
    
    /**
     * ExtensibilityElement subclasses need to override for exact namespace.
     */
    protected String getNamespaceURI() {
        return WSDLQNames.WSDL_NS_URI;
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
        return model.getDocument().createElementNS(
                qName.getNamespaceURI(),
                qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, WSDLModel model){
        org.w3c.dom.Element e = createNewElement(qName, model);
        e.setPrefix(qName.getPrefix());
        return e;
    }
    
    public void setDocumentation(Documentation doc) {
        addBefore(DOCUMENTATION_PROPERTY, doc, TypeCollection.ALL.types());
    }
    
    public Documentation getDocumentation() {
        return getChild(Documentation.class);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }
    
    public int findPosition() {
        PositionFinderVisitor v = new PositionFinderVisitor();
        return v.findPosition((Document)getModel().getDocument(), (Element)getPeer());
    }
    
    protected <T extends ReferenceableWSDLComponent> GlobalReferenceImpl<T> resolveGlobalReference(
            Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    protected <T extends ReferenceableWSDLComponent> void setGlobalReference(String propertyName, 
            Attribute attr, GlobalReference<T> t){
        String value = t.getDeclaredURI();
        setAttribute(propertyName, attr, value);
    }
    
    protected String getPrefix(String namespace){
        String prefix = ((WSDLComponentBase)this.getWSDLModel().getDefinitions() ).lookupPrefix(namespace);
        return prefix == null ? "" : prefix;
    }

    protected String getPrefixedName(String namespace, String localName) {
        String prefix = getPrefix(namespace);
        return prefix.length() == 0 ? localName : prefix+':'+localName; //NOI18N
    }

    public WSDLModel getWSDLModel() {
        return getModel();
    }
    
    public <T extends ReferenceableWSDLComponent> GlobalReference<T> createReferenceTo(T target, Class<T> type) {
        return new GlobalReferenceImpl<T>(target, type, this);
    }
    
    protected <T extends ReferenceableSchemaComponent>
            SchemaReferenceImpl<T> resolveSchemaReference(Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new SchemaReferenceImpl<T>(c, this, v);
    }
    
    protected <T extends ReferenceableSchemaComponent>
            void setSchemaReference(String propertyName, Attribute attr, GlobalReference<T> t) {
        String v = (t == null) ? null : getPrefixedName(t.getEffectiveNamespace(), t.get().getName());
        setAttribute(propertyName, attr, v);
    }
    
    public <T extends ReferenceableSchemaComponent> 
            GlobalReference<T> createSchemaReference(T target, Class<T> type) {
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
}
