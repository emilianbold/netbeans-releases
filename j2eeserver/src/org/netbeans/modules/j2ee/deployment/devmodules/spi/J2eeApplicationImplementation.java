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

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;

/** 
 * Base SPI interface for {@link J2eeApplication}. Implementation of this interface 
 * is used to create {@link J2eeApplication} instance using the {@link J2eeModuleFactory}.
 * 
 * @author sherold
 * 
 * @since 1.23
 */
public interface J2eeApplicationImplementation extends J2eeModuleImplementation {

    /**
     * Returns a list of all the J2EEModules which this J2eeApplication contains.
     * 
     * @return list of all the child J2EEModules
     */
    J2eeModule[] getModules();

    /**
     * Registers the specified ModuleListener for notification about the module
     * changes.
     * 
     * @param listener ModuleListener
     */
    void addModuleListener(ModuleListener listener);

    /**
     * Unregister the specified ModuleListener.
     * 
     * @param listener ModuleListener
     */
    void removeModuleListener(ModuleListener listener);
    
}
