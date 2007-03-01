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

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPFaultImpl extends SOAPMessageBaseImpl implements SOAPFault {
    
    /** Creates a new instance of SOAPFaultImpl */
    public SOAPFaultImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPFaultImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.FAULT.getQName(), model));
    }
    
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, SOAPAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(SOAPAttribute.NAME);
    }

    public void setFault(Reference<Fault> fault) {
        Fault f = fault.get();
        setName(f == null ? null : f.getName());
    }

    public Reference<Fault> getFault() {
        String v = getName();
        return v == null ? null : new FaultReference(this, v); 
    }


    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingFault) {
            return true;
        }
        return false;
    }
}
