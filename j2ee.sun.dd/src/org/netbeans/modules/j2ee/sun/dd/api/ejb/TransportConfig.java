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
/*
 * TransportConfig.java
 *
 * Created on November 18, 2004, 10:02 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface TransportConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String INTEGRITY = "Integrity";	// NOI18N
    public static final String CONFIDENTIALITY = "Confidentiality";	// NOI18N
    public static final String ESTABLISH_TRUST_IN_TARGET = "EstablishTrustInTarget";	// NOI18N
    public static final String ESTABLISH_TRUST_IN_CLIENT = "EstablishTrustInClient";	// NOI18N
        
    /** Setter for integrity property
     * @param value property value
     */
    public void setIntegrity(java.lang.String value);
    /** Getter for integrity property.
     * @return property value
     */
    public java.lang.String getIntegrity();
    /** Setter for confidentiality property
     * @param value property value
     */
    public void setConfidentiality(java.lang.String value);
    /** Getter for confidentiality property.
     * @return property value
     */
    public java.lang.String getConfidentiality();
    /** Setter for establish-trust-in-target property
     * @param value property value
     */
    public void setEstablishTrustInTarget(java.lang.String value);
    /** Getter for establish-trust-in-target property.
     * @return property value
     */
    public java.lang.String getEstablishTrustInTarget();
    /** Setter for establish-trust-in-client property
     * @param value property value
     */
    public void setEstablishTrustInClient(java.lang.String value);
    /** Getter for establish-trust-in-client property.
     * @return property value
     */
    public java.lang.String getEstablishTrustInClient();
    
}
