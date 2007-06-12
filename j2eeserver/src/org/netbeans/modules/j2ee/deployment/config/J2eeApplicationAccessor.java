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

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing non-public constructor of the J2eeApplication.
 * 
 * 
 * @author sherold
 */
public abstract class J2eeApplicationAccessor {
    
    public static J2eeApplicationAccessor DEFAULT;
    
    // force loading of J2eeApplication class. That will set DEFAULT variable.
    static {
        try {
            Object o = Class.forName("org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication", true, J2eeApplicationAccessor.class.getClassLoader()); // NOI18N
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }
    }
    
    /**
     * Factory method that creates a J2eeApplication for the J2eeApplicationImplementation.
     * 
     * @param impl SPI J2eeApplicationImplementation object
     * @return J2eeApplication
     */
    public abstract J2eeApplication createJ2eeApplication(J2eeApplicationImplementation impl);
}
