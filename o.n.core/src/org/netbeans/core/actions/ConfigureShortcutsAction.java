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

import com.netbeans.developer.impl.ShortcutsEditor;

/** The action that shows the Configere Shortcuts dialog.
*
* @author Ian Formanek
*/
public class ConfigureShortcutsAction extends org.openide.util.actions.CallableSystemAction {

  /** Shows the dialog.
  */
  public void performAction () {
    ShortcutsEditor se = new ShortcutsEditor ();
    DialogDescriptor dd = new DialogDescriptor (se, NbBundle.getBundle (ConfigureShortcutsAction.class).getString("CTL_ConfigureShortcuts_Title"));
    TopManager.getDefault ().createDialog (dd).show ();
    if (dd.getValue() == DialogDescriptor.OK_OPTION) {
      try {
        if (se.isModified ()) ShortcutsEditor.saveCustomKeys ();
      } catch (java.io.IOException e) {
        TopManager.getDefault ().notifyException (e); // [PENDING]
      }
    } else {
      ShortcutsEditor.installCurrentBindings (); // Cancel the modifications performed in Configure Shortcuts dialog
    }
  }

  /** URL to this action.
  * @return URL to the action icon
  */
  public String iconResource () {
    return "/com/netbeans/developer/impl/resources/actions/configureShortcuts.gif"; // NOI18N
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
 *  7    Gandalf   1.6         2/7/00   David Simonek   Icon resource added
 *  6    Gandalf   1.5         1/5/00   Ian Formanek    NOI18N
 *  5    Gandalf   1.4         1/4/00   Ian Formanek    
 *  4    Gandalf   1.3         12/21/99 Ian Formanek    
 *  3    Gandalf   1.2         12/1/99  Ian Formanek    
 *  2    Gandalf   1.1         11/30/99 Ian Formanek    
 *  1    Gandalf   1.0         11/30/99 Ian Formanek    
 * $
 */
