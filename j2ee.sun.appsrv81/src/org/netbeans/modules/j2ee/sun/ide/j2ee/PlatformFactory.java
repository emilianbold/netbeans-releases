/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import javax.enterprise.deploy.spi.DeploymentManager;

/**
 */
public final class PlatformFactory extends J2eePlatformFactory {
    
    /** Creates a new instance of PlatformFactory */
    public PlatformFactory() {
    }
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties (dm);
//        String location = dmProps.getLocation();
        String location = PluginProperties.getDefault().getInstallRoot().getAbsolutePath();
        if (location != null) {
            File root = new File (location);
            if (root != null && root.exists()) {
                return new PlatformImpl (root, dmProps.getDisplayName (), dmProps.getInstanceProperties ());
            }
        }
        return null;
    }
    
}
