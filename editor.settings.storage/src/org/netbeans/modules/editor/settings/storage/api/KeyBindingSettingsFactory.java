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
