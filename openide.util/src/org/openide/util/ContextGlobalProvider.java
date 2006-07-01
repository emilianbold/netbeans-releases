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
package org.openide.util;

import org.openide.util.Lookup;


/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to. The global
 * context is accessible via {@link Utilities#actionsGlobalContext} method
 * and is expected to contain similar content as the context used when
 * context aware actions (see {@link ContextAwareAction}) are being
 * manipulated for example via method {@link Utilities#actionsToPopup}, so
 * in current state it is reasonable to put there all currently active
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html">Node</a>, their cookies and {@link javax.swing.ActionMap}.
 * By default this interface is implemented by window system to delegate
 * to currently activated <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent's  lookup</a>.
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
