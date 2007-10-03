/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.websphere6.config;

/**
 *
 * @author dkumar
 */

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;

public class WSModuleConfigurationFactory implements ModuleConfigurationFactory {

    /** Creates a new instance of JBModuleConfigurationFactory */
    public WSModuleConfigurationFactory() {
    }

    public ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException {

        if (J2eeModule.WAR == j2eeModule.getModuleType()) {
            //Dileep -- return new WarDeploymentConfiguration(j2eeModule);
        } else if (J2eeModule.EJB == j2eeModule.getModuleType()) {
            //Dileep -- return new EjbDeploymentConfiguration(j2eeModule);
        } else {
            //Dileep -- return new EarDeploymentConfiguration(j2eeModule);
        }
        return null;
    }

}
