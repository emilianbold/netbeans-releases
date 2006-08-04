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
 * Configutration.java
 *
 * Created on June 22, 2005, 1:44 PM
 *
 */
package org.netbeans.modules.mobility.end2end.client.config;

import java.util.List;

import org.netbeans.modules.mobility.end2end.classdata.AbstractService;

/**
 *
 * @author Michal Skvor
 */
public class Configuration {
    
    private String serviceType;
    
    private ClientConfiguration clientConfiguration;
    
    private ServerConfiguration serverConfiguration;
    
    private List<AbstractService> services;
    
    public static final String CLASS_TYPE       = "class";      // NOI18N
    public static final String WSDLCLASS_TYPE   = "wsdlClass";  // NOI18N
    public static final String JSR172_TYPE      = "jsr-172";    // NOI18N
    
    /**
     * Returns type of the service
     *
     * @return type of the service
     */
    public String getServiceType() {
        return serviceType;
    }
    
    /**
     * Sets type of the service
     *
     * @param type of the service
     */
    public void setServiceType( final String type ) {
        serviceType = type;
    }
    
    /**
     * Gets configuration for the client
     *
     * @return client configuration
     */
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }
    
    public void setClientConfiguration( final ClientConfiguration clientConfiguration ) {
        this.clientConfiguration = clientConfiguration;
    }
    
    /**
     * Gets configuration for the server
     *
     * @return server configuration
     */
    public ServerConfiguration getServerConfigutation() {
        return serverConfiguration;
    }
    
    public void setServerConfiguration( final ServerConfiguration serverConfiguration ) {
        this.serverConfiguration = serverConfiguration;
    }
    
    public void setServices( final List<AbstractService> services ) {
        this.services = services;
    }
    
    /**
     * Gets array of services
     *
     * @return array of services
     */
    public List<AbstractService> getServices() {
        return services;
    }
}
