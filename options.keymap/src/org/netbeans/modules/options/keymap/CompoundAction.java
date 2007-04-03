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

package org.netbeans.modules.options.keymap;

import java.util.Map;
import org.netbeans.core.options.keymap.api.ShortcutAction;

/**
 *
 * @author Jan Jancura, David Strupl
 */
public class CompoundAction implements ShortcutAction {
    private static final String DEFAULT_PROVIDER = "EditorBridge";
    private Map<String, ShortcutAction> actions;

    public CompoundAction(Map<String, ShortcutAction> actions) {
        this.actions = actions;
    }

    public String getDisplayName () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDisplayName();
}
        for (ShortcutAction sa: actions.values()) {
            String dn = sa.getDisplayName();
            if (dn != null) {
                return dn;
            }
        }
        return "<error>"; // TODO:
    }

    public String getId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getId();
            if (id != null) {
                return id;
            }
        }
        return "<error>"; // TODO:
    }

    public String getDelegatingActionId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDelegatingActionId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getDelegatingActionId();
            if (id != null) {
                return id;
            }
        }
        return null; // TODO:
    }
    
    public boolean equals (Object o) {
        if (! (o instanceof CompoundAction)) {
            return false;
        }
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return (getKeymapManagerInstance(DEFAULT_PROVIDER).equals(
                ((CompoundAction)o).getKeymapManagerInstance(DEFAULT_PROVIDER)
            ));
        }
        if (actions.keySet().isEmpty()) {
            return false;
        }
        String k = actions.keySet().iterator().next();
        return (getKeymapManagerInstance(k).equals(
                ((CompoundAction)o).getKeymapManagerInstance(k)
            ));
    }
    
    public int hashCode () {
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return getKeymapManagerInstance(DEFAULT_PROVIDER).hashCode() * 2;
        }
        if (actions.keySet().isEmpty()) {
            return 0;
        }
        String k = actions.keySet().iterator().next();
        return actions.get(k).hashCode() * 2;
    }

    public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
        return actions.get(keymapManagerName);
    }
    
    public String toString() {
        return "CompoundAction[" + actions + "]";
    }
}
