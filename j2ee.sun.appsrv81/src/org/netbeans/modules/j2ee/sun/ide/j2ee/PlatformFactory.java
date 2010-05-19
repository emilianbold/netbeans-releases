// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            case ServerLocationManager.GF_V2point1:
            case ServerLocationManager.GF_V2point1point1:
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
