/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * SQLHistoryPanel.java
 *
 * @author jbaker
 */
package org.netbeans.modules.db.sql.execute.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.db.sql.history.SQLHistory;
import org.netbeans.modules.db.sql.history.SQLHistoryModel;
import org.netbeans.modules.db.sql.history.SQLHistoryModelImpl;
import org.netbeans.modules.db.sql.history.SQLHistoryPersistenceManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
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
    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    public static final String SQL_HISTORY_FILE_NAME = "sql_history";  // NOI18N
    public static final String SAVE_STATEMENTS_MAX_LIMIT_ENTERED = "10000"; // NOI18N
    public static final String SAVE_STATEMENTS_CLEARED = ""; // NOI18N  
    public static final int SAVE_STATEMENTS_MAX_LIMIT = 10000; 
    public static final int TABLE_DATA_WIDTH_SQL = 125;
    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPanel.class.getName());
    private static Object[][] data;
    private Object[] comboData;
    private SQLHistoryView view;
    private JEditorPane editorPane;

    /** Creates new form SQLHistoryPanel */
    public SQLHistoryPanel(final JEditorPane editorPane) {
        this.editorPane = editorPane;
        final Task task = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                view = new SQLHistoryView(new SQLHistoryModelImpl());
            }
        });
        task.run();
        initSQLHistoryTableData(view);
        initComponents();
        connectionComboBox.addActionListener((HistoryTableModel) sqlHistoryTable.getModel());
        searchTextField.getDocument().addDocumentListener((HistoryTableModel) sqlHistoryTable.getModel());
        sqlHistoryTable.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SQLTableTitle"));
        sqlHistoryTable.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DateTableTitle"));
        // Initialize data for the Connection combo box  
        this.view.updateUrl();
        // SQL statments save limit
        inputWarningLabel.setVisible(false);
        String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", ""); // NOI18N
        if (savedLimit != null) {
            sqlLimitTextField.setText(savedLimit);
        } else {
            sqlLimitTextField.setText(SAVE_STATEMENTS_MAX_LIMIT_ENTERED); // NOI18N
        }
        // Make sure the save limit is considered
        if (savedLimit.equals(SAVE_STATEMENTS_CLEARED)) {
            savedLimit = SAVE_STATEMENTS_MAX_LIMIT_ENTERED;
        }
        SQLHistoryPersistenceManager.getInstance().updateSQLSaved(Integer.parseInt(savedLimit), Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(SQL_HISTORY_FOLDER));
        // Check SQL statements limit
        verifySQLLimit();
        // Adjust table column width
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                adjustColumnPreferredWidths(sqlHistoryTable);
                sqlHistoryTable.revalidate();
            }
        });
    }

    private void initSQLHistoryTableData(SQLHistoryView localSQLView) {
            // Initialize sql column data          
            List<String> sqlList = view.getSQLList();
            List<String> dateList = view.getDateList();
            data = new Object[sqlList.size()][2];
            int row = 0;
            int maxLength; 
            int length;
            for (String sql : sqlList) {
                length = sql.trim().length();
                maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                data[row][0] = sql.trim().substring(0, maxLength);
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
        connectionComboBox = new javax.swing.JComboBox();
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

        connectionComboBox.setRenderer(new ConnectionRenderer());

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Match")); // NOI18N

        searchTextField.setMinimumSize(new java.awt.Dimension(20, 22));

        insertSQLButton.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Insert")); // NOI18N
        insertSQLButton.setEnabled(false);
        insertSQLButton.setFocusTraversalPolicyProvider(true);
        insertSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSQLButtonActionPerformed(evt);
            }
        });

        sqlHistoryTable.setModel(new HistoryTableModel());
        sqlHistoryTable.setGridColor(java.awt.Color.lightGray);
        sqlHistoryTable.setNextFocusableComponent(sqlLimitTextField);
        sqlHistoryTable.setSelectionBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.selectionBackground"));
        jScrollPane1.setViewportView(sqlHistoryTable);
        sqlHistoryTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_History")); // NOI18N
        sqlHistoryTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_History")); // NOI18N

        sqlLimitLabel.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SqlLimit")); // NOI18N

        sqlLimitTextField.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_InitialLimit")); // NOI18N
        sqlLimitTextField.setFocusTraversalPolicyProvider(true);
        sqlLimitTextField.setMinimumSize(new java.awt.Dimension(18, 22));

        sqlLimitButton.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ApplyButton")); // NOI18N
        sqlLimitButton.setNextFocusableComponent(insertSQLButton);
        sqlLimitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sqlLimitButtonActionPerformed(evt);
            }
        });

        inputWarningLabel.setForeground(java.awt.Color.red);
        inputWarningLabel.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_TextInputWarningLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(connectionComboBox, 0, 208, Short.MAX_VALUE)
                                .add(18, 18, 18)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(searchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(insertSQLButton))
                    .add(layout.createSequentialGroup()
                        .add(sqlLimitLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sqlLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sqlLimitButton)
                        .add(18, 18, 18)
                        .add(inputWarningLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(searchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(connectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(insertSQLButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(sqlLimitLabel)
                            .add(sqlLimitTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(sqlLimitButton)
                            .add(inputWarningLabel))))
                .addContainerGap())
        );

        connectionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ASCN_ConnectionCombo")); // NOI18N
        connectionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_ConnectionCombo")); // NOI18N
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
    int rowSelected = sqlHistoryTable.getSelectedRow();
    try {
        // Make sure to insert the entire SQL, not just what appears in the Table
        List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
        int i = 0;
        String sqlToInsert = ""; // NOI18N
        for (SQLHistory sqlHistory : sqlHistoryList) {
            if (rowSelected == i) {
                sqlToInsert = sqlHistory.getSql().trim();
            }
            // increment for the next row
            i++;
        }
        new InsertSQLUtility().insert(sqlToInsert, editorPane);
    } catch (BadLocationException ex) {
        Exceptions.printStackTrace(ex);
    }

}//GEN-LAST:event_insertSQLButtonActionPerformed

