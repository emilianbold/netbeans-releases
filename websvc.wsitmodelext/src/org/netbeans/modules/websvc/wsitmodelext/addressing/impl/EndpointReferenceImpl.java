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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.EndpointReference;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceParameters;
import org.netbeans.modules.websvc.wsitmodelext.addressing.ReferenceProperties;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingPortType;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingServiceName;

/**
 *
 * @author Martin Grebac
 */
public class EndpointReferenceImpl extends AddressingComponentImpl implements EndpointReference {
    
    /**
     * Creates a new instance of EndpointReferenceImpl
     */
    public EndpointReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public EndpointReferenceImpl(WSDLModel model){
        this(model, createPrefixedElement(AddressingQName.ENDPOINTREFERENCE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setAddress(Address address) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address.class, ADDRESS_PROPERTY, address, classes);
    }

    public Address getAddress() {
        return getChild(Address.class);
    }

    public void removeAddress(Address address) {
        removeChild(ADDRESS_PROPERTY, address);
    }

    public void setServiceName(AddressingServiceName serviceName) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingServiceName.class, SERVICENAME_PROPERTY, serviceName, classes);
    }

    public AddressingServiceName getServiceName() {
        return getChild(AddressingServiceName.class);
    }

    public void removeServiceName(AddressingServiceName serviceName) {
        removeChild(SERVICENAME_PROPERTY, serviceName);
    }
    
    public void setPortType(AddressingPortType addressingPortType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(AddressingPortType.class, ADDRESSING_PORTTYPE_PROPERTY, addressingPortType, classes);
    }

    public AddressingPortType getPortType() {
        return getChild(AddressingPortType.class);
    }

    public void removePortType(AddressingPortType portType) {
        removeChild(ADDRESSING_PORTTYPE_PROPERTY, portType);
    }

    public void setReferenceProperties(ReferenceProperties referenceProperties) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceProperties.class, REFERENCE_PROPERTIES_PROPERTY, referenceProperties, classes);
    }

    public ReferenceProperties getReferenceProperties() {
        return getChild(ReferenceProperties.class);
    }

    public void removeReferenceProperties(ReferenceProperties referenceProperties) {
        removeChild(REFERENCE_PROPERTIES_PROPERTY, referenceProperties);
    }

    public void setReferenceParameters(ReferenceParameters referenceParameters) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ReferenceParameters.class, REFERENCE_PARAMETERS_PROPERTY, referenceParameters, classes);
    }

    public ReferenceParameters getReferenceParameters() {
        return getChild(ReferenceParameters.class);
    }

    public void removeReferenceParameters(ReferenceParameters referenceParameters) {
        removeChild(REFERENCE_PARAMETERS_PROPERTY, referenceParameters);
    }
    
}
