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
import java.util.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.NbBundle;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  sherylsu
 */
public class PublicConversationUserSearchForm extends javax.swing.JPanel implements ListSelectionListener {
    private static boolean _isSearching = false;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel TypeLb;
    private javax.swing.JComboBox accessComboBox;
    private javax.swing.JLabel accessLabel;
    private javax.swing.JList contactJList;
    private javax.swing.JRadioButton containsOptionRadioButton;
    private javax.swing.JRadioButton endsOptionRadioButton;
    private javax.swing.JButton findBtn;
    private javax.swing.JLabel findContactLb;
    private javax.swing.JTextField findContactTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton nameTypeRadioButton;
    private javax.swing.ButtonGroup optionsButtonGroup;
    private javax.swing.JLabel optionsLb;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton removeBtn;
    private javax.swing.JPanel resultsPanel1;
    private javax.swing.JLabel searchMessageLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JRadioButton startsOptionRadioButton;
    private javax.swing.ButtonGroup typeButtonGroup;
    private javax.swing.JPanel typePanel;
    private javax.swing.JRadioButton userIDTypeRadioButton;
    // End of variables declaration//GEN-END:variables
    private DialogDescriptor dialogDescriptor;
    private ArrayList contactList = new ArrayList();
    private ResourceBundle addContactBundle;
    private CollabSession session;
    private boolean limitWasExceeded = false;
    private Vector result = new Vector(20);

    /**
     *
     *
     */
    public PublicConversationUserSearchForm(CollabSession session) {
        super();
        this.session = session;
        initialize();
    }

    /**
     *
     *
     */
    private void initialize() {
        // Create the dialog descriptor here, since we need it in order to
        // be able to change the valid state of the dialog
        dialogDescriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(PublicConversationUserSearchForm.class, "TITLE_PublicConversationUserSearchForm")
            ); // NOI18N

        //		inviteOption=
        //			NbBundle.getMessage(ParticipantSearchForm.class,
        //				"OPT_ParticipantSearchForm_Invite"); // NOI18N
        //		dialogDescriptor.setOptions(new Object[] {inviteOption,
        //			DialogDescriptor.CANCEL_OPTION});
        dialogDescriptor.setValid(false);

        initComponents();

        //	optionsPanel.setVisible(!useComboBoxes);
        optionsPanel.setVisible(false);
        optionsLb.setVisible(false);

        addContactBundle = NbBundle.getBundle(AddContactForm.class);

        ListModel model = new ListModel(contactJList, result, false, true, false);

        ListRenderer renderer = new ListRenderer(model);
        contactJList.setCellRenderer(renderer);
        contactJList.setModel(model);
        contactJList.addListSelectionListener(this);

