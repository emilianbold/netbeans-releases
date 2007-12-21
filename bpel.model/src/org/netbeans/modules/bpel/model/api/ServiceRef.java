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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * <p>
 * Java class for ServiceRefType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *      &lt;xsd:element name="service-ref" type="sref:ServiceRefType">
 *              &lt;xsd:annotation>
 *                      &lt;xsd:documentation>
 *                              This element can be used within a from-spec.
 *                      &lt;/xsd:documentation>
 *              &lt;/xsd:annotation>
 *      &lt;/xsd:element>
 *      &lt;xsd:complexType name="ServiceRefType">
 *              &lt;xsd:annotation>
 *                      &lt;xsd:documentation>
 *                              This type definition is for service reference container.
 *                              This container is used as envelope to wrap around the actual endpoint reference value,
 *                              when a BPEL process interacts the endpoint reference of a partnerLink.
 *                              It provides pluggability of different versions of service referencing schemes
 *                              being used within a BPEL program. The design pattern here is similar to those of
 *                              expression language.
 *                      &lt;/xsd:documentation>
 *              &lt;/xsd:annotation>
 *              &lt;xsd:sequence>
 *                      &lt;xsd:any namespace="##other" processContents="lax"/>
 *              &lt;/xsd:sequence>
 *              &lt;xsd:attribute name="reference-scheme" type="xsd:anyURI"/>
 *      &lt;/xsd:complexType>
 * </pre>
 * 
 * @author ads
 */
public interface ServiceRef extends BpelContainer {
    
    String SERVICE_REF_NS   = 
        "http://docs.oasis-open.org/wsbpel/2.0/serviceref";     // NOI18N

    String REFERENCE_SCHEME = "reference-scheme";               // NOI18N
    
    /**
     * @return "reference-scheme" attribute value.
     */
    String getReferenceScheme();
    
    /**
     * Setter for "reference-scheme" attribute.
     * @param value New "reference-scheme" attribute value.
     * @throws VetoException {@link VetoException} will be thrown if <code>value</code>
     * is not acceptable here.
     */
    void setReferenceScheme( String value ) throws VetoException ;
    
    /**
     * Removes "reference-scheme" attribute.
     */
    void removeReferenceScheme();
}
