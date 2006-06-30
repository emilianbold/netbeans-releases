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
