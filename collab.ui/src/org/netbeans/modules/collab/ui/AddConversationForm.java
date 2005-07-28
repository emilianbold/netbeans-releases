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
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.util.*;

import java.awt.Dialog;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  sherylsu
 */
public class AddConversationForm extends javax.swing.JPanel implements ListSelectionListener {
    // End of variables declaration//GEN-END:variables
    private static boolean _isSearching = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseBtn;
    private javax.swing.JTextField conferenceNameField;
    private javax.swing.JLabel conferenceNameLabel;
    private javax.swing.JButton findBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeBtn;
    private javax.swing.JList resultJList;
    private javax.swing.JLabel searchMessageLabel;
    private CollabSession session;
    private Vector result = new Vector();
    private DialogDescriptor descriptor;

    /** Creates new form AddConversationForm */
    public AddConversationForm(CollabSession session) {
        this.session = session;
        initialize();
    }

    private void initialize() {
        descriptor = new DialogDescriptor(
                this, NbBundle.getMessage(AddConversationForm.class, "TITLE_AddConversationForm")
            );

        descriptor.setValid(false);
        initComponents();

        ListModel model = new ListModel(resultJList, result, false, true, false);

        ListRenderer renderer = new ListRenderer(model);
        resultJList.setCellRenderer(renderer);
        resultJList.setModel(model);
        resultJList.addListSelectionListener(this);

        // Listen to changes to the search field in order to enable or 
        // disable the find button
        conferenceNameField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    findBtn.setEnabled(
                        (conferenceNameField.getText() != null) && (conferenceNameField.getText().length() > 0)
                    );
                }

