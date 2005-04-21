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


/** Delete an object.
*
* @author   Ian Formanek
*/
public class DeleteAction extends CallbackSystemAction {
    public DeleteAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected void initialize() {
        super.initialize();
    }

    public Object getActionMapKey() {
        return "delete"; // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(DeleteAction.class, "Delete");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(DeleteAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/delete.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return true;
    }
}
