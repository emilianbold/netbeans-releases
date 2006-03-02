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
