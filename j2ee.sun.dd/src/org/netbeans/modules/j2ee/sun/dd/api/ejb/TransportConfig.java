/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
