/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
    private static final String NEW_MODULE_COUNTER = "newModuleCounter";  //NOI18N
    private static final String NEW_SUITE_COUNTER = "newSuiteCounter";  //NOI18N

    public static ModuleUISettings getDefault() {
        return (ModuleUISettings) SystemOption.findObject(ModuleUISettings.class, true);
    }
    
    public String displayName() {
        return "NBMProjectUISetting"; // NOI18N (not shown in UI)
    }
    
    public int getNewModuleCounter() {
        Integer counter = (Integer) getProperty(NEW_MODULE_COUNTER);
        return counter == null ? 0 : counter.intValue();
    }
    
    public void setNewModuleCounter(int count) {
        putProperty(NEW_MODULE_COUNTER, new Integer(count), true);
    }
    
    public int getNewSuiteCounter() {
        Integer counter = (Integer) getProperty(NEW_SUITE_COUNTER);
        return counter == null ? 0 : counter.intValue();
    }
    
    public void setNewSuiteCounter(int count) {
        putProperty(NEW_SUITE_COUNTER, new Integer(count), true);
    }
    
    public String getLastUsedModuleLocation() {
        String location = (String) getProperty(LAST_USED_MODULE_LOCATION);
        if (location == null) {
            location = System.getProperty("user.home"); // NOI18N
        }
        return location;
    }
    
    public void setLastUsedModuleLocation(String location) {
        assert location != null : "Location can not be null"; // NOI18N
        putProperty(LAST_USED_MODULE_LOCATION, location, true);
    }
    
}
