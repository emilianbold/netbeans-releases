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
