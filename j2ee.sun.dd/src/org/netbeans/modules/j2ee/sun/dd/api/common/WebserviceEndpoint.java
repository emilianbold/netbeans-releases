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
 * WebserviceEndpoint.java
 *
 * Created on November 17, 2004, 5:21 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface WebserviceEndpoint extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String VERSION_SERVER_8_0 = "Server 8.0";
    public static final String VERSION_SERVER_8_1 = "Server 8.1";

    public static final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
    public static final String ENDPOINT_ADDRESS_URI = "EndpointAddressUri";	// NOI18N
    public static final String LOGIN_CONFIG = "LoginConfig";	// NOI18N
    public static final String TRANSPORT_GUARANTEE = "TransportGuarantee";	// NOI18N
    public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
    public static final String TIE_CLASS = "TieClass";	// NOI18N
    public static final String SERVLET_IMPL_CLASS = "ServletImplClass";	// NOI18N
    public static final String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding";	// NOI18N
    public static final String DEBUGGING_ENABLED = "DebuggingEnabled";	// NOI18N
    public static final String PROPERTY = "PropertyElement";	// NOI18N

    /** Setter for port-component-name property
     * @param value property value
     */
    public void setPortComponentName(java.lang.String value);
    /** Getter for port-component-name property.
     * @return property value
     */
    public java.lang.String getPortComponentName();
    
    /** Setter for endpoint-address-uri property
     * @param value property value
     */
    public void setEndpointAddressUri(java.lang.String value);
    /** Getter for endpoint-address-uri property.
     * @return property value
     */
    public java.lang.String getEndpointAddressUri();
    
    /** Setter for login-config property
     * @param value property value
     */
    public void setLoginConfig(LoginConfig value);
    /** Getter for login-config property.
     * @return property value
     */
    public LoginConfig getLoginConfig();
    
    public LoginConfig newLoginConfig();
    
    /** Setter for transport-guarantee property
     * @param value property value
     */
    public void setTransportGuarantee(java.lang.String value);
    /** Getter for transport-guarantee property.
     * @return property value
     */
    public java.lang.String getTransportGuarantee();
    
    
    /** Setter for service-qname property
     * @param value property value
     */
    public void setServiceQname(ServiceQname value);
    /** Getter for service-qname property.
     * @return property value
     */
    public ServiceQname getServiceQname(); 
    
    public ServiceQname newServiceQname();

    /** Setter for tie-class property
     * @param value property value
     */
    public void setTieClass(java.lang.String value);
    /** Getter for tie-class property.
     * @return property value
     */
    public java.lang.String getTieClass();
    
    /** Setter for servlet-impl-class property
     * @param value property value
     */
    public void setServletImplClass(java.lang.String value);
    /** Getter for servlet-impl-class property.
     * @return property value
     */
    public java.lang.String getServletImplClass();
    
    //For AppServer 8.1 & 9.0
    /** Setter for message-security-binding property
     * @param value property value
     */
    public void setMessageSecurityBinding(MessageSecurityBinding value) throws VersionNotSupportedException; 
    /** Getter for message-security-binding property.
     * @return property value
     */
    public MessageSecurityBinding getMessageSecurityBinding() throws VersionNotSupportedException; 
    
    public MessageSecurityBinding newMessageSecurityBinding() throws VersionNotSupportedException; 
    
    //For Appserver 9.0
    public void setDebuggingEnabled(String value) throws VersionNotSupportedException; 
   
    public String getDebuggingEnabled() throws VersionNotSupportedException; 
     
    public PropertyElement[] getPropertyElement() throws VersionNotSupportedException;
    public PropertyElement getPropertyElement(int index) throws VersionNotSupportedException;
    public void setPropertyElement(PropertyElement[] value) throws VersionNotSupportedException;
    public void setPropertyElement(int index, PropertyElement value) throws VersionNotSupportedException;
    public int addPropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public int removePropertyElement(PropertyElement value) throws VersionNotSupportedException;
    public int sizePropertyElement() throws VersionNotSupportedException;
    public PropertyElement newPropertyElement() throws VersionNotSupportedException;
    
}
