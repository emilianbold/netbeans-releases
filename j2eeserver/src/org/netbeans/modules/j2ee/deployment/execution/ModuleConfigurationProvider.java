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

package org.netbeans.modules.j2ee.deployment.execution;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;


/**
 * Provide access to deployment configuration data from top-level J2EE module
 * @author  nn136682
 */
public interface ModuleConfigurationProvider {
    /**
     * @return deployment configuration data for a j2ee module.
     */
    ModuleConfiguration getModuleConfiguration();

    /**
     * Retrieve DeployableModule representing a child module
     * specifed by the provided URI.
     *
     * @param moduleUri URI for the child module to retrieve.
     * @return DeployableModule for the specified child module.
     */
    J2eeModule getJ2eeModule(String moduleUri);
}
