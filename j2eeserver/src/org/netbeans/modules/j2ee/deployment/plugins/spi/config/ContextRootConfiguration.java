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

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;

/**
 * Configuration useful for setting and getting the web module context root.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface ContextRootConfiguration {
    
    /**
     * Return the web module context root.
     *
     * @return web module context root.
     *
     * @throws ConfigurationException reports errors in getting the web context
     *         root.
     */
    String getContextRoot() throws ConfigurationException;

    /**
     * Set the web context root.
     *
     * @param contextRoot context root to be set.
     *
     * @throws ConfigurationException reports errors in setting the web context
     *         root.
     */
    void setContextRoot(String contextRoot) throws ConfigurationException;
    
}
