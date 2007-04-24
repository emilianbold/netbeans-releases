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

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.api.project.Project;

import java.awt.*;


/**
 * @author David Kaspar
 */
public final class MidpProjectPropertiesSupport {

    public static Dimension getDeviceScreenSizeFromProject (DataObjectContext context) {
        return MidpProjectPropertiesSupportImpl.getDeviceScreenSizeFromProject (context);
    }

    public static String evaluateProperty (EditableProperties ep, String propertyName, String configuration) {
        return MidpProjectPropertiesSupportImpl.evaluateProperty (ep, propertyName, configuration);
    }

    public static void setProperty (EditableProperties ep, String propertyName, String configuration, String propertyValue) {
        MidpProjectPropertiesSupportImpl.setProperty (ep, propertyName, configuration, propertyValue);
    }

    public static void addDeviceListener (DataObjectContext context, DeviceListener listener) {
        MidpProjectPropertiesSupportImpl.addDeviceListener (context, listener);
    }

    public static void removeDeviceChangedListener (DataObjectContext context, DeviceListener listener) {
        MidpProjectPropertiesSupportImpl.removeDeviceListener (context, listener);
    }

    public static boolean isMobileProject (Project project) {
        return MidpProjectPropertiesSupportImpl.isMobileProject (project);
    }

    public static String getActiveConfiguration (Project project) {
        return MidpProjectPropertiesSupportImpl.getActiveConfiguration (project);
    }
}
