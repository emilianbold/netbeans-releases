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
import com.netbeans.ide.util.actions.CallbackSystemAction;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
* @version 0.10, Mar 01, 1998
*/
public class AboutAction extends CallbackSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 6074126305723764618L;
  /** URL of icon for this action */
  private static java.net.URL urlIcon = null;

  /** URL to this action.
  * @return URL to the action icon
  */
  public java.net.URL getDefaultIcon() {
    if (urlIcon == null)
      urlIcon = getClass().getResource("/com.netbeans.developer.impl.resources/actions/about.gif");
    return urlIcon;
  }

  public HelpCtx getHelpCtx() {
    return new HelpCtx("com.netbeans.developer.docs.Users_Guide.usergd-action", "USERGD-ACTION-TABLE-3");
  }

  public String getName() {
    return com.netbeans.developer.impl.Actions.getActionsBundle().getString("About");
  }

}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 */
