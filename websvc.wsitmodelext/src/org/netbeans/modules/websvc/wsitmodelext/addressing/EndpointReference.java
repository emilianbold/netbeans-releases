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

package org.netbeans.modules.websvc.wsitmodelext.addressing;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface EndpointReference extends ExtensibilityElement {
    public static final String ADDRESS_PROPERTY = "ADDRESS";            //NOI18N
    public static final String REFERENCE_PROPERTIES_PROPERTY = "REFERENCE_PROPERTIES_INTERVAL";     //NOI18N
    public static final String REFERENCE_PARAMETERS_PROPERTY = "REFERENCE_PARAMETERS";              //NOI18N
    public static final String ADDRESSING_PORTTYPE_PROPERTY = "PORTTYPE";          //NOI18N
    public static final String SERVICENAME_PROPERTY = "SERVICENAME";    //NOI18N
    
    ReferenceProperties getReferenceProperties();
    void setReferenceProperties(ReferenceProperties referenceProperties);
    void removeReferenceProperties(ReferenceProperties referenceProperties);

    ReferenceParameters getReferenceParameters();
    void setReferenceParameters(ReferenceParameters referenceParameters);
    void removeReferenceParameters(ReferenceParameters referenceParameters);

    Address getAddress();
    void setAddress(Address address);
    void removeAddress(Address address);
    
    AddressingPortType getPortType();
    void setPortType(AddressingPortType portType);
    void removePortType(AddressingPortType portType);

    AddressingServiceName getServiceName();
    void setServiceName(AddressingServiceName serviceName);
    void removeServiceName(AddressingServiceName serviceName);
}
