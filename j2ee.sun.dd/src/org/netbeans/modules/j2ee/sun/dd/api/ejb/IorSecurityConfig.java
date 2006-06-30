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
 * IorSecurityConfig.java
 *
 * Created on November 17, 2004, 5:16 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface IorSecurityConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String TRANSPORT_CONFIG = "TransportConfig";	// NOI18N
    public static final String AS_CONTEXT = "AsContext";	// NOI18N
    public static final String SAS_CONTEXT = "SasContext";	// NOI18N

    /** Setter for transport-config property
     * @param value property value
     */
    public void setTransportConfig(TransportConfig value);
    /** Getter for transport-config property.
     * @return property value
     */
    public TransportConfig getTransportConfig(); 
        
    public TransportConfig newTransportConfig();
    /** Setter for as-context property
     * @param value property value
     */
    public void setAsContext(AsContext value);
    /** Getter for as-context property.
     * @return property value
     */
    public AsContext getAsContext();  
    
    public AsContext newAsContext();
    /** Setter for sas-context property
     * @param value property value
     */
    public void setSasContext(SasContext value); 
    /** Getter for sas-context property.
     * @return property value
     */
    public SasContext getSasContext(); 
        
    public SasContext newSasContext();
}
