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

package org.netbeans.core.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/** The action which invoke next "jump" getLine in output window (like next error)
*
* @author Petr Hamernik
*/
public class JumpNextAction extends CallbackSystemAction {

    protected void initialize() {
        super.initialize();
        setSurviveFocusChange(true);
        putProperty ("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void setEnabled (boolean b) {
        super.setEnabled (b);
    }

    protected String iconResource () {
        return "org/netbeans/core/resources/actions/nextOutJump.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (JumpNextAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(JumpNextAction.class).getString("JumpNextAction");
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public void setActionPerformer (org.openide.util.actions.ActionPerformer performer) {
        throw new java.lang.UnsupportedOperationException ();
    }
    public Object getActionMapKey () {
        return "jumpNext"; // NOI18N
    }
}
