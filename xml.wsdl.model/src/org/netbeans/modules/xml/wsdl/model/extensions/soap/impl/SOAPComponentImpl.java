/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public abstract class SOAPComponentImpl extends WSDLComponentBase {
    
    /** Creates a new instance of SOAPComponentImpl */
    public SOAPComponentImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    protected String getNamespaceURI() {
        return SOAPQName.SOAP_NS_URI;
    }
}
