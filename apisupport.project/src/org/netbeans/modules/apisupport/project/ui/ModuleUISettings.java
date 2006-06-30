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

import org.openide.options.SystemOption;

/**
 * Storage for settings used by a module's UI (wizards, properties, ...)
 *
 * @author Martin Krauskopf
 */
public class ModuleUISettings extends SystemOption {

    private static final long serialVersionUID = 736430338589073397L;

    private static final String LAST_USED_MODULE_LOCATION = "lastUsedModuleLocation"; // NOI18N
    private static final String LAST_CHOSEN_LIBRARY_LOCATION = "lastChosenLibraryLocation"; // NOI18N
    private static final String LAST_USED_NB_PLATFORM_LOCATION = "lastUsedNbPlatformLocation"; // NOI18N
    private static final String NEW_MODULE_COUNTER = "newModuleCounter";  //NOI18N
    private static final String NEW_SUITE_COUNTER = "newSuiteCounter";  //NOI18N
    private static final String CONFIRM_RELOAD_IN_IDE = "confirmReloadInIDE"; // NOI18N
    private static final String LAST_USED_PLATFORM_ID = "lastUsedPlatformId"; // NOI18N
    private static final String HARNESSES_UPGRADED = "harnessesUpgraded"; // NOI18N
    
    public static ModuleUISettings getDefault() {
        return (ModuleUISettings) SystemOption.findObject(ModuleUISettings.class, true);
    }
    
    public String displayName() {
        return "NBMProjectUISetting"; // NOI18N (not shown in UI)
    }
    
    private Object getProperty(Object key, Object fallback) {
        Object value = getProperty(key);
        return value == null ? fallback : value;
    }
    
    public int getNewModuleCounter() {
        Integer counter = (Integer) getProperty(NEW_MODULE_COUNTER, new Integer(0));
        return counter.intValue();
    }
    
    public void setNewModuleCounter(int count) {
        putProperty(NEW_MODULE_COUNTER, new Integer(count), true);
    }
    
    public int getNewSuiteCounter() {
        Integer counter = (Integer) getProperty(NEW_SUITE_COUNTER, new Integer(0));
        return counter.intValue();
    }
    
    public void setNewSuiteCounter(int count) {
        putProperty(NEW_SUITE_COUNTER, new Integer(count), true);
    }
    
    public String getLastUsedModuleLocation() {
        return (String) getProperty(LAST_USED_MODULE_LOCATION, System.getProperty("user.home")); // NOI18N
    }
    
    public void setLastUsedModuleLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        putProperty(LAST_USED_MODULE_LOCATION, location, true);
    }
    
    public String getLastUsedNbPlatformLocation() {
        return (String) getProperty(LAST_USED_NB_PLATFORM_LOCATION, System.getProperty("user.home")); // NOI18N
    }
    
    public void setLastUsedNbPlatformLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        putProperty(LAST_USED_NB_PLATFORM_LOCATION, location, true);
    }
    
    public boolean getConfirmReloadInIDE() {
        Boolean b = (Boolean) getProperty(CONFIRM_RELOAD_IN_IDE, Boolean.TRUE);
        return b.booleanValue();
    }
    
    public void setConfirmReloadInIDE(boolean b) {
        putProperty(CONFIRM_RELOAD_IN_IDE, Boolean.valueOf(b), true);
    }
    
    public String getLastChosenLibraryLocation() {
        return (String) getProperty(LAST_CHOSEN_LIBRARY_LOCATION, System.getProperty("user.home")); // NOI18N
    }
    
    public void setLastChosenLibraryLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        putProperty(LAST_CHOSEN_LIBRARY_LOCATION, location, true);
    }
    
    public String getLastUsedPlatformID() {
        return (String) getProperty(LAST_USED_PLATFORM_ID, "default"); // NOI18N
    }
    
    public void setLastUsedPlatformID(String id) {
        assert id != null : "Platform ID can not be null"; // NOI18N
        putProperty(LAST_USED_PLATFORM_ID, id, true);
    }

    public boolean getHarnessesUpgraded() {
        return ((Boolean) getProperty(HARNESSES_UPGRADED, Boolean.FALSE)).booleanValue();
    }

    public void setHarnessesUpgraded(boolean b) {
        putProperty(HARNESSES_UPGRADED, Boolean.valueOf(b), true);
    }
    
}
