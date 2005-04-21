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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;


/** Create a clone of the current cloneable top component.
* @see org.openide.windows.CloneableTopComponent#clone
*
* @author   Petr Hamernik, Ian Formanek
*/
public class CloneViewAction extends CallbackSystemAction {
    public Object getActionMapKey() {
        return "cloneWindow"; // NOI18N
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getMessage(CloneViewAction.class, "CloneView");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CloneViewAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/clone.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }
}
