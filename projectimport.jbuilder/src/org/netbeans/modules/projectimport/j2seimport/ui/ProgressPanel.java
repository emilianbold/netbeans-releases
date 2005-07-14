/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.WindowConstants;
//import org.netbeans.api.progress.ProgressHandle;
//import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.projectimport.jbuilder.ImportAction;
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
        final ProgressPanel progressPanel = new ProgressPanel(/*iProcess.getProgressHandle()*/);
        String title = NbBundle.getMessage(ProgressPanel.class, "CTL_ProgressDialogTitle");//NOI18N
        
        DialogDescriptor desc = new DialogDescriptor(progressPanel,title,true, new Object[]{}, null, 0, null, null);
        desc.setClosingOptions(new Object[]{});
        
        final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(desc);
        ((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressPanel.progressBar.setValue(iProcess.getCurrentStep());
                progressPanel.progressBar.setString(iProcess.getCurrentStatus());
                if (iProcess.isFinished()) {
                    progressTimer.stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                }
            }
        });
        iProcess.startImport(true); // runs importing in separate thread
        progressPanel.progressBar.setMaximum(iProcess.getNumberOfSteps());
        progressTimer.start();
        progressDialog.setVisible(true);        
    }
    
    
    /** Creates new form ProgressPanel */
    private ProgressPanel(/*final ProgressHandle progressHandle*/) {
        initComponents(/*progressHandle*/);
    }
    
    
    private void initComponents(/*final ProgressHandle progressHandle*/) {
        progressBar = new javax.swing.JProgressBar();        
        java.awt.GridBagConstraints gridBagConstraints;
        
        setLayout(new java.awt.GridBagLayout());
        
        setPreferredSize(new java.awt.Dimension(450, 80));
        progressBar.setString("");
        progressBar.setStringPainted(true);        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        //add(ProgressHandleFactory.createProgressComponent(progressHandle), gridBagConstraints);
        add(progressBar,gridBagConstraints);
    }
    
    private javax.swing.JProgressBar progressBar;    
}
