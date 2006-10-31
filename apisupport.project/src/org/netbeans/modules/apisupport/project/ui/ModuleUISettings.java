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

package org.netbeans.modules.apisupport.project.ui;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage for settings used by a module's UI (wizards, properties, ...)
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class ModuleUISettings {

    private static final String LAST_CHOSEN_LIBRARY_LOCATION = "lastChosenLibraryLocation"; // NOI18N
    private static final String LAST_USED_NB_PLATFORM_LOCATION = "lastUsedNbPlatformLocation"; // NOI18N
    private static final String NEW_MODULE_COUNTER = "newModuleCounter";  //NOI18N
    private static final String NEW_SUITE_COUNTER = "newSuiteCounter";  //NOI18N
    private static final String CONFIRM_RELOAD_IN_IDE = "confirmReloadInIDE"; // NOI18N
    private static final String LAST_USED_PLATFORM_ID = "lastUsedPlatformID"; // NOI18N
    private static final String HARNESSES_UPGRADED = "harnessesUpgraded"; // NOI18N

    public static ModuleUISettings getDefault() {
        return new ModuleUISettings(); // stateless
    }

    private Preferences prefs() {
        return NbPreferences.forModule(ModuleUISettings.class);
    }

    public int getNewModuleCounter() {
        return prefs().getInt(NEW_MODULE_COUNTER, 0);
    }

    public void setNewModuleCounter(int count) {
        prefs().putInt(NEW_MODULE_COUNTER, count);
    }

    public int getNewSuiteCounter() {
        return prefs().getInt(NEW_SUITE_COUNTER, 0);
    }

    public void setNewSuiteCounter(int count) {
        prefs().putInt(NEW_SUITE_COUNTER, count);
    }

    public String getLastUsedNbPlatformLocation() {
        return prefs().get(LAST_USED_NB_PLATFORM_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastUsedNbPlatformLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_USED_NB_PLATFORM_LOCATION, location);
    }

    public boolean getConfirmReloadInIDE() {
        return prefs().getBoolean(CONFIRM_RELOAD_IN_IDE, true);
    }

    public void setConfirmReloadInIDE(boolean b) {
        prefs().putBoolean(CONFIRM_RELOAD_IN_IDE, b);
    }

    public String getLastChosenLibraryLocation() {
        return prefs().get(LAST_CHOSEN_LIBRARY_LOCATION, System.getProperty("user.home")); // NOI18N
    }

    public void setLastChosenLibraryLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        prefs().put(LAST_CHOSEN_LIBRARY_LOCATION, location);
    }

    public String getLastUsedPlatformID() {
        return prefs().get(LAST_USED_PLATFORM_ID, "default"); // NOI18N
    }

    public void setLastUsedPlatformID(String id) {
        assert id != null : "Platform ID can not be null"; // NOI18N
        prefs().put(LAST_USED_PLATFORM_ID, id);
    }

    public boolean getHarnessesUpgraded() {
        return prefs().getBoolean(HARNESSES_UPGRADED, false);
    }

    public void setHarnessesUpgraded(boolean b) {
        prefs().putBoolean(HARNESSES_UPGRADED, b);
    }

}
