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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.modules.debugger.ui.views.WatchesView;

import org.openide.DialogDisplayer;
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

    
//    public AddWatchAction () {
//        putValue (
//            Action.NAME, 
//            NbBundle.getMessage (
//                AddWatchAction.class, 
//                "CTL_New_Watch"
//            )
//        );
//        putValue (
//            Action.SMALL_ICON, 
//            Utils.getIcon (
//                "org/netbeans/modules/debugger/resources/actions/NewWatch" // NOI18N
//            )
//        );
//    }

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
        WatchesViewAction.openComponent (WatchesView.class, false);
    }
}
