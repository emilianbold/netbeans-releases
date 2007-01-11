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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;


public class OptionsDisplayerImpl {
    /** Link to dialog, if its opened. */
    private static Dialog           dialog;
    /** weak link to options dialog DialogDescriptor. */
    private static WeakReference<DialogDescriptor>    descriptorRef = new WeakReference<DialogDescriptor> (null);
    private static String title = loc("CTL_Options_Dialog_Title");    
    private static Logger log = Logger.getLogger(OptionsDisplayerImpl.class.getName ());    
    private boolean modal;
    
    public OptionsDisplayerImpl (boolean modal) {
        this.modal = modal;
    }
    
    public boolean isOpen() {
        return dialog != null;
    }
    
    public void showOptionsDialog (String categoryID) {
        if (isOpen()) {
            // dialog already opened
            dialog.setVisible (true);
            dialog.toFront ();
            log.fine("Front Options Dialog"); //NOI18N
            return;
        }
                
        DialogDescriptor descriptor = (DialogDescriptor) 
            descriptorRef.get ();
        
        OptionsPanel optionsPanel = null;
        if (descriptor == null) {
            optionsPanel = categoryID == null ? new OptionsPanel () : new OptionsPanel(categoryID);            
            JButton bOK = (JButton) loc(new JButton(), "CTL_OK");//NOI18N
            JButton bClassic = (JButton) loc(new JButton(), "CTL_Classic");//NOI18N
            boolean isMac = Utilities.isMac();
            Object[] options = new Object[2];            
            options[0] = isMac ? DialogDescriptor.CANCEL_OPTION : bOK;
            options[1] = isMac ? bOK : DialogDescriptor.CANCEL_OPTION;
            descriptor = new DialogDescriptor(optionsPanel,title,modal,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setAdditionalOptions(new Object[] {bClassic});
            descriptor.setHelpCtx(optionsPanel.getHelpCtx());
            OptionsPanelListener listener = new OptionsPanelListener(descriptor, optionsPanel, bOK, bClassic);
            descriptor.setButtonListener(listener);
            optionsPanel.addPropertyChangeListener(listener);
            descriptorRef = new WeakReference<DialogDescriptor>(descriptor);
            log.fine("Create new Options Dialog"); //NOI18N
        } else {
            optionsPanel = (OptionsPanel) descriptor.getMessage ();            
            //TODO: 
            //just in case that switched from advanced
            optionsPanel.update();
            log.fine("Reopen Options Dialog"); //NOI18N
        }
        
        dialog = DialogDisplayer.getDefault ().createDialog (descriptor);
        optionsPanel.initCurrentCategory(categoryID);
        dialog.addWindowListener (new MyWindowListener (optionsPanel));
        dialog.setVisible (true);        
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsDisplayerImpl.class, key);
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
            DialogDescriptor    descriptor, 
            OptionsPanel        optionsPanel,
            JButton             bOK,
            JButton             bClassic
        ) {
            this.descriptor = descriptor;
            this.optionsPanel = optionsPanel;
            this.bOK = bOK;
            this.bClassic = bClassic;
        }
        
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_HELP_CTX)) {               //NOI18N            
                descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            } else if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_VALID)) {                  //NOI18N            
                bOK.setEnabled (optionsPanel.dataValid ());
            }
        }
        
        @SuppressWarnings("unchecked")
        public void actionPerformed (ActionEvent e) {
            if (!isOpen()) return; //WORKARROUND for some bug in NbPresenter
                // listener is called twice ...
            if (e.getSource () == bOK) {
                log.fine("Options Dialog - Ok pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                d.dispose ();
                RequestProcessor.getDefault ().post (new Runnable () {
                   public void run () {
                        optionsPanel.save ();
                   } 
                });
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION ||
                e.getSource () == DialogDescriptor.CLOSED_OPTION
            ) {
                log.fine("Options Dialog - Cancel pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                d.dispose ();
                RequestProcessor.getDefault ().post (new Runnable () {
                   public void run () {
                        optionsPanel.cancel ();
                   } 
                });
            } else
            if (e.getSource () == bClassic) {
                log.fine("Options Dialog - Classic pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                if (optionsPanel.isChanged ()) {
                    Confirmation descriptor = new Confirmation (
                        loc ("CTL_Some_values_changed"), 
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE
                    );
                    Object result = DialogDisplayer.getDefault ().
                        notify (descriptor);
                    if (result == NotifyDescriptor.YES_OPTION) {
                        d.dispose ();
                        RequestProcessor.getDefault ().post (new Runnable () {
                           public void run () {
                                optionsPanel.save ();
                           } 
                        });
                    } else
                    if (result == NotifyDescriptor.NO_OPTION) {
                        d.dispose ();
                        RequestProcessor.getDefault ().post (new Runnable () {
                           public void run () {
                                optionsPanel.cancel ();
                           } 
                        });
                    } else {
                        dialog = d;
                        return;
                    }
                } else {
                    d.dispose ();
                    RequestProcessor.getDefault ().post (new Runnable () {
                       public void run () {
                            optionsPanel.cancel ();
                       } 
                    });
                }
                try {
                    ClassLoader cl = (ClassLoader) Lookup.getDefault ().lookup (ClassLoader.class);
                    Class<CallableSystemAction> clz = (Class<CallableSystemAction>)cl.loadClass("org.netbeans.core.actions.OptionsAction");
                    CallableSystemAction a = SystemAction.findObject(clz, true);
                    a.putValue ("additionalActionName", loc ("CTL_Modern"));
                    a.putValue ("optionsDialogTitle", loc ("CTL_Classic_Title"));
                    a.putValue ("additionalActionListener", new OpenOptionsListener ()
                    );
                    a.performAction ();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } // classic
        }
    }
    
    private class MyWindowListener implements WindowListener {        
        private OptionsPanel optionsPanel;
        private Dialog originalDialog;
        
                
        MyWindowListener (OptionsPanel optionsPanel) {
            this.optionsPanel = optionsPanel;
            this.originalDialog = dialog;
        }
        
        public void windowClosing (WindowEvent e) {
            if (dialog == null) return;
            log.fine("Options Dialog - windowClosing "); //NOI18N
            RequestProcessor.getDefault ().post (new Runnable () {
               public void run () {
                    optionsPanel.cancel ();
               } 
            });
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
        }

        public void windowClosed(WindowEvent e) {
            optionsPanel.storeUserSize();
            if (optionsPanel.needsReinit()) {
                descriptorRef = new WeakReference<DialogDescriptor>(null);
            }
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
            log.fine("Options Dialog - windowClosed"); //NOI18N
        }
        public void windowDeactivated (WindowEvent e) {}
        public void windowOpened (WindowEvent e) {}
        public void windowIconified (WindowEvent e) {}
        public void windowDeiconified (WindowEvent e) {}
        public void windowActivated (WindowEvent e) {}
    }
    
    class OpenOptionsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            log.fine("Options Dialog - Back to modern."); //NOI18N
                            //OptionsDisplayerImpl.this.showOptionsDialog(null);
                            OptionsDisplayer.getDefault().open();
                        }
                    });
                }
            });
        }
    }
}

