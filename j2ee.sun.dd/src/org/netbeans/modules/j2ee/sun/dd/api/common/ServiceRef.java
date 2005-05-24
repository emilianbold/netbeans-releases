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
 * ServiceRef.java
 *
 * Created on November 17, 2004, 5:09 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServiceRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
    public static final String PORT_INFO = "PortInfo";	// NOI18N
    public static final String CALL_PROPERTY = "CallProperty";	// NOI18N
    public static final String WSDL_OVERRIDE = "WsdlOverride";	// NOI18N
    public static final String SERVICE_IMPL_CLASS = "ServiceImplClass";	// NOI18N
    public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
        
    /** Setter for service-ref-name property
     * @param value property value
     */
    public void setServiceRefName(java.lang.String value);
    /** Getter for service-ref-name property.
     * @return property value
     */
    public java.lang.String getServiceRefName();
    
    public PortInfo[] getPortInfo(); 
    public PortInfo getPortInfo(int index);
    public void setPortInfo(PortInfo[] value);
    public void setPortInfo(int index, PortInfo value);
    public int addPortInfo(PortInfo value);
    public int removePortInfo(PortInfo value);
    public int sizePortInfo();
    public PortInfo newPortInfo();
    
    public CallProperty[] getCallProperty(); 
    public CallProperty getCallProperty(int index);
    public void setCallProperty(CallProperty[] value);
    public void setCallProperty(int index, CallProperty value);
    public int addCallProperty(CallProperty value);
    public int removeCallProperty(CallProperty value);
    public int sizeCallProperty(); 
    public CallProperty newCallProperty(); 
    
    public void setWsdlOverride(java.lang.String value);
    public java.lang.String getWsdlOverride();
    
    public void setServiceImplClass(java.lang.String value);
    public java.lang.String getServiceImplClass();
    
    public void setServiceQname(ServiceQname value);
    public ServiceQname getServiceQname();
    public ServiceQname newServiceQname();
}
