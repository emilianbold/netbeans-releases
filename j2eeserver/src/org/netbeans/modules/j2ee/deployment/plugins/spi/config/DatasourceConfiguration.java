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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;

/**
 * Configuration useful for managing module data sources.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface DatasourceConfiguration {
    
    /**
     * Returns the data sources defined in the module.
     * 
     * @return a set of data sources defined in the module.
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     */
    Set<Datasource> getDatasources() throws ConfigurationException;
    
    /**
     * Returns true if data source creation is supported, false otherwise.
     * 
     * @return true if data source creation is supported, false otherwise.
     */
    boolean supportsCreateDatasource();
    
    /**
     * Creates the data source definition in the module.
     * 
     * @param jndiName data source JNDI name.
     * @param url database URL.
     * @param username database user.
     * @param password user's password.
     * @param driver fully qualified name of the database driver class.
     * 
     * @return created data source.
     * 
     * @throws UnsupportedOperationException if operation is not supported.
     * @throws ConfigurationException reports problems in creating data source
     *         definition.
     * @throws DatasourceAlreadyExistsException if a data source with the same
     *         JNDI name already exists.
     */
    Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException;
}
