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

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.core.ShortcutsEditor;

/** The action that shows the Configere Shortcuts dialog.
*
* @author Ian Formanek
*/
public class ConfigureShortcutsAction extends org.openide.util.actions.CallableSystemAction {

    /** Shows the dialog.
    */
    public void performAction () {
        ShortcutsEditor se = new ShortcutsEditor ();
        DialogDescriptor dd = new DialogDescriptor (
            se,
            NbBundle.getBundle (ConfigureShortcutsAction.class).getString("CTL_ConfigureShortcuts_Title"),
            true,
            new Object[] {DialogDescriptor.OK_OPTION},
            DialogDescriptor.OK_OPTION, 
            DialogDescriptor.BOTTOM_ALIGN,
            null,
            null
        );
        TopManager.getDefault ().createDialog (dd).show ();
//        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
//            se.flushChanges();
//        } else {
//            se.resetChanges();
//        }
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/core/resources/actions/configureShortcuts.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (ConfigureShortcutsAction.class);
    }

    public String getName() {
        return NbBundle.getBundle (ConfigureShortcutsAction.class).getString("CTL_ConfigureShortcuts");
    }

}
