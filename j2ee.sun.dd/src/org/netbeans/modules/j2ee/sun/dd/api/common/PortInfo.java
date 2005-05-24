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
    
    //For AppServer 8.1
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
