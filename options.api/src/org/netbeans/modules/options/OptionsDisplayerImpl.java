/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
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
    static final LookupListener lookupListener = new LookupListenerImpl();
    
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
        
        DialogDescriptor descriptor = null;
        synchronized(lookupListener) {
            descriptor = (DialogDescriptor)descriptorRef.get ();
        }
        
        OptionsPanel optionsPanel = null;
        if (descriptor == null) {
            optionsPanel = categoryID == null ? new OptionsPanel () : new OptionsPanel(categoryID);            
            JButton bOK = (JButton) loc(new JButton(), "CTL_OK");//NOI18N
            bOK.getAccessibleContext().setAccessibleDescription(loc("ACS_OKButton"));//NOI18N
            JButton bClassic = (JButton) loc(new JButton(), "CTL_Classic");//NOI18N
            bClassic.getAccessibleContext().setAccessibleDescription(loc("ACS_ClassicButton"));//NOI18N
            boolean isMac = Utilities.isMac();
            Object[] options = new Object[2];            
            options[0] = isMac ? DialogDescriptor.CANCEL_OPTION : bOK;
            options[1] = isMac ? bOK : DialogDescriptor.CANCEL_OPTION;
            descriptor = new DialogDescriptor(optionsPanel,title,modal,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null, false);
            descriptor.setAdditionalOptions(new Object[] {bClassic});
            descriptor.setHelpCtx(optionsPanel.getHelpCtx());
            OptionsPanelListener listener = new OptionsPanelListener(descriptor, optionsPanel, bOK, bClassic);
            descriptor.setButtonListener(listener);
            optionsPanel.addPropertyChangeListener(listener);
            synchronized(lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(descriptor);
            }
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
                optionsPanel.save ();
                d.dispose ();
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION ||
                e.getSource () == DialogDescriptor.CLOSED_OPTION
            ) {
                log.fine("Options Dialog - Cancel pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                optionsPanel.cancel ();
                d.dispose ();                
            } else
            if (e.getSource () == bClassic) {
                log.fine("Options Dialog - Classic pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                if (optionsPanel.isChanged ()) {
                    Confirmation confirmationDescriptor = new Confirmation (
                        loc ("CTL_Some_values_changed"), 
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE
                    );
                    Object result = DialogDisplayer.getDefault ().
                        notify (confirmationDescriptor);
                    if (result == NotifyDescriptor.YES_OPTION) {
                        optionsPanel.save ();
                        d.dispose ();                        
                    } else
                    if (result == NotifyDescriptor.NO_OPTION) {
                        optionsPanel.cancel ();
                        d.dispose ();                        
                    } else {
                        dialog = d;
                        return;
                    }
                } else {
                    d.dispose ();
                    optionsPanel.cancel ();
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
            optionsPanel.cancel ();
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
        }

        public void windowClosed(WindowEvent e) {
            optionsPanel.storeUserSize();
            if (optionsPanel.needsReinit()) {
                synchronized (lookupListener) {
                    descriptorRef = new WeakReference<DialogDescriptor>(null);
                }
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
    
    private static class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            synchronized (lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(null);
            }
        }
        
    }
}

