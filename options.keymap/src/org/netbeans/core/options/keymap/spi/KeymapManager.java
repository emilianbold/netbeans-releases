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

package org.netbeans.core.options.keymap.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.core.options.keymap.api.ShortcutAction;

/**
 * The SPI class allowing to plug in implementations of shortcuts storage.
 * The instances of this class are registered in the global lookup
 * (Lookup.getDefault()).
 * 
 * The class can implement also the keyboard shortcuts profiles manipulation
 * (switching of the profiles, saving of the profiles etc.)
 * 
 * @author David Strupl
 */
public abstract class KeymapManager {

    /** My instance name */
    private String name;

    /**
     * Every instance is represented by an unique name.
     * @param name 
     */
    protected KeymapManager(String name) {
        this.name = name;
    }

    /**
     * This method should return the currently running shortcuts action
     * assignment.
     * @return 
     */
    public abstract Map<String, Set<ShortcutAction>> getActions();

    /**
     * Refreshes the current keymap model by reading the assignments stored
     * in the persistent storage.
     */
    public abstract void refreshActions();

    /**
     * Retrieves the action shortcuts assignments for given profile.
     * @param profileName The name of the profile to get shortcuts for.
     * @return Shortcuts for given profile.
     */
    public abstract Map<ShortcutAction, Set<String>> getKeymap(String profileName);

    /**
     * Retrieves the default action shortcuts assignments for given profile.
     * @param profileName The name of the profile to get shortcuts for.
     * @return Default shortcuts for given profile.
     */
    public abstract Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName);
    
    /**
     * Saves the given action shortcuts assignment under given profile name.
     * @param profileName 
     * @param actionToShortcuts 
     */
    public abstract void saveKeymap(String profileName,
            Map<ShortcutAction, Set<String>> actionToShortcuts);
    
    /**
     * Lists all profiles known to this KeymapManager.
     * @return the existing profile names.
     */
    public abstract List<String> getProfiles();

    /**
     * @return Currently active profile.
     */
    public abstract String getCurrentProfile();
    
    /**
     * Allows switching of the profiles.
     * @param profileName 
     */
    public abstract void setCurrentProfile(String profileName);
    
    /**
     * Deletes the given profile.
     * @param profileName 
     */
    public abstract void deleteProfile(String profileName);
 
    /**
     * The profile can be either default or custom.
     * @param profileName 
     * @return 
     */
    public abstract boolean isCustomProfile(String profileName);
    
    /**
     * @return this instance name (should be unique amongst all registered
     *      instances.
     */
    public final String getName() {
        return name;
    }
}
