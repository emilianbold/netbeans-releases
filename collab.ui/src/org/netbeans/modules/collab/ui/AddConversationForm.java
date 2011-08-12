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
import java.awt.GridBagConstraints;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

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
public class AddConversationForm extends javax.swing.JPanel implements ListSelectionListener {
    // End of variables declaration                   
    private static boolean _isSearching = false;
    private boolean visible;

    private CollabSession session;
    private Vector result = new Vector();
    private DialogDescriptor descriptor;
    
    private ProgressHandle progress;
    private JComponent progressComponent;
    private GridBagConstraints progressSpace;

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
        
        progressSpace = new GridBagConstraints();
        progressSpace.gridx = 2;
        progressSpace.gridy = 1;
        progressSpace.gridwidth = GridBagConstraints.RELATIVE;
        progressSpace.gridheight = GridBagConstraints.REMAINDER;
        progressSpace.anchor = GridBagConstraints.WEST;
        progressSpace.insets = new java.awt.Insets(0, 5, 5, 5);

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
            visible = true;
            dialog.setVisible(true);
            visible = false;

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
    
    private void prepareProgress() {
        browseBtn.setEnabled(false);
        conferenceNameField.setEditable(false);
        findBtn.setEnabled(false);
        removeBtn.setEnabled(false);
        resultJList.setEnabled(false);
        
        searchMessageLabel.setText(NbBundle.getMessage(AddConversationForm.class, "LBL_AddConversationForm_Searching"));
        progress = ProgressHandleFactory.createHandle("");
        progressComponent = ProgressHandleFactory.createProgressComponent(progress);
        progress.start();
        jPanel1.add(progressComponent, progressSpace);
        revalidate();
    }

    private void finishProgress() {
        progress.finish();
        jPanel1.remove(progressComponent);
        searchMessageLabel.setText("");
        progressComponent = null;
        progress = null;

        
        browseBtn.setEnabled(true);
        conferenceNameField.setEditable(true);
        findBtn.setEnabled(conferenceNameField.getText().length() > 0);
        updateRemoveButtonState();
        resultJList.setEnabled(true);
    }

    private void getAllConferences() {
        if (!sessionExists()) {
            return;
        }
        
        prepareProgress();

        Runnable listRunnable = new Runnable() {
            String[] conversations = null;
            
            public void run() {
                if (conversations == null) {
                    try {
                        conversations = session.getPublicConversations();
                    } catch (CollabException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    if (conversations == null) conversations = new String[0];
                    javax.swing.SwingUtilities.invokeLater(this);
                } else {
                    finishProgress();
                    
                    if (!visible) return; // dialog canceled
                    
                    if (conversations.length == 0) {
                        searchMessageLabel.setText(NbBundle.getMessage(
                                AddConversationForm.class, "LBL_AddConversationForm_No_Conferences_Found" // NOI18N
                        ));
                        return;
                    }

                    String[] ip = new SelectConferenceForm(conversations).getSelectedConversations();
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
                }
            }
        };
        
        RequestProcessor.getDefault().post(listRunnable);
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

        final String txt = conferenceNameField.getText();

        prepareProgress();

        Runnable listRunnable = new Runnable() {
            String[] conversations = null;
            
            public void run() {
                if (conversations == null) {
                    try {
                        conversations = session.findPublicConversations(CollabSession.SEARCHTYPE_CONTAINS, txt);
                    } catch (CollabException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    if (conversations == null) conversations = new String[0];
                    javax.swing.SwingUtilities.invokeLater(this);
                } else {
                    finishProgress();
                    
                    if (!visible) return; // dialog canceled
                    
                    if (conversations.length == 0) {
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
                }
            }
        };
        
        RequestProcessor.getDefault().post(listRunnable);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
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

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AddConversationForm.class, "TITLE_AddConversationForm_FindConference")), null)); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(conferenceNameLabel, org.openide.util.NbBundle.getMessage(AddConversationForm.class, "LBL_AddConversationForm_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(conferenceNameLabel, gridBagConstraints);

        conferenceNameField.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel1.add(conferenceNameField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(findBtn, org.openide.util.NbBundle.getMessage(AddConversationForm.class, "BTN_AddConversationForm_Find")); // NOI18N
        findBtn.setEnabled(false);
        findBtn.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel1.add(findBtn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseBtn, org.openide.util.NbBundle.getMessage(AddConversationForm.class, "BTN_AddConversationForm_BrowseConferences")); // NOI18N
        browseBtn.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        jPanel1.add(browseBtn, gridBagConstraints);

        searchMessageLabel.setForeground(new java.awt.Color(0, 0, 255));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanel1.add(searchMessageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jPanel1, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(AddConversationForm.class, "LBL_AddConversationForm_ConferencesToAdd")), javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1))); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(resultJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(AddConversationForm.class, "BTN_AddConversationForm_Remove")); // NOI18N
        removeBtn.setEnabled(false);
        removeBtn.addActionListener(formListener);
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
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == conferenceNameField) {
                AddConversationForm.this.conferenceNameFieldActionPerformed(evt);
            }
            else if (evt.getSource() == findBtn) {
                AddConversationForm.this.findBtnActionPerformed(evt);
            }
            else if (evt.getSource() == browseBtn) {
                AddConversationForm.this.browseBtnActionPerformed(evt);
            }
            else if (evt.getSource() == removeBtn) {
                AddConversationForm.this.removeBtnActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed

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

        resultJList.clearSelection();
        resultJList.updateUI();
        updateValidStatus();
        updateRemoveButtonState();
    }//GEN-LAST:event_removeBtnActionPerformed

    private void conferenceNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conferenceNameFieldActionPerformed
        searchMessageLabel.setText("");
        conferenceNameField.selectAll();
        conferenceNameField.requestFocus();
        findConference();
    }//GEN-LAST:event_conferenceNameFieldActionPerformed

    private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBtnActionPerformed
        searchMessageLabel.setText("");
        getAllConferences();
    }//GEN-LAST:event_browseBtnActionPerformed

    private void findBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findBtnActionPerformed
        searchMessageLabel.setText("");
        conferenceNameField.selectAll();
        conferenceNameField.requestFocus();
        findConference();
    }//GEN-LAST:event_findBtnActionPerformed

    public void valueChanged(ListSelectionEvent e) {
        updateRemoveButtonState();
    }

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
    // End of variables declaration//GEN-END:variables
}
