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

package org.netbeans.modules.java.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences for the module.
 * @author Tomas Zezula, Jesse Glick
 */
public class JavaProjectSettings {

    private JavaProjectSettings() {}

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(JavaProjectSettings.class);

    /**
     * The package view should be displayed as a list of packages.
     */
    public static final int TYPE_PACKAGE_VIEW = 0;

    /**
     * The package view should be displayed as a tree of folders.
     */
    public static final int TYPE_TREE = 1;

    public static final String PROP_PACKAGE_VIEW_TYPE = "packageViewType"; //NOI18N
    private static final String PROP_SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    private static Preferences prefs() {
        return NbPreferences.forModule(JavaProjectSettings.class);
    }

    /**
     * Returns how the package view should be displayed.
     * @return {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static int getPackageViewType() {
        return prefs().getInt(PROP_PACKAGE_VIEW_TYPE, TYPE_PACKAGE_VIEW);
    }

    /**
     * Sets how the package view should be displayed.
     * @param type either {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static void setPackageViewType(int type) {
        int currentType = getPackageViewType();
        if (currentType != type) {
            prefs().putInt(PROP_PACKAGE_VIEW_TYPE, type);
            pcs.firePropertyChange(PROP_PACKAGE_VIEW_TYPE, currentType, type);
        }
    }

    public static boolean isShowAgainBrokenRefAlert() {
        return prefs().getBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }

    public static void setShowAgainBrokenRefAlert(boolean again) {
        prefs().putBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
