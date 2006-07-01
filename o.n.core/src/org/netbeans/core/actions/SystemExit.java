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

    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
}
