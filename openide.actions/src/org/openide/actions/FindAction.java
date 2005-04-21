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


/** Search for something.
*
* @author   Miloslav Metelka
*/
public class FindAction extends CallbackSystemAction {
    public String getName() {
        return NbBundle.getMessage(FindAction.class, "Find");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(FindAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/find.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }
}
