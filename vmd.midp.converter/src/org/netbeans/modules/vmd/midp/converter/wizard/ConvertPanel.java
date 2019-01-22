/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 */package org.netbeans.modules.vmd.midp.converter.wizard;

import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * 
 */
public final class ConvertPanel extends javax.swing.JPanel implements ActionListener, Runnable, DocumentListener {
    
    private DialogDescriptor descriptor = new DialogDescriptor (this, NbBundle.getMessage (ConvertPanel.class, "TITLE_ConvertPanel")); // NOI18N
    private JButton startButton = new JButton (NbBundle.getMessage (ConvertPanel.class, "DISP_Start")); // NOI18N
    private JButton finishButton = new JButton (NbBundle.getMessage (ConvertPanel.class, "DISP_Close")); // NOI18N
    private FileObject inputPrimaryFile;
    private FileObject inputSecondaryFile;
    private FileObject outputDirectory;

    /** Creates new form ConvertPanel */
    public ConvertPanel() {
        initComponents();
        ImageIcon warningMessage = ImageUtilities.loadImageIcon("org/netbeans/modules/vmd/midp/resources/warning.gif", false);
        finishIcon.setIcon (warningMessage); // NOI18N
        message.setIcon (warningMessage); // NOI18N
        startButton.setDefaultCapable(true);
        startButton.addActionListener(this);
        finishButton.setDefaultCapable(true);
        descriptor.setClosingOptions (new Object[] { finishButton, DialogDescriptor.CANCEL_OPTION });
    }

    public DialogDescriptor getDialogDescriptor () {
        return descriptor;
    }
    
    public void switchToShown (FileObject inputPrimaryFile, FileObject inputSecondaryFile, FileObject outputDirectory) {
        this.inputPrimaryFile = inputPrimaryFile;
        this.inputSecondaryFile = inputSecondaryFile;
        this.outputDirectory = outputDirectory;
        this.inputFileName.setText (inputPrimaryFile.getName ());
        progress.setIndeterminate(false);
        progress.setValue(100);
        finishIcon.setVisible (false);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_ShownMessage")); // NOI18N
        startButton.setEnabled(true);
        descriptor.setOptions(new Object[] { startButton, DialogDescriptor.CANCEL_OPTION });

        outputFileName.getDocument ().removeDocumentListener (this);
        outputFileName.getDocument ().addDocumentListener (this);
        outputFileName.setText ("Converted" + inputPrimaryFile.getName ()); // NOI18N
        outputFileName.setEditable(true);
        outputFileName.selectAll ();
        outputFileName.requestFocus ();
    }
    
    public void switchToStarted () {
        outputFileName.getDocument ().removeDocumentListener (this);
        outputFileName.setEditable(false);
        
        progress.setIndeterminate(true);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_StartMessage")); // NOI18N
        startButton.setEnabled(false);
        descriptor.setOptions(new Object[0]);
    }
    
    public void switchToFinished () {
        progress.setIndeterminate(false);
        progress.setValue(100);
        finishIcon.setVisible (true);
        finishMessage.setText(NbBundle.getMessage (ConvertPanel.class, "MSG_FinishMessage")); // NOI18N
        descriptor.setOptions(new Object[] { finishButton });
    }

    public void switchToErrors (ArrayList<String> errors) {
        progress.setIndeterminate(false);
        progress.setValue(100);
        finishIcon.setVisible (true);
        StringBuffer str = new StringBuffer();
        for (String string : errors) {
            str.append(string);
            str.append('\n'); // NOI18N
        }
        finishMessage.setText(str.toString());
        descriptor.setOptions(new Object[] { finishButton });
    }

    public void insertUpdate (DocumentEvent e) {
        checkErrors ();
    }

    public void removeUpdate (DocumentEvent e) {
        checkErrors ();
    }

    public void changedUpdate (DocumentEvent e) {
        checkErrors ();
    }

    private void checkErrors () {
        boolean exists = outputDirectory.getFileObject (outputFileName.getText (), "java") != null // NOI18N
            || outputDirectory.getFileObject (outputFileName.getText (), "vmd") != null; // NOI18N
        message.setVisible(exists);
        startButton.setEnabled (! exists);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        inputFileName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        outputFileName = new javax.swing.JTextField();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        progress = new javax.swing.JProgressBar();
        finishMessage = new javax.swing.JLabel();
        finishIcon = new javax.swing.JLabel();
        message = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 400));

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/converter/wizard/Bundle").getString("ConvertPanel.jLabel1.mnemonic").charAt(0));
        jLabel2.setLabelFor(inputFileName);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel2.text")); // NOI18N

        inputFileName.setEditable(false);
        inputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.inputFileName.text")); // NOI18N

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/converter/wizard/Bundle").getString("ConvertPanel.jLabel2.mnemonic").charAt(0));
        jLabel1.setLabelFor(outputFileName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel1.text")); // NOI18N

        outputFileName.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.outputFileName.text")); // NOI18N

        finishMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        finishIcon.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.finishIcon.text")); // NOI18N
        finishIcon.setPreferredSize(new java.awt.Dimension(16, 16));

        message.setText(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.message.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(finishIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(finishMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outputFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                            .addComponent(inputFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(progress, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(message, javax.swing.GroupLayout.PREFERRED_SIZE, 480, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(inputFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(outputFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(finishIcon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(finishMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(message)
                .addContainerGap())
        );

        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel2.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ConvertPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ACC_NAME_ConvertPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConvertPanel.class, "ACC_DESC_ConvertPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel finishIcon;
    private javax.swing.JLabel finishMessage;
    private javax.swing.JTextField inputFileName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel message;
    private javax.swing.JTextField outputFileName;
    private javax.swing.JProgressBar progress;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed (ActionEvent e) {
        switchToStarted ();
        RequestProcessor.getDefault ().post (this);
    }

    public void run () {
        final ArrayList<String> errors = new ArrayList<String>();
        try {
            ArrayList<String> _errors = Converter.convert (inputPrimaryFile, inputSecondaryFile, outputFileName.getText ());
            if (!_errors.isEmpty()) {
                errors.addAll(_errors);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace (e);
        }
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                if (errors.isEmpty()) {
                    switchToFinished ();
                } else {
                    switchToErrors (errors);
                }
            }
        });
    }

}
