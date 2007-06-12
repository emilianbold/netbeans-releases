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
package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * This interface must be implemented by J2EE Application support and an instance 
 * added into project lookup.
 *
 * @author sherold
 * 
 * @since 1.23
 */
public abstract class J2eeApplicationProvider extends J2eeModuleProvider {

    /**
     * Returns the provider for the child module specified by given URI.
     * 
     * @param uri the child module URI within the J2EE application.
     * 
     * @return J2eeModuleProvider object
     */
    public abstract J2eeModuleProvider getChildModuleProvider(String uri);

    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * 
     * @return array of J2eeModuleProvider objects.
     */
    public abstract J2eeModuleProvider[] getChildModuleProviders();
    
    /**
     * Overrides the <code>J2eeModuleProvider's</code> implementation so that 
     * the data sources from the child modules are returned
     * 
     * @throws ConfigurationException when an error occured while retrieving 
     *         module data sources.
     */
    public Set<Datasource> getModuleDatasources() throws ConfigurationException {
        
        Set<Datasource> projectDS = new HashSet<Datasource>();
        
        for (J2eeModuleProvider modProvider : getChildModuleProviders()) {
            projectDS.addAll(modProvider.getModuleDatasources());
        }
        
        return projectDS;
    }
    
}
