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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
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
     * Asks the user for the value of the Derby system home. Never returns null.
     */
    public static String findDerbySystemHome() {
        // since this could be called from StartAction, which is asynchronous
        return Mutex.EVENT.writeAccess(new Mutex.Action<String>() {
            public String run() {
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
                        return ""; // NOI18N
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
                    
                    return panel.getDerbySystemHome();
                }
            }
        });
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
    }
    
    private void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validatePanel();
    }

    private String getDerbySystemHome() {
        return derbySystemHomeTextField.getText().trim();
    }
    
    private void setDerbySystemHome(String derbySystemHome) {
        derbySystemHomeTextField.setText(derbySystemHome);
    }
    
    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        File derbySystemHome = new File(getDerbySystemHome());
        
        if (derbySystemHome.getPath().length() <= 0) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotEntered");
        } else if (derbySystemHome.exists() && !derbySystemHome.isDirectory()) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotDirectory");
        } else if (!derbySystemHome.isAbsolute()) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotAbsolute");
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

        infoScrollPane.setBorder(null);
        infoScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        infoTextArea.setColumns(20);
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setRows(5);
        infoTextArea.setText(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_DerbySystemHomeInfo"));
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setFocusable(false);
        infoTextArea.setOpaque(false);
        infoScrollPane.setViewportView(infoTextArea);

        derbySystemHomeLabel.setLabelFor(derbySystemHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(derbySystemHomeLabel, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_DerbySystemHome"));

        derbySystemHomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField"));

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "LBL_Browse"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbySystemHomePanel.class, "ACSD_DerbySystemHomePanel_browseButton"));

        messageLabel.setForeground(nbErrorForeground);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, infoScrollPane, 0, 505, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(derbySystemHomeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(derbySystemHomeTextField, 0, 280, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, messageLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(derbySystemHomeLabel)
                    .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(infoScrollPane, 0, 134, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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
    public javax.swing.JLabel derbySystemHomeLabel;
    public javax.swing.JTextField derbySystemHomeTextField;
    public javax.swing.JScrollPane infoScrollPane;
    public javax.swing.JTextArea infoTextArea;
    public javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
    
}
