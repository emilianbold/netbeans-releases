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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;

/**
 * Abstraction of J2EE Application. Provides access to basic server-neutral properties 
 * of the application: J2EE version, module type, deployment descriptor and its child
 * modules.
 * <p>
 * It is not possible to instantiate this class directly. Implementators have to
 * implement the {@link J2eeApplicationImplementation} first and then use the
 * {@link J2eeModuleFactory} to create a J2eeApplication instance.
 * 
 * @author Pavel Buzek
 */
public class J2eeApplication extends J2eeModule {
    
    private final J2eeApplicationImplementation impl;
    
    private J2eeApplication(J2eeApplicationImplementation impl) {
        super(impl);
        this.impl = impl;
    }

    /**
     * Returns a list of all the J2EEModules which this J2eeApplication contains.
     * 
     * @return list of all the child J2EEModules
     */
    public J2eeModule[] getModules() {
        return impl.getModules();
    }

    /**
     * Registers the specified ModuleListener for notification about the module
     * changes.
     * 
     * @param listener ModuleListener
     */
    public void addModuleListener(ModuleListener listener) {
        impl.addModuleListener(listener);
    }

    /**
     * Unregister the specified ModuleListener.
     * 
     * @param listener ModuleListener
     */
    public void removeModuleListener(ModuleListener listener) {
        impl.removeModuleListener(listener);
    }
    
    static {
        J2eeApplicationAccessor.DEFAULT = new J2eeApplicationAccessor() {
            public J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
                return new J2eeApplication(impl);
            }
        };
    }
}
