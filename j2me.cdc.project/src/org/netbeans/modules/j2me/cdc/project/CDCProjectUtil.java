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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2me.cdc.project;

import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;

/**
 * Miscellaneous utilities for the cdcproject module.
 * @author  Jiri Rechtacek
 */
public class CDCProjectUtil {
    private CDCProjectUtil () {}
    
    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static CDCPlatform getActivePlatform (final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification (CDCPlatform.PLATFORM_CDC,null));   //NOI18N
        for (JavaPlatform platform : installedPlatforms ){
            if (platform.getDisplayName().equals(activePlatformId)) {
                return (CDCPlatform) platform;
            }
        }
        return null;
    }
    
    
    
    public static Map<String,String> getExecutionModes(ProjectProperties props){
        String activePlatformId = (String)props.get("platform.active");  //NOI18N
        String defaultDevice    = (String)props.get("platform.device");  //NOI18N
        CDCPlatform platform = getActivePlatform (activePlatformId);
        if (platform == null)
            return null;
        CDCDevice[] devices = platform.getDevices();
        Map<String,String> executionModes = null;
        for (int i = 0; i < devices.length && executionModes == null; i++) {
            if (devices[i].getName().equals(defaultDevice)){
                CDCDevice.CDCProfile[] profiles = devices[i].getProfiles();
                for (CDCDevice.CDCProfile profile : profiles ) {
                    if (profile.isDefault()){
                        executionModes = profile.getExecutionModes();
                        break;
                    }
                }
            }            
        }
        return executionModes;
    }    
}
