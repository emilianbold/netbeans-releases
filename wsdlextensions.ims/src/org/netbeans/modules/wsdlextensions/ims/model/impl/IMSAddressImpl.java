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

package org.netbeans.modules.wsdlextensions.ims.model.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.wsdlextensions.ims.model.IMSAddress;
import org.netbeans.modules.wsdlextensions.ims.model.IMSComponent;
import org.netbeans.modules.wsdlextensions.ims.model.IMSQName;

import org.w3c.dom.Element;

public class IMSAddressImpl extends IMSComponentImpl implements IMSAddress {
    public IMSAddressImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public IMSAddressImpl(WSDLModel model){
        this(model, createPrefixedElement(IMSQName.ADDRESS.getQName(), model));
    }
    
    public void accept(IMSComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setImsServerLocation(String url){
        setAttribute(IMSAddress.IMS_SERVER_LOCATION, IMSAttribute.IMS_SERVER_LOCATION, url);
    }

    public String getImsServerLocation(){
        return getAttribute(IMSAttribute.IMS_SERVER_LOCATION);
    }
}
