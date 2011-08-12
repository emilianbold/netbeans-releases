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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.openide.*;
import org.openide.util.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ManagePublicConversationForm extends JPanel {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////

    /** Note, the order of this array is important.  The indices correspond
     * to the value of the associated constant
     */
    private static final String[] ACCESS_NAMES = new String[] {
            NbBundle.getMessage(ManagePublicConversationForm.class, "LBL_ManagePublicConversationForm_NoneAccess"),
            NbBundle.getMessage(ManagePublicConversationForm.class, "LBL_ManagePublicConversationForm_ReadAccess"),
            NbBundle.getMessage(ManagePublicConversationForm.class, "LBL_ManagePublicConversationForm_WriteAccess"),
            NbBundle.getMessage(ManagePublicConversationForm.class, "LBL_ManagePublicConversationForm_ManageAccess")
        };

    /*pkg*/ static final AccessElement[] ACCESS_ELEMENTS = new AccessElement[] {
            new AccessElement(ConversationPrivilege.NONE, ACCESS_NAMES[ConversationPrivilege.NONE]),
            new AccessElement(ConversationPrivilege.READ, ACCESS_NAMES[ConversationPrivilege.READ]),
            new AccessElement(ConversationPrivilege.WRITE, ACCESS_NAMES[ConversationPrivilege.WRITE]),
            new AccessElement(ConversationPrivilege.MANAGE, ACCESS_NAMES[ConversationPrivilege.MANAGE])
        };
    private static final String[] COL_NAMES = new String[] {
            NbBundle.getMessage(ManagePublicConversationForm.class, "COL_ManagePublicConversationForm_UserColumn"),
            NbBundle.getMessage(ManagePublicConversationForm.class, "COL_ManagePublicConversationForm_IDColumn"),
            NbBundle.getMessage(ManagePublicConversationForm.class, "COL_ManagePublicConversationForm_AccessColumn")
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;
    private String conferenceName;
    private List privileges = new ArrayList();
    private JTable privilegeTable;

    /**
     *
     *
     */
    public ManagePublicConversationForm(
        CollabSession session, String conferenceName, ConversationPrivilege[] privilegeArray
    ) {
        super();
        this.session = session;
        this.conferenceName = conferenceName;
        this.privileges.addAll(Arrays.asList(privilegeArray));

        initialize();
    }

    /**
     *
     *
     */
    private void initialize() {
        initComponents();

        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

        TableColumn userColumn = new TableColumn(0);
        userColumn.setHeaderValue(COL_NAMES[0]);

        TableColumn idColumn = new TableColumn(1);
        idColumn.setHeaderValue(COL_NAMES[1]);

        TableColumn accessColumn = new TableColumn(2);
        accessColumn.setHeaderValue(COL_NAMES[2]);

        // Create a combo box for the access column cell editor
        JComboBox accessComboBox = new JComboBox();

        for (int i = 0; i < ACCESS_ELEMENTS.length; i++)
            accessComboBox.addItem(ACCESS_ELEMENTS[i]);

        accessColumn.setCellEditor(new DefaultCellEditor(accessComboBox));

        // Add the columns to the table
        columnModel.addColumn(userColumn);
        columnModel.addColumn(idColumn);
        columnModel.addColumn(accessColumn);

        // Create the privilege table
        privilegeTable = new JTable(new PrivilegeTableModel(), columnModel);
        privilegeTable.setPreferredScrollableViewportSize(new Dimension(350, 200));
        privilegeTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        privilegeTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    int[] rows = privilegeTable.getSelectedRows();
                    Object[] values = new Object[rows.length];

                    for (int i = 0; i < values.length; i++) {
                        if (
                            getCollabSession().getUserPrincipal().equals(
                                    ((ConversationPrivilege) privileges.get(rows[i])).getPrincipal()
                                )
                        ) {
                            removeButton.setEnabled(false);

                            return;
                        }
                    }

                    removeButton.setEnabled(privilegeTable.getSelectedRowCount() != 0);
                }
            }
        );

        // Create and set the table header
        JTableHeader header = new JTableHeader(columnModel);
        header.setReorderingAllowed(false);
        privilegeTable.setTableHeader(header);

        // Calculate an optimal row height
        JLabel tempLabel = new JLabel();
        int rowHeight = Math.max(22, tempLabel.getFontMetrics(tempLabel.getFont()).getHeight());
        privilegeTable.setRowHeight(rowHeight);

        // Add the scroll pane with the table to the form
        JScrollPane scrollPane = new JScrollPane(privilegeTable);

        //        GridBagConstraints gridBagConstraints=new java.awt.GridBagConstraints();
        //        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        //        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        //        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        //        gridBagConstraints.weightx = 1.0;
        //        gridBagConstraints.weighty = 1.0;
        //        privilegePanel.add(scrollPane,gridBagConstraints);
        privilegePanel.add(scrollPane, BorderLayout.CENTER);

        // Populate the default access combo
        for (int i = 0; i < (ACCESS_ELEMENTS.length - 1); i++)
            defaultAccessComboBox.addItem(ACCESS_ELEMENTS[i]);

        int defaultAccess = getOriginalDefaultPrivilege().getAccess();
        defaultAccessComboBox.setSelectedIndex(defaultAccess);
        
        initAccessibility();
    }

    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_NAME_ManagePublicConversationForm"));
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_DESC_ManagePublicConversationForm"));
        
        defaultAccessComboBox.getAccessibleContext().setAccessibleName(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_NAME_ManagePublicConversationForm_DefaultAccess"));
        defaultAccessComboBox.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_DESC_ManagePublicConversationForm_DefaultAccess"));
        
        addButton.getAccessibleContext().setAccessibleName(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_NAME_ManagePublicConversationForm_AddPrivilegeButton"));
        addButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_DESC_ManagePublicConversationForm_AddPrivilegeButton"));
        
        removeButton.getAccessibleContext().setAccessibleName(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_NAME_ManagePublicConversationForm_RemovePrivilegeButton"));
        removeButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_DESC_ManagePublicConversationForm_RemovePrivilegeButton"));
        
        privilegeTable.getAccessibleContext().setAccessibleName(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_NAME_ManagePublicConversationForm_PrivilegeTable"));
        privilegeTable.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(AddContactForm.class).getString("ACSD_DESC_ManagePublicConversationForm_PrivilegeTable"));
    }
    
    
    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }
    
    /**
     *
     *
     */
    public String getConversationName() {
        return conferenceName;
    }
    
    /**
     *
     *
     */
    public ConversationPrivilege[] getPrivileges() {
        return (ConversationPrivilege[]) privileges.toArray(new ConversationPrivilege[privileges.size()]);
    }
    
    /**
     *
     *
     */
    public ConversationPrivilege getDefaultPrivilege() {
        int defaultAccess = defaultAccessComboBox.getSelectedIndex();
        
        return new ConversationPrivilege(null, defaultAccess);
    }
    
    /**
     *
     *
     */
    public ConversationPrivilege getOriginalDefaultPrivilege() {
        ConversationPrivilege privilege = null;
        
        try {
            privilege = getCollabSession().getPublicConversationDefaultPrivilege(getConversationName());
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        }
        
        Debug.out.println(" original default privilege: " + privilege.getAccess());
        
        return privilege;
    }
    
    /**
     *
     *
     */
    public static void _test(CollabSession session, String conference, ConversationPrivilege[] privileges) {
        ManagePublicConversationForm form = new ManagePublicConversationForm(session, conference, privileges);
        
        DialogDescriptor descriptor = new DialogDescriptor(form, "Manage Public Conversation");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        try {
            dialog.show();
        } finally {
            dialog.dispose();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        controlGroupPanel = new javax.swing.JPanel();
        defaultAccessLabel = new javax.swing.JLabel();
        defaultAccessComboBox = new javax.swing.JComboBox();
        privilegePanel = new javax.swing.JPanel();
        controlGroupPanel1 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
        setPreferredSize(new java.awt.Dimension(440, 400));
        setLayout(new java.awt.BorderLayout());

        controlGroupPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 0));
        controlGroupPanel.setLayout(new java.awt.GridBagLayout());

        defaultAccessLabel.setLabelFor(defaultAccessComboBox);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(defaultAccessLabel, bundle.getString("LBL_ManagePublicConversationForm_DefaultAccess")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        controlGroupPanel.add(defaultAccessLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        controlGroupPanel.add(defaultAccessComboBox, gridBagConstraints);

        add(controlGroupPanel, java.awt.BorderLayout.NORTH);

        privilegePanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LBL_ManagePublicConversationForm_PrivilegePanelTitle")), javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5))); // NOI18N
        privilegePanel.setLayout(new java.awt.BorderLayout());

        controlGroupPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        controlGroupPanel1.setLayout(new java.awt.BorderLayout());

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("BTN_ManagePublicConversationForm_AddPrivilegeButton")); // NOI18N
        addButton.addActionListener(formListener);
        buttonPanel.add(addButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("BTN_ManagePublicConversationForm_RemovePrivilegeButton")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(formListener);
        buttonPanel.add(removeButton);

        controlGroupPanel1.add(buttonPanel, java.awt.BorderLayout.WEST);

        privilegePanel.add(controlGroupPanel1, java.awt.BorderLayout.SOUTH);

        add(privilegePanel, java.awt.BorderLayout.CENTER);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == addButton) {
                ManagePublicConversationForm.this.addButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                ManagePublicConversationForm.this.removeButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed

        int[] rows = privilegeTable.getSelectedRows();

        if (rows.length == 0) {
            return;
        }

        // Find the selected elements
        Object[] values = new Object[rows.length];

        for (int i = 0; i < values.length; i++)
            values[i] = privileges.get(rows[i]);

        // Remove all the selected elements
        privileges.removeAll(Arrays.asList(values));

        // Notify the table that the data changed
        ((PrivilegeTableModel) privilegeTable.getModel()).fireTableDataChanged();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        // Show the user search form
        PublicConversationUserSearchForm form = new PublicConversationUserSearchForm(getCollabSession());

        if (form.showDialog() == DialogDescriptor.OK_OPTION) {
            CollabPrincipal[] users = form.getSelectedUsers();
            int access = form.getSelectedAccess();

            // Purge the list of any privileges matching these users.
            // Note, we must do this because ConversationPrivilege is immutable.
            for (int i = 0; i < users.length; i++) {
                for (Iterator j = privileges.iterator(); j.hasNext();) {
                    ConversationPrivilege privilege = (ConversationPrivilege) j.next();

                    if (privilege.getPrincipal().equals(users[i])) {
                        j.remove();
                    }
                }
            }

            // Add each of the users to our list
            for (int i = 0; i < users.length; i++)
                privileges.add(new ConversationPrivilege(users[i], access));

            // Notify the table that the data changed
            ((PrivilegeTableModel) privilegeTable.getModel()).fireTableDataChanged();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    private class PrivilegeTableModel extends AbstractTableModel {
        private final Class[] TYPES = new Class[] { String.class, String.class, String.class };
        private final boolean[] EDITABLE = new boolean[] { false, false, true };

        /**
         *
         *
         */
        public PrivilegeTableModel() {
            super();
        }

        /**
         *
         *
         */
        public int getColumnCount() {
            return COL_NAMES.length;
        }

        /**
         *
         *
         */
        public int getRowCount() {
            return privileges.size();
        }

        /**
         *
         *
         */
        public ConversationPrivilege getPrivilege(int row) {
            return (ConversationPrivilege) privileges.get(row);
        }

        /**
         *
         *
         */
        public Object getValueAt(int row, int col) {
            switch (col) {
            case 0: {
                CollabPrincipal principal = getPrivilege(row).getPrincipal();

                //					return NbBundle.getMessage(SessionNode.class,
                //						"LBL_ManagePublicConversationForm_UserColumnFormat",
                //						principal.getDisplayName(),
                //						principal.getIdentifier());
                return " " + principal.getDisplayName();
            }

            case 1: {
                CollabPrincipal principal = getPrivilege(row).getPrincipal();

                return " " + principal.getIdentifier();
            }

            case 2:return " " + ACCESS_NAMES[getPrivilege(row).getAccess()];

            default:throw new IllegalArgumentException("Column " + // NOI18N
                    "index " + col + " out of bounds"
                ); // NOI18N
            }
        }

        /**
         *
         *
         */
        public void setValueAt(Object value, int row, int col) {
            if (col == 2) {
                AccessElement element = (AccessElement) value;
                ConversationPrivilege oldPrivilege = getPrivilege(row);
                ConversationPrivilege newPrivilege = new ConversationPrivilege(
                        oldPrivilege.getPrincipal(), element.access
                    );

                // Repalce the privilege in the list
                privileges.set(row, newPrivilege);

                fireTableCellUpdated(row, col);
            }
        }

        /**
         *
         *
         */
        public String getColumnName(int col) {
            return COL_NAMES[col];
        }

        /**
         *
         *
         */
        public Class getColumnClass(int columnIndex) {
            return TYPES[columnIndex];
        }

        /**
         *
         *
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (
                getCollabSession().getUserPrincipal().equals(
                        ((ConversationPrivilege) privileges.get(rowIndex)).getPrincipal()
                    )
            ) {
                return false;
            }

            return EDITABLE[columnIndex];
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */

    /*pkg*/ static class AccessElement extends Object {
        public final String name;
        public final int access;

        /**
         *
         *
         */
        public AccessElement(int access, String name) {
            this.name = name;
            this.access = access;
        }

        /**
         *
         *
         */
        public String toString() {
            return name;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel controlGroupPanel;
    private javax.swing.JPanel controlGroupPanel1;
    private javax.swing.JComboBox defaultAccessComboBox;
    private javax.swing.JLabel defaultAccessLabel;
    private javax.swing.JPanel privilegePanel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
