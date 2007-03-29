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

package org.netbeans.modules.j2ee.deployment.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing some of the non-public methods of the J2eeModule.
 * 
 * @author sherold
 */
public abstract class J2eeModuleAccessor {
    
    public static J2eeModuleAccessor DEFAULT;
    
    // force loading of J2eeModule class. That will set DEFAULT variable.
    static {
        try {
            Object o = Class.forName("org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule", true, J2eeModuleAccessor.class.getClassLoader()); // NOI18N
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }
    }
    
    /**
     * Factory method that creates a J2eeModule for the J2eeModuleImplementation.
     * 
     * @param impl SPI J2eeModuleImplementation object
     * 
     * @return J2eeModule for the J2eeModuleImplementation.
     */
    public abstract J2eeModule createJ2eeModule(J2eeModuleImplementation impl);
    
    /**
     * Returns the J2eeModuleProvider that belongs to the given j2eeModule.
     * 
     * @param j2eeModule J2eeModule
     * 
     * @return J2eeModuleProvider that belongs to the given j2eeModule.
     */
    public abstract J2eeModuleProvider getJ2eeModuleProvider(J2eeModule j2eeModule);
    
    /**
     * Associates the J2eeModuleProvider with the spcecified J2eeModule.
     * 
     * @param j2eeModule J2eeModule
     * @param J2eeModuleProvider J2eeModuleProvider that belongs to the given J2eeModule.
     */
    public abstract void setJ2eeModuleProvider(J2eeModule j2eeModule, J2eeModuleProvider j2eeModuleProvider);
}
