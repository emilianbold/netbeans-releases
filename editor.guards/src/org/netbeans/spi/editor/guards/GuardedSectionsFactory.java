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

package org.netbeans.spi.editor.guards;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;

/**
 * The factory allows to look up {@link GuardedSectionsProvider} factories for given
 * mime type. Factories have to be registered under <code>Editors/&lt;mime path&gt;</code>
 * in the module's layer.
 * 
 * 
 * 
 * @author Jan Pokorsky
 */
public abstract class GuardedSectionsFactory {

    /**
     * Use this to find a proper factory instance for the passed mime path.
     * @param mimePath a mime path
     * @return the factory instance or <code>null</code>
     */
    public static GuardedSectionsFactory find(String mimePath) {
        MimePath mp = MimePath.get(mimePath);
        GuardedSectionsFactory factory = null;
        if (mp != null) {
            factory = MimeLookup.getLookup(mp).lookup(GuardedSectionsFactory.class);
        }
        return factory;
    }
    
    /**
     * Creates a guarded sections provider.
     * @param editor an editor support
     * @return the provider impl
     */
    public abstract GuardedSectionsProvider create(GuardedEditorSupport editor);
    
}
