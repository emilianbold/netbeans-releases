/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;

import org.netbeans.modules.dbschema.jdbcimpl.DDLBridge;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;

public class DBSchemaTablesPanel extends JPanel implements ListDataListener {

    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N
    private final ResourceBundle bundleDB = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    private LinkedList tables;
    private LinkedList views;
    private ConnectionProvider cp;
    private String schema;

    private DatabaseConnection dbconnOld;

    private ConnectionDialog dlg = null;

    private DBSchemaWizardData data;

    private int tablesCount;

    private ArrayList list;

    /** Creates new form DBSchemaTablesPanel */
    public DBSchemaTablesPanel(DBSchemaWizardData data, ArrayList list) {
        this.list = list;
        this.data = data;
        tables = new LinkedList();
        views = new LinkedList();
        cp = null;

        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(2)); //NOI18N
        setName(bundle.getString("TablesChooser")); //NOI18N

        initComponents();
        initAccessibility();
        jLabelAvailableTables.setDisplayedMnemonic(bundle.getString("AvailableTables_Mnemonic").charAt(0)); //NOI18N
        jLabelSelectedTables.setDisplayedMnemonic(bundle.getString("SelectedTables_Mnemonic").charAt(0)); //NOI18N
        jButtonAdd.setMnemonic(bundle.getString("AddButton_Mnemonic").charAt(0)); //NOI18N
        jButtonRemove.setMnemonic(bundle.getString("RemoveButton_Mnemonic").charAt(0)); //NOI18N
        jButtonAddAll.setMnemonic(bundle.getString("AddAllButton_Mnemonic").charAt(0)); //NOI18N
        jButtonRemoveAll.setMnemonic(bundle.getString("RemoveAllButton_Mnemonic").charAt(0)); //NOI18N

        jListAvailableTables.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jListAvailableTables.requestFocus();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        jListSelectedTables.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jListSelectedTables.requestFocus();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        jListSelectedTables.getModel().addListDataListener(this);

