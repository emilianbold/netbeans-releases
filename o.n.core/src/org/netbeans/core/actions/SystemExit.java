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

import org.openide.LifecycleManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Shut down the system.
 * @author Ian Formanek, Jesse Glick, et al.
 */
public class SystemExit extends CallableSystemAction implements Runnable {
    
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 5198683109749927396L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle(SystemExit.class).getString("Exit");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SystemExit.class);
    }
    
    protected boolean asynchronous() {
        // Not managed alongside other actions.
        return false;
    }

    public void performAction() {
        // Do not run in AWT.
        RequestProcessor.getDefault().post(this);
    }

    /* Performs the exit (by calling LifecycleManager).*/
    public void run() {
        LifecycleManager.getDefault().exit();
    }
    
}
