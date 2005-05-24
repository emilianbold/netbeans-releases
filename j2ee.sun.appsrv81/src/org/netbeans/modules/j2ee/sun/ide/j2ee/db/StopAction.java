/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
