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

package org.netbeans.modules.derby.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.Util;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DerbySystemHomePanel extends javax.swing.JPanel {

    private DialogDescriptor descriptor;
    private Color nbErrorForeground;
    
    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }
    };
    
    /**
     * Asks the user for the value of the Derby install location and system home. 
     * Never returns null.
     */
    public static boolean checkDerbyInstallAndHome() {
        String derbySystemHome = DerbyOptions.getDefault().getSystemHome();
        if (derbySystemHome.length() <= 0 || DerbyOptions.getDefault().isLocationNull()) {
            return derbySettings();
        }
        return true;
    }
    
    public static boolean derbySettings() {
        // since this could be called from StartAction, which is asynchronous
        return Mutex.EVENT.writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                DerbySystemHomePanel panel = new DerbySystemHomePanel();
                String title = NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_SetDerbySystemHome");

                DialogDescriptor desc = new DialogDescriptor(panel, title);
                panel.setDialogDescriptor(desc);
                
                for (;;) {                    
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
                    String acsd = NbBundle.getMessage(DerbySystemHomePanel.class, "ACSD_DerbySystemHomePanel");
                    dialog.getAccessibleContext().setAccessibleDescription(acsd);
                    dialog.setVisible(true);
                    dialog.dispose();
                    
                    if (!desc.OK_OPTION.equals(desc.getValue())) {
                        return Boolean.FALSE; // NOI18N
                    }
                    
                    File derbySystemHome = new File(panel.getDerbySystemHome());
                    
                    if (!derbySystemHome.exists()) {
                        boolean success = derbySystemHome.mkdirs();
                        if (!success) {
                            String message = NbBundle.getMessage(DerbySystemHomePanel.class, "ERR_DerbySystemHomeCantCreate");
                            NotifyDescriptor ndesc = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(ndesc);
                            continue;
                        }
                    }
                    DerbyOptions.getDefault().setSystemHome(panel.getDerbySystemHome());
                    DerbyOptions.getDefault().setLocation(panel.getInstallLocation());
                    return Boolean.TRUE;
                }
            }
        }).booleanValue();
    }
    
    private DerbySystemHomePanel() {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        derbySystemHomeTextField.getDocument().addDocumentListener(docListener);
        derbySystemHomeTextField.setText(DerbyOptions.getDefault().getSystemHome());
        derbyInstall.getDocument().addDocumentListener(docListener);
        derbyInstall.setText(DerbyOptions.getDefault().getLocation());
    }
    
    private void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validatePanel();
    }

    private String getDerbySystemHome() {
        return derbySystemHomeTextField.getText().trim();
    }
    
    private String getInstallLocation() {
        return derbyInstall.getText().trim();
    }
    
    private void setInstallLocation(String location) {
        derbyInstall.setText(location);
    }
    
    private void setDerbySystemHome(String derbySystemHome) {
        derbySystemHomeTextField.setText(derbySystemHome);
    }
    
    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        
        String location = getInstallLocation();
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", locationFile);
            }
            if (!Util.isDerbyInstallLocation(locationFile)) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", locationFile);
            }
        } else {
            error = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", location);
        }

        if (error == null) {
            File derbySystemHome = new File(getDerbySystemHome());

            if (derbySystemHome.getPath().length() <= 0) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotEntered");
            } else if (derbySystemHome.exists() && !derbySystemHome.isDirectory()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotDirectory");
            } else if (!derbySystemHome.isAbsolute()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotAbsolute");
            }
        }
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoScrollPane = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        derbySystemHomeLabel = new javax.swing.JLabel();
        derbySystemHomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        installLabel = new javax.swing.JLabel();
        derbyInstall = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        infoScrollPane1 = new javax.swing.JScrollPane();
        infoTextArea1 = new javax.swing.JTextArea();

        infoScrollPane.setBorder(null);
        infoScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        infoTextArea.setColumns(20);
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setRows(5);
        infoTextArea.setText(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_DerbySystemHomeInfo")); // NOI18N
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setFocusable(false);
        infoTextArea.setOpaque(false);
        infoScrollPane.setViewportView(infoTextArea);

        derbySystemHomeLabel.setLabelFor(derbySystemHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(derbySystemHomeLabel, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_DerbySystemHome")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        messageLabel.setForeground(nbErrorForeground);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.openide.awt.Mnemonics.setLocalizedText(installLabel, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Install")); // NOI18N

        derbyInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derbyInstallActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Browse")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        infoScrollPane1.setBorder(null);
        infoScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        infoTextArea1.setColumns(20);
        infoTextArea1.setEditable(false);
        infoTextArea1.setLineWrap(true);
        infoTextArea1.setRows(5);
        infoTextArea1.setText(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_InstallationInfo")); // NOI18N
        infoTextArea1.setWrapStyleWord(true);
        infoTextArea1.setFocusable(false);
        infoTextArea1.setOpaque(false);
        infoScrollPane1.setViewportView(infoTextArea1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(installLabel)
                    .add(derbySystemHomeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(derbyInstall, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, Short.MAX_VALUE)))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(infoScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addContainerGap(10, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(infoScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {browseButton, jButton1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(installLabel)
                    .add(derbyInstall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(derbySystemHomeLabel)
                    .add(browseButton)
                    .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        derbySystemHomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "ACSD_DerbySystemHomePanel_browseButton")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String location = getInstallLocation();
        if (location.length() > 0) {
            chooser.setSelectedFile(new File(location));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setInstallLocation(chooser.getSelectedFile().getAbsolutePath());
}//GEN-LAST:event_jButton1ActionPerformed

    private void derbyInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derbyInstallActionPerformed
    // TODO add your handling code here:
    
}//GEN-LAST:event_derbyInstallActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String derbySystemHome = getDerbySystemHome();
        if (derbySystemHome.length() > 0) {
            chooser.setSelectedFile(new File(derbySystemHome));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setDerbySystemHome(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton browseButton;
    public javax.swing.JTextField derbyInstall;
    public javax.swing.JLabel derbySystemHomeLabel;
    public javax.swing.JTextField derbySystemHomeTextField;
    public javax.swing.JScrollPane infoScrollPane;
    public javax.swing.JScrollPane infoScrollPane1;
    public javax.swing.JTextArea infoTextArea;
    public javax.swing.JTextArea infoTextArea1;
    public javax.swing.JLabel installLabel;
    public javax.swing.JButton jButton1;
    public javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
    
}
