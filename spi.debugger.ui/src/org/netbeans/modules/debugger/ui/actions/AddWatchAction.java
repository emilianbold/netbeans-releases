/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.ui.WatchPanel;

import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;


/**
 * DebuggerManager Window action.
 *
 * @author   Jan Jancura
 */
public class AddWatchAction extends CallableSystemAction {

    private static String watchHistory = ""; // NOI18N

    
    public AddWatchAction () {
        // The action is not in the toolbar by default, so it should not have the
        // icon in the menu.
        putValue("noIconInMenu", Boolean.TRUE);
    }

    protected boolean asynchronous () {
        return false;
    }

    public String getName () {
        return NbBundle.getMessage (
            AddWatchAction.class,
            "CTL_New_Watch"
        );
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (AddWatchAction.class);

    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "org/netbeans/modules/debugger/resources/actions/NewWatch.gif"; // NOI18N
    }
    
    public void performAction () {
        ResourceBundle bundle = NbBundle.getBundle (AddWatchAction.class);

        WatchPanel wp = new WatchPanel (watchHistory);
        JComponent panel = wp.getPanel ();

        // <RAVE>
        // Add help ID for 'Add Watch' dialog
        // org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
        //      panel,
        //      bundle.getString ("CTL_WatchDialog_Title") // NOI18N
        // );
        // ====
        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
            panel, 
            bundle.getString ("CTL_WatchDialog_Title"), // NOI18N
            true,
            org.openide.DialogDescriptor.OK_CANCEL_OPTION,
            null,
            org.openide.DialogDescriptor.DEFAULT_ALIGN,
            new org.openide.util.HelpCtx("debug.add.watch"),
            null
        );
        // </RAVE>
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
        dialog.setVisible (true);
        dialog.dispose ();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        String watch = wp.getExpression ();
        if ( (watch == null) || 
             (watch.trim ().length () == 0)
        )   return;
        
        String s = watch;
        int i = s.indexOf (';');
        while (i > 0) {
            String ss = s.substring (0, i).trim ();
            if (ss.length () > 0)
                DebuggerManager.getDebuggerManager ().createWatch (ss);
            s = s.substring (i + 1);
            i = s.indexOf (';');
        }
        s = s.trim ();
        if (s.length () > 0)
            DebuggerManager.getDebuggerManager ().createWatch (s);
        
        watchHistory = watch;
        
        // open watches view
        ViewActions.openComponent ("watchesView", false).requestVisible();
    }
}
