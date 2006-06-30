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
 * PortInfo.java
 *
 * Created on November 18, 2004, 9:46 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PortInfo extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String VERSION_SERVER_8_0 = "Server 8.0";

    public static final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
    public static final String WSDL_PORT = "WsdlPort";	// NOI18N
    public static final String STUB_PROPERTY = "StubProperty";	// NOI18N
    public static final String CALL_PROPERTY = "CallProperty";	// NOI18N
    public static final String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding";	// NOI18N

        
    /** Setter for service-endpoint-interface property
     * @param value property value
     */
    public void setServiceEndpointInterface(java.lang.String value);
    /** Getter for service-endpoint-interface property.
     * @return property value
     */
    public java.lang.String getServiceEndpointInterface();
    /** Setter for wsdl-port property
     * @param value property value
     */
    public void setWsdlPort(WsdlPort value);
    /** Getter for wsdl-port property.
     * @return property value
     */
    public WsdlPort getWsdlPort(); 
    public WsdlPort newWsdlPort(); 
    
    public StubProperty[] getStubProperty(); 
    public StubProperty getStubProperty(int index);
    public void setStubProperty(StubProperty[] value);
    public void setStubProperty(int index, StubProperty value);
    public int addStubProperty(StubProperty value);
    public int removeStubProperty(StubProperty value);
    public int sizeStubProperty();
    public StubProperty newStubProperty();
    
    public CallProperty[] getCallProperty(); 
    public CallProperty getCallProperty(int index);
    public void setCallProperty(CallProperty[] value);
    public void setCallProperty(int index, CallProperty value);
    public int addCallProperty(CallProperty value);
    public int removeCallProperty(CallProperty value);
    public int sizeCallProperty();
    public CallProperty newCallProperty();
    
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
}
