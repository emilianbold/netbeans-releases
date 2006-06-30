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

package org.netbeans.modules.j2ee.sun.ide.j2ee.db;



import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;


/** Action that can always be invoked and work procedurally.
 * This action will display the URL for the given admin server node in the runtime explorer
 * @author  ludo
 */
public class StopAction extends CallableSystemAction {

    public StopAction(){
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public boolean isEnabled() {
        return (RegisterPointbase.getDefault().isRunning()==true);
    }
    
    public void performAction()  {
        RegisterPointbase.getDefault().stop();
        
    }
    
    
    public String getName() {
        return NbBundle.getMessage(StopAction.class, "LBL_StopAction");
    }
    
    
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    
    
    protected boolean asynchronous() {
        return true;
    }
    
    
}
