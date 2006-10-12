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
 * Endpoint.java
 *
 * Created on March 19, 2006, 9:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author rico
 */
public class Endpoint {
    org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint;
    /** Creates a new instance of Handler */
    public Endpoint(org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint endpoint) {
        this.endpoint=endpoint;
    }
    
    public org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoint 
            getOriginal(){
        return endpoint;
    }
    public String getEndpointName() {
        return endpoint.getName();
    }
    
     public void setEndpointName(String name) {
        endpoint.setName(name);
    }
    public String getImplementation(){
        return endpoint.getImplementation();
    }
   
    public void setImplementation(String value) {
        endpoint.setImplementation(value);
    }
    
    public String getUrlPattern(){
        return endpoint.getUrlPattern();
    }
    
    public void setUrlPattern(String value){
        endpoint.setUrlPattern(value);
    }
}
