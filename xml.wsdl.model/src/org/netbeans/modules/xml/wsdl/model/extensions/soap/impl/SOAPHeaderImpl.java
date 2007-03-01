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
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
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
    
    public void accept(SOAPComponent.Visitor visitor) {
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

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingInput || target instanceof BindingOutput) {
            return true;
        }
        return false;
    }
}
