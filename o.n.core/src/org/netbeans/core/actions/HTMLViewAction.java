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
import com.netbeans.ide.util.actions.ActionPerformer;
import com.netbeans.ide.util.actions.CallableSystemAction;

/** Opens a HTML Browser on the home URL specified in IDESettings.
*
* @author Ian Formanek
* @version 0.10, May 24, 1998
*/
public class HTMLViewAction extends CallableSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 281181711813174400L;

  /** URL of icon for this action */
  private static java.net.URL urlIcon = null;

  /** URL to this action.
  * @return URL to the action icon
  */
  public java.net.URL getDefaultIcon() {
    if (urlIcon == null)
      urlIcon = getClass().getResource("/com.netbeans.developer.impl.resources/actions/htmlView.gif");
    return urlIcon;
  }

  public void performAction() {
    com.netbeans.ide.TopManager.getDefault ().setStatusText (com.netbeans.developer.impl.Actions.getActionsBundle().getString("CTL_OpeningBrowser"));
    com.netbeans.ide.TopManager.getDefault ().showUrl (com.netbeans.developer.impl.IDESettings.getRealHomeURL ());
    com.netbeans.ide.TopManager.getDefault ().setStatusText ("");
  }

  public String getName() {
    return com.netbeans.developer.impl.Actions.getActionsBundle().getString("HTMLView");
  }

  /** @return the action's help context */
  public HelpCtx getHelpCtx() {
    return new HelpCtx("com.netbeans.developer.docs.Users_Guide.usergd-action", "USERGD-ACTION-TABLE-4");
  }

}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
