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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.options.OptionsCategory.PanelController;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.NotifyDescriptor.Message;
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

    /** Link to dialog, if its opened. */
    private Dialog              dialog;
    /** weak link to options dialog DialogDescriptor. */
    private WeakReference       optionsDialogDescriptor = 
                                    new WeakReference (null);
    
    
    public OptionsWindowAction () {
        putValue (
            Action.NAME, 
            loc ("CTL_Options_Window_Action")
        );
    }

    public void actionPerformed (ActionEvent evt) {     
        if (dialog != null) {
            // dialog already opened
            dialog.setVisible (true);
            dialog.toFront ();
            return;
        }
        
        DialogDescriptor descriptor = (DialogDescriptor) 
            optionsDialogDescriptor.get ();
        
        if (descriptor == null) {
            // create new DialogDescriptor for options dialog
            JButton bClassic = (JButton) loc (new JButton (), "CTL_Classic");//NOI18N
            JButton bOK = (JButton) loc (new JButton (), "CTL_OK");//NOI18N

            OptionsPanel optionsPanel = new OptionsPanel ();
            descriptor = new DialogDescriptor (
                optionsPanel,
                "Options",
                false,
                new Object[] {
                    bOK,
                    DialogDescriptor.CANCEL_OPTION
                },
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null
            );
            descriptor.setAdditionalOptions (new Object[] {bClassic});
            descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            OptionsPanelListener listener = new OptionsPanelListener 
                (descriptor, optionsPanel, bOK, bClassic);
            descriptor.setButtonListener (listener);
            optionsPanel.addPropertyChangeListener (listener);
            //optionsDialogDescriptor = new WeakReference (descriptor);
        } else {
            OptionsPanel optionsPanel = (OptionsPanel) descriptor.getMessage ();
            optionsPanel.update ();
        }
        
        dialog = DialogDisplayer.getDefault ().createDialog (descriptor);
        dialog.setVisible (true);
        
        descriptor = null;
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
    
    private class OptionsPanelListener implements PropertyChangeListener,
    ActionListener {
        private DialogDescriptor    descriptor;
        private OptionsPanel        optionsPanel;
        private JButton             bOK;
        private JButton             bClassic;
        
        
        OptionsPanelListener (
            DialogDescriptor descriptor, 
            OptionsPanel optionsPanel,
            JButton bOK,
            JButton bClassic
        ) {
            this.descriptor = descriptor;
            this.optionsPanel = optionsPanel;
            this.bOK = bOK;
            this.bClassic = bClassic;
        }
        
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals (
                "buran" + PanelController.PROP_HELP_CTX)               //NOI18N
            )
                descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            else
            if (ev.getPropertyName ().equals (
                "buran" + PanelController.PROP_VALID)                  //NOI18N
            )
                bOK.setEnabled (optionsPanel.dataValid ());
        }
        
        public void actionPerformed (ActionEvent e) {
            if (e.getSource () == bOK) {
                dialog.setVisible (false);
                optionsPanel.save ();
                dialog = null;
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION) {
                dialog.setVisible (false);
                optionsPanel.cancel ();
                dialog = null;
            } else
            if (e.getSource () == bClassic) {
                if (optionsPanel.isChanged ()) {
                    Confirmation descriptor = new Confirmation (
                        loc ("CTL_Some_values_changed"), 
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE
                    );
                    if (DialogDisplayer.getDefault ().notify (descriptor) ==
                        NotifyDescriptor.OK_OPTION
                    ) {
                        dialog.setVisible (false);
                        optionsPanel.save ();
                    } else {
                        dialog.setVisible (false);
                        optionsPanel.cancel ();
                    }
                } else {
                    dialog.setVisible (false);
                    optionsPanel.cancel ();
                }
                dialog = null;
                try {
                    ClassLoader cl = (ClassLoader) Lookup.getDefault ().
                        lookup (ClassLoader.class);
                    Class clz = cl.loadClass 
                        ("org.netbeans.core.actions.OptionsAction");
                    CallableSystemAction a = (CallableSystemAction) 
                        SystemAction.findObject (clz, true);
                    a.putValue ("additionalActionName", loc ("CTL_Modern"));
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
                } catch (Exception ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            } // classic
        }
    }
}

