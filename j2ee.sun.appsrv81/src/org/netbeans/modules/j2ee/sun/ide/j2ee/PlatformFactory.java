// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

/**
 */
public final class PlatformFactory extends J2eePlatformFactory {
        
    private static final WeakHashMap<InstanceProperties,J2eePlatformImpl> instanceCache = 
            new WeakHashMap<InstanceProperties,J2eePlatformImpl>();
    
    /** Creates a new instance of PlatformFactory */
    public PlatformFactory() {
    }
    
    public synchronized J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        // Ensure that for each server instance will be always used the same instance of the J2eePlatformImpl
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(dm);
        InstanceProperties ip = dmProps.getInstanceProperties();
        File rootDir = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
        J2eePlatformImpl platform = instanceCache.get(ip);
        if (platform == null) {
            switch (ServerLocationManager.getAppServerPlatformVersion(rootDir)) {
            case ServerLocationManager.SJSAS_82:
                platform = new PlatformImpl82(rootDir,dmProps);
                break;
            case ServerLocationManager.GF_V1:
                platform = new PlatformImpl90(rootDir,dmProps);
                break;
            case ServerLocationManager.GF_V2: 
                platform = new PlatformImpl91(rootDir,dmProps);
                break;
            default:
                platform = new UnknownPlatformImpl(rootDir);
                break;                
            }
            instanceCache.put(ip, platform);
        }
        return platform;
    }

    
}
