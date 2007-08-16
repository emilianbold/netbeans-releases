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
