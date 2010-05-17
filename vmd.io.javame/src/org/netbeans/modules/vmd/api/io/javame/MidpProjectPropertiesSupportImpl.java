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

package org.netbeans.modules.vmd.api.io.javame;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;

import java.awt.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class MidpProjectPropertiesSupportImpl {

    private static final HashMap<DeviceListener, AntProjectListener> deviceListeners = new HashMap<DeviceListener, AntProjectListener> ();

    static Dimension getDeviceScreenSizeFromProject (DataObjectContext context) {
        //start Issue 116639 bug fix, If for some reasone sombody from non Java ME module ask for screen size
        if (!(ProjectUtils.getProject(context) instanceof J2MEProject)) {
            return new Dimension();
        }
        //end Issue 116639
        return getDeviceScreenSizeFromProject ((J2MEProject) ProjectUtils.getProject(context));
    }
    
    private static Dimension getDeviceScreenSizeFromProject (J2MEProject project) {
        AntProjectHelper helper = project.getLookup ().lookup (AntProjectHelper.class);
        EditableProperties ep = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ProjectConfigurationsHelper confs = project.getConfigurationHelper ();
        String activeConfiguration = confs.getActiveConfiguration () != confs.getDefaultConfiguration () ? confs.getActiveConfiguration ().getDisplayName () : null;

        String platformActive = evaluateProperty (ep, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, activeConfiguration);
        String deviceActive = evaluateProperty (ep, DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeConfiguration);
        if (platformActive != null  &&  deviceActive != null) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault ().getPlatforms (null, new Specification (J2MEPlatform.SPECIFICATION_NAME, null));
            J2MEPlatform platform = null;

            if (platforms != null) for (JavaPlatform javaPlatform : platforms) {
                if (javaPlatform instanceof J2MEPlatform) {
                    if (platformActive.equals ((((J2MEPlatform) javaPlatform).getName ()))) {
                        platform = (J2MEPlatform) javaPlatform;
                        break;
                    }
                }
            }

            if (platform != null) {
                J2MEPlatform.Device[] devices = platform.getDevices ();
                if (devices != null) for (J2MEPlatform.Device device : devices) {
                    if (deviceActive.equals (device.getName ())) {
                        J2MEPlatform.Screen screen = device.getScreen ();
                        if (screen != null) {
                            Integer height = screen.getHeight ();
                            Integer width = screen.getWidth ();
                            if (height != null && width != null)
                                return new Dimension (width, height);
                        }
                    }

                }
            }
        }
        return null;
    }
    
    public static String evaluateProperty (EditableProperties ep, String propertyName, String configuration) {
        if (configuration == null)
            return ep.getProperty (propertyName);
        String value = ep.getProperty ("configs." + configuration + "." + propertyName); // NOI18N
        return value != null ? value : evaluateProperty (ep, propertyName, null);
    }

    public static void addDeviceListener (DataObjectContext context, DeviceListener listener) {
        Project project = ProjectUtils.getProject (context);
        if (project == null)
            return;
        AntProjectHelper helper = project.getLookup ().lookup (AntProjectHelper.class);
        if (helper == null)
            return;

        if (deviceListeners.containsKey (listener))
            Debug.warning ("DeviceListener already registered", listener); // NOI18N
        DeviceAntProjectListener antListener = new DeviceAntProjectListener (listener);
        helper.addAntProjectListener (antListener);
        deviceListeners.put (listener, antListener);
    }

    public static void removeDeviceListener (DataObjectContext context, DeviceListener listener) {
        Project project = ProjectUtils.getProject (context);
        if (project == null)
            return;
        AntProjectHelper helper = project.getLookup ().lookup (AntProjectHelper.class);
        if (helper == null)
            return;

        AntProjectListener antListener = deviceListeners.remove (listener);
        if (antListener != null)
            helper.removeAntProjectListener (antListener);
        else
            Debug.warning ("DeviceListener not registered", listener); // NOI18N
    }

    public static boolean isMobileProject (Project project) {
        return project instanceof J2MEProject; // "J2MEProject".equals (prj.getClass ().getSimpleName ()); // NOI18N
    }

    public static String getActiveConfiguration (Project project) {
        ProjectConfigurationsHelper confs = project.getLookup ().lookup (ProjectConfigurationsHelper.class);
        return confs.getActiveConfiguration () != confs.getDefaultConfiguration () ? confs.getActiveConfiguration ().getDisplayName () : null;
    }

    public static void setProperty (EditableProperties ep, String propertyName, String configuration, String propertyValue) {
        if (configuration == null)
            ep.put (propertyName, propertyValue);
        else
            ep.put ("configs." + configuration + "." + propertyName, propertyValue); // NOI18N
    }

    private static class DeviceAntProjectListener implements AntProjectListener {

        private DeviceListener listener;

        public DeviceAntProjectListener (DeviceListener listener) {
            this.listener = listener;
        }

        public void configurationXmlChanged (AntProjectEvent ev) {
            listener.deviceChanged ();
        }

        public void propertiesChanged (AntProjectEvent ev) {
            listener.deviceChanged ();
        }

    }

}
