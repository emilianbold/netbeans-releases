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

package org.netbeans.api.editor.settings;

/**
 * KeyBindings settings are represented by List of keybindings.
 * The List contains the instances of {@link MultiKeyBinding}.
 * <br>
 * Instances of this class should be retrieved from the {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * for a given mime-type.
 * <br>
 * <font color="red">This class must NOT be extended by any API clients</font>
 *
 * @author Martin Roskanin
 */
public abstract class KeyBindingSettings {

    /**
     * Construction prohibited for API clients.
     */
    public KeyBindingSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.storage.KeyBindingSettingsImpl".equals(getClass().getName())) {
            throw new IllegalStateException("Instantiation prohibited."); // NOI18N
        }
    }
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public abstract java.util.List/*<MultiKeyBinding>*/ getKeyBindings();

}
