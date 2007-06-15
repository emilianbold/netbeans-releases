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
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;

/**
 * Configuration CMP mapping.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface MappingConfiguration {
    
    
    /**
     * Sets the resource for the specified CMP bean. Some containers may not
     * support fine-grained per bean resource definition, in which case global
     * EJB module CMP resource is set.
     *
     * @param ejbName   name of the CMP bean.
     * @param jndiName  the JNDI name of the resource.
     *
     * @throws ConfigurationException reports errors in setting the CMP resource.
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * 
     * @since 1.30
     */
    void setCMPResource(String ejbName, String jndiName) throws ConfigurationException;
    
    /**    
     * Sets the CMP mapping info for the EJB by the given name.
     * 
     * @param mappings All the mapping info needed to be pushed in one batch.
     * 
     * @throws ConfigurationException reports errors in setting the CMP mapping info.
     */
    void setMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException;
}
