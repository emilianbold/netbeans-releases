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

package org.netbeans.core.options.keymap.api;

import java.util.Set;

/**
 * Objects implementing this interface serve as providers for storing
 * the shortcuts definitions.
 * 
 * @author David Strupl
 */
public interface ShortcutsFinder {
    /**
     * Find a ShortcutAction for a given string representation of the shortcut.
     * @param shortcuts 
     * @return the shortcut
     */
    ShortcutAction findActionForShortcut(String shortcuts);
    
    /**
     * The actions can be assigned IDs when storing them. This method
     * finds a ShortcutAction instance by given id.
     * @param id 
     * @return the shortcut
     */
    
    ShortcutAction findActionForId(String id);
    /**
     * This method will show the shortcut selection dialog to the user.
     * @return The shortcut that the user has selected on <code>null</code>
     *      if the user has cancelled the dialog
     */
    
    String showShortcutsDialog();
    /**
     * Retrieve all the shortcuts assigned to a given action.
     * @param action 
     * @return all the shortcuts for the action.
     */
    String[] getShortcuts(ShortcutAction action);
    
    /**
     * Refreshes the model by reading the stored assignments.
     */
    void refreshActions();
    
    /**
     * Assigns the given shortcuts to the given action.
     * @param action 
     * @param shortcuts 
     */
    void setShortcuts(ShortcutAction action, Set shortcuts);
    
    /**
     * Applies the changes by storing them to the storage.
     */
    void apply();
}
