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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import java.util.*;

/**
 *
 * @author mkuchtiak
 */
public class WsdlService {
    
    private Service service;
    /** Creates a new instance of WsdlService */
    WsdlService(Service service) {
        this.service=service;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Service*/ getInternalJAXWSService() {
        return service;
    }
    
    public List<WsdlPort> getPorts() {
        List<WsdlPort> wsdlPorts = new ArrayList<WsdlPort>();
        if (service==null) return wsdlPorts;
        List<Port> ports = service.getPorts();
        for (Port p:ports)
            wsdlPorts.add(new WsdlPort(p));
        return wsdlPorts;
    }
    
    public String getName() {
        if (service==null) return null;
        return service.getName().getLocalPart();
    }
    
    public String getNamespaceURI() {
        return service.getName().getNamespaceURI();
    }
    
    public String getJavaName() {
        if (service==null) return null;
        return service.getJavaInterface().getName();
    }
    
    public WsdlPort getPortByName(String portName) {
        List<Port> ports = service.getPorts();
        for (Port p:ports)
            if (portName.equals(p.getName().getLocalPart())) return new WsdlPort(p);
        return null;
    }
}
