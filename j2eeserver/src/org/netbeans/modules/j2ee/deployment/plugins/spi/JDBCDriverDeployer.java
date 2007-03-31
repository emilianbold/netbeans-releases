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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.util.Set;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * JDBC driver deployer useful for deploying drivers to the server.
 * <p>
 * Implementation of JDBC driver deployer should be registerd via the {@link 
 * OptionalDeploymentManagerFactory}.
 * 
 * @author sherold
 * 
 * @since 1.24
 */
public interface JDBCDriverDeployer {
    
    /**
     * Returns true if the specified target supports deployment of JDBC drivers,
     * false otherwise.
     * 
     * @params target the JDBC drivers maight be deployed to.
     * 
     * @return true if the specified target supports deployment of JDBC drivers,
     *         false otherwise.
     */
    boolean supportsDeployJDBCDrivers(Target target);
    
    /**
     * Deploys JDBC drivers for all the specified resources to the specified target
     * server if the drivers have not been deployed yet.
     * 
     * @param target where the drivers should be deployed to.
     * @param datasources 
     */
    ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources);
    
}
