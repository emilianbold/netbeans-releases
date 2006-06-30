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

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.projectimport.j2seimport.ImportProcess;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Shows status(progress) of importing.
 *
 * @author Radek Matous
 */
public class ProgressPanel extends javax.swing.JPanel {
    public static void showProgress(final ImportProcess iProcess) {
        final ProgressHandle ph = iProcess.getProgressHandle();
        final ProgressPanel progressPanel = new ProgressPanel(ph);
        String title = NbBundle.getMessage(ProgressPanel.class, "CTL_ProgressDialogTitle");//NOI18N
        
        DialogDescriptor desc = new DialogDescriptor(progressPanel,title,true, new Object[]{}, null, 0, null, null);
        desc.setClosingOptions(new Object[]{});
        
        final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(desc);
        ((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iProcess.isFinished()) {
                    progressTimer.stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                }
            }
        });
        iProcess.startImport(true); // runs importing in separate thread
        progressTimer.start();
        progressDialog.setVisible(true);        
    }
    
    
    /** Creates new form ProgressPanel */
    private ProgressPanel(final ProgressHandle progressHandle) {
        initComponents(progressHandle);
    }
    
    
    private void initComponents(final ProgressHandle progressHandle) {
        java.awt.GridBagConstraints gridBagConstraints;
        
        setLayout(new java.awt.GridBagLayout());
        
        setPreferredSize(new java.awt.Dimension(450, 80));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(ProgressHandleFactory.createProgressComponent(progressHandle), gridBagConstraints);
    }    
}