private void sqlLimitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sqlLimitButtonActionPerformed
    verifySQLLimit();
}//GEN-LAST:event_sqlLimitButtonActionPerformed

private void verifySQLLimit() {
    String limit = sqlLimitTextField.getText();
    int iLimit = 0;
    SQLHistoryPersistenceManager sqlPersistanceManager = SQLHistoryPersistenceManager.getInstance();
    inputWarningLabel.setVisible(false);
    if (limit.equals(SAVE_STATEMENTS_CLEARED)) { // NOI18N
        iLimit = SAVE_STATEMENTS_MAX_LIMIT;
        view.setSQLHistoryList(sqlPersistanceManager.updateSQLSaved(iLimit, Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(SQL_HISTORY_FOLDER)));
        ((HistoryTableModel) sqlHistoryTable.getModel()).refreshTable(null);
        NbPreferences.forModule(SQLHistoryPanel.class).put("SQL_STATEMENTS_SAVED_FOR_HISTORY", Integer.toString(iLimit));  // NOI18N               
        sqlLimitTextField.setText(SAVE_STATEMENTS_MAX_LIMIT_ENTERED);
    } else {
        try {
            iLimit = Integer.parseInt(limit);
            String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", SAVE_STATEMENTS_CLEARED); // NOI18N
            if (iLimit < 0 || iLimit > SAVE_STATEMENTS_MAX_LIMIT) {
                sqlLimitButton.setEnabled(true);
                inputWarningLabel.setVisible(true);
                inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_NumberInputWarningLabel"));
                // reset user's input
                if (savedLimit != null) {
                    sqlLimitTextField.setText(savedLimit);
                } else {
                    sqlLimitTextField.setText(SAVE_STATEMENTS_CLEARED); // NOI18N
                    sqlLimitTextField.setText(SAVE_STATEMENTS_MAX_LIMIT_ENTERED); // NOI18N
                }
            } else {
                SQLHistoryPersistenceManager.getInstance().updateSQLSaved(iLimit, Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(SQL_HISTORY_FOLDER));
                ((HistoryTableModel) sqlHistoryTable.getModel()).refreshTable(null);
                NbPreferences.forModule(SQLHistoryPanel.class).put("SQL_STATEMENTS_SAVED_FOR_HISTORY", Integer.toString(iLimit));  // NOI18N               
            }
        } catch (NumberFormatException ne) {
            inputWarningLabel.setVisible(true);
            inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_TextInputWarningLabel"));
            // reset user's input
            String savedLimit = NbPreferences.forModule(SQLHistoryPanel.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", ""); // NOI18N
            if (savedLimit != null) {
                sqlLimitTextField.setText(savedLimit);
            } else {
                sqlLimitTextField.setText(SAVE_STATEMENTS_MAX_LIMIT_ENTERED); // NOI18N
            }
        }
    }
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionComboBox;
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
        List<SQLHistory> sqlHistoryList;
        public static final String MATCH_EMPTY = ""; // NOI18N
        public static final String NO_MATCH = ""; // NOI18N

        public SQLHistoryView(SQLHistoryModel model) {
            this.model = model;
            this.sqlHistoryList = model.getSQLHistoryList();
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
            List<SQLHistory> sqlHistoryListForTooltip =  view.filterSQLHistoryList();
            if (row < sqlHistoryListForTooltip.size()) {
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
                return NO_MATCH;
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
            StringBuffer buffer = new StringBuffer();
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
        
        public List<String> getUrlList() {
            List<String> urlList = new ArrayList<String>();
            for (SQLHistory sqlHistory : sqlHistoryList) {
                String url = sqlHistory.getUrl();
                if (!urlList.contains(url)) {
                    urlList.add(url);
                }
            }
            return urlList;
        }

        public List<String> getSQLList() {
            List<String> sqlList = new ArrayList<String>();

            for (SQLHistory sqlHistory : sqlHistoryList) {
                String sql = sqlHistory.getSql();
                if (!sqlList.contains(sql)) {
                    sqlList.add(sql); 
                }
            }
            return sqlList;
        }

        public List<String> getDateList() {
            List<String> dateList = new ArrayList<String>();
            List<String> sqlList = new ArrayList<String>();

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

        public void updateUrl() {
            // Initialize combo box data
            connectionComboBox.addItem(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem"));
            List<String> urlList = getUrlList();
            for (String url : urlList) {
                Object item = new Object();
                item = url;
                connectionComboBox.addItem(item);
            }
        }

        private List<SQLHistory> filterSQLHistoryList() {
            List<SQLHistory> filteredSqlHistoryList = new ArrayList<SQLHistory>();
            String match = searchTextField.getText();
            String url = (String)connectionComboBox.getSelectedItem();
            // modify list of SQL to reflect a selection from the Connection dropdown or if a match text entered
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (sqlHistory.getUrl().equals(url) || url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ConnectionCombo"))) {
                    if (!match.equals(MATCH_EMPTY)) {
                        if (sqlHistory.getSql().toLowerCase().indexOf(match.toLowerCase()) != -1) {
                            filteredSqlHistoryList.add(sqlHistory);
                        }
                    } else {
                        filteredSqlHistoryList.add(sqlHistory);
                    }
                }
            }
            return filteredSqlHistoryList;
        }
    }

    private final class UrlComboBoxModel implements ComboBoxModel, ActionListener {

        public void setSelectedItem(Object item) {
            connectionComboBox.setSelectedItem(item);
        }

        public Object getSelectedItem() {
            return (String) connectionComboBox.getSelectedItem();
        }

        public int getSize() {
            return comboData.length;
        }

        public Object getElementAt(int index) {
            return comboData[index];
        }

        public void addListDataListener(ListDataListener arg0) {
        }

        public void removeListDataListener(ListDataListener arg0) {
        }

        public void actionPerformed(ActionEvent arg0) {
        }
    }

    private final class HistoryTableModel extends DefaultTableModel implements ActionListener, DocumentListener {
        List<String> sqlList;
        List<String> dateList;
            
        public int getRowCount() {
            if (sqlHistoryTable.getSelectedRow() == -1) {
                insertSQLButton.setEnabled(false);
            } 
            return data.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public Class<?> getColumnClass(int c) {
            Object value = getValueAt(0, c);
            if (value == null) {
                return String.class;
            } else {
                return getValueAt(0, c).getClass();
            }
        }

        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }

        public Object getValueAt(int row, int col) {
            if (sqlHistoryTable.isRowSelected(row)) {
                insertSQLButton.setEnabled(true);
            } 
            return data[row][col];
        }

        public void setValueAt(Object value, int row, int col) {
            adjustColumnPreferredWidths(sqlHistoryTable);
            fireTableCellUpdated(row, col);
        }

        public void addTableModelListener(TableModelListener arg0) {
            // not used
        }

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
            }
        }

        public void actionPerformed(ActionEvent evt) {
            searchTextField.setText(""); // NOI18N
            refreshTable(evt);
        }
        
        public void refreshTable(ActionEvent evt) {
            String url;
            List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
            if (evt != null) {
                url = ((javax.swing.JComboBox) evt.getSource()).getSelectedItem().toString();
                sqlHistoryList = view.getSQLHistoryList();
            } else {
                url = connectionComboBox.getSelectedItem().toString();
                FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(SQL_HISTORY_FOLDER);
                String historyFilePath = FileUtil.getFileDisplayName(root) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N
                try {
                    sqlHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve(historyFilePath, root);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            sqlList = new ArrayList<String>();
            dateList = new ArrayList<String>();
            connectionComboBox.setToolTipText(url);
            int i = 0;
            int length;
            int maxLength;
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem"))) {
                    length = sqlHistory.getSql().trim().length();
                    maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                    sqlList.add(sqlHistory.getSql().trim().substring(0, maxLength));
                    dateList.add(DateFormat.getInstance().format(sqlHistory.getDate()));
                } else if (url.equals(sqlHistory.getUrl())) {
                    length = sqlHistory.getSql().trim().length();
                    maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                    sqlList.add(sqlHistory.getSql().trim().substring(0, maxLength));
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
                data[row][0] = sql.trim().substring(0, maxLength);
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

        public void insertUpdate(DocumentEvent evt) {
            // Read the contents
            try {
                String matchText = read(evt.getDocument());
                Object[][] localData = new Object[sqlList.size()][2];
                int row = 0;
                int length;
                int maxLength;
                Iterator dateIterator = dateList.iterator();
                for (String sql : sqlList) {
                    if (sql.trim().toLowerCase().indexOf(matchText.toLowerCase()) != -1) {
                        length = sql.trim().length();
                        maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                        localData[row][0] = sql.trim().substring(0, maxLength);
                        localData[row][1] = dateIterator.next();
                        row++;
                    } 
                }

                // Adjust size of data for the table
                if (row > 0) {
                    data = new Object[row][2];
                    for (int i = 0; i < row; i++) {
                        data[i][0] = localData[i][0];
                        data[i][1] = localData[i][1];
                    }
                } else {
                    data = new Object[0][0];
                    insertSQLButton.setEnabled(false);
                }
                // Refresh the table
                sqlHistoryTable.revalidate();
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void removeUpdate(DocumentEvent evt) {
             // Read the contents
            try {
                String matchText = read(evt.getDocument());
                Object[][] localData = new Object[sqlList.size()][2];
                int row = 0;
                int length;
                int maxLength;                                
                Iterator dateIterator = dateList.iterator();
                for (String sql : sqlList) {
                    if (sql.trim().toLowerCase().indexOf(matchText.toLowerCase()) != -1) {
                        length = sql.trim().length();
                        maxLength = length > TABLE_DATA_WIDTH_SQL ? TABLE_DATA_WIDTH_SQL : length;
                        localData[row][0] = sql.trim().substring(0, maxLength);
                        localData[row][1] = dateIterator.next();
                        row++;
                    }
                }
                // no matches so clean the table
                if (row == 0) {
                    cleanTable();
                }
                // Adjust size of data for the table
                if (row > 0) {
                    data = new Object[row][2];
                    for (int i = 0; i < row; i++) {
                        data[i][0] = localData[i][0];
                        data[i][1] = localData[i][1];
                    }                    
                } else {
                    data = new Object[0][0];                                        
                    insertSQLButton.setEnabled(false);
                }
                // Refresh the table
                sqlHistoryTable.revalidate();
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void changedUpdate(DocumentEvent arg0) {
            // unused
        }

        private void cleanTable() {
            List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
            data = null;                         
            data = new Object[sqlHistoryList.size()][2];
            sqlHistoryTable.repaint();
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
            if (doc == null) {
                return;
            }

            if (doc instanceof BaseDocument) {
                ((BaseDocument) doc).atomicLock();
            }

            int start = insert(s, target, doc);
//            // format the inserted text
//            if (reformat && start >= 0 && doc instanceof BaseDocument) {  
//                int end = start + s.length();
//                Formatter f = ((BaseDocument) doc).getFormatter();
//                f.reformat((BaseDocument) doc, start, end);
//            }

            if (doc instanceof BaseDocument) {
                ((BaseDocument) doc).atomicUnlock();
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
                doc.insertString(start, s + ";", null); // NOI18N
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

    private static final class ConnectionRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setToolTipText((String) value);
            return component;
        }
    }

}
