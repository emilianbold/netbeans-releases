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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.core.Splash;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
* @version 0.10, Mar 01, 1998
*/
public class AboutAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6074126305723764618L;

    /** Shows the dialog.
    */
    public void performAction () {
        Splash.showSplashDialog ();
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "org/netbeans/core/resources/actions/about.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (AboutAction.class);
    }

    public String getName() {
        return NbBundle.getBundle (AboutAction.class).getString("About");
    }

}
