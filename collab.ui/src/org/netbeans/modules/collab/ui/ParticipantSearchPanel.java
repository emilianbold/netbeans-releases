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

import java.util.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.*;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.Conversation;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  todd
 */
public class ParticipantSearchPanel extends javax.swing.JPanel implements ListSelectionListener {
    private static boolean _isSearching = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JList contactJList;
    private javax.swing.JRadioButton containsBtn;
    private javax.swing.JButton findBtn;
    private javax.swing.JTextField findContactTextField;
    private javax.swing.JButton inviteBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JRadioButton nameBtn;
    private javax.swing.JLabel optionsLbl;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel searchTextPanel;
    private javax.swing.JButton showSearchButton;
    private javax.swing.JRadioButton startsBtn;
    private javax.swing.JLabel typeLbl;
    private javax.swing.JPanel typePanel;
    private javax.swing.JRadioButton userIdBtn;

    // End of variables declaration//GEN-END:variables
    private DialogDescriptor dialogDescriptor;
    private Vector result = new Vector(20);
    private Conversation conversation;
    private boolean showSearchState;

    /** Creates new form ParticipantSearchPanel */
    public ParticipantSearchPanel(Conversation conversation) {
        this.conversation = conversation;
        initialize();
    }

    private void initialize() {
        initComponents();

        // Initially hide the search panel
        searchPanel.setVisible(false);
        resultPanel.setVisible(false);

        optionsPanel.setVisible(false);
        optionsLbl.setVisible(false);

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
    }