        // Listen to changes to the search field in order to enable or 
        // disable the find button
        findContactTextField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    findBtn.setEnabled(
                        (findContactTextField.getText() != null) && (findContactTextField.getText().length() > 0)
                    );
                }

                public void removeUpdate(DocumentEvent e) {
                    findBtn.setEnabled(
                        (findContactTextField.getText() != null) && (findContactTextField.getText().length() > 0)
                    );
                }
            }
        );

        // Populate the access combo box
        for (int i = 0; i < ManagePublicConversationForm.ACCESS_ELEMENTS.length; i++) {
            accessComboBox.addItem(ManagePublicConversationForm.ACCESS_ELEMENTS[i]);
        }
    }

    /**
     *
     *
     */
    protected CollabSession getCollabSession() {
        return this.session;
    }

    /**
     *
     *
     */
    public Object showDialog() {
        Dialog dialog = null;

        try {
            dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog.show();
        } finally {
            dialog.dispose();
        }

        return dialogDescriptor.getValue();
    }

    /**
     *
     *
     */
    public CollabPrincipal[] getSelectedUsers() {
        CollabPrincipal[] ret = new CollabPrincipal[result.size()];
        result.copyInto(ret);

        return ret;
    }

    /**
     *
     *
     */
    public int getSelectedAccess() {
        return ((ManagePublicConversationForm.AccessElement) accessComboBox.getSelectedItem()).access;
    }

    /**
     *
     *
     */
    public void search() {
        if (!sessionExists()) {
            return;
        }

        if (findContactTextField.getText().equals("")) {
            String msg = NbBundle.getMessage(AddContactForm.class, "Enter_a_String_to_search_for"); // NOI18N
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE)
            );

            return;
        }

        if (!isSearchByID()) {
            byte[] x;
            int minLength = 3;
            int length;

            String slen = addContactBundle.getString("Minimum_search_length"); // NOI18N

            if (slen != null) {
                try {
                    Integer ilen = new Integer(slen);
                    minLength = ilen.intValue();
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }
            }

            try {
                x = findContactTextField.getText().getBytes("UTF-8"); // NOI18N
                length = x.length;
            } catch (Exception e) {
                length = findContactTextField.getText().length();
                Debug.debugNotify(e);
            }

            if (length < minLength) {
                String msg = NbBundle.getMessage(
                        AddContactForm.class, "Enter_at_least_num", // NOI18N
                        Integer.toString(minLength)
                    );

                // Indicate the problem to user
                searchMessageLabel.setText(msg); // NOI18N

                return;
            }
        }

        String txt = findContactTextField.getText();
        int searchType = CollabSession.SEARCHTYPE_EQUALS;

        if (!isSearchByID()) {
            /*
            if (containsOptionRadioButton.isSelected())
                    searchType = CollabSession.SEARCHTYPE_CONTAINS;
            else
            if (startsOptionRadioButton.isSelected())
                    searchType = CollabSession.SEARCHTYPE_STARTSWITH;
            else
            if (endsOptionRadioButton.isSelected())
                    searchType = CollabSession.SEARCHTYPE_ENDSWITH;
             */
            searchType = CollabSession.SEARCHTYPE_CONTAINS;
        } else {
            searchType = CollabSession.SEARCHTYPE_EQUALS;
        }

        Debug.out.println(" search type: " + searchType); // NOI18N
        doSearch("", txt, searchType); // NOI18N
    }

    /**
     *
     *
     */
    protected void doSearch(String serv, String txt, int searchType) {
        if (_isSearching) {
            return;
        }

        limitWasExceeded = false;

        try {
            _isSearching = true;

            findBtn.setEnabled(false);

            // Indicate that searching has begin
            searchMessageLabel.setText(addContactBundle.getString("MSG_AddContactForm_Searching")); // NOI18N

            CollabPrincipal[] users = getCollabSession().findPrincipals(searchType, txt);
            Debug.out.println("Matches found for \"" + txt + "\": " + ((users != null) ? ("" + users.length) : "null"));

            if ((users == null) || (users.length == 0)) {
                // Let user know there were no matches
                searchMessageLabel.setText(addContactBundle.getString("No_Matches_Found")); // NOI18N
            } else if (users != null) {
                Vector selectedUsersCheck = new Vector(result.size());

                for (int i = 0; i < result.size(); i++) {
                    selectedUsersCheck.add(((CollabPrincipal) result.get(i)).getIdentifier());
                }

                if (users.length == 1) {
                    Debug.out.println("Search result: " + users[0].getIdentifier());

                    if (!selectedUsersCheck.contains(users[0].getIdentifier())) {
                        selectedUsersCheck.addElement(users[0].getIdentifier());
                        result.addElement(users[0]);
                        Debug.out.println("Added uid to result: " + users[0].getIdentifier());
                    }

                    // Indicate that a contact was added
                    searchMessageLabel.setText(
                        addContactBundle.getString("MSG_PublicConversationUserSearchForm_UserAdded")
                    ); // NOI18N

                    contactJList.setListData(result);
                    updateValidStatus();
                } else if (users.length > 1) {
                    SelectUserForm f = new SelectUserForm(users);
                    CollabPrincipal[] ip = f.getSelectedUsers();

                    if (ip != null) {
                        for (int n = 0; n < ip.length; n++) {
                            Debug.out.println("selected uid = " + ip[n].getIdentifier());

                            if (!selectedUsersCheck.contains(ip[n].getIdentifier())) {
                                selectedUsersCheck.addElement(ip[n].getIdentifier());
                                result.addElement(ip[n]);
                                Debug.out.println("added uid to result = " + ip[n].getIdentifier());
                            }
                        }

                        // Indicate that a contact was added
                        searchMessageLabel.setText(
                            addContactBundle.getString("MSG_PublicConversationUserSearchForm_UsersAdded")
                        ); // NOI18N

                        contactJList.setListData(result);
                        updateValidStatus();
                    } else {
                        Debug.out.println("No search result selected");

                        // Indicate that no contacts were added
                        searchMessageLabel.setText(
                            addContactBundle.getString("MSG_AddContactForm_ContactSelectionCancelled")
                        ); // NOI18N
                    }
                }

                for (int i = 0; i < users.length; i++) {
                    Debug.out.println("search result: " + users[i].getDisplayName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Debug.logDebugException("Unknown error performing search", e, true);

            // TODO: Nice exception dialog here
            Debug.debugNotify(e);

            // Let user know there were no matches due to an error
            searchMessageLabel.setText(addContactBundle.getString("No_Matches_Found_Error")); // NOI18N
        } finally {
            _isSearching = false;
            findBtn.setEnabled(true);
        }
    }

    /**
     *
     *
     */
    public boolean isSearchByID() {
        return !nameTypeRadioButton.isSelected();
    }

    /**
     *
     *
     */
    private Vector toUserName(Vector users) {
        Vector names = new Vector(users.size());

        for (int i = 0; i < users.size(); i++) {
            names.add(((CollabPrincipal) users.elementAt(i)).getDisplayName());
        }

        return names;
    }

    /**
     *
     *
     */
    protected void updateValidStatus() {
        dialogDescriptor.setValid(contactJList.getModel().getSize() != 0);
    }

    /**
     *
     *
     */
    protected void updateRemoveButtonState() {
        // #6176075
        removeBtn.setEnabled((contactJList.getModel().getSize() > 0) && !contactJList.isSelectionEmpty());
    }

    /**
     *
     *
     */
    private boolean sessionExists() {
        // check session status #5080138
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].equals(getCollabSession())) {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        optionsButtonGroup = new javax.swing.ButtonGroup();
        typeButtonGroup = new javax.swing.ButtonGroup();
        searchPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        findContactLb = new javax.swing.JLabel();
        findContactTextField = new javax.swing.JTextField();
        findBtn = new javax.swing.JButton();
        optionsLb = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        containsOptionRadioButton = new javax.swing.JRadioButton();
        startsOptionRadioButton = new javax.swing.JRadioButton();
        endsOptionRadioButton = new javax.swing.JRadioButton();
        TypeLb = new javax.swing.JLabel();
        typePanel = new javax.swing.JPanel();
        nameTypeRadioButton = new javax.swing.JRadioButton();
        userIDTypeRadioButton = new javax.swing.JRadioButton();
        searchMessageLabel = new javax.swing.JLabel();
        resultsPanel1 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contactJList = new javax.swing.JList();
        removeBtn = new javax.swing.JButton();
        accessLabel = new javax.swing.JLabel();
        accessComboBox = new javax.swing.JComboBox();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        setMinimumSize(new java.awt.Dimension(600, 400));
        setPreferredSize(new java.awt.Dimension(440, 400));
        setLayout(new java.awt.GridBagLayout());

        searchPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "KEY_PublicConversationUserSearchForm_SearchPanelTitle")), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))); // NOI18N
        searchPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        findContactLb.setLabelFor(findContactTextField);
        org.openide.awt.Mnemonics.setLocalizedText(findContactLb, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_FindContact")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel2.add(findContactLb, gridBagConstraints);

        findContactTextField.setColumns(32);
        findContactTextField.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel2.add(findContactTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(findBtn, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "BTN_AddContactForm_Find")); // NOI18N
        findBtn.setEnabled(false);
        findBtn.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel2.add(findBtn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(optionsLb, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Options")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 5);
        jPanel2.add(optionsLb, gridBagConstraints);

        optionsButtonGroup.add(containsOptionRadioButton);
        containsOptionRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(containsOptionRadioButton, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Contains")); // NOI18N
        optionsPanel.add(containsOptionRadioButton);

        optionsButtonGroup.add(startsOptionRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(startsOptionRadioButton, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Starts")); // NOI18N
        optionsPanel.add(startsOptionRadioButton);

        optionsButtonGroup.add(endsOptionRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(endsOptionRadioButton, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Ends")); // NOI18N
        optionsPanel.add(endsOptionRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(optionsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(TypeLb, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 5);
        jPanel2.add(TypeLb, gridBagConstraints);

        typeButtonGroup.add(nameTypeRadioButton);
        nameTypeRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(nameTypeRadioButton, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_Name")); // NOI18N
        typePanel.add(nameTypeRadioButton);

        typeButtonGroup.add(userIDTypeRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(userIDTypeRadioButton, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_AddContactForm_UserID")); // NOI18N
        typePanel.add(userIDTypeRadioButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(typePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        searchPanel.add(jPanel2, gridBagConstraints);

        searchMessageLabel.setForeground(new java.awt.Color(0, 0, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        searchPanel.add(searchMessageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(searchPanel, gridBagConstraints);

        resultsPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_PublicConversationUserSearchForm_searchResultLabel")), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5))); // NOI18N
        resultsPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        contactJList.setMaximumSize(null);
        contactJList.setMinimumSize(null);
        contactJList.setPreferredSize(null);
        contactJList.setVisibleRowCount(10);
        jScrollPane1.setViewportView(contactJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultsPanel1.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "BTN_AddContactForm_Remove")); // NOI18N
        removeBtn.setEnabled(false);
        removeBtn.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        resultsPanel1.add(removeBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(resultsPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(accessLabel, org.openide.util.NbBundle.getMessage(PublicConversationUserSearchForm.class, "LBL_PublicConversationUserSearchForm_Access")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 5);
        add(accessLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        add(accessComboBox, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == findContactTextField) {
                PublicConversationUserSearchForm.this.findContactTextFieldActionPerformed(evt);
            }
            else if (evt.getSource() == findBtn) {
                PublicConversationUserSearchForm.this.findBtnActionPerformed(evt);
            }
            else if (evt.getSource() == removeBtn) {
                PublicConversationUserSearchForm.this.removeBtnActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void findContactTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findContactTextFieldActionPerformed
        search();
        findContactTextField.selectAll();
        findContactTextField.requestFocus();
    }//GEN-LAST:event_findContactTextFieldActionPerformed

    private void findBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findBtnActionPerformed
        search();
        findContactTextField.selectAll();
        findContactTextField.requestFocus();
    }//GEN-LAST:event_findBtnActionPerformed

    private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed

        // Remove the selected contacts from the list
        int[] selected = contactJList.getSelectedIndices();

        // Retrieve the set of selected elements
        List selectedElements = new ArrayList();

        for (int i = 0; i < selected.length; i++) {
            selectedElements.add(contactJList.getModel().getElementAt(selected[i]));
        }

        // Remove the elements from the model's vector by identity
        for (Iterator i = selectedElements.iterator(); i.hasNext();)
            result.removeElement(i.next());

        contactJList.clearSelection();
        contactJList.updateUI();
        updateValidStatus();
        updateRemoveButtonState();
    }//GEN-LAST:event_removeBtnActionPerformed

    public void valueChanged(ListSelectionEvent event) {
        updateRemoveButtonState();
    }

    //	private String inviteOption;
}
