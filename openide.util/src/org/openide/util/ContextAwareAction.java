/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import javax.swing.Action;

/**
 * Interface to be implemented by an action whose behavior
 * is dependent on some context.
 * The action created by {@link #createContextAwareInstance}
 * is bound to the provided context: {@link Action#isEnabled},
 * {@link Action#actionPerformed}, etc. may be specific to that context.
 * <p class="nonnormative">For example, the action representing a context menu item will usually implement
 * this interface. When the actual context menu is created, rather than making a
 * presenter for the generic action, the menu will contain a presenter for the
 * context-aware instance. The context will then be taken from the GUI
 * environment where the context menu was shown; for example it may be a
 * <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent's context</a>,
 * often taken from an activated node selection. The context action might be
 * enabled only if a certain "cookie" is present in that selection. When invoked,
 * the action need not search for an object to act on, since it can use the context.
 *
 * @author Jaroslav Tulach, Peter Zavadsky
 *
 * @see org.openide.util.Utilities#actionsToPopup
 * @since 3.29
 */
public interface ContextAwareAction extends Action {

    /**
     * Creates action instance for provided context.
     * @param actionContext an arbitrary context (e.g. "cookies" from a node selection)
     * @return a transient action whose behavior applies only to that context
     */
    public Action createContextAwareInstance(Lookup actionContext);

}
