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

package org.netbeans.core.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/**
 * A skeleton action, useful just as a placeholder for global shortcut.
 * Components wishing to use it's shortcut should place the "NextViewAction" key into their action maps.
 *
 * @author mkleint
 */
public class NextViewCallbackAction extends CallbackSystemAction {

    /** Creates a new instance of NextViewCallbackAction */
    public NextViewCallbackAction() {
    }

    public String getName() {
        return NbBundle.getMessage(NextViewCallbackAction.class, "LBL_NextViewCallbackAction");
    }

    public Object getActionMapKey() {
        return "NextViewAction"; //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
    
}
