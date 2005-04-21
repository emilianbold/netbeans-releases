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


/** (Search-and-)replace (for example, in an Editor).
*
* @author   Miloslav Metelka
*/
public class ReplaceAction extends CallbackSystemAction {
    public String getName() {
        return NbBundle.getMessage(ReplaceAction.class, "Replace");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReplaceAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }
}
