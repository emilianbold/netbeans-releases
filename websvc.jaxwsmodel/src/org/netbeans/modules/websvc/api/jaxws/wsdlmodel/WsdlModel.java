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

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Service;
import java.util.*;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModel {
    
    private Model model;
    
    /** Creates a new instance of WsdlModel */
    WsdlModel(Model model) {
        this.model=model;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Model*/ getInternalJAXWSModel() {
        return model;
    }
    
    public List<WsdlService> getServices() {
        List<WsdlService> wsdlServices = new ArrayList<WsdlService> ();
        if (model==null) return wsdlServices;
        List<Service> services = model.getServices();
        for (Service s:services)
            wsdlServices.add(new WsdlService(s));
        return wsdlServices;
    }
    
    public WsdlService getServiceByName(String serviceName) {
        List<Service> services = model.getServices();
        for (Service s:services)
            if (serviceName.equals(s.getName().getLocalPart())) return new WsdlService(s);
        return null;
    }
}
