/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.wizard;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableColumnModel;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.editor.utils.DBExplorerUtil;
import org.netbeans.modules.dm.virtual.db.api.AxionDBConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public final class ChooseTablesVisualPanel extends JPanel {

    private ChooseTablesWizardPanel owner;
    private Map<String, String> userMap = new HashMap<String, String>();
    private Map<String, String> passwdMap = new HashMap<String, String>();
    private DBMetaDataFactory meta = new DBMetaDataFactory();
    DatabaseConnection conn = null;
    private Map<String, String> driverMap = new HashMap<String, String>();

    /**
     * Creates new form ChooseTableVisualPanel
     */
    public ChooseTablesVisualPanel(ChooseTablesWizardPanel panel) {
        owner = panel;
        initComponents();
        jLabel2.setDisplayedMnemonic('T');
        selectButton.setMnemonic('S');
        removeButton.setMnemonic('R');
        connectionList.setModel(new DefaultListModel());
        tableList.setModel(new DefaultListModel());
        tableList.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && evt.getSource() instanceof JList) {
                    moveSelectedTables();
                } // end if
            }
        });
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectButton.setEnabled(false);
        removeButton.setEnabled(false);
        populateDBList();
        populateConnections();
        connectionList.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                String jdbcUrl = (String) connectionList.getSelectedValue();
                DatabaseConnection dbConn = null;
                DatabaseConnection dbConns[] = ConnectionManager.getDefault().getConnections();
                for (DatabaseConnection dc : dbConns) {
                    if (dc.getDatabaseURL().equals(jdbcUrl)) {
                        dbConn = dc;
                        break;
                    }
                }

                conn = dbConn;
                ConnectionManager.getDefault().showConnectionDialog(conn);
                try {
                    userMap.put(conn.getDatabaseURL(), conn.getUser());
                    passwdMap.put(conn.getDatabaseURL(), conn.getPassword());
                    driverMap.put(conn.getDatabaseURL(), conn.getDriverClass());

                    meta.connectDB(meta.showConnectionDialog(dbConn));
                    String[] schemas = meta.getSchemas();


                    schemaCombo.setEnabled(false);
                    schemaCombo.removeAllItems();

                    if (schemas != null) {
                        schemaCombo.setEnabled(true);
                        for (String schema : schemas) {
                            schemaCombo.addItem(schema);
                            if (schema.equalsIgnoreCase(conn.getSchema())) {
                                schemaCombo.setSelectedItem(schema);
                            }
                        }

                        if (schemaCombo.getItemCount() != 0) {
                            String schema = (String) schemaCombo.getSelectedItem();
                            if (schema == null || schema.length() == 0) {
                                schema = (String) schemaCombo.getItemAt(0);
                            }
                            populateTable(schema);
                        }
                    } else {
                        populateTable("");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ChooseTablesVisualPanel.class, "LBL_Choose_Tables");
    }

    private void moveSelectedTables() {
        final DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Object[] tables = (Object[]) tableList.getSelectedValues();
        String schema = (String) schemaCombo.getSelectedItem();
        String jdbcUrl = (String) connectionList.getSelectedValue();
        for (Object table : tables) {
            Vector<String> row = new Vector<String>();
            row.add(table.toString());
            row.add(schema);
            row.add(jdbcUrl);
            model.addRow(row);
        }
        if (model.getRowCount() != 0) {
            removeButton.setEnabled(true);
            error.setText("");
            if (connectionList.getModel().getSize() != 0 &&
                    connectionList.getSelectedValue() != null) {
            }
        } else {
            removeButton.setEnabled(false);
            error.setText(NbBundle.getMessage(ChooseTablesVisualPanel.class, "LBL_No_table_processing"));
        }
        Runnable run = new Runnable() {

            public void run() {
                jTable1.setModel(model);
            }
        };
        SwingUtilities.invokeLater(run);
        owner.fireChangeEvent();
    }

    private void populateDBList() {
        Set<String> urls = new HashSet<String>();
        AxionDBConfiguration config = new AxionDBConfiguration();
        File f = new File(config.getLocation());
        File[] db = null;
        if (f.exists()) {
            db = f.listFiles();
            for (int i = 0; i < db.length; i++) {
                String ver = null;
                try {
                    ver = db[i].getCanonicalPath() + File.separator + db[i].getName().toUpperCase() + ".VER";
                    File version = new File(ver);
                    if (version.exists()) {
                        String url = "jdbc:axiondb:" + db[i].getName() + ":" +
                                config.getLocation() + db[i].getName();
                        urls.add(url);
                        DatabaseConnection con = ConnectionManager.getDefault().getConnection(url);
                        if (con == null) {
                            DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
        for (DatabaseConnection dbconn : dbconns) {
            if (dbconn.getDriverClass().equals("org.axiondb.jdbc.AxionDriver")) {
                urls.add(dbconn.getDatabaseURL());
            }
        }
    }

    public String getMashupConnection() {
        return (String) connectionList.getSelectedValue();//connectionCombo.getSelectedItem();
    }

    public DefaultTableModel getTables() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Vector<String> userVector = new Vector<String>();
        Vector<String> passVector = new Vector<String>();
        Vector<String> driverVector = new Vector<String>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String url = (String) model.getValueAt(i, 2);
            userVector.add(userMap.get(url));
            passVector.add(passwdMap.get(url));
            driverVector.add(driverMap.get(url));
        }
        model.addColumn("user", userVector);
        model.addColumn("pass", passVector);
        model.addColumn("driver", driverVector);
        TableColumnModel columnModel1 = jTable1.getColumnModel();
        for (int i = 3; i < jTable1.getColumnCount(); i++) {
            columnModel1.getColumn(i).setMinWidth(0);
            columnModel1.getColumn(i).setMaxWidth(0);
            columnModel1.getColumn(i).setPreferredWidth(0);
        }
        return model;
    }

    public void cleanup() {
        try {
            if (meta != null) {
                meta.disconnectDB();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        connectionList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableList = new javax.swing.JList();
        schemaCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        error = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        selectButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setFocusTraversalPolicy(null);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Table Selection"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Table Name", "Schema", "Connection Url"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jScrollPane3.setViewportView(jTable1);
        jTable1.getAccessibleContext().setAccessibleName("jtable1");
        jTable1.getAccessibleContext().setAccessibleDescription("jTable1");

        connectionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(connectionList);
        connectionList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.connectionList.AccessibleContext.accessibleName")); // NOI18N
        connectionList.getAccessibleContext().setAccessibleDescription("connectionlist");

        jScrollPane2.setViewportView(tableList);
        tableList.getAccessibleContext().setAccessibleDescription("tablelist");

        schemaCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemaComboActionPerformed(evt);
            }
        });

        jLabel1.setDisplayedMnemonic('C');
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setLabelFor(schemaCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(ChooseTablesVisualPanel.class, "LBL_Schema"));

        error.setDisplayedMnemonic('E');
        error.setForeground(new java.awt.Color(255, 0, 0));
        error.setLabelFor(jTable1);

        org.openide.awt.Mnemonics.setLocalizedText(selectButton, NbBundle.getMessage(ChooseTablesVisualPanel.class, "BTN_Select"));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(ChooseTablesVisualPanel.class, "BTN_Remove"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, selectButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, removeButton)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(selectButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selectButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.selectButton.AccessibleContext.accessibleName")); // NOI18N
        selectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.selectButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N

        jLabel2.setDisplayedMnemonic('T');
        jLabel2.setForeground(new java.awt.Color(0, 0, 255));
        jLabel2.setLabelFor(tableList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(ChooseTablesVisualPanel.class, "LBL_Tables"));

        jLabel4.setDisplayedMnemonic('D');
        jLabel4.setForeground(new java.awt.Color(0, 51, 255));
        jLabel4.setLabelFor(connectionList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(ChooseTablesVisualPanel.class, "LBL_Database_Connections"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(error, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 232, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel2)
                        .add(16, 16, 16)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 357, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(134, 134, 134)
                        .add(jLabel1)
                        .add(18, 18, 18)
                        .add(schemaCombo, 0, 188, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel4)
                            .add(jLabel1))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(schemaCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(25, 25, 25)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(74, 74, 74))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(1, 1, 1)))
                .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane3.getAccessibleContext().setAccessibleDescription("jscrollpane3");
        jScrollPane1.getAccessibleContext().setAccessibleName("jscrollpane1");
        jScrollPane1.getAccessibleContext().setAccessibleDescription("jscrollpane1");
        jScrollPane2.getAccessibleContext().setAccessibleName("jscrollpane2");
        jScrollPane2.getAccessibleContext().setAccessibleDescription("jscrollpane2");
        schemaCombo.getAccessibleContext().setAccessibleDescription("schemecombo");
        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N
        error.getAccessibleContext().setAccessibleName("error");
        error.getAccessibleContext().setAccessibleDescription("error");
        jPanel1.getAccessibleContext().setAccessibleName("jpanel1");
        jPanel1.getAccessibleContext().setAccessibleDescription("jpanel1");
        jLabel2.getAccessibleContext().setAccessibleName("Available Tables");
        jLabel2.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.jLabel2.AccessibleContext.accessibleDescription")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.jLabel4.AccessibleContext.accessibleName")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChooseTablesVisualPanel.class, "ChooseTablesVisualPanel.jLabel4.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName("form");
        getAccessibleContext().setAccessibleDescription("form");
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] rowIndexes = jTable1.getSelectedRows();

        for (int i = 0; i < rowIndexes.length; i++) {
            if ((rowIndexes[i] >= 0) && (jTable1.getRowCount() >= 1)) {
                removeFromSelectedTables(rowIndexes[i]);
            }
            for (int j = 0; j < rowIndexes.length; j++) {
                rowIndexes[j]--;
            }
        }
        owner.fireChangeEvent();//GEN-LAST:event_removeButtonActionPerformed
    }

    public void removeFromSelectedTables(int rowIndex) {
        try {
            final DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.removeRow(rowIndex);
        } catch (ArrayIndexOutOfBoundsException aix) {
            aix.printStackTrace();
        }
    }

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        moveSelectedTables();
        owner.fireChangeEvent();//GEN-LAST:event_selectButtonActionPerformed
    }

    private void schemaComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemaComboActionPerformed
        JComboBox combo = (JComboBox) evt.getSource();
        String schema = (String) combo.getSelectedItem();
        populateTable(schema);//GEN-LAST:event_schemaComboActionPerformed
    }                                           
    
    private void populateTable(String schema) {
        if(conn != null) {
            try {
                DefaultListModel model = (DefaultListModel) tableList.getModel();
                model.clear();
                meta.connectDB(conn.getJDBCConnection());
                String[][] tables = meta.getTablesAndViews("", schema, "", false);
                String[] currTable = null;
                if (tables != null) {
                    for (int i = 0; i < tables.length; i++) {
                        currTable = tables[i];
                        model.addElement(currTable[DBMetaDataFactory.NAME]);
                    }
                }
                if(model.getSize() != 0) {
                    selectButton.setEnabled(true);
                } else {
                    selectButton.setEnabled(false);
                }
                tableList.setModel(model);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void populateConnections() {
        DefaultListModel model = (DefaultListModel) connectionList.getModel();
        model.clear();
        driverMap.clear();
        DatabaseConnection connections[] = ConnectionManager.getDefault().getConnections();
        for(DatabaseConnection conn1 : connections) {
            model.addElement(conn1.getDatabaseURL());
        }
        setModel(connectionList, model);
    }
    
    private void setModel(final JList list, final DefaultListModel model) {
        Runnable run = new Runnable(){
            public void run() {
                list.setModel(model);
            }
        };
        SwingUtilities.invokeLater(run);
    }
    
    public Map<String, String> getUserMap() {
        return userMap;
    }
    
    public Map<String, String> getPasswordMap() {
        return passwdMap;
    }
    
    public Map<String, String> getDriverMap() {
        return driverMap;
    }
    
    public boolean canAdvance() {
        return (jTable1.getModel().getRowCount() != 0 && error.getText().trim().equals(""));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList connectionList;
    private javax.swing.JLabel error;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton removeButton;
    private javax.swing.JComboBox schemaCombo;
    private javax.swing.JButton selectButton;
    private javax.swing.JList tableList;
    // End of variables declaration//GEN-END:variables
}