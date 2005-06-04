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

package org.netbeans.modules.openide.actions;


/** Implements the delegation to ActionManager that is called from
 * openide/util.
 */
public class ActionsBridgeImpl extends org.netbeans.modules.openide.util.ActionsBridge {
    /** Invokes an action.
     */
    protected void invokeAction (javax.swing.Action action, java.awt.event.ActionEvent ev) {
        org.openide.actions.ActionManager.getDefault().invokeAction(action, ev);
    }
}
