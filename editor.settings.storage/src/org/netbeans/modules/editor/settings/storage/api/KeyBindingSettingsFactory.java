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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;


/**
 * Getters and setters for keymap editor profiles. Instances of this 
 * class should be registerred in {@MimeLookup} for particular mime types.
 *
 * @author Jan Jancura
 */
public abstract class KeyBindingSettingsFactory {
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public abstract List/*<MultiKeyBinding>*/ getKeyBindings ();
    
    /**
     * Gets the keybindings list for given keymap name, where items 
     * are instances of {@link MultiKeyBinding}.
     *
     * @param keymapName a name of keymap
     * @return List of {@link MultiKeyBinding}
     */
    public abstract List/*<MultiKeyBinding>*/ getKeyBindings 
            (String profile);

    
    /**
     * Returns default keybindings list for given keymap name, where items 
     * are instances of {@link MultiKeyBinding}.
     *
     * @return List of {@link MultiKeyBinding}
     */
    public abstract List getKeyBindingDefaults (String profile);
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public abstract void setKeyBindings (
        String profile, 
        List/*<MultiKeyBinding>*/ keyBindings
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
