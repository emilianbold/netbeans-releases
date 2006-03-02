/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPHeaderImpl extends SOAPHeaderBaseImpl implements SOAPHeader {
    
    /** Creates a new instance of SOAPHeaderImpl */
    public SOAPHeaderImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPHeaderImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.HEADER.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void removeSOAPHeaderFault(SOAPHeaderFault soapHeaderFault) {
        removeChild(HEADER_FAULT_PROPERTY, soapHeaderFault);
    }

    public void addSOAPHeaderFault(SOAPHeaderFault soapHeaderFault) {
        appendChild(HEADER_FAULT_PROPERTY, soapHeaderFault);
    }

    public Collection<SOAPHeaderFault> getSOAPHeaderFaults() {
        return getChildren(SOAPHeaderFault.class);
    }
}