        (jListAvailableTables.getSelectionModel()).addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    jButtonAdd.setEnabled(jListAvailableTables.getSelectedIndex() == -1 ? false : true);
                }
            });
        (jListSelectedTables.getSelectionModel()).addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    jButtonRemove.setEnabled(jListSelectedTables.getSelectedIndex() == -1 ? false : true);
                }
            });
    }

    protected boolean init() {
        String driver = null;
        Connection conn = null;
        boolean init = true;

        if (data.getConnectionProvider() != null) {
            if (data.getDatabaseConnection() == dbconnOld)
                init = false;

            if (init) {
                cp = data.getConnectionProvider();
                uninit();
            }
        }

        if (init) {
            data.setConnected(false);

            if (data.isExistingConn()) {
                final DatabaseConnection dbconn = data.getDatabaseConnection();
                conn = dbconn.getJDBCConnection();

                //fix for bug #4746507 - if the connection was broken outside of the IDE, set the connection to null and try to reconnect
                if (conn != null)
                    try {
                        conn.getCatalog(); //test if the connection is alive - if it is alive, it should return something
                    } catch (SQLException exc) {
                        conn = null;
                    }

                if (conn == null) {
                    ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    conn = dbconn.getJDBCConnection();

                    //fix for bug #4746507 - if the connection was broken outside of the IDE, set the connection to null and try to reconnect
                    if (conn != null)
                        try {
                            conn.getCatalog(); //test if the connection is alive - if it is alive, it should return something
                        } catch (SQLException exc) {
                            conn = null;
                            data.setConnected(false);
                            return false;
                        }

                    data.setConnected(true);
                }
                schema = dbconn.getSchema();
                driver = dbconn.getDriverClass();

                dbconnOld = dbconn;
            }

            try {
                if (conn == null)
                    return false;

                cp = new ConnectionProvider(conn, driver);
                cp.setSchema(schema);
            } catch (SQLException exc) {
                //PENDING
            }

            data.setConnectionProvider(cp);

            tables.clear();
            views.clear();

            try {
                DDLBridge bridge = new DDLBridge(cp.getConnection(), cp.getSchema(), cp.getDatabaseMetaData());

                ResultSet rs;
                bridge.getDriverSpecification().getTables("%", new String[] {"TABLE"}); //NOI18N
                rs = bridge.getDriverSpecification().getResultSet();
                if (rs != null) {
                    while (rs.next())
                        tables.add(rs.getString("TABLE_NAME").trim()); //NOI18N
                    rs.close();
                }

                rs = null;
                if (bridge.getDriverSpecification().areViewsSupported()) {
                    bridge.getDriverSpecification().getTables("%", new String[] {"VIEW"}); //NOI18N
                    rs = bridge.getDriverSpecification().getResultSet();
                }
                if (rs != null) {
                    while (rs.next())
                        views.add(rs.getString("TABLE_NAME").trim()); //NOI18N
                    rs.close();
                }
            } catch (SQLException exc) {
                org.openide.ErrorManager.getDefault().notify(exc);
            }

            ((SortedListModel) jListAvailableTables.getModel()).clear();
            ((SortedListModel) jListSelectedTables.getModel()).clear();

            tablesCount = tables.size();

            for (int i = 0; i < tables.size(); i++)
                ((SortedListModel) jListAvailableTables.getModel()).add(bundle.getString("TablePrefix") + " " + tables.get(i).toString()); //NOI18N

            for (int i = 0; i < views.size(); i++)
                ((SortedListModel) jListAvailableTables.getModel()).add(bundle.getString("ViewPrefix") + " " + views.get(i).toString()); //NOI18N
            if (jListAvailableTables.getModel().getSize() > 0)
                jListAvailableTables.setSelectedIndex(0);
            tables.clear();
            views.clear();
        }
        updateButtons();

        return true;
    }

    public void uninit() {
        try {
            if (cp != null)
                if (data.isConnected())
                    if (data.isExistingConn())
                        ConnectionManager.getDefault().disconnect(dbconnOld);
                    else
                        if (dbconnOld.getJDBCConnection() != null)
                            ConnectionManager.getDefault().disconnect(dbconnOld);
                        else
                            cp.closeConnection();
        } catch (Exception exc) {
            //unable to disconnect
        }
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TablesPanelA11yDesc"));  // NOI18N
        jLabelAvailableTables.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AvailableTablesA11yDesc"));  // NOI18N
        jListAvailableTables.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AvailableTablesListA11yName"));  // NOI18N
        jLabelSelectedTables.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_SelectedTablesA11yDesc"));  // NOI18N
        jListSelectedTables.getAccessibleContext().setAccessibleName(bundle.getString("ACS_SelectedTablesListA11yName"));  // NOI18N
        jLabelNote.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_FKReferenceNoteA11yDesc"));  // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelAvailableTables = new javax.swing.JLabel();
        jScrollPaneAvailableTables = new javax.swing.JScrollPane();
        jListAvailableTables = new javax.swing.JList();
        jPanelButtons = new javax.swing.JPanel();
        jButtonAdd = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jButtonAddAll = new javax.swing.JButton();
        jButtonRemoveAll = new javax.swing.JButton();
        jLabelSelectedTables = new javax.swing.JLabel();
        jScrollPaneSelectedTables = new javax.swing.JScrollPane();
        jListSelectedTables = new javax.swing.JList();
        jLabelNote = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelAvailableTables.setText(bundle.getString("AvailableTables"));
        jLabelAvailableTables.setLabelFor(jListAvailableTables);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabelAvailableTables, gridBagConstraints);

        jListAvailableTables.setToolTipText(bundle.getString("ACS_AvailableTablesListA11yDesc"));
        jListAvailableTables.setModel(new SortedListModel());
        jScrollPaneAvailableTables.setViewportView(jListAvailableTables);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(jScrollPaneAvailableTables, gridBagConstraints);

        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        jButtonAdd.setToolTipText(bundle.getString("ACS_AddButtonA11yDesc"));
        jButtonAdd.setText(bundle.getString("AddButton"));
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelButtons.add(jButtonAdd, gridBagConstraints);

        jButtonRemove.setToolTipText(bundle.getString("ACS_RemoveButtonA11yDesc"));
        jButtonRemove.setText(bundle.getString("RemoveButton"));
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanelButtons.add(jButtonRemove, gridBagConstraints);

        jButtonAddAll.setToolTipText(bundle.getString("ACS_AddAllButtonA11yDesc"));
        jButtonAddAll.setText(bundle.getString("AddAllButton"));
        jButtonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAllActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        jPanelButtons.add(jButtonAddAll, gridBagConstraints);

        jButtonRemoveAll.setToolTipText(bundle.getString("ACS_RemoveAllButtonA11yDesc"));
        jButtonRemoveAll.setText(bundle.getString("RemoveAllButton"));
        jButtonRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveAllActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanelButtons.add(jButtonRemoveAll, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
        add(jPanelButtons, gridBagConstraints);

        jLabelSelectedTables.setText(bundle.getString("SelectedTables"));
        jLabelSelectedTables.setLabelFor(jListSelectedTables);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jLabelSelectedTables, gridBagConstraints);

        jListSelectedTables.setToolTipText(bundle.getString("ACS_SelectedTablesListA11yDesc"));
        jListSelectedTables.setModel(new SortedListModel());
        jScrollPaneSelectedTables.setViewportView(jListSelectedTables);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(jScrollPaneSelectedTables, gridBagConstraints);

        jLabelNote.setText(bundle.getString("FKReferenceNote"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(jLabelNote, gridBagConstraints);

    }//GEN-END:initComponents

    private void jButtonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveAllActionPerformed
        SortedListModel ulm = (SortedListModel) jListAvailableTables.getModel();
        SortedListModel slm = (SortedListModel) jListSelectedTables.getModel();

        Object[] values = slm.toArray();
        for (int i = 0; i < values.length; i++) {
            ulm.add(values[i]);
            slm.remove(values[i]);
        }

        tables.clear();
        views.clear();

        int[] sel = new int[values.length];
        for (int i = 0; i < values.length; i++)
            sel[i] = ulm.indexOf(values[i]);
        jListAvailableTables.setSelectedIndices(sel);

        setSelection();
        updateButtons();
    }//GEN-LAST:event_jButtonRemoveAllActionPerformed

    private void jButtonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAllActionPerformed
        SortedListModel ulm = (SortedListModel) jListAvailableTables.getModel();
        SortedListModel slm = (SortedListModel) jListSelectedTables.getModel();

        String name;
        Object[] values = ulm.toArray();
        for (int i = 0; i < values.length; i++) {
            slm.add(values[i]);
            ulm.remove(values[i]);

            name = values[i].toString();
            if (name.startsWith(bundle.getString("TablePrefix"))) //NOI18N
                tables.add(name.substring(name.indexOf(" ") + 1)); //NOI18N
            else
                views.add(name.substring(name.indexOf(" ") + 1)); //NOI18N
        }

        int[] sel = new int[values.length];
        for (int i = 0; i < values.length; i++)
            sel[i] = slm.indexOf(values[i]);
        jListSelectedTables.setSelectedIndices(sel);

        setSelection();
        updateButtons();
    }//GEN-LAST:event_jButtonAddAllActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        SortedListModel ulm = (SortedListModel) jListAvailableTables.getModel();
        SortedListModel slm = (SortedListModel) jListSelectedTables.getModel();

        String name;
        Object[] values = jListSelectedTables.getSelectedValues();
        for (int i = 0; i < values.length; i++) {
            ulm.add(values[i]);
            slm.remove(values[i]);

            name = values[i].toString();
            name = (name.substring(name.indexOf(" "))).trim(); //NOI18N
            if (tables.contains(name))
                tables.remove(name);
            else
                views.remove(name);
        }

        int[] sel = new int[values.length];
        for (int i = 0; i < values.length; i++)
            sel[i] = ulm.indexOf(values[i]);
        jListAvailableTables.setSelectedIndices(sel);

        setSelection();
        updateButtons();
    }//GEN-LAST:event_jButtonRemoveActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        SortedListModel ulm = (SortedListModel) jListAvailableTables.getModel();
        SortedListModel slm = (SortedListModel) jListSelectedTables.getModel();

        String name;
        Object[] values = jListAvailableTables.getSelectedValues();
        for (int i = 0; i < values.length; i++) {
            slm.add(values[i]);
            ulm.remove(values[i]);

            name = values[i].toString();
            if (name.startsWith(bundle.getString("TablePrefix"))) //NOI18N
                tables.add(name.substring(name.indexOf(" ") + 1)); //NOI18N
            else
                views.add(name.substring(name.indexOf(" ") + 1)); //NOI18N
        }

        int[] sel = new int[values.length];
        for (int i = 0; i < values.length; i++)
            sel[i] = slm.indexOf(values[i]);
        jListSelectedTables.setSelectedIndices(sel);

        setSelection();
        updateButtons();
    }//GEN-LAST:event_jButtonAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneSelectedTables;
    private javax.swing.JLabel jLabelNote;
    private javax.swing.JButton jButtonAddAll;
    private javax.swing.JList jListSelectedTables;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JButton jButtonRemoveAll;
    private javax.swing.JLabel jLabelSelectedTables;
    private javax.swing.JList jListAvailableTables;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JScrollPane jScrollPaneAvailableTables;
    private javax.swing.JLabel jLabelAvailableTables;
    // End of variables declaration//GEN-END:variables

    private void setSelection() {
        data.setTables(tables);
        data.setViews(views);

        if (tablesCount == tables.size())
            data.setAllTables(true);
        else
            data.setAllTables(false);
    }

    private void updateButtons() {
        jButtonAdd.setEnabled(jListAvailableTables.getSelectedIndex() == -1 ? false : true);
        jButtonAddAll.setEnabled(((SortedListModel) jListAvailableTables.getModel()).isEmpty() ? false : true);
        jButtonRemove.setEnabled(jListSelectedTables.getSelectedIndex() == -1 ? false : true);
        jButtonRemoveAll.setEnabled(((SortedListModel) jListSelectedTables.getModel()).isEmpty() ? false : true);
    }

    public boolean isValid() {
        if (jListSelectedTables.getModel().getSize() > 0)
            return true;
        else
            return false;
    }

    public void intervalAdded(javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void contentsChanged(javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void fireChange (Object source) {
        ArrayList lst;

        synchronized (this) {
            lst = (ArrayList) this.list.clone();
        }

        ChangeEvent event = new ChangeEvent(source);
        for (int i=0; i< lst.size(); i++){
            ChangeListener listener = (ChangeListener) lst.get(i);
            listener.stateChanged(event);
        }
    }
}
