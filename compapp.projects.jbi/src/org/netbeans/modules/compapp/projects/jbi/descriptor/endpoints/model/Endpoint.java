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

package org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model;

import java.io.Serializable;
import javax.xml.namespace.QName;


/**
 * DOCUMENT ME!
 *
 * @author Graj TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class Endpoint implements Serializable {
    
    private String endpointName;    
    private QName serviceQName;
    private QName interfaceQName;
    private boolean isConsumes;
  
    public Endpoint(String endpointName, 
            QName serviceQName, QName interfaceQName) {
        this.endpointName = endpointName;
        this.serviceQName = serviceQName;
        this.interfaceQName = interfaceQName;
    }
    
    public Endpoint(String endpointName, 
            QName serviceQName, QName interfaceQName,
            boolean isConsumes) {
        this(endpointName, serviceQName, interfaceQName);
        this.isConsumes = isConsumes;
    }
    
    public String getEndpointName() {
        return endpointName;
    }
    
    public QName getServiceQName() {
        return serviceQName;
    }
    
    public QName getInterfaceQName() {
        return interfaceQName;
    }
    
    public boolean isConsumes() {
        return isConsumes;
    }
    
    public String getFullyQualifiedName() {
        return getServiceQName().toString() + "." + getEndpointName();
    }

    public boolean equals(Object p) {
        if (!(p instanceof Endpoint)) {
            return false;
        }
        
        Endpoint endpoint = (Endpoint) p;
        if (endpointName.equals(endpoint.getEndpointName()) &&
                serviceQName.equals(endpoint.getServiceQName()) &&
                interfaceQName.equals(endpoint.getInterfaceQName())) {
            // don't check direction yet..
            return true;
        }
        return false;
    }
}
