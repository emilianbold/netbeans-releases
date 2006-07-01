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
