/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPHeaderFaultImpl extends SOAPHeaderBaseImpl implements SOAPHeaderFault {
    
    /** Creates a new instance of SOAPHeaderFaultImpl */
    public SOAPHeaderFaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPHeaderFaultImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.BINDING.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
   
}
