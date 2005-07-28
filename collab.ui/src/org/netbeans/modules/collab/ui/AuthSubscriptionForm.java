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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.ContactGroup;

import org.openide.*;
import org.openide.util.*;

import java.awt.Dialog;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
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
    private javax.swing.JLabel groupLabel;
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

        //		rbAccept.setVisible(false);
        //		rbDeny.setVisible(false);
        groupLabel.setVisible(false);

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

        //		String acceptOption=NbBundle.getMessage(AuthSubscriptionForm.class,
        //			"LBL_AuthSubscriptionForm_AcceptOption");
        //		String denyOption=NbBundle.getMessage(AuthSubscriptionForm.class,
        //			"LBL_AuthSubscriptionForm_DenyOption");
        //		descriptor.setOptions(new Object[] {acceptOption, denyOption});
        descriptor.setOptions(new Object[] { DialogDescriptor.OK_OPTION });
        descriptor.setClosingOptions(null);
        descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
        descriptor.setMessageType(DialogDescriptor.QUESTION_MESSAGE);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

        try {
            dialog.show();

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

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        rbAccept = new javax.swing.JRadioButton();
        rbDeny = new javax.swing.JRadioButton();
        cbxAdd = new javax.swing.JCheckBox();
        cmbGroup = new javax.swing.JComboBox();
        groupLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 0, 5)));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText(
            NbBundle.getMessage(AuthSubscriptionForm.class, "LBL_AuthSubscriptionForm_Subscriber", subscriberName)
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jLabel2, gridBagConstraints);

        buttonGroup1.add(rbAccept);
        rbAccept.setSelected(true);
        rbAccept.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_AuthSubscriptionForm_Accept"
            )
        );
        rbAccept.addItemListener(
            new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    rbAcceptItemStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(rbAccept, gridBagConstraints);

        buttonGroup1.add(rbDeny);
        rbDeny.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_AuthSubscriptionForm_Deny"
            )
        );
        rbDeny.addItemListener(
            new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    rbDenyItemStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(rbDeny, gridBagConstraints);

        cbxAdd.setSelected(true);
        cbxAdd.setText(
            NbBundle.getMessage(AuthSubscriptionForm.class, "CBX_AuthSubscriptionForm_AddTo", subscriberName)
        );
        cbxAdd.addItemListener(
            new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    cbxAddItemStateChanged(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(cbxAdd, gridBagConstraints);

        cmbGroup.setPreferredSize(new java.awt.Dimension(100, 19));
        cmbGroup.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel1.add(cmbGroup, gridBagConstraints);

        groupLabel.setLabelFor(cmbGroup);
        groupLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_AddContactForm_AddContactTo"
            )
        );
        groupLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 0);
        jPanel1.add(groupLabel, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }

    // </editor-fold>//GEN-END:initComponents
    private void rbDenyItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_rbDenyItemStateChanged

        if (rbDeny.isSelected()) {
            cbxAdd.setEnabled(false);
        } else {
            cbxAdd.setEnabled(true);
            cbxAdd.setSelected(true);
        }
    } //GEN-LAST:event_rbDenyItemStateChanged

    private void cbxAddItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbxAddItemStateChanged

        if (cbxAdd.isSelected()) {
            groupLabel.enable();
            cmbGroup.enable();
        } else {
            groupLabel.disable();
            cmbGroup.disable();
        }

        updateUI();
    } //GEN-LAST:event_cbxAddItemStateChanged

    private void rbAcceptItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_rbAcceptItemStateChanged

        if (rbAccept.isSelected()) {
            cbxAdd.setEnabled(true);
            cbxAdd.setSelected(true);
        }
    } //GEN-LAST:event_rbAcceptItemStateChanged
}
