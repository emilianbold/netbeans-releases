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


package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
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
    
    public void accept(SOAPComponent.Visitor visitor) {
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


    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof Binding) {
            return true;
        }
        return false;
    }
}
