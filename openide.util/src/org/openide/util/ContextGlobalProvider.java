/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util;

import org.openide.util.Lookup;


/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to. The global
 * context is accessible via {@link Utilities#actionsGlobalContext} method
 * and is expected to contain similar content as the context used when
 * context aware actions (see {@link ContextAwareAction}) are being
 * manipulated for example via method {@link Utilities#actionsToPopup}, so
 * in current state it is reasonable to put there all currently active
 * {@link org.openide.nodes.Node}, their cookies and {@link javax.swing.ActionMap}.
 * By default this interface is implemented by window system to delegate
 * to currently activated {@link org.openide.windows.TopComponent}'s lookup.
 *
 * @author Jaroslav Tulach
 * @since 4.10
*/
public interface ContextGlobalProvider {
    /** Creates the context in form of Lookup.
     * @return the context
     */
    public abstract Lookup createGlobalContext();
}
