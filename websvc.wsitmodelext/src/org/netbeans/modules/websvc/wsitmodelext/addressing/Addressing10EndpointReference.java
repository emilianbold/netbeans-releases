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
public interface Addressing10EndpointReference extends ExtensibilityElement {
    public static final String ADDRESS_PROPERTY = "ADDRESS";            //NOI18N
    public static final String REFERENCE_PROPERTIES_PROPERTY = "REFERENCE_PROPERTIES_INTERVAL";     //NOI18N
    public static final String METADATA_PROPERTY = "METADATA_PARAMETERS";              //NOI18N
    
    Addressing10ReferenceProperties getReferenceProperties();
    void setReferenceProperties(Addressing10ReferenceProperties referenceProperties);
    void removeReferenceProperties(Addressing10ReferenceProperties referenceProperties);

    Addressing10Metadata getAddressing10Metadata();
    void setAddressing10Metadata(Addressing10Metadata addressingMetadata);
    void removeAddressing10Metadata(Addressing10Metadata addressingMetadata);

    Address10 getAddress();
    void setAddress(Address10 address);
    void removeAddress(Address10 address);
    
}
