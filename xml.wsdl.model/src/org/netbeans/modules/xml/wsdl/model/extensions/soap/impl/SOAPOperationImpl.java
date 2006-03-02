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
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.Util;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPOperationImpl extends SOAPComponentImpl implements SOAPOperation {
    
    /** Creates a new instance of SOAPOperationImpl */
    public SOAPOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.OPERATION.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setSoapAction(String soapActionURI) {
        setAttribute(SOAP_ACTION_PROPERTY, SOAPAttribute.SOAP_ACTION, soapActionURI);
    }

    public String getSoapAction() {
        return getAttribute(SOAPAttribute.SOAP_ACTION);
    }
   
    public void setStyle(Style v) {
        setAttribute(STYLE_PROPERTY, SOAPAttribute.STYLE, v);
    }

    public Style getStyle() {
        String s = getAttribute(SOAPAttribute.STYLE);
        if (s == null) {
            WSDLComponent ancestor = getParent() == null? null : getParent().getParent();
            if (ancestor instanceof Binding) {
                Binding b = (Binding) ancestor;
                Collection<SOAPBinding> sbs = b.getExtensibilityElements(SOAPBinding.class);
                if (sbs.size() > 0) {
                    SOAPBinding sb = sbs.iterator().next();
                     return sb.getStyle();
                }
            }
        }
        return Style.DOCUMENT;
    }
}
