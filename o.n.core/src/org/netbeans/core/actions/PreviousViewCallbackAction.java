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

package org.netbeans.core.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/**
 * A skeleton action, useful just as a placeholder for global shortcut.
 * Components wishing to use it's shortcut should place the "PreviousViewAction" key into their action maps.

 * @author mkleint
 */
public class PreviousViewCallbackAction extends CallbackSystemAction {
    
    /** Creates a new instance of PreviousViewCallbackAction */
    public PreviousViewCallbackAction() {
    }

    public String getName() {
        return NbBundle.getMessage(PreviousViewCallbackAction.class, "LBL_PreviousViewCallbackAction");
    }

    public Object getActionMapKey() {
        return "PreviousViewAction";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }    
}
