/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.actions;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;

/** SystemExit action.
* @author   Ian Formanek
* @version  0.14, Feb 13, 1998
*/
public class SystemExit extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5198683109749927396L;

    
    public SystemExit() {
        super();
     
        // Important flag.
        // See org.netbeans.core.ModuleActions#addRunningActions into.
        putValue("ModuleActions.ignore", Boolean.TRUE); // NOI18N
    }
    
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

    public void performAction() {
        org.openide.TopManager.getDefault().exit();
    }



}
