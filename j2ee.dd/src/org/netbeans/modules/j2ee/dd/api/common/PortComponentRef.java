/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Generated interface for PortComponentRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface PortComponentRef extends CommonDDBean {
    
    public static final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
    public static final String PORT_COMPONENT_LINK = "PortComponentLink"; // NOI18N
    
    /** Setter for service-endpoint-interface property.
     * @param value property value
     */
    public void setServiceEndpointInterface(java.lang.String value);
    /** Getter for service-endpoint-interface property.
     * @return property value 
     */
    public java.lang.String getServiceEndpointInterface();
    /** Setter for port-component-link property.
     * @param value property value
     */
    public void setPortComponentLink(java.lang.String value);
    /** Getter for port-component-link property.
     * @return property value 
     */
    public java.lang.String getPortComponentLink();

}
