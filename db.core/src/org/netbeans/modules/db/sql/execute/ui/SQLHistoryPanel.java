/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 - 2010 Sun Microsystems, Inc.
 */

/*
 * SQLHistoryPanel.java
 *
 * @author jbaker
 */
package org.netbeans.modules.db.sql.execute.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.db.sql.history.SQLHistory;
import org.netbeans.modules.db.sql.history.SQLHistoryException;
import org.netbeans.modules.db.sql.history.SQLHistoryManager;
import org.netbeans.modules.db.sql.history.SQLHistoryModel;
import org.netbeans.modules.db.sql.history.SQLHistoryModelImpl;
import org.netbeans.modules.db.sql.history.SQLHistoryPersistenceManager;
import org.netbeans.modules.db.sql.loader.SQLDataLoader;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author John Baker
 */
public class SQLHistoryPanel extends javax.swing.JPanel {
    public static final String SAVE_STATEMENTS_CLEARED = ""; // NOI18N  
    public static final int SAVE_STATEMENTS_MAX_LIMIT = 10000; 
    public static final int TABLE_DATA_WIDTH_SQL = 125;
    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPanel.class.getName());
    private static final FileObject USERDIR = FileUtil.getConfigRoot();
    private static final FileObject historyRoot = USERDIR.getFileObject(SQLHistoryManager.SQL_HISTORY_FOLDER);
    private static Object[][] data;
    private Set<String> currentConnections = new HashSet<String>();
    private SQLHistoryView view;
    private JEditorPane editorPane;
    private Map<String,String> urlAliasMap = new HashMap<String, String>();
    private Map<String,String> aliasUrlMap = new HashMap<String, String>();
    private static RequestProcessor RP = new RequestProcessor(SQLHistoryPanel.class);
    private final Task initTask;

    /** Creates new form SQLHistoryPanel */
    public SQLHistoryPanel(final JEditorPane editorPane) {
        this.editorPane = editorPane;

        // dummy model
        view = new SQLHistoryView(new SQLHistoryModel() {

            @Override
            public void initialize() {
            }

            @Override
            public void setFilter(String filter) {
            }

            @Override
            public String getFilter() {
                return ""; // NOI18N
            }

            @Override
            public List<SQLHistory> getSQLHistoryList() throws SQLHistoryException {
                return Collections.emptyList();
            }

            @Override
            public void setSQLHistoryList(List<SQLHistory> sqlHistoryList) {
            }
        });

        initSQLHistoryTableData();
        initComponents();
        setupSQLSaveLimit();
        initTask = RP.post(new Runnable() {

            @Override
            public void run() {
                lazyInit();
            }
        });
    }

    public void lazyInit() {
        view = new SQLHistoryView(new SQLHistoryModelImpl());
        for (DatabaseConnection existingConnection : ConnectionManager.getDefault().getConnections()) {
            urlAliasMap.put(existingConnection.getDatabaseURL(), existingConnection.getDisplayName());
            aliasUrlMap.put(existingConnection.getDisplayName(), existingConnection.getDatabaseURL());
        }
        // Adjust table column width
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setWaitingState(false);
                initSQLHistoryTableData();
                initComponentData();
                adjustColumnPreferredWidths(sqlHistoryTable);
                sqlHistoryTable.revalidate();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //show progress for initialize method
        final Window w = findWindowParent();
        if (w != null) {
            w.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setWaitingState(! initTask.isFinished());
                }
            });
        }
    }

    private void setWaitingState(boolean waitingState) {
        boolean enabled = !waitingState;
        Component parent = getParent();
        Component rootPane = getRootPane();
        if (parent != null) {
            parent.setEnabled(enabled);
        }
        if (rootPane != null) {
            if (enabled) {
                rootPane.setCursor(null);
            } else {
                rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        }
    }

    private Window findWindowParent() {
        Component c = this;
        while (c != null) {
            c = c.getParent();
            if (c instanceof Window) {
                return (Window) c;
            }
        }
        return null;
    }

    private void setupSQLSaveLimit() {
        // SQL statments save limit
        String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", ""); // NOI18N
        if (null != savedLimit && !savedLimit.equals(SAVE_STATEMENTS_CLEARED)) {
            sqlLimitTextField.setText(savedLimit);
        } else {
            sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED);
            savedLimit = SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED;
            NbPreferences.forModule(SQLHistoryPanel.class).put("SQL_STATEMENTS_SAVED_FOR_HISTORY", SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED);  // NOI18N
        }
    }

    private void initComponentData() {
        searchTextField.getDocument().addDocumentListener((HistoryTableModel) sqlHistoryTable.getModel());
        sqlHistoryTable.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SQLTableTitle"));
        sqlHistoryTable.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DateTableTitle"));
        // Add mouse listener to listen for mouse click on the table header so columns can be sorted
        JTableHeader header = sqlHistoryTable.getTableHeader();
        header.addMouseListener(new ColumnListener());

        // Add mouse listener for the case when a user double-clicks on a row to insert SQL
        sqlHistoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseUtils.isDoubleClick(e)) {
                    insertSQL();
                    e.consume();
                }
            }
        });

        // Initialize sql column data
        connectionUrlComboBox.addActionListener((HistoryTableModel) sqlHistoryTable.getModel());
        view.updateConnectionUrl();
    }
    
    private void initSQLHistoryTableData() {
        if (initTask == null || ! initTask.isFinished()) {
            data = new Object[1][2];
            data[0][0] = NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel_PleaseWait");
            return ;
        }
        // Initialize sql column data          
        List<String> sqlList = view.getSQLList(null);
        List<String> dateList = view.getDateList(null);
        data = new Object[sqlList.size()][2];
        int row = 0;
        int maxLength; 
        int length;
        for (String sql : sqlList) {
            length = sql.trim().length();
            maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
            data[row][0] = sql.trim().substring(0, maxLength).replaceAll("\n", " ");
            row++;
        }
        // Initialize data
        row = 0;
        for (String date : dateList) {
            data[row][1] = date;
            row++;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        connectionUrlComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        insertSQLButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sqlHistoryTable = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(view.getSQLHistoryTooltipValue(rowIndex, vColIndex));
                }
                return c;
            }
        };
        sqlLimitLabel = new javax.swing.JLabel();
        sqlLimitTextField = new javax.swing.JTextField();
        sqlLimitButton = new javax.swing.JButton();
        inputWarningLabel = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Connection")); // NOI18N

        connectionUrlComboBox.setRenderer(new ConnectionUrlRenderer());

        jLabel2.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Match")); // NOI18N

        searchTextField.setMinimumSize(new java.awt.Dimension(20, 22));

        org.openide.awt.Mnemonics.setLocalizedText(insertSQLButton, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Insert")); // NOI18N
        insertSQLButton.setEnabled(false);
        insertSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSQLButtonActionPerformed(evt);
            }
        });

        sqlHistoryTable.setModel(new HistoryTableModel());
        sqlHistoryTable.setGridColor(java.awt.Color.lightGray);
        jScrollPane1.setViewportView(sqlHistoryTable);
        sqlHistoryTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_History")); // NOI18N
        sqlHistoryTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_History")); // NOI18N

        sqlLimitLabel.setLabelFor(sqlLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(sqlLimitLabel, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SqlLimit")); // NOI18N

        sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED);
        sqlLimitTextField.setMinimumSize(new java.awt.Dimension(18, 22));

        org.openide.awt.Mnemonics.setLocalizedText(sqlLimitButton, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ApplyButton")); // NOI18N
        sqlLimitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sqlLimitButtonActionPerformed(evt);
            }
        });

        inputWarningLabel.setForeground(java.awt.Color.red);
        inputWarningLabel.setFocusable(false);
        inputWarningLabel.setRequestFocusEnabled(false);
        inputWarningLabel.setVerifyInputWhenFocusTarget(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(connectionUrlComboBox, 0, 306, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertSQLButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputWarningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                        .addGap(493, 493, 493))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sqlLimitLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sqlLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sqlLimitButton))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(connectionUrlComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(insertSQLButton)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sqlLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sqlLimitButton)
                            .addComponent(sqlLimitLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inputWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        connectionUrlComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ASCN_ConnectionCombo")); // NOI18N
        connectionUrlComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_ConnectionCombo")); // NOI18N
        searchTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Match")); // NOI18N
        searchTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Match")); // NOI18N
        insertSQLButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Insert")); // NOI18N
        insertSQLButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Insert")); // NOI18N
        sqlLimitTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Save")); // NOI18N
        sqlLimitTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Save")); // NOI18N
        sqlLimitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Apply")); // NOI18N
        sqlLimitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Apply")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void insertSQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSQLButtonActionPerformed
    insertSQL();
}//GEN-LAST:event_insertSQLButtonActionPerformed

