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

import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.config.J2eeApplicationAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;

/**
 * A factory class to create {@link J2eeModule} and {@link J2eeApplication} 
 * instances. You are not permitted to create them directly; instead you implement 
 * {@link J2eeModuleImplementation} or {@link J2eeApplicationImplementation} 
 * and use this factory.
 * 
 * 
 * @author sherold
 * @since 1.23
 */
public class J2eeModuleFactory {
    
    /** Creates a new instance of J2eeModuleFactory */
    private J2eeModuleFactory() {
    }
    
    /**
     * Creates a J2eeModule for the specified J2eeModuleImplementation.
     * 
     * @param impl the J2eeModule SPI object
     * 
     * @return J2eeModule API instance.
     */
    public static J2eeModule createJ2eeModule(J2eeModuleImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeModuleAccessor.DEFAULT.createJ2eeModule(impl);
    }
    
    /**
     * Creates a J2eeApplication for the specified J2eeApplicationImplementation.
     * 
     * 
     * @param impl the J2eeApplication SPI object
     * @return J2eJ2eeApplicationI instance.
     */
    public static J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl) {
        if (impl == null) {
            throw new NullPointerException();
        }
        return J2eeApplicationAccessor.DEFAULT.createJ2eeApplication(impl);
    }
}
