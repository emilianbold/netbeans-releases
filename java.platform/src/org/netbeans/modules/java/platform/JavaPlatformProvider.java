/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.platform;

import org.netbeans.api.java.platform.JavaPlatform;

import java.beans.PropertyChangeListener;

public interface JavaPlatformProvider {

    public static final String PROP_INSTALLED_PLATFORMS = "installedPlatforms"; //NOI18N

    public JavaPlatform [] getInstalledPlatforms ();

    public void addPropertyChangeListener (PropertyChangeListener listener);

    public void removePropertyChangeListener (PropertyChangeListener listener);
}