                public void removeUpdate(DocumentEvent e) {
                    findBtn.setEnabled(
                        (conferenceNameField.getText() != null) && (conferenceNameField.getText().length() > 0)
                    );
                }
            }
        );
    }

    public void addConversation() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

        try {
            dialog.show();

            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                Iterator it = result.iterator();

                while (it.hasNext()) {
                    if (sessionExists()) {
                        try {
                            session.subscribePublicConversation((String) it.next());
                        } catch (CollabException e) {
                            Debug.out.println(e.getMessage());
                        }
                    }
                }
            }
        } finally {
            dialog.dispose();
        }
    }

    private void getAllConferences() {
        if (!sessionExists()) {
            return;
        }

        try {
            String[] conversations = session.getPublicConversations();

            if ((conversations == null) || (conversations.length == 0)) {
                String msg = NbBundle.getMessage(
                        AddConversationForm.class, "LBL_AddConversationForm_No_Conferences_Found"
                    ); // NOI18N
                searchMessageLabel.setText(msg);
                findBtn.setEnabled(true);
                _isSearching = false;

                return;
            }

            SelectConferenceForm f = new SelectConferenceForm(conversations);
            String[] ip = f.getSelectedConversations();
            Vector selectedConv = new Vector(result.size());

            if (ip != null) {
                for (int n = 0; n < ip.length; n++) {
                    Debug.out.println("selected conference id = " + ip[n]);

                    if (!selectedConv.contains(ip[n])) {
                        selectedConv.addElement(ip[n]);
                        result.addElement(ip[n]);
                    }
                }

                resultJList.setListData(result);
                updateValidStatus();
            } else {
                Debug.out.println("No search result selected");
            }
        } catch (CollabException e) {
            // TODO: Nice exception dialog here
            Debug.debugNotify(e);
        }
    }

    private void findConference() {
        if (!sessionExists()) {
            return;
        }

        if (conferenceNameField.getText().equals("")) {
            String msg = NbBundle.getMessage(AddConversationForm.class, "Enter_a_String_to_search_for"); // NOI18N
            searchMessageLabel.setText(msg);

            return;
        }

        byte[] x;
        int minLength = 3;
        int length;

        String slen = NbBundle.getMessage(AddConversationForm.class, "Minimum_search_length"); //NOI18N

        if (slen != null) {
            try {
                Integer ilen = new Integer(slen);
                minLength = ilen.intValue();
            } catch (Exception e) {
            }
        }

        try {
            x = conferenceNameField.getText().getBytes("UTF-8"); // NOI18N
            length = x.length;
        } catch (Exception e) {
            length = conferenceNameField.getText().length();
            Debug.errorManager.notify(e);
        }

        if (length < minLength) {
            String msg = NbBundle.getMessage(
                    AddConversationForm.class, "Enter_at_least_num", Integer.toString(minLength)
                ); // NOI18N

            searchMessageLabel.setText(msg);

            return;
        }

        findBtn.setEnabled(false);

        if (_isSearching) {
            return;
        }

        _isSearching = true;

        String txt = conferenceNameField.getText();

        try {
            String[] conversations = session.findPublicConversations(CollabSession.SEARCHTYPE_CONTAINS, txt);

            if ((conversations == null) || (conversations.length == 0)) {
                final String msg = NbBundle.getMessage(AddConversationForm.class, "No_Matches_Found"); // NOI18N
                searchMessageLabel.setText(msg);
            } else if (conversations != null) {
                Vector selectedConv = new Vector(result.size());

                for (int i = 0; i < result.size(); i++) {
                    selectedConv.add(((String) result.get(i)));
                }

                if (conversations.length == 1) {
                    Debug.out.println("Search result: " + conversations[0]);

                    if (!selectedConv.contains(conversations[0])) {
                        selectedConv.addElement(conversations[0]);
                        result.addElement(conversations[0]);
                    }

                    // Cleans the text input field whe search has been successful
                    conferenceNameField.setText("");
                    conferenceNameField.requestFocus();
                    conferenceNameField.setCaretPosition(0);

                    resultJList.setListData(result);
                    updateValidStatus();
                } else if (conversations.length > 1) {
                    SelectConferenceForm f = new SelectConferenceForm(conversations);
                    String[] ip = f.getSelectedConversations();

                    if (ip != null) {
                        for (int n = 0; n < ip.length; n++) {
                            if (!selectedConv.contains(ip[n])) {
                                selectedConv.addElement(ip[n]);
                                result.addElement(ip[n]);
                            }
                        }

                        resultJList.setListData(result);
                        updateValidStatus();
                    } else {
                        Debug.out.println("No search result selected");
                    }
                }
            }
        } catch (CollabException e) {
            // TODO: Nice exception dialog here
            Debug.debugNotify(e);
        } finally {
            _isSearching = false;
            findBtn.setEnabled(true);
        }
    }

    /**
     *
     *
     */
    protected void updateValidStatus() {
        descriptor.setValid(resultJList.getModel().getSize() != 0);
    }

    /**
     *
     *
     */
    protected void updateRemoveButtonState() {
        // #6176075
        removeBtn.setEnabled((resultJList.getModel().getSize() > 0) && !resultJList.isSelectionEmpty());
    }

    /**
     *
     *
     */
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
    private void initComponents() { //GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        conferenceNameLabel = new javax.swing.JLabel();
        conferenceNameField = new javax.swing.JTextField();
        findBtn = new javax.swing.JButton();
        browseBtn = new javax.swing.JButton();
        searchMessageLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultJList = new javax.swing.JList();
        removeBtn = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), null
            )
        );
        setPreferredSize(new java.awt.Dimension(400, 300));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                        "TITLE_AddConversationForm_FindConference"
                    )
                ), null
            )
        );
        conferenceNameLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_AddConversationForm_Name"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(conferenceNameLabel, gridBagConstraints);

        conferenceNameField.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    conferenceNameFieldActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(conferenceNameField, gridBagConstraints);

        findBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_AddConversationForm_Find"
            )
        );
        findBtn.setEnabled(false);
        findBtn.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    findBtnActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(findBtn, gridBagConstraints);

        browseBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_AddConversationForm_BrowseConferences"
            )
        );
        browseBtn.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    browseBtnActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        jPanel1.add(browseBtn, gridBagConstraints);

        searchMessageLabel.setForeground(new java.awt.Color(0, 0, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(searchMessageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                        "LBL_AddConversationForm_ConferencesToAdd"
                    )
                ), new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1))
            )
        );
        jScrollPane1.setViewportView(resultJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        removeBtn.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "BTN_AddConversationForm_Remove"
            )
        );
        removeBtn.setEnabled(false);
        removeBtn.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeBtnActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel2.add(removeBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.8;
        add(jPanel2, gridBagConstraints);
    } //GEN-END:initComponents

    private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_removeBtnActionPerformed

        // Remove the selected conversations from the list
        int[] selected = resultJList.getSelectedIndices();

        // Retrieve the set of selected elements
        List selectedElements = new ArrayList();

        for (int i = 0; i < selected.length; i++) {
            selectedElements.add(resultJList.getModel().getElementAt(selected[i]));
        }

        // Remove the elements from the model's vector by identity
        for (Iterator i = selectedElements.iterator(); i.hasNext();)
            result.removeElement(i.next());

        resultJList.updateUI();
        updateValidStatus();
        updateRemoveButtonState();
    } //GEN-LAST:event_removeBtnActionPerformed

    private void conferenceNameFieldActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_conferenceNameFieldActionPerformed
        findConference();
        conferenceNameField.selectAll();
        conferenceNameField.requestFocus();
    } //GEN-LAST:event_conferenceNameFieldActionPerformed

    private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_browseBtnActionPerformed
        searchMessageLabel.setText("");
        getAllConferences();
    } //GEN-LAST:event_browseBtnActionPerformed

    private void findBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_findBtnActionPerformed
        findConference();
        conferenceNameField.selectAll();
        conferenceNameField.requestFocus();
    } //GEN-LAST:event_findBtnActionPerformed

    public void valueChanged(ListSelectionEvent e) {
        updateRemoveButtonState();
    }
}
