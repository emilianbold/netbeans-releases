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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;

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
    return new org.openide.util.HelpCtx (AboutAction.class);
  }

  public String getName() {
    return NbBundle.getBundle (AboutAction.class).getString("About");
  }

}

/*
 * Log
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  6    Gandalf   1.5         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         1/20/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 */
