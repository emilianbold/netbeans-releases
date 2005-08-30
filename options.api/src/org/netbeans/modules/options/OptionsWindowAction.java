/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.options;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.NewAction;
import org.openide.actions.OpenAction;
import org.openide.actions.RedoAction;
import org.openide.actions.SaveAction;
import org.openide.actions.UndoAction;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class OptionsWindowAction extends AbstractAction {

    private WeakReference       optionsPanel;
    private WeakReference       dialog;
    private int                 index = -1;
    
    
    public OptionsWindowAction () {
        putValue (
            Action.NAME, 
            loc ("CTL_Options_Window_Action")
        );
        putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/options/test/stop.png" // NOI18N
        );
    }

    public void actionPerformed (ActionEvent evt) {     
        Object source = evt.getSource ();
        if (source == DialogDescriptor.OK_OPTION) {
            OptionsPanel op = (OptionsPanel) optionsPanel.get ();
            Dialog d = (Dialog) dialog.get ();
            op.save ();
            index = op.getCurrentIndex ();
            d.setVisible (false);
            dialog = null;
        } else
//        if (evt.getActionCommand ().equals ("Apply")) {
//            optionsPanel.save ();
//            optionsPanel = null;
//        } else
        if (source == DialogDescriptor.CANCEL_OPTION) {
            OptionsPanel op = (OptionsPanel) optionsPanel.get ();
            Dialog d = (Dialog) dialog.get ();
            op.cancel ();
            index = op.getCurrentIndex ();
            d.setVisible (false);
            dialog = null;
        } else
        if (evt.getActionCommand ().equals ("Classic")) {
            OptionsPanel op = (OptionsPanel) optionsPanel.get ();
            Dialog d = (Dialog) dialog.get ();
            op.cancel ();
            index = op.getCurrentIndex ();
            d.setVisible (false);
            dialog = null;
            try {
                ClassLoader cl = (ClassLoader) Lookup.getDefault ().lookup (ClassLoader.class);
                Class clz = cl.loadClass ("org.netbeans.core.actions.OptionsAction");
                CallableSystemAction a = (CallableSystemAction) SystemAction.
                    findObject (clz, true);
                a.putValue ("additionalActionName", loc ("Modern"));
                a.putValue (
                    "additionalActionListener", 
                    new ActionListener () {
                        public void actionPerformed (ActionEvent e) {
                            RequestProcessor.getDefault ().post (new Runnable () {
                                public void run () {
                                    OptionsWindowAction.this.actionPerformed 
                                        (new ActionEvent (this, 0, "Open"));
                                }
                            });
                        }
                    }
                );
                a.performAction ();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            JButton bApply = (JButton) loc (new JButton (), "Apply");
//            bApply.setActionCommand ("Classic");                      //NOI18N
            Dialog d = dialog == null ? null : (Dialog) dialog.get ();
            if (d != null) {
                d.setVisible (true);
                d.toFront ();
                return;
            }
            JButton bClassic = (JButton) loc (new JButton (), "Classic");
            bClassic.setActionCommand ("Classic");                      //NOI18N
            OptionsPanel op = optionsPanel == null ? 
                null : (OptionsPanel) optionsPanel.get ();
            if (op == null) {
                op = new OptionsPanel ();
                optionsPanel = new WeakReference (op);
            }
            
            final DialogDescriptor descriptor = new DialogDescriptor (
                op,
                "Options",
                false,
                new Object[] {
                    DialogDescriptor.OK_OPTION,
                    //bApply,
                    DialogDescriptor.CANCEL_OPTION
                },
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, this
            );
            if (index >= 0)
                op.setCurrentIndex (index);
            descriptor.setAdditionalOptions (new Object[] {bClassic});
            descriptor.setHelpCtx (op.getHelpCtx ());
            final OptionsPanel op1 = op;
            op.addPropertyChangeListener ("helpCtx", new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent evt) {
                    descriptor.setHelpCtx (op1.getHelpCtx ());
                }
            });
            d = DialogDisplayer.getDefault ().createDialog (descriptor);
            d.setVisible (true);
            dialog = new WeakReference (d);
        }
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsWindowAction.class, key);
    }
    
    private static Component loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
        return c;
    }
}

