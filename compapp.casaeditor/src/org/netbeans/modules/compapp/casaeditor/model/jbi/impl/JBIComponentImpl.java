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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ExtensibilityElement;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public abstract class JBIComponentImpl extends AbstractDocumentComponent<JBIComponent>
        implements JBIComponent {
    
    /**
     * Creates a new instance of JBIComponentImpl
     */
    public JBIComponentImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public JBIModel getModel() {
        return (JBIModel) super.getModel();
    }
    
    protected void populateChildren(List<JBIComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            JBIModel model = getModel();
            JBIComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    JBIComponent comp = componentFactory.create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    public static Element createElementNS(JBIModel model, JBIQNames rq) {
        return model.getDocument().createElementNS(rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
    // FIXME
    protected static Element createNewElement(JBIModel model, QName qName){
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
    }
    
    protected static Element createPrefixedElement(JBIModel model, QName qName) {
        String qualified = qName.getPrefix() == null ? qName.getLocalPart() :
            qName.getPrefix() + Constants.COLON_STRING + qName.getLocalPart();
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qualified);
    }
    
    protected Object getAttributeValueOf(Attribute attribute, String stringValue) {
        return stringValue;
    }
    
    // Support for mapping elements of simple type to attributes
    // The following will be removed when Nam's fix in NetBeans trunk (11/30) is 
    // merged back into 5.5.1
    protected void setChildElementText(String propertyName, String text, QName qname) {
        verifyWrite();
        Element childElement = getChildElement(qname);
        String oldVal = childElement == null ? null : getText(childElement);
        if (childElement == null) {
            childElement =
                    getModel().getDocument().createElementNS(qname.getNamespaceURI(),
                    qname.getLocalPart());
            getModel().getAccess().appendChild(getPeer(), childElement, this);
        }
        getModel().getAccess().setText(childElement, text, this);
        firePropertyChange(propertyName, oldVal, text);
        fireValueChanged();
    }    
    
    // TMP
    public QName getQName(String prefixedName) {
        assert prefixedName != null;
        
        String localPart;
        String namespaceURI;
        String prefix;
        
        int colonIndex = prefixedName.indexOf(Constants.COLON_STRING);
        if (colonIndex != -1) {
            prefix = prefixedName.substring(0, colonIndex);
            localPart = prefixedName.substring(colonIndex + 1);
            namespaceURI = getPeer().lookupNamespaceURI(prefix);
        } else {
            prefix = Constants.EMPTY_STRING;
            localPart = prefixedName;
            namespaceURI = Constants.EMPTY_STRING;
        }
        
        return new QName(namespaceURI, localPart, prefix);
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
