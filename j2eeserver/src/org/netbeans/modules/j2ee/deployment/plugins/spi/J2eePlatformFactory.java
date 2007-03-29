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

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;

/**
 * Mandatory factory class for producing {@link J2eePlatformImpl}. Plugin is
 * required to register instance of this class in module layer in the
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code> folder.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public abstract class J2eePlatformFactory {

    /**
     * Return <code>J2eePlatformImpl</code> for the given <code>DeploymentManager</code>.
     *
     * @param dm <code>DeploymentManager</code>.
     */
    public abstract J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm);
    
}
