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

package org.netbeans.modules.compapp.projects.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * A Factory to create dialog components for the NetBeans environment, that
 * display the progress of some work.  It uses the NetBeans Progress and
 * DialogDescriptor APIs for visualizing the tracking of the progress. It also
 * supports cancellable work.
 *
 * @see org.netbeans.api.progress
 * @see org.openide.util.Cancellable
 *
 * @author Tientien Li
 */
public final class ProgressDialogFactory {
    
    private ProgressDialogFactory() {
    }

    /**
     * Creates a progress dialog.
     *
     * @param title      Descriptive title for the dialog.
     * @param cancelable Whether or not the progress is cancelable.
     *
     * @return A ProgressDescriptor object for the requested progress indicator.
     */
    public static ProgressDescriptor createProgressDialog(
            final String title,
            final boolean cancelable) {

        // Create progress bar
        ProgressHandle pDesc = ProgressHandleFactory.createHandle(title);
        JComponent progressBar = ProgressHandleFactory.createProgressComponent(pDesc);
        ProgressPanel panel = new ProgressPanel(progressBar);
        
        // Disable cancel button if this is not a cancelable task.
        Component buttonCancel = new JButton(CANCEL_OPTION);
        buttonCancel.setEnabled(cancelable);
        
        ProgressDialogCancellationListener progressListener =
                new ProgressDialogCancellationListener(
                    panel,
                    buttonCancel
                );

        // Create dialog for progress bar
        DialogDescriptor dDesc = new DialogDescriptor(
                panel,
                makeDialogTitle(title),
                true,
                new Object[] { buttonCancel },
                buttonCancel,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                progressListener,
                false
                );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dDesc);
        
        // Create ProgressDescriptor
        ProgressDescriptor descriptor = new ProgressDescriptorImpl(dialog, pDesc);
        
        if (progressListener != null) {
            // Reference to controller, to update model state on cancelation.
            progressListener.setController(descriptor.getController());
            
            // Listener interested in Controller (not GUI)-actioned
            // cancelation and finalization; e.g., hook for interface
            // embellishments.
            dialog.addPropertyChangeListener(progressListener);
        }
        
        dialog.pack();
        dialog.setResizable(false);
        return descriptor;
    }
    
    private static String makeDialogTitle(String title) {
        String titlePrefix = NbBundle.getMessage(ProgressDialogFactory.class,
                "ProgressDialogFactory.dialog_title_prefix");
        return (titlePrefix != null ? titlePrefix : "").concat(title);
    }
    
    static class ProgressDescriptorImpl implements ProgressDescriptor {
        
        public ProgressDescriptorImpl(Dialog dialog, ProgressHandle progress) {
            
            if (dialog == null) {
                throw new NullPointerException("dialog");
            }
            
            if (progress == null) {
                throw new NullPointerException("progress");
            }
            
            this.dialog = dialog;
            controller = new ProgressController(progress, dialog);
        }

        public Component getGUIComponent() {
            return dialog;
        }

        public ProgressController getController() {
            return controller;
        }
        
        private final Dialog dialog;
        private final ProgressController controller;
    }
    
    static class ProgressDialogCancellationListener
            implements ActionListener, PropertyChangeListener {
        
        public ProgressDialogCancellationListener(
                ProgressPanel panel, Component component) {
            if (component == null) {
                throw new NullPointerException("component");
            }
            if (panel == null) {
                throw new NullPointerException("panel");
            }
            this.component = component;
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (e.getID() == ActionEvent.ACTION_PERFORMED) {
                if (component.equals(e.getSource())) {
                    component.setEnabled(false);
                    controller.setCanceled();
                    panel.getMessageComponent().setText(
                            NbBundle.getMessage(
                                    getClass(),
                                    "ProgressDialogFactory.progress_msg_canceled")
                            );
                }
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            
            // The task is finished (or canceled):
            if (FINISH_OPTION.equals(propertyName)) {
                component.setEnabled(false);
                panel.getMessageComponent().setText(
                        NbBundle.getMessage(
                                getClass(),
                                "ProgressDialogFactory.progress_msg_done")
                        );
            }
            
            else if (CANCEL_OPTION.equals(propertyName)) {
                component.setEnabled(false);
                panel.getMessageComponent().setText(
                        NbBundle.getMessage(
                                getClass(),
                                "ProgressDialogFactory.progress_msg_canceled")
                        );
            }
                
            else if (UPDATE_OPTION.equals(propertyName)) {
                JLabel label = panel.getMessageComponent();
                label.setText(String.valueOf(evt.getNewValue()));
            }
            
            else if (CANCEL_LOCKOUT_OPTION.equals(propertyName)) {
                component.setEnabled(false);
            }
        }
        
        public void setController(ProgressController controller) {
            this.controller = controller;
        }
        
        private final Component component;
        private final ProgressPanel panel;
        private ProgressController controller;
    }

    static final String CANCEL_OPTION = NbBundle.getMessage(
            ProgressDialogFactory.class,
            "ProgressDialogFactory.dialog_cancel_text");

    static final String FINISH_OPTION = "finish";
    static final String UPDATE_OPTION = "update";
    static final String CANCEL_LOCKOUT_OPTION = "lockout";
}
