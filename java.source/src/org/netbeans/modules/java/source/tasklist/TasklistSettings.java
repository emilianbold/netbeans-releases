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
package org.netbeans.modules.java.source.tasklist;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class TasklistSettings {

    private static final String KEY_ENABLED = "enabled"; //NOI18N
    private static final String KEY_ERROR_BADGES = "error-badges"; //NOI18N
    private static final String KEY_DEPENDENCY_TRACKING = "dependency-tracking"; //NOI18N
    
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_ERROR_BADGES = true;
    private static final boolean DEFAULT_DEPENDENCY_TRACKING = true;
    
    private TasklistSettings() {
    }
    
    public static boolean isTasklistEnabled() {
        return getPreferencesNode().getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }
    
    public static void setTasklistsEnabled(boolean enabled) {
        if (isTasklistEnabled() != enabled) {
            getPreferencesNode().putBoolean(KEY_ENABLED, enabled);
            if (enabled) {
                RepositoryUpdater.getDefault().rebuildAll();
            }
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
            
            JavaTaskProvider.refreshAll();
        }
    }
    
    public static boolean isBadgesEnabled() {
        return getPreferencesNode().getBoolean(KEY_ERROR_BADGES, DEFAULT_ERROR_BADGES);
    }

    public static void setBadgesEnabled(boolean enabled) {
        if (isBadgesEnabled() != enabled) {
            getPreferencesNode().putBoolean(KEY_ERROR_BADGES, enabled);
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
        }
    }
    
    public static boolean isDependencyTrackingEnabled() {
        return getPreferencesNode().getBoolean(KEY_DEPENDENCY_TRACKING, DEFAULT_DEPENDENCY_TRACKING);
    }
    
    public static void setDependencyTrackingEnabled(boolean enabled) {
        if (isDependencyTrackingEnabled() != enabled) {
            getPreferencesNode().putBoolean(KEY_DEPENDENCY_TRACKING, enabled);
            if (enabled) {
                RepositoryUpdater.getDefault().rebuildAll();
            }
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
        }
    }
    
    private static Preferences getPreferencesNode() {
        return NbPreferences.forModule(TasklistSettings.class).node("tasklist");
    }
    
}
