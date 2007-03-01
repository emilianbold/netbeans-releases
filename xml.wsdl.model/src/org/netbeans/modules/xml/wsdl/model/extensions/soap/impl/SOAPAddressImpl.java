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

import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLAttribute;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPAddressImpl extends SOAPComponentImpl implements SOAPAddress {
    
    /** Creates a new instance of SOAPAddressImpl */
    public SOAPAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.ADDRESS.getQName(), model));
    }
    
    public void accept(SOAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setLocation(String locationURI) {
        setAttribute(LOCATION_PROPERTY, WSDLAttribute.LOCATION, locationURI);
    }

    public String getLocation() {
        return getAttribute(WSDLAttribute.LOCATION);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof Port) {
            return true;
        }
        return false;
    }
}