    public void search() {
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

            String slen = NbBundle.getMessage(AddContactForm.class, "Minimum_search_length"); // NOI18N

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
                //searchMessageLabel.setText(msg); // NOI18N
                return;
            }
        }

        String txt = findContactTextField.getText();
        int searchType = CollabSession.SEARCHTYPE_EQUALS;

        if (!isSearchByID()) {
            searchType = CollabSession.SEARCHTYPE_CONTAINS;

            /*
            if (containsBtn.isSelected())
                    searchType = CollabSession.SEARCHTYPE_CONTAINS;
            else
            if (startsBtn.isSelected())
                    searchType = CollabSession.SEARCHTYPE_STARTSWITH;
             */
        } else {
            searchType = CollabSession.SEARCHTYPE_EQUALS;
        }

        doSearch("", txt, searchType); // NOI18N
    }

    protected void doSearch(String serv, String txt, int searchType) {
        if (_isSearching) {
            return;
        }

        //	limitWasExceeded = false;
        try {
            _isSearching = true;

            findBtn.setEnabled(false);

            // Indicate that searching has begin

            /*
            searchMessageLabel.setText(addContactBundle.getString(
                    "MSG_AddContactForm_Searching")); // NOI18N
             */
            CollabPrincipal[] users = getConversation().getCollabSession().findPrincipals(searchType, txt);
            Debug.out.println("Matches found for \"" + txt + "\": " + ((users != null) ? ("" + users.length) : "null"));

            result.clear();

            if ((users == null) || (users.length == 0)) {
                // should we display a message?
            } else if (users != null) {
                for (int n = 0; n < users.length; n++) {
                    result.addElement(users[n]);
                }
            }

            contactJList.setListData(result);
        } catch (Exception e) {
            e.printStackTrace();

            // TODO: Nice exception dialog here
            Debug.debugNotify(e);
        } finally {
            _isSearching = false;
            findBtn.setEnabled(true);
        }
    }

    public boolean isSearchByID() {
        return !nameBtn.isSelected();
    }

    /**
     *
     *
     */
    protected void updateInviteButtonState() {
        // #6176075
        inviteBtn.setEnabled((contactJList.getModel().getSize() > 0) && !contactJList.isSelectionEmpty());
    }

    /*
     *
     *
     */
    public void valueChanged(ListSelectionEvent event) {
        updateInviteButtonState();
    }

    /*
     *
     *
     */
    private Conversation getConversation() {
        return conversation;
    }

    /*
     *
     *
     */
    private void clear() {
        findContactTextField.setText("");
        contactJList.removeAll();
        result.clear();
        updateUI();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        showSearchButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        searchPanel = new javax.swing.JPanel();
        searchTextPanel = new javax.swing.JPanel();
        findContactTextField = new javax.swing.JTextField();
        findBtn = new javax.swing.JButton();
        typeLbl = new javax.swing.JLabel();
        typePanel = new javax.swing.JPanel();
        nameBtn = new javax.swing.JRadioButton();
        userIdBtn = new javax.swing.JRadioButton();
        optionsLbl = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        containsBtn = new javax.swing.JRadioButton();
        startsBtn = new javax.swing.JRadioButton();
        resultPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contactJList = new javax.swing.JList();
        inviteBtn = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        showSearchButton.setIcon(
            new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/collab/ui/resources/user_png.gif"))
        );
        showSearchButton.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_ParticipantSearchForm_ShowParticipantSearch"
            )
        );
        showSearchButton.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showSearchButtonActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(showSearchButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchPanel.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                        "LBL_ParticipantSearchForm_ContactSearch"
                    )
                ), new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 5, 5))
            )
        );
        searchPanel.setMinimumSize(null);
        searchPanel.setPreferredSize(null);
        searchTextPanel.setLayout(new java.awt.GridBagLayout());

        findContactTextField.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    findContactTextFieldActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        searchTextPanel.add(findContactTextField, gridBagConstraints);

        findBtn.setText("Find");
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        searchTextPanel.add(findBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchPanel.add(searchTextPanel, gridBagConstraints);

        typeLbl.setText("Search For:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        searchPanel.add(typeLbl, gridBagConstraints);

        typePanel.setLayout(new java.awt.GridBagLayout());

        typePanel.setMinimumSize(null);
        nameBtn.setSelected(true);
        nameBtn.setText("Name");
        buttonGroup1.add(nameBtn);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        typePanel.add(nameBtn, gridBagConstraints);

        userIdBtn.setText("User ID");
        buttonGroup1.add(userIdBtn);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typePanel.add(userIdBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        searchPanel.add(typePanel, gridBagConstraints);

        optionsLbl.setText("Search Options:");
        optionsLbl.setMaximumSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        searchPanel.add(optionsLbl, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setMinimumSize(null);
        containsBtn.setSelected(true);
        containsBtn.setText("Contains text");
        buttonGroup2.add(containsBtn);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(containsBtn, gridBagConstraints);

        startsBtn.setText("Starts with text");
        buttonGroup2.add(startsBtn);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        optionsPanel.add(startsBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        searchPanel.add(optionsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(searchPanel, gridBagConstraints);

        resultPanel.setLayout(new java.awt.GridBagLayout());

        resultPanel.setBorder(
            new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                        "LBL_ParticipantSearchForm_SearchResults"
                    )
                ), new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 5, 5))
            )
        );
        resultPanel.setMinimumSize(null);
        resultPanel.setPreferredSize(null);
        contactJList.setMaximumSize(null);
        contactJList.setMinimumSize(null);
        contactJList.setPreferredSize(new java.awt.Dimension(100, 100));
        contactJList.setVisibleRowCount(5);
        jScrollPane1.setViewportView(contactJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultPanel.add(jScrollPane1, gridBagConstraints);

        inviteBtn.setText("Invite");
        inviteBtn.setMaximumSize(null);
        inviteBtn.setMinimumSize(null);
        inviteBtn.setEnabled(false);
        inviteBtn.addActionListener(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    inviteBtnActionPerformed(evt);
                }
            }
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 2);
        gridBagConstraints.weightx = 1.0;
        resultPanel.add(inviteBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(resultPanel, gridBagConstraints);
    }//GEN-END:initComponents

    private void showSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSearchButtonActionPerformed

        if (showSearchState) {
            // Hide the search
            searchPanel.setVisible(false);
            resultPanel.setVisible(false);
            showSearchButton.setText(
                NbBundle.getMessage(ParticipantSearchPanel.class, "LBL_ParticipantSearchForm_ShowParticipantSearch")
            );
        } else {
            // Show the search
            searchPanel.setVisible(true);
            resultPanel.setVisible(true);
            showSearchButton.setText(
                NbBundle.getMessage(ParticipantSearchPanel.class, "LBL_ParticipantSearchForm_HideParticipantSearch")
            );
        }

        showSearchState = !showSearchState;
    }//GEN-LAST:event_showSearchButtonActionPerformed

    private void findContactTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findContactTextFieldActionPerformed
        search();
        findContactTextField.selectAll();
        findContactTextField.requestFocus();
    }//GEN-LAST:event_findContactTextFieldActionPerformed

    private void inviteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inviteBtnActionPerformed

        int[] indices = contactJList.getSelectedIndices();
        CollabPrincipal[] invitee = new CollabPrincipal[indices.length];

        for (int i = 0; i < indices.length; i++) {
            int j = indices[i];
            invitee[i] = (CollabPrincipal) result.elementAt(j);
        }

        getConversation().invite(invitee, "");
        clear();
    }//GEN-LAST:event_inviteBtnActionPerformed

    private void findBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findBtnActionPerformed
        search();
        findContactTextField.selectAll();
        findContactTextField.requestFocus();
    }//GEN-LAST:event_findBtnActionPerformed
}
