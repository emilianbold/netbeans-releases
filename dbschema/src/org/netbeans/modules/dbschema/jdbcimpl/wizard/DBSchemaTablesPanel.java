/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

import org.netbeans.modules.dbschema.jdbcimpl.DDLBridge;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

public class DBSchemaTablesPanel extends JPanel implements ListDataListener {
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance("org.netbeans.modules.dbschema.jdbcimpl.wizard"); // NOI18N
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);

    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    private LinkedList tables;
    private LinkedList views;
    private ConnectionProvider cp;
    private String schema;
    private String driver;

    private DatabaseConnection dbconnOld;
    private Connection conn;

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
        
        List handlers = new ArrayList();
        Parameters params = new Parameters();
        
        boolean init = true;

        if (data.getConnectionProvider() != null) {
            if (data.getDatabaseConnection() == dbconnOld)
                init = false;

            if (init) {
                cp = data.getConnectionProvider();
                handlers.add(new Handler() {
                    public void handle(Parameters params) {
                        uninit();
                    }
                    public String getMessage() {
                        return NbBundle.getMessage(DBSchemaTablesPanel.class, "MSG_ClosingPrevious");
                    }
                });
            }
        }

        if (!init) {
            updateButtons();
            return true;
        }
        
        data.setConnected(false);
        if (!data.isExistingConn()) {
            return false;
        }
        
        // the init starts here
        
        final DatabaseConnection dbconn = data.getDatabaseConnection();
        conn = dbconn.getJDBCConnection();

        //fix for bug #4746507 - if the connection was broken outside of the IDE, set the connection to null and try to reconnect
        if (conn != null) {
            handlers.add(new Handler() {
                public void handle(Parameters params) {
                    try {
                        conn.getCatalog(); //test if the connection is alive - if it is alive, it should return something
                    } catch (SQLException exc) {
                        conn = null;
                    }
                }
                public String getMessage() {
                    return NbBundle.getMessage(DBSchemaTablesPanel.class, "MSG_CheckingExisting");
                }
            });
        }
        
        handlers.add(new Handler() {
            public void handle(Parameters params) {
                ConnectionManager.getDefault().showConnectionDialog(dbconn);
                conn = dbconn.getJDBCConnection();
            }
            public boolean getRunInEDT() {
                return true;
            }
            public boolean isRunnable() {
                return conn == null;
            }
        });
        
        handlers.add(new Handler() {
            public void handle(Parameters params) {
                
                //fix for bug #4746507 - if the connection was broken outside of the IDE, set the connection to null and try to reconnect
                if (conn != null) {
                    try {
                        conn.getCatalog(); //test if the connection is alive - if it is alive, it should return something
                    } catch (SQLException exc) {
                        conn = null;
                        data.setConnected(false);
                        params.setResult(false);
                        return;
                    }
                }

                data.setConnected(true);
                
                schema = dbconn.getSchema();
                driver = dbconn.getDriverClass();

                dbconnOld = dbconn;

                try {
                    if (conn == null) {
                        params.setResult(false);
                        return;
                    }

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
                
                params.setResult(true);
            }
            
            public String getMessage() {
                return NbBundle.getMessage(DBSchemaTablesPanel.class, "MSG_RetrievingTables");
            }
            
            public boolean isRunnable() {
                return conn != null;
            }
        });

        invokeHandlers(handlers, params);
        
        updateButtons();

        return params.getResult();
    }

    private void invokeHandlers(final List/*<Handler>*/ handlers, final Parameters params) {
        final ProgressPanel progressPanel = new ProgressPanel();
        
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(null);
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
        
        progressHandle.start();
        progressHandle.switchToIndeterminate();
        
        final int[] index = new int[1];
        
        try {
            RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    index[0] = invokeHandlers(handlers, index[0], params, progressPanel);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressPanel.close();
                        }
                    });
                }
            });
            
            while (index[0] < handlers.size()) {
                index[0] = invokeHandlers(handlers, index[0], params, null);
                if (index[0] < handlers.size()) {
                    task.schedule(0);
                    progressPanel.open(progressComponent);
                }
            }
        } finally {
            progressHandle.finish();
        }
    }
    
    private int invokeHandlers(List/*<Handler>*/ handlers, int start, Parameters params, final ProgressPanel progressPanel) {
        boolean isEDT = SwingUtilities.isEventDispatchThread();
        int i;
        
        for (i = start; i < handlers.size(); i++) {
            Handler h = (Handler)handlers.get(i);
            if (!h.isRunnable()) {
                if (LOG) {
                    LOGGER.log("Skipping " + h); // NOI18N
                }
                continue;
            }
            if (h.getRunInEDT() != isEDT) {
                break;
            }
            if (LOG) {
                LOGGER.log("Invoking " + h); // NOI18N
            }
            if (progressPanel != null) {
                final String message = h.getMessage();
                if (message != null) {
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            progressPanel.setText(message);
                        }
                    });
                }
            }
            h.handle(params);
        }
        
        return i;
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
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

        FormListener formListener = new FormListener();

        setPreferredSize(new java.awt.Dimension(400, 199));
        setLayout(new java.awt.GridBagLayout());

        jLabelAvailableTables.setLabelFor(jListAvailableTables);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAvailableTables, bundle.getString("AvailableTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabelAvailableTables, gridBagConstraints);

        jListAvailableTables.setToolTipText(bundle.getString("ACS_AvailableTablesListA11yDesc")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAdd, bundle.getString("AddButton")); // NOI18N
        jButtonAdd.setToolTipText(bundle.getString("ACS_AddButtonA11yDesc")); // NOI18N
        jButtonAdd.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelButtons.add(jButtonAdd, gridBagConstraints);

        jButtonRemove.setToolTipText(bundle.getString("ACS_RemoveButtonA11yDesc")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, bundle.getString("RemoveButton")); // NOI18N
        jButtonRemove.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanelButtons.add(jButtonRemove, gridBagConstraints);

        jButtonAddAll.setToolTipText(bundle.getString("ACS_AddAllButtonA11yDesc")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddAll, bundle.getString("AddAllButton")); // NOI18N
        jButtonAddAll.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        jPanelButtons.add(jButtonAddAll, gridBagConstraints);

        jButtonRemoveAll.setToolTipText(bundle.getString("ACS_RemoveAllButtonA11yDesc")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveAll, bundle.getString("RemoveAllButton")); // NOI18N
        jButtonRemoveAll.addActionListener(formListener);
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

        jLabelSelectedTables.setLabelFor(jListSelectedTables);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSelectedTables, bundle.getString("SelectedTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jLabelSelectedTables, gridBagConstraints);

        jListSelectedTables.setModel(new SortedListModel());
        jListSelectedTables.setToolTipText(bundle.getString("ACS_SelectedTablesListA11yDesc")); // NOI18N
        jScrollPaneSelectedTables.setViewportView(jListSelectedTables);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(jScrollPaneSelectedTables, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelNote, bundle.getString("FKReferenceNote")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(jLabelNote, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jButtonAdd) {
                DBSchemaTablesPanel.this.jButtonAddActionPerformed(evt);
            }
            else if (evt.getSource() == jButtonRemove) {
                DBSchemaTablesPanel.this.jButtonRemoveActionPerformed(evt);
            }
            else if (evt.getSource() == jButtonAddAll) {
                DBSchemaTablesPanel.this.jButtonAddAllActionPerformed(evt);
            }
            else if (evt.getSource() == jButtonRemoveAll) {
                DBSchemaTablesPanel.this.jButtonRemoveAllActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonAddAll;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JButton jButtonRemoveAll;
    private javax.swing.JLabel jLabelAvailableTables;
    private javax.swing.JLabel jLabelNote;
    private javax.swing.JLabel jLabelSelectedTables;
    private javax.swing.JList jListAvailableTables;
    private javax.swing.JList jListSelectedTables;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JScrollPane jScrollPaneAvailableTables;
    private javax.swing.JScrollPane jScrollPaneSelectedTables;
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
    
    private static abstract class Handler {
        
        public abstract void handle(Parameters params);
        
        public String getMessage() {
            return null;
        }
        
        public boolean getRunInEDT() {
            return false;
        }
        
        public boolean isRunnable() {
            return true;
        }
        
        public String toString() {
            return "Handler[message='" + getMessage() + "',runInEDT=" + getRunInEDT() + ",runnable=" + isRunnable() + "]"; // NOI18N
        }
    }
    
    private static final class Parameters {
        
        private boolean result;

        public boolean getResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }
    }
}