private void sqlLimitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sqlLimitButtonActionPerformed
    verifySQLLimit();
}//GEN-LAST:event_sqlLimitButtonActionPerformed


    private void insertSQL() {
        try {
            // Make sure to insert the entire SQL, not just what appears in the Table
            List<SQLHistory> sqlHistoryList = view.getCurrentSQLHistoryList();
            int i = 0;
            String sqlToInsert = ""; // NOI18N
            InsertSQLUtility insertUtility = new InsertSQLUtility();
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (sqlHistoryTable.isRowSelected(i)) {
                    sqlToInsert = sqlHistory.getSql().trim();
                    JEditorPane pane = (JEditorPane)EditorRegistry.lastFocusedComponent();
                    String mime = pane.getContentType();
                    if (mime.equals(SQLDataLoader.SQL_MIME_TYPE)) {
                        editorPane = pane;
                    }
                    insertUtility.insert(sqlToInsert, editorPane);
                }
                // increment for the next row
                i++;
            }
            
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void verifySQLLimit() {
        String enteredLimit = sqlLimitTextField.getText();
        int iLimit = 0;
        if (enteredLimit.equals(SAVE_STATEMENTS_CLEARED)) {
            updateSaveLimitUponClear(iLimit);
            inputWarningLabel.setText(""); // NOI18N
        } else { // user enters a value to limit the number of SQL statements to save
            updateSaveLimitUponReset(enteredLimit);
        }
    }

    private void updateSaveLimitUponClear(int iLimit) {
        iLimit = SAVE_STATEMENTS_MAX_LIMIT;
        List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
        try {
            view.setSQLHistoryList(SQLHistoryPersistenceManager.getInstance().updateSQLSaved(iLimit, historyRoot));
            sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve(historyRoot);
            view.setCurrentSQLHistoryList(sqlHistoryList);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLHistoryException ex) {
            handleSQLHistoryException();
        }
        ((HistoryTableModel) sqlHistoryTable.getModel()).refreshTable(sqlHistoryList);
        NbPreferences.forModule(SQLHistoryPanel.class).put("SQL_STATEMENTS_SAVED_FOR_HISTORY", Integer.toString(iLimit));  // NOI18N               
        sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED);
    }
    
    private void updateSaveLimitUponReset(String enteredLimit) {
        try {
            int iLimit;
            if (enteredLimit.trim().length() < 10) {
                iLimit = Integer.parseInt(enteredLimit);
            } else {
                // too long number
                iLimit = SAVE_STATEMENTS_MAX_LIMIT + 1;
            }
            String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", SAVE_STATEMENTS_CLEARED); // NOI18N
            if (iLimit < 0) {
                inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_TextInputWarningLabel"));
                if (savedLimit != null) {
                    sqlLimitTextField.setText(savedLimit);
                } else {
                    sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED); 
                }
            } else if (iLimit > SAVE_STATEMENTS_MAX_LIMIT) {
                sqlLimitButton.setEnabled(true);
                inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_NumberInputWarningLabel"));
                // reset user's input
                if (savedLimit != null) {
                    sqlLimitTextField.setText(savedLimit);
                } else {
                    sqlLimitTextField.setText(SAVE_STATEMENTS_CLEARED); 
                    sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED); 
                }
            } else {
                inputWarningLabel.setText(""); // NOI18N
                if (SQLHistoryPersistenceManager.getInstance().updateSQLSaved(iLimit, historyRoot).size() > 0) {
                    List<SQLHistory> sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve(historyRoot);
                    view.setCurrentSQLHistoryList(sqlHistoryList);
                    ((HistoryTableModel) sqlHistoryTable.getModel()).refreshTable(sqlHistoryList);
                    view.updateConnectionUrl();
                    NbPreferences.forModule(SQLHistoryPanel.class).put("SQL_STATEMENTS_SAVED_FOR_HISTORY", Integer.toString(iLimit));  // NOI18N
                }
            }
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLHistoryException ex) {
            handleSQLHistoryException();
        } catch (NumberFormatException ne) {
            inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_TextInputWarningLabel"));
            // reset user's input
            String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", ""); // NOI18N
            if (savedLimit != null) {
                sqlLimitTextField.setText(savedLimit);
            } else {
                sqlLimitTextField.setText(SQLHistoryManager.SAVE_STATEMENTS_MAX_LIMIT_ENTERED); 
            }
        }        
    }
    
    private void handleSQLHistoryException() {
        LOGGER.log(Level.WARNING, NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ErrorParsingSQLHistory"));
        List<SQLHistory> sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve();
        view.setCurrentSQLHistoryList(sqlHistoryList);
        ((HistoryTableModel) sqlHistoryTable.getModel()).refreshTable(sqlHistoryList);
        view.updateConnectionUrl();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionUrlComboBox;
    private javax.swing.JLabel inputWarningLabel;
    private javax.swing.JButton insertSQLButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JTable sqlHistoryTable;
    private javax.swing.JButton sqlLimitButton;
    private javax.swing.JLabel sqlLimitLabel;
    private javax.swing.JTextField sqlLimitTextField;
    // End of variables declaration//GEN-END:variables

    private class SQLHistoryView {
        private SQLHistoryModel model;
        List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
        List<SQLHistory> currentSQLHistoryList  = new ArrayList<SQLHistory>();
        public static final String MATCH_EMPTY = ""; // NOI18N

        public SQLHistoryView(SQLHistoryModel model) {
            this.model = model;
            init();
        }
        
        private void init() {
            try {
                this.sqlHistoryList = model.getSQLHistoryList();
                this.currentSQLHistoryList = model.getSQLHistoryList();
            } catch (SQLHistoryException ex) {
                LOGGER.log(Level.INFO, NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ErrorParsingSQLHistory"), ex); // NOI18N
                sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve();
                setCurrentSQLHistoryList(sqlHistoryList);
            }
        }

        public void setCurrentSQLHistoryList(List<SQLHistory> sqlHistoryList) {
            currentSQLHistoryList = sqlHistoryList;
        }

        public List<SQLHistory> getCurrentSQLHistoryList() {
            return currentSQLHistoryList;
        }

        public List<SQLHistory> getSQLHistoryList() {
            return sqlHistoryList;
        }
        
        public void setSQLHistoryList(List<SQLHistory> sqlHistoryList) {
             this.sqlHistoryList = sqlHistoryList;
        }
        
        /**
         * Get the SQL statement string at the row,col position in the table and convert the string to html
         * @param row - table row
         * @param col - table column
         * @return    - formatted SQL statement for the specified row, col
         */
        public String getSQLHistoryTooltipValue(int row, int col) {
            List<SQLHistory> sqlHistoryListForTooltip =  view.getCurrentSQLHistoryList();
            if (sqlHistoryListForTooltip != null && row < sqlHistoryListForTooltip.size()) {
                if (col == 0) {
                    String sqlRow = sqlHistoryListForTooltip.get(row).getSql().trim();
                    while (sqlRow.indexOf("\n") != -1) {        // NOI18N
                        sqlRow = replace(sqlRow, "\n", "<br>"); // NOI18N
                    }
                    return "<html>" + sqlRow + "</html>";       // NOI18N
                } else {
                    return DateFormat.getInstance().format(sqlHistoryListForTooltip.get(row).getDate());
                }
            } else {
                return null;
            }
        }
        
        /**
         * Convert sql statement to html for proper rendering in the table's tooltip
         * @param target - original string
         * @param from - string to replace
         * @param to - string to replace with
         * @return - updated string
         */
        public String replace(String target, String from, String to) {
            int start = target.indexOf(from);
            if (start == -1) {
                return target;
            }
            int lf = from.length();
            char[] targetChars = target.toCharArray();
            StringBuilder buffer = new StringBuilder();
            int copyFrom = 0;
            while (start != -1) {
                buffer.append(targetChars, copyFrom, start - copyFrom);
                buffer.append(to);
                copyFrom = start + lf;
                start = target.indexOf(from, copyFrom);
            }
            buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
            return buffer.toString();
        }
             
        public List<String> getSQLList(List<SQLHistory> sqlHistoryList) {
            List<String> sqlList = new ArrayList<String>();
            if (sqlHistoryList == null) {
                sqlHistoryList = getSQLHistoryList();
            }

            for (SQLHistory sqlHistory : sqlHistoryList) {
                String sql = sqlHistory.getSql();
                if (!sqlList.contains(sql)) {
                    sqlList.add(sql); 
                }
            }
            return sqlList;
        }

        public List<String> getDateList(List<SQLHistory> sqlHistoryList) {
            List<String> dateList = new ArrayList<String>();
            List<String> sqlList = new ArrayList<String>();
            if (sqlHistoryList == null) {
                sqlHistoryList = getSQLHistoryList();
            }

            for (SQLHistory sqlHistory : sqlHistoryList) {
                String date = DateFormat.getInstance().format(sqlHistory.getDate());
                String sql = sqlHistory.getSql();
                // need to make sure that the date is the one that belongs with the SQL
                if (!sqlList.contains(sql)) {                                        
                    sqlList.add(sql); 
                    dateList.add(date);
                }
            }
            return dateList;
        }

        public void updateConnectionUrl() {
            // Initialize combo box data
            currentConnections.clear();
            // Set default item in the combo box
            String defaultSelectedItem = NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem");
            currentConnections.add(defaultSelectedItem);

            for (SQLHistory sqlHistory : sqlHistoryList) {
                String url = sqlHistory.getUrl();
                if (urlAliasMap.containsKey(url)) {
                    // add connection display name
                    currentConnections.add(urlAliasMap.get(url));
                } else {
                    // add URL
                    currentConnections.add(url);
                }
            }
            // Initialize combo box
            connectionUrlComboBox.setModel(new DefaultComboBoxModel(currentConnections.toArray()));
            connectionUrlComboBox.setSelectedItem(defaultSelectedItem);
            connectionUrlComboBox.revalidate();
        }

        private List<SQLHistory> filterSQLHistoryList() {
            List<SQLHistory> filteredSqlHistoryList = new ArrayList<SQLHistory>();
            String match = searchTextField.getText();
            String url = (String)connectionUrlComboBox.getSelectedItem();
            if (aliasUrlMap.containsKey(url)) {
                url = aliasUrlMap.get(url);
            }
            // modify list of SQL to reflect a selection from the Connection dropdown or if a match text entered
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (sqlHistory.getUrl().equals(url) || url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem"))) {
                    if (!match.equals(MATCH_EMPTY)) {
                        if (sqlHistory.getSql().toLowerCase().indexOf(match.toLowerCase()) != -1) {
                            filteredSqlHistoryList.add(sqlHistory);
                        }
                    } else {
                        filteredSqlHistoryList.add(sqlHistory);
                    }
                }
            }
            currentSQLHistoryList = filteredSqlHistoryList;
            return filteredSqlHistoryList;
        }
    }

    private final class HistoryTableModel extends DefaultTableModel implements ActionListener, DocumentListener {
        List<String> sqlList;
        List<String> dateList;
        int sortCol = 1;
        boolean sortAsc = false;

        @Override
        public int getRowCount() {
            if (sqlHistoryTable.getSelectedRow() == -1) {
                insertSQLButton.setEnabled(false);
            } 
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            Object value = getValueAt(0, c);
            if (value == null) {
                return String.class;
            } else {
                return getValueAt(0, c).getClass();
            }
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (sqlHistoryTable.isRowSelected(row)) {
                insertSQLButton.setEnabled(true);
            } 
            return data[row][col];
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            adjustColumnPreferredWidths(sqlHistoryTable);
            fireTableCellUpdated(row, col);
        }

        @Override
        public void addTableModelListener(TableModelListener arg0) {
            // not used
        }

        @Override
        public void removeTableModelListener(TableModelListener arg0) {
            // not used
        }

        public void adjustColumnPreferredWidths(JTable table) {
            // Get max width for cells in column and make that the preferred width
            TableColumnModel columnModel = table.getColumnModel();
            for (int col = 0; col < table.getColumnCount(); col++) {

                int maxwidth = 0;
                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer rend =
                            table.getCellRenderer(row, col);
                    Object value = table.getValueAt(row, col);
                    Component comp =
                            rend.getTableCellRendererComponent(table,
                            value,
                            false,
                            false,
                            row,
                            col);
                    maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
                }
                TableColumn column = columnModel.getColumn(col);
                column.setPreferredWidth(maxwidth);
                column.setHeaderRenderer(createDefaultRenderer());
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            processUpdate();
        }
        
        public void refreshTable(List<SQLHistory> sqlHistoryList) {
            if (initTask == null) {
                return ;
            }
            String url;
            // Get the connection url from the combo box
            if (sqlHistoryList.size() > 0) {
                url = connectionUrlComboBox.getSelectedItem().toString();
                if (aliasUrlMap.containsKey(url)) {
                    url = aliasUrlMap.get(url);
                }
            } else {
                url = NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem");
            }
            sqlList = new ArrayList<String>();
            dateList = new ArrayList<String>();
            connectionUrlComboBox.setToolTipText(url);
            int length;
            int maxLength;
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem")) ||
                      url.equals(sqlHistory.getUrl())) {
                    length = sqlHistory.getSql().trim().length();
                    maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                    sqlList.add(sqlHistory.getSql().trim().substring(0, maxLength).replaceAll("\n", " "));
                    dateList.add(DateFormat.getInstance().format(sqlHistory.getDate()));
                }
            }
            // Initialize sql column data
            data = null;
            data = new Object[sqlList.size()][2];
            int row = 0;
            for (String sql : sqlList) {
                length = sql.trim().length();
                maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                data[row][0] = sql.trim().substring(0, maxLength).replaceAll("\n", " ");
                row++;
            }
            // Initialize date column data
            row = 0;
            for (String date : dateList) {
                data[row++][1] = date;
            }
            // Refresh table
            if (data.length > 0) {
                sqlHistoryTable.revalidate();
            } else {
                sqlHistoryTable.revalidate();
                insertSQLButton.setEnabled(false);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent evt) {
            processUpdate();
                    } 

        @Override
        public void removeUpdate(DocumentEvent evt) {
            processUpdate();
                    }

        private void processUpdate() {
            view.setCurrentSQLHistoryList(view.filterSQLHistoryList());
            sqlHistoryTable.repaint();
            sqlHistoryTable.clearSelection();
            refreshTable(view.getCurrentSQLHistoryList());
                    }

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            // unused
        }

        public List<SQLHistory> sortData() {
            // Refresh the table
            List<SQLHistory> filteredSQLHistoryList = view.filterSQLHistoryList();
            SQLComparator sqlComparator = new SQLComparator(sortCol, sortAsc);
            Collections.sort(filteredSQLHistoryList, sqlComparator);
            view.setCurrentSQLHistoryList(filteredSQLHistoryList);
            refreshTable( filteredSQLHistoryList);
            return filteredSQLHistoryList;
        }
    }

    public static String read(Document doc) throws InterruptedException, Exception {
        Renderer r = new Renderer(doc);
        doc.render(r);

        synchronized (r) {
            while (!r.done) {
                r.wait();
                if (r.err != null) {
                    throw new Exception(r.err);
                }
            }
        }
        return r.result;
    }

    private static class Renderer implements Runnable {
        Document doc;
        String result;
        Throwable err;
        boolean done;

        Renderer(Document doc) {
            this.doc = doc;
        }

        @Override
        public synchronized void run() {
            try {
                result = doc.getText(0, doc.getLength());
            } catch (Throwable e) {
                err = e;
                Exceptions.printStackTrace(e);
            }
            done = true;
            notify();
        }
    }

    private class InsertSQLUtility {

        public InsertSQLUtility() {
        }

        public void insert(String s, JEditorPane target)
                throws BadLocationException {
            insert(s, target, false);
        }

        public void insert(String s, JEditorPane target, boolean reformat)
                throws BadLocationException {

            if (s == null) {
                s = "";  // NOI18N
            }

            Document doc = target.getDocument();
            if (doc != null) {
                insert(s, target, doc);
            }
        }

        private int insert(String s, JEditorPane target, Document doc)
                throws BadLocationException {

            int start = -1;
            try {
                Caret caret = target.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                doc.remove(p0, p1 - p0);
                start = caret.getDot();
                doc.insertString(start, s + ";\n", null); // NOI18N
            } catch (BadLocationException ble) {
                LOGGER.log(Level.WARNING, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_InsertAtLocationError") + ble);
            }
            return start;
        }
    }

    private static void adjustColumnPreferredWidths(JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {

            int maxwidth = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer rend =
                        table.getCellRenderer(row, col);
                Object value = table.getValueAt(row, col);
                Component comp =
                        rend.getTableCellRendererComponent(table,
                        value,
                        false,
                        false,
                        row,
                        col);
                maxwidth = Math.max(comp.getPreferredSize().width, maxwidth);
            }
            TableColumn column = columnModel.getColumn(col);
            column.setPreferredWidth(maxwidth);
        }
    }

    private static final class ConnectionUrlRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setToolTipText((String) value);
            return component;
        }
    }

    private TableCellRenderer createDefaultRenderer() {
        DefaultTableCellRenderer label = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (table != null) {
                    JTableHeader header = table.getTableHeader();
                    if (header != null) {
                        setForeground(header.getForeground());
                        setBackground(header.getBackground());
                        setFont(header.getFont());
                    }
                }
                setText((value == null) ? "" : value.toString());
                setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                return SQLHistoryPanel.this;
            }
        };
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }

    private class ColumnListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            HistoryTableModel model = (HistoryTableModel)sqlHistoryTable.getModel();
            TableColumnModel colModel = sqlHistoryTable.getColumnModel();
            int colModelIndex = colModel.getColumnIndexAtX(e.getX());
            int modelIndex = colModel.getColumn(colModelIndex).getModelIndex();
            if (modelIndex < 0) {
                return;
            }
            if (model.sortCol == modelIndex) {
                model.sortAsc = !model.sortAsc;
            } else {
                model.sortCol = modelIndex;
            }
            model.sortData();
            sqlHistoryTable.tableChanged(new TableModelEvent(model));
            sqlHistoryTable.repaint();
        }
    }

    private class SQLComparator implements Comparator<SQLHistory> {

        protected int sortCol;
        protected boolean sortAsc;

        public SQLComparator(int sortCol, boolean sortAsc) {
            this.sortCol = sortCol;
            this.sortAsc = sortAsc;
        }

        @Override
        public int compare(SQLHistory sql1, SQLHistory sql2) {
            int result = 0;
            if (!(sql1 instanceof SQLHistory) || !(sql2 instanceof SQLHistory)) {
                return result;
            }
            SQLHistory sqlHistory1 = sql1;
            SQLHistory sqlHistory2 = sql2;

            switch (sortCol) {
                case 0: // SQL
                    String s1 = sqlHistory1.getSql().trim().toLowerCase();
                    String s2 = sqlHistory2.getSql().trim().toLowerCase();
                    result = s1.compareTo(s2);
                    break;
                case 1: // Date
                    Date d1 = sqlHistory1.getDate();
                    Date d2 = sqlHistory2.getDate();
                    result = d1.compareTo(d2);
                    break;
            }
            if (!sortAsc) {
                result = -result;
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SQLComparator) {
                SQLComparator compObj = (SQLComparator) obj;
                return (compObj.sortCol == sortCol) && (compObj.sortAsc == sortAsc);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + this.sortCol;
            hash = 17 * hash + (this.sortAsc ? 1 : 0);
            return hash;
        }
    }
}
