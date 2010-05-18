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
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  sherylsu
 */
public class AddContactGroupForm extends javax.swing.JPanel {
    private CollabSession session;
    private DialogDescriptor dialogDescriptor;

    /**
     *
     *
     */
    public AddContactGroupForm(CollabSession session) {
        this.session = session;

        initComponents();

        dialogDescriptor = new DialogDescriptor(
                this, NbBundle.getMessage(AddContactGroupForm.class, "TITLE_AddContactGroupForm")
            ); // NOI18N
        dialogDescriptor.setValid(false);

        // Listen to changes to the search field in order to enable or 
        // disable the find button
        groupTextField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    dialogDescriptor.setValid(groupTextField.getText().length() > 0);
                }

                public void removeUpdate(DocumentEvent e) {
                    dialogDescriptor.setValid(groupTextField.getText().length() > 0);
                }
            }
        );
    }

    /**
     *
     *
     */
    public void addContactGroup() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);

        try {
            dialog.show();

            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                if (sessionExists()) {
                    String groupName = groupTextField.getText().trim();

                    if (session.getContactGroup(groupName) != null) {
                        // Notify user that group already exists
                        String message = NbBundle.getMessage(
                                AddContactGroupForm.class, "MSG_AddContactGroupForm_GroupAlreadyExists", groupName
                            );
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                    } else {
                        try {
                            session.createContactGroup(groupName);
                        } catch (CollabException e) {
                            String message = NbBundle.getMessage(
                                    AddContactGroupForm.class, "MSG_AddContactGroupForm_GroupNotCreated",
                                    new Object[] { groupName, e.getMessage() }
                                );
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                            Debug.debugNotify(e);
                        }
                    }
                }
            }
        } finally {
            dialog.dispose();
        }
    }

    private boolean sessionExists() {
        // check session status #5080138
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].equals(this.session)) {
                return true;
            }
        }

        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        groupLbl = new javax.swing.JLabel();
        groupTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        groupLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_AddContactGroupForm_EnterGroupName"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(groupLbl, gridBagConstraints);

        groupTextField.setColumns(32);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(groupTextField, gridBagConstraints);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel groupLbl;
    private javax.swing.JTextField groupTextField;
    // End of variables declaration//GEN-END:variables
}
