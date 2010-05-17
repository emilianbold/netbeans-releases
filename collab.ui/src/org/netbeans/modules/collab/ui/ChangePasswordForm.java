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
 */
package org.netbeans.modules.collab.ui;

import java.awt.Dialog;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.*;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;

public class ChangePasswordForm extends javax.swing.JPanel {

    private DialogDescriptor dialogDescriptor;
    private CollabSession session;

    /** Creates new form ChangePasswordForm */
    public ChangePasswordForm(CollabSession session) {
        initComponents();

        this.session = session;

        dialogDescriptor = new DialogDescriptor(
                this, NbBundle.getMessage(ChangePasswordForm.class, "TITLE_ChangePasswordForm")
            ); // NOI18N
        dialogDescriptor.setValid(false);

        DocumentListener docListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    checkValidity();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkValidity();
                }
            };

        newPasswdTextField.getDocument().addDocumentListener(docListener);
        confirmTextField.getDocument().addDocumentListener(docListener);
    }

    private void checkValidity() {
        String message = "";
        char[] password = newPasswdTextField.getPassword();
        char[] passwordConfirmation = confirmTextField.getPassword();
        boolean valid = (password.length > 0) && (passwordConfirmation.length > 0);

        if (valid && !new String(password).equals(new String(passwordConfirmation))) {
            valid = false;
            message = NbBundle.getMessage(ChangePasswordForm.class, "MSG_ChangePasswordForm_PasswordMismatch"); // NOI18N
            messageLabel.setText(message);
        }

        if (valid) {
            messageLabel.setText("");
        }

        dialogDescriptor.setValid(valid);
    }

    /**
     *
     *
     */
    public void changePassword() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);

        try {
            dialog.setVisible(true);

            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                char[] password = newPasswdTextField.getPassword();
                session.changePassword(new String(password));
            }
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        } finally {
            dialog.dispose();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents                      
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        newPasswdLabel = new javax.swing.JLabel();
        newPasswdTextField = new javax.swing.JPasswordField();
        confirmLabel = new javax.swing.JLabel();
        confirmTextField = new javax.swing.JPasswordField();
        messageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 5, 5)));
        setMinimumSize(new java.awt.Dimension(300, 95));
        setPreferredSize(new java.awt.Dimension(300, 95));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setMinimumSize(new java.awt.Dimension(200, 95));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 95));
        newPasswdLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_ChangePasswordForm_NewPassword"
            )
        );
        newPasswdLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel2.add(newPasswdLabel, gridBagConstraints);

        newPasswdTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel2.add(newPasswdTextField, gridBagConstraints);

        confirmLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_ChangePasswordForm_Confirm"
            )
        );
        confirmLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel2.add(confirmLabel, gridBagConstraints);

        confirmTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel2.add(confirmTextField, gridBagConstraints);

        messageLabel.setForeground(new java.awt.Color(0, 0, 204));
        messageLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        messageLabel.setMaximumSize(new java.awt.Dimension(200, 30));
        messageLabel.setMinimumSize(new java.awt.Dimension(200, 14));
        messageLabel.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel2.add(messageLabel, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel confirmLabel;
    private javax.swing.JPasswordField confirmTextField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel newPasswdLabel;
    private javax.swing.JPasswordField newPasswdTextField;
    // End of variables declaration//GEN-END:variables
}
