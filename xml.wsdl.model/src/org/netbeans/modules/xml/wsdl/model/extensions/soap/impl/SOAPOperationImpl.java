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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
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
    
    public void accept(SOAPComponent.Visitor visitor) {
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
                    Style sbStyle = sb.getStyle();
                    if (sbStyle != null) {
                        return sbStyle;
                    }
                }
            }
            return Style.DOCUMENT;
        }

        return Style.valueOf(s.toUpperCase());

    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingOperation) {
            return true;
        }
        return false;
    }
}
