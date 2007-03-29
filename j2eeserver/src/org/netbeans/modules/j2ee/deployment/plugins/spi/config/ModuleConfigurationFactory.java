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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 * Factory for creating {@link ModuleConfiguration}. Plugin is required to register 
 * instance of this class in module layer in the <code>J2EE/DeploymentPlugins/{plugin_name}</code> 
 * folder.
 * 
 * @since 1.23
 * @author sherold
 */
public interface ModuleConfigurationFactory {
    
    /**
     * Creates a {@link ModuleConfiguration} instance associated with the specified 
     * J2EE module.
     * 
     * @param j2eeModule J2EE module the created ModuleConfigucation should be 
     *        associated with.
     * 
     * @return ModuleConfigucation associated with the specified J2EE module.
     */
    ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException;
}
