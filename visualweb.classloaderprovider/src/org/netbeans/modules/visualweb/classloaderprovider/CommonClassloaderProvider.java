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

package org.netbeans.modules.visualweb.classloaderprovider;

import java.util.Properties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

/**
 * This intreface is used to lookup a provider for Common Classloader
 * that is shared by the IDE implementation and the user's Project. The actual
 * implementation must have the specified capabilities.
 *
 * @author Sandip V. Chitale
 */
public interface CommonClassloaderProvider {
    public String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    public String J2EE_1_3 = J2eeModule.J2EE_13;
    public String J2EE_1_4 = J2eeModule.J2EE_14;
    public String JAVA_EE_5 = J2eeModule.JAVA_EE_5;

    /**
     * This is used to find out if this Designtime ClassLoader factory is able
     * to handle the specified capabilities. The first one that has capabilities
     * should return true. That is the one that will be used. Note: The order
     * depends on the order in which
     * <code>Lookup.getDefault().lookup(Lookup.Template)</code> return the
     * instances. Currently the Web Project's J2EE platform property is used as
     * a capability.
     */
    public boolean     isCapableOf(Properties capabilities);

    /**
     * The implementors should simply return their ClassLoader which will be
     * their modules classloader. The implemenotrs modules should declare
     * dependencies on the modules and/or library wrapper modules which are
     * shared by the IDE implementation and the user project.
     **/
    public ClassLoader getClassLoader();
}
