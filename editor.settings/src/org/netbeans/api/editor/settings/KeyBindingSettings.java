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

package org.netbeans.api.editor.settings;

/**
 * The list of available key bindings. Each key binding is represented by an
 * instance of the <code>MultiKeyBinding</code> class, which associates one
 * or more keyboard shortcuts with an <code>Action</code>.
 * 
 * <p>Instances of this class should be retrieved from <code>MimeLookup</code>.
 * 
 * <p><font color="red">This class must NOT be extended by any API clients.</font>
 *
 * @author Martin Roskanin
 */
public abstract class KeyBindingSettings {

    /**
     * Construction prohibited for API clients.
     */
    public KeyBindingSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.storage.KeyBindingSettingsImpl$Immutable".equals(getClass().getName())) { // NOI18N
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
