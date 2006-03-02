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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public abstract class SOAPMessageBaseImpl extends SOAPComponentImpl implements SOAPMessageBase {
    
    /** Creates a new instance of SOAPMessageBaseImpl */
    public SOAPMessageBaseImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public Use getUse() {
        String s = getAttribute(SOAPAttribute.USE);
        return s == null ? null : getUseValueOf(s);
    }
    
    public void setUse(Use use) {
        setAttribute(USE_PROPERTY, SOAPAttribute.USE, use);
    }

    private Use getUseValueOf(String s) {
        return s == null ? null : Use.valueOf(s.toUpperCase());
    }
    
    protected Object getAttributeValueOf(SOAPAttribute attr, String s) {
        if (attr == SOAPAttribute.USE) {
            return getUseValueOf(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }
    
    public String getNamespaceURI() {
        return getAttribute(SOAPAttribute.NAMESPACE);
    }

    public void setNamespaceURI(String namespaceURI) {
        setAttribute(NAMESPACE_PROPERTY, SOAPAttribute.NAMESPACE, namespaceURI);
    }

    public void removeEncodingStyle(String encodingStyle) {
        Collection<String> styles = getEncodingStyles();
        styles.remove(encodingStyle);
        setAttribute(ENCODING_STYLE_PROPERTY, SOAPAttribute.ENCODING_STYLE, Util.toString(styles));
    }

    public void addEncodingStyle(String encodingStyle) {
        Collection<String> styles = getEncodingStyles();
        styles.add(encodingStyle);
        setAttribute(ENCODING_STYLE_PROPERTY, SOAPAttribute.ENCODING_STYLE, Util.toString(styles));
    }

    public java.util.Collection<String> getEncodingStyles() {
        String s = getAttribute(SOAPAttribute.ENCODING_STYLE);
        return s == null ? null : Util.parse(s);
    }
}
