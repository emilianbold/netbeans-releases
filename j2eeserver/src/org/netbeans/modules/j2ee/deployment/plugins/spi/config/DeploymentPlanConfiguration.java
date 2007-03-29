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

import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;

/**
 * Configuration useful for creating the deployment plan file. The 
 * deployment plan file is created when the JSR-88 based deployment is used.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface DeploymentPlanConfiguration {
    
    /**
     * Write the deployment plan file to the specified output stream.
     * 
     * @param outputStream the deployment paln file should be written to.
     * @throws ConfigurationException if an error during saving the deployment
     *         plan configuration occurs.
     */
    void save(OutputStream outputStream) throws ConfigurationException;
    
}
