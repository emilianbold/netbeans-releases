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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Peter Liu
 */
public class ProgressDialog {
    private ProgressHandle pHandle;
    private Dialog dialog;
    
    public ProgressDialog(String title) {
        createDialog(title);
    }
    
    public void open() {
        while (dialog != null) {
            dialog.setVisible(true);
        }
    }
    
    public void close() {
        if (dialog != null) {
            Dialog oldDialog = dialog;
            dialog = null;
            oldDialog.setVisible(false);
            oldDialog.dispose();
        }
    }
    
    public ProgressHandle getProgressHandle() {
        return pHandle;
    }
    
    private void createDialog(String title) {
        pHandle = ProgressHandleFactory.createHandle(title);
        JPanel panel = new ProgressPanel(pHandle);
        
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, title
                ); // NOI18N
        
        final Object[] OPTIONS = new Object[0];
        descriptor.setOptions(OPTIONS);
        descriptor.setClosingOptions(OPTIONS);
        descriptor.setModal(true);
        descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        int windowX = mainWindow.getX();
        int windowY = mainWindow.getY();
        int windowWidth = mainWindow.getWidth();
        int windowHeight = mainWindow.getHeight();
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();
        int dialogX = (int)(windowWidth/2.0) - (int)(dialogWidth/2.0);
        int dialogY = (int)(windowHeight/2.0) - (int)(dialogHeight/2.0);
        
        dialog.setLocation(dialogX, dialogY);
    }
    
    private class ProgressPanel extends JPanel {
        private JLabel messageLabel;
        private JComponent progressBar;
        
        public ProgressPanel(ProgressHandle pHandle) {
            messageLabel = ProgressHandleFactory.createDetailLabelComponent(pHandle);
            messageLabel.setText(NbBundle.getMessage(RESTServicesFromEntitiesIterator.class,
                    "MSG_StartingProgress"));
            progressBar = ProgressHandleFactory.createProgressComponent(pHandle);
            
            initComponents();
        }
        
        private void initComponents() {
            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE))
                    .addContainerGap())
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(messageLabel)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
        }
        
        
        public Dimension getPreferredSize() {
            Dimension orig = super.getPreferredSize();
            return new Dimension(500, orig.height);
        }
        
    }
    
}
