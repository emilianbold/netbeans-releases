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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.casaeditor.model.jbi;

import javax.xml.namespace.QName;

/**
 *
 * @author jqian
 */
public interface Endpoint extends JBIComponent {
    public static final String INTERFACE_NAME_PROPERTY = "interface-name"; 
    public static final String SERVICE_NAME_PROPERTY = "service-name";
    public static final String ENDPOINT_NAME_PROPERTY = "endpoint-name"; 
    
    String getInterfaceName();
    void setInterfaceName(String interfaceName);
    
    String getServiceName();
    void setServiceName(String serviceName);
    
    String getEndpointName();
    void setEndpointName(String endpointName);
    
    // Convenience method
    QName getInterfaceQName();
    QName getServiceQName();
    boolean isPair(Endpoint anotherEndpoint);
    void setInterfaceQName(QName qname);
    void setServiceQName(QName qname);    
}
