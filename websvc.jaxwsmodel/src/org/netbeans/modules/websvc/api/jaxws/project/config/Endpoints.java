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
 * Endpoints.java
 *
 * Created on March 19, 2006, 8:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author Roderico Cruz
 */
public class Endpoints {
     private org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints;
    /** Creates a new instance of HandlerChains */
    public Endpoints(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints endpoints) {
        this.endpoints = endpoints;
    }
    
    public Endpoint[] getEndpoints() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint[] endpointArray = 
                endpoints.getEndpoint();
        Endpoint[] newEndpoints = new Endpoint[endpointArray.length];
        for (int i=0;i<endpointArray.length;i++) {
            newEndpoints[i]=new Endpoint(endpointArray[i]);
        }
        return newEndpoints;
    }
    
    public Endpoint newEndpoint() {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint = 
                endpoints.newEndpoint();
        return new Endpoint(endpoint);
    }
    
    public void addEnpoint(Endpoint endpoint) {
        endpoints.addEndpoint((org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint)endpoint.getOriginal());
    }
    
    public void removeEndpoint(Endpoint endpoint) {
        endpoints.removeEndpoint((org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint)endpoint.getOriginal());
    }
    
    public Endpoint findEndpointByName(String endpointName) {
        Endpoint[] endpoints = getEndpoints();
        for (int i=0;i<endpoints.length;i++) {
            Endpoint endpoint = endpoints[i];
            if(endpointName.equals(endpoint.getEndpointName())){
                return endpoint;
            }
        }
        return null;
    }
    

    public void addPropertyChangeListener(PropertyChangeListener l) {
        endpoints.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        endpoints.removePropertyChangeListener(l);
    }
    
    public void merge(Endpoints newEndpoints) {
        if (newEndpoints.endpoints!=null)
            endpoints.merge(newEndpoints.endpoints,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws java.io.IOException {
        endpoints.write(os);
    }
    
}
