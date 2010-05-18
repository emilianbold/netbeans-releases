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
/*
 * ScreenSizeHelper.java
 *
 * Created on July 3, 2006, 3:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dimension;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
//import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;

/**
 *
 * @author suchys
 */
public class ScreenSizeHelper {
    
    /**
     * Gets current device screen size (the device which is currently used
     * in the project)
     *
     */
    public static Dimension getCurrentDeviceScreenSize(FileObject primaryFile, String configuration) {
        Project p = FileOwnerQuery.getOwner (primaryFile);
        if (p == null || !(p instanceof J2MEProject)){
            return new Dimension(320, 240);
        }
        Dimension dim = getDeviceScreenSizeFromProject((J2MEProject) p, configuration);
        if ( dim == null || dim.getHeight() < 10.0 || dim.getWidth() < 10.0){
            return new Dimension(320, 240);
        }
        return dim;
    }
    
    
    /**
     * Gets currently used device screen size from J2ME project
     * @param project
     * @return
     */
    private static Dimension getDeviceScreenSizeFromProject(J2MEProject project, String configuration) {
        AntProjectHelper helper = (AntProjectHelper) project.getLookup ().lookup (AntProjectHelper.class);
        EditableProperties ep = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ProjectConfigurationsHelper confs = project.getConfigurationHelper ();
        String activeConfiguration = configuration;
        
        String platformActive = evaluateProperty (ep, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, activeConfiguration);
        String deviceActive = evaluateProperty (ep, DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeConfiguration);
        
        if (platformActive != null  &&  deviceActive != null) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault ().getPlatforms (null, new Specification (J2MEPlatform.SPECIFICATION_NAME, null));
            J2MEPlatform platform = null;
            
            if (platforms != null) for (int i = 0; i < platforms.length; i++) {
                JavaPlatform javaPlatform = platforms[i];
                if (javaPlatform instanceof J2MEPlatform) {
                    if (platformActive.equals ((((J2MEPlatform) javaPlatform).getName ()))) {
                        platform = (J2MEPlatform) javaPlatform;
                        break;
                    }
                }
            }
            
            if (platform != null) {
                J2MEPlatform.Device[] devices = platform.getDevices ();
                if (devices != null) for (int i = 0; i < devices.length; i++) {
                    J2MEPlatform.Device device = devices[i];
                    if (deviceActive.equals (device.getName ())) {
                        J2MEPlatform.Screen screen = device.getScreen ();
                        if (screen != null) {
                            Integer height = screen.getHeight ();
                            Integer width = screen.getWidth ();
                            if (height != null  &&  width != null)
                                return new Dimension (width.intValue (), height.intValue ());
                        }
                    }
                    
                }
            }
        }
        return null;
    }
    
    private static String evaluateProperty (EditableProperties ep, String propertyName, String configuration) {
        if (configuration == null)
            return ep.getProperty (propertyName);
        String value = ep.getProperty ("configs." + configuration + "." + propertyName); // NOI18N
        return value != null ? value : evaluateProperty (ep, propertyName, null);
    }
    
}
