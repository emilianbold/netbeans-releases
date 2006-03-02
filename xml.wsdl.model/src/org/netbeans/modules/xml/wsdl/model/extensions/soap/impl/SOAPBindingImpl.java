/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SOAPBindingImpl extends SOAPComponentImpl implements SOAPBinding{
    
    /** Creates a new instance of SOAPBindingImpl */
    public SOAPBindingImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPBindingImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.BINDING.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setTransportURI(String transportURI) {
        setAttribute(TRANSPORT_URI_PROPERTY, SOAPAttribute.TRANSPORT_URI, transportURI);
    }
    
    public String getTransportURI() {
        return getAttribute(SOAPAttribute.TRANSPORT_URI);
    }
    
    public void setStyle(Style style) {
        setAttribute(STYLE_PROPERTY, SOAPAttribute.STYLE, style);
    }
    
    public Style getStyle() {
        String s = getAttribute(SOAPAttribute.STYLE);
        return s == null ? null : Style.valueOf(s.toUpperCase());
    }

    private Style getStyleValueOf(String s) {
        return s == null ? null : Style.valueOf(s.toUpperCase());
    }
    
    protected Object getAttributeValueOf(SOAPAttribute attr, String s) {
        if (attr == SOAPAttribute.STYLE) {
            return getStyleValueOf(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }
}
