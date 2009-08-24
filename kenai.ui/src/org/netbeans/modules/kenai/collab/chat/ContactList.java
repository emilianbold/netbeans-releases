/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ContactList.java
 *
 * Created on Jul 28, 2009, 2:03:40 PM
 */

package org.netbeans.modules.kenai.collab.chat;

import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
//import org.jivesoftware.smack.Roster;
//import org.jivesoftware.smack.RosterEntry;
//import org.jivesoftware.smack.RosterGroup;
//import org.jivesoftware.smack.RosterListener;
import org.netbeans.modules.kenai.api.Kenai;

/**
 *
 * @author Jan Becicka
 */
public class ContactList extends javax.swing.JPanel {

    private DefaultComboBoxModel filterModel = new DefaultComboBoxModel();
    private DefaultListModel listModel = new DefaultListModel();
    private FakeRoster roster = null;
    private FilterItem oldFilter=new FilterItem();

    /** Creates new form ContactList */
    public ContactList() {
        initComponents();
        filterCombo.setModel(filterModel);
        filterCombo.setRenderer(new FilterRenderer());
        contactJList.setModel(listModel);
        contactJList.setCellRenderer(new ContactListCellRenderer());
        searchPanel.setVisible(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updateContacts();
            }

            public void removeUpdate(DocumentEvent e) {
                updateContacts();
            }

            public void changedUpdate(DocumentEvent e) {
                updateContacts();
            }
        });
    }


    public void updateFilter() {
        roster = new FakeRoster(Kenai.getDefault().getXMPPConnection());
        filterModel.removeAllElements();
        filterModel.addElement(new FilterItem());
        for (FakeRosterGroup group : roster.getGroups()) {
            filterModel.addElement(new FilterItem(group.getName()));
        }
        filterCombo.setSelectedIndex(0);
        
    }
    public void updateContacts() {
        if (filterCombo.getSelectedIndex()!=0) {
            oldFilter = (FilterItem) filterCombo.getSelectedItem();
            filterCombo.setSelectedIndex(0);
        }
        listModel.clear();
        for (FakeRosterGroup group : roster.getGroups()) {
            if (group.getName().toLowerCase().contains(searchField.getText().toLowerCase())) {
                listModel.addElement(new GroupListItem(group));
            }
        }

        for (FakeRosterEntry entry : roster.getEntries()) {
            UserListItem i = new UserListItem(entry);
            if (!listModel.contains(i)) {
                if (entry.getUser().toLowerCase().contains(searchField.getText().toLowerCase())) {
                    listModel.addElement(i);
                }
            }
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        contactJList.requestFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterCombo = new javax.swing.JComboBox();
        contactListScrollPane = new javax.swing.JScrollPane();
        contactJList = new javax.swing.JList();
        searchPanel = new javax.swing.JPanel();
        searchField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 3, 0, 3));
        setFocusCycleRoot(true);

        filterCombo.setNextFocusableComponent(contactJList);
        filterCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterComboItemStateChanged(evt);
            }
        });
        filterCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterComboKeyPressed(evt);
            }
        });

        contactJList.setNextFocusableComponent(searchField);
        contactJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                contactJListMouseClicked(evt);
            }
        });
        contactJList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                contactJListFocusLost(evt);
            }
        });
        contactJList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                contactJListKeyPressed(evt);
            }
        });
        contactListScrollPane.setViewportView(contactJList);

        searchPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 3, 5, 3));

        searchField.setNextFocusableComponent(filterCombo);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchFieldFocusLost(evt);
            }
        });
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchFieldKeyPressed(evt);
            }
        });

        searchLabel.setText(org.openide.util.NbBundle.getMessage(ContactList.class, "ContactList.searchLabel.text", new Object[] {})); // NOI18N

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .add(searchLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(searchLabel)
                .add(searchField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterCombo, 0, 247, Short.MAX_VALUE)
            .add(searchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(contactListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contactListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterComboItemStateChanged
        listModel.clear();
        if (filterCombo.getSelectedIndex()<=0) {

            for (FakeRosterGroup group : roster.getGroups()) {
                listModel.addElement(new GroupListItem(group));
            }

            for (FakeRosterEntry entry:roster.getEntries()) {
                UserListItem i = new UserListItem(entry);
                if (!listModel.contains(i)) {
                    listModel.addElement(i);
                }
            }
        } else {
            String group = ((FilterItem) filterCombo.getSelectedItem()).getName();
            FakeRosterGroup g = roster.getGroup(group);
            listModel.addElement(new GroupListItem(g));
            for (FakeRosterEntry entry:g.getEntries()) {
                listModel.addElement(new UserListItem(entry));
            }
        }
        contactJList.setSelectedIndex(0);
    }//GEN-LAST:event_filterComboItemStateChanged

    private void contactJListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactJListKeyPressed
        if (evt.isActionKey() && evt.getKeyCode()==KeyEvent.VK_ENTER) {
            ((ContactListItem) contactJList.getSelectedValue()).openChat();
        } else if (!evt.isActionKey() && ("" + evt.getKeyChar()).trim().length()!=0 && evt.getKeyChar()!='\uffff') {
            searchPanel.setVisible(true);
            searchField.requestFocus();
            searchField.setText(""+evt.getKeyChar());
        } 
    }//GEN-LAST:event_contactJListKeyPressed

    private void contactJListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contactJListMouseClicked
        if (evt.getClickCount()==2 && !evt.isPopupTrigger()) {
            ((ContactListItem) contactJList.getSelectedValue()).openChat();
        }
    }//GEN-LAST:event_contactJListMouseClicked

    private void searchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER) {
            contactJList.requestFocus();
            ((ContactListItem) contactJList.getSelectedValue()).openChat();
            searchPanel.setVisible(false);
            searchField.setText("");
        } else if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
            int next = contactJList.getSelectedIndex();
            if (next+1!=contactJList.getModel().getSize()) {
                next++;
            }
            contactJList.setSelectedIndex(next);
        } else if (evt.getKeyCode()==KeyEvent.VK_UP) {
            int prev = contactJList.getSelectedIndex();
            if (prev!=0) {
                prev--;
            }
            contactJList.setSelectedIndex(prev);
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            contactJList.requestFocus();
            searchPanel.setVisible(false);
            searchField.setText("");
        }
    }//GEN-LAST:event_searchFieldKeyPressed

    private void searchFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchFieldFocusLost
        if (evt.getOppositeComponent()!=contactJList) {
            searchPanel.setVisible(false);
            searchField.setText("");
        }
    }//GEN-LAST:event_searchFieldFocusLost

    private void contactJListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_contactJListFocusLost
        if (evt.getOppositeComponent() != searchField) {
            searchPanel.setVisible(false);
            searchField.setText("");
        }
    }//GEN-LAST:event_contactJListFocusLost

    private void filterComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterComboKeyPressed
        if (!evt.isActionKey() && ("" + evt.getKeyChar()).trim().length()!=0 && evt.getKeyChar()!='\uffff') {
            searchPanel.setVisible(true);
            searchField.requestFocus();
            searchField.setText("" + evt.getKeyChar());
        }
    }//GEN-LAST:event_filterComboKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList contactJList;
    private javax.swing.JScrollPane contactListScrollPane;
    private javax.swing.JComboBox filterCombo;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    // End of variables declaration//GEN-END:variables

}
