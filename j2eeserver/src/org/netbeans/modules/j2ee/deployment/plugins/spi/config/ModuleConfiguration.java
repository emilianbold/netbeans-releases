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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.util.Lookup;

/**
 * An interface that defines a container for all the server-specific configuration 
 * information for a single top-level J2EE module. The ModuleConfiguration object 
 * could represent a single stand-alone module or a J2EE application that contains 
 * several sub-modules. The ModuleConfiguration object contains in its lookup a set 
 * of configurations that are used for managing the server-specific settings.
 *
 * @since 1.23
 * @author sherold
 */
public interface ModuleConfiguration extends Lookup.Provider {
    
    /**
     * Returns lookup associated with the object. This lookup should contain
     * implementations of all the supported configurations.
     * <p>
     * The configuration are:  {@link ContextRootConfiguration},  {@link DatasourceConfiguration}, 
     * {@link MappingConfiguration}, {@link EjbResourceConfiguration}, {@link DeploymentPlanConfiguration},
     * {@link MessageDestinationConfiguration}
     * <p>
     * Implementators are advised to use {@link org.openide.util.lookup.Lookups#fixed}
     * to implement this method.
     * 
     * @return lookup associated with the object containing all the supported
     *         ConfigurationProvider implementations.
     */
    Lookup getLookup();
    
    /**
     * Returns a J2EE module associated with this ModuleConfiguration instance.
     * 
     * @return a J2EE module associated with this ModuleConfiguration instance.
     */
    J2eeModule getJ2eeModule();
    
    /**
     * The j2eeserver calls this method when it is done using this ModuleConfiguration 
     * instance. The server plug-in should free all the associated resources -
     * listeners for example.
     */
    void dispose();
}
