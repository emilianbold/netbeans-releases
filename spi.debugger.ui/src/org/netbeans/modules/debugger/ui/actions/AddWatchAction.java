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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.Utils;

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

        JPanel panel = new JPanel();
        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JTextField textField;
        JLabel textLabel = new JLabel (bundle.getString ("CTL_Watch_Name")); // NOI18N
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add ("West", textLabel); // NOI18N
        panel.add ("Center", textField = new JTextField (25)); // NOI18N
        textField.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        textField.setBorder (
            new CompoundBorder (textField.getBorder (), 
            new EmptyBorder (2, 0, 2, 0))
        );
        textLabel.setDisplayedMnemonic (
            bundle.getString ("CTL_Watch_Name_Mnemonic").charAt (0) // NOI18N
        );
        String t = Utils.getIdentifier ();
        if (t != null)
            textField.setText (t);
        else
            textField.setText (watchHistory);
        textField.selectAll ();
            
        textLabel.setLabelFor (textField);
        textField.requestFocus ();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
            panel, 
            bundle.getString ("CTL_Watch_Title") // NOI18N
        );
        dd.setHelpCtx (new HelpCtx ("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
        dialog.setVisible(true);
        dialog.dispose ();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        String watch = textField.getText();
        if ((watch == null) || (watch.trim ().length () == 0)) {
            return;
        }
        
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
        new WatchesAction ().actionPerformed (null);
    }
}
