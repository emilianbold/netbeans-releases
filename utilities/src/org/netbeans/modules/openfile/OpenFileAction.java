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

package org.netbeans.modules.openfile;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Opens a file by file chooser. */
public class OpenFileAction extends CallableSystemAction {

    static final long serialVersionUID =-3424129228987962529L;
    public String getName () {
        return SettingsBeanInfo.getString ("LBL_openFile");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (OpenFileAction.class);
    }

    protected String iconResource () {
        return "/org/netbeans/modules/openfile/openFile.gif"; // NOI18N
    }

    /** Last-used directory. */
    private static File currDir = null;
    public void performAction () {
        JFileChooser chooser = new JFileChooser ();
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled (true);
        if (currDir != null) chooser.setCurrentDirectory (currDir);
        while (chooser.showOpenDialog (null) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles ();

            if (files.length == 0) { // selected file doesn't exist
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    SettingsBeanInfo.getString("MSG_noFileSelected"),NotifyDescriptor.WARNING_MESSAGE));
                continue;
            }

            for (int i = 0; i < files.length; i++)
                OpenFile.open (files[i], false, null, 0, -1);

            break;
        }
        currDir = chooser.getCurrentDirectory ();
    }

}
