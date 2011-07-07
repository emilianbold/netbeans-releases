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

import org.openide.*;
import org.openide.util.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  Yang Su
 */
public class AuthSubscriptionForm extends javax.swing.JPanel {
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox cbxAdd;
    private javax.swing.JComboBox cmbGroup;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton rbAccept;
    private javax.swing.JRadioButton rbDeny;
    // End of variables declaration//GEN-END:variables
    private CollabPrincipal subscriber;
    private String subscriberName;
    private CollabSession session;

    /** Creates new form AuthSubscriptionForm */
    public AuthSubscriptionForm(CollabSession session, CollabPrincipal subscriber) {
        this.subscriber = subscriber;
        subscriberName = subscriber.getDisplayName();
        this.session = session;

        initComponents();

        if (isInContactList(subscriber)) {
            cbxAdd.setVisible(false);
            cmbGroup.setVisible(false);
        }

        ContactGroup[] groups = session.getContactGroups();

        if (groups.length == 0) {
            try {
                session.createContactGroup(AddContactForm.DEFAULT_CONTACT_LIST);
                groups = session.getContactGroups();
            } catch (CollabException ce) {
                Debug.errorManager.notify(ce);
            }
        }

        for (int i = 0; i < groups.length; i++) {
            cmbGroup.addItem(groups[i].getName());
        }

        updateUI();
    }

    public boolean approve() {
        DialogDescriptor descriptor = new DialogDescriptor(
                this, NbBundle.getMessage(AuthSubscriptionForm.class, "TITLE_AuthSubscriptionForm")
            ); // NOI18N

        descriptor.setOptions(new Object[] { DialogDescriptor.OK_OPTION });
        descriptor.setClosingOptions(null);
        descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
        descriptor.setMessageType(DialogDescriptor.QUESTION_MESSAGE);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

        try {
            dialog.setVisible(true);

            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                if (rbAccept.isSelected()) {
                    if (cbxAdd.isSelected()) {
                        String groupName = (String) cmbGroup.getSelectedItem();

                        ContactGroup group = session.getContactGroup(groupName);
                        assert group != null : "Contact group was null; shouldn't be possible";

                        if (group.getContact(subscriber.getIdentifier()) == null) {
                            group.addContact(subscriber);
                        }
                    }

                    return true;
                }
            }
        } catch (CollabException ce) {
            Debug.errorManager.notify(ce);
        }
        finally {
            dialog.dispose();
        }

        return false;
    }

    /**
     *
     *
     */
    private boolean isInContactList(CollabPrincipal subscriber) {
        ContactGroup[] groups = session.getContactGroups();

        for (int i = 0; i < groups.length; i++) {
            CollabPrincipal[] contacts = groups[i].getContacts();

            for (int j = 0; j < contacts.length; j++) {
                if (contacts[j] == subscriber) {
                    return true;
                }
            }
        }

        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        rbAccept = new javax.swing.JRadioButton();
        rbDeny = new javax.swing.JRadioButton();
        cbxAdd = new javax.swing.JCheckBox();
        cmbGroup = new javax.swing.JComboBox();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(AuthSubscriptionForm.class, "LBL_AuthSubscriptionForm_Subscriber", subscriberName)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel2, gridBagConstraints);

        buttonGroup1.add(rbAccept);
        rbAccept.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbAccept, org.openide.util.NbBundle.getMessage(AuthSubscriptionForm.class, "BTN_AuthSubscriptionForm_Accept")); // NOI18N
        rbAccept.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(rbAccept, gridBagConstraints);

        buttonGroup1.add(rbDeny);
        org.openide.awt.Mnemonics.setLocalizedText(rbDeny, org.openide.util.NbBundle.getMessage(AuthSubscriptionForm.class, "BTN_AuthSubscriptionForm_Deny")); // NOI18N
        rbDeny.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(rbDeny, gridBagConstraints);

        cbxAdd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbxAdd, NbBundle.getMessage(AuthSubscriptionForm.class, "CBX_AuthSubscriptionForm_AddTo", subscriberName)); // NOI18N
        cbxAdd.addItemListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(cbxAdd, gridBagConstraints);

        cmbGroup.setPreferredSize(new java.awt.Dimension(100, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel1.add(cmbGroup, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ItemListener {
        FormListener() {}
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == rbAccept) {
                AuthSubscriptionForm.this.rbAcceptItemStateChanged(evt);
            }
            else if (evt.getSource() == rbDeny) {
                AuthSubscriptionForm.this.rbDenyItemStateChanged(evt);
            }
            else if (evt.getSource() == cbxAdd) {
                AuthSubscriptionForm.this.cbxAddItemStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents
    private void rbDenyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbDenyItemStateChanged
        if (rbDeny.isSelected()) {
            cbxAdd.setEnabled(false);
            cmbGroup.setEnabled(false);
        }
    }//GEN-LAST:event_rbDenyItemStateChanged

    private void cbxAddItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxAddItemStateChanged
        cmbGroup.setEnabled(cbxAdd.isSelected());
    }//GEN-LAST:event_cbxAddItemStateChanged

    private void rbAcceptItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbAcceptItemStateChanged

        if (rbAccept.isSelected()) {
            cbxAdd.setEnabled(true);
            cbxAdd.setSelected(true);
            cmbGroup.setEnabled(true);
        }
    }//GEN-LAST:event_rbAcceptItemStateChanged
}
