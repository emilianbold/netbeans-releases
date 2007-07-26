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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.Util;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Despite the name, serves as a settings dialog for Derby (not only
 * system home, but also database location).
 * 
 * @author Andrei Badea
 */
public class DerbySystemHomePanel extends javax.swing.JPanel {
    
    // XXX rename to something more meaningful, e.g., DerbySettingsPanel

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
    
    public static boolean showDerbySettings() {
        assert SwingUtilities.isEventDispatchThread();
        
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
                return false; // NOI18N
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
            return true;
        }
    }
    
    private DerbySystemHomePanel() {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        jTextPane1.setBackground(getBackground());
        messageLabel.setBackground(getBackground());
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

        derbySystemHomeLabel = new javax.swing.JLabel();
        derbySystemHomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        installLabel = new javax.swing.JLabel();
        derbyInstall = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jTextPane1 = new javax.swing.JTextPane();
        messageLabel = new javax.swing.JTextPane();

        derbySystemHomeLabel.setLabelFor(derbySystemHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(derbySystemHomeLabel, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_DerbySystemHome")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(installLabel, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Install")); // NOI18N

        derbyInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derbyInstallActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Browse2")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextPane1.setEditable(false);
        jTextPane1.setText(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_InstallationInfo")); // NOI18N
        jTextPane1.setAutoscrolls(false);

        messageLabel.setEditable(false);
        messageLabel.setForeground(nbErrorForeground);
        messageLabel.setText(" ");
        messageLabel.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(jTextPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(derbySystemHomeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(installLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(derbyInstall, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTextPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(installLabel)
                    .add(derbyInstall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(derbySystemHomeLabel)
                    .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
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
    public javax.swing.JLabel installLabel;
    public javax.swing.JButton jButton1;
    public javax.swing.JTextPane jTextPane1;
    public javax.swing.JTextPane messageLabel;
    // End of variables declaration//GEN-END:variables
    
}
