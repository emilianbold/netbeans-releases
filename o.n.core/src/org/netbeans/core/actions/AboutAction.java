/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl.actions;

import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.ActionPerformer;
import com.netbeans.ide.util.actions.CallableSystemAction;

import com.netbeans.developer.impl.Splash;

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
    return "/com/netbeans/developer/impl/resources/actions/about.gif";
  }

  public HelpCtx getHelpCtx() {
    return new HelpCtx("com.netbeans.developer.docs.Users_Guide.usergd-action", "USERGD-ACTION-TABLE-3");
  }

  public String getName() {
    return NbBundle.getBundle (AboutAction.class).getString("About");
  }

}

/*
 * Log
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         1/20/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 */
