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

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;

import com.netbeans.developer.impl.ShortcutsEditor;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
*/
public class ConfigureShortcutsAction extends CallableSystemAction {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 6074126305723764618L;

  /** Shows the dialog.
  */
  public void performAction () {
    DialogDescriptor dd = new DialogDescriptor (new ShortcutsEditor (), NbBundle.getBundle (ConfigureShortcutsAction.class).getString("CTL_ConfigureShortcuts_Title"));
    TopManager.getDefault ().createDialog (dd).show ();
    // [PENDING - if OK pressed, install shortcuts]
  }

  /** URL to this action.
  * @return URL to the action icon
  */
  public String iconResource () {
    return "/com/netbeans/developer/impl/resources/actions/empty.gif";
  }

  public HelpCtx getHelpCtx() {
    return new org.openide.util.HelpCtx (ConfigureShortcutsAction.class);
  }

  public String getName() {
    return NbBundle.getBundle (ConfigureShortcutsAction.class).getString("CTL_ConfigureShortcuts");
  }

}

/*
 * Log
 *  2    Gandalf   1.1         11/30/99 Ian Formanek    
 *  1    Gandalf   1.0         11/30/99 Ian Formanek    
 * $
 */
