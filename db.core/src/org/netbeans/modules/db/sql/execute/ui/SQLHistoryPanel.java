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
 * SQLHistoryDlg2.java
 *
 * Created on Jun 5, 2008, 4:55:52 PM
 */
package org.netbeans.modules.db.sql.execute.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
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
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author John Baker
 */
public class SQLHistoryPanel extends javax.swing.JPanel {

    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPanel.class.getName());
    private Object[][] data;
    private Object[] comboData;
    private SQLHistoryView view;
    private JEditorPane editorPane;

    /** Creates new form SQLHistoryDlg2 */
    public SQLHistoryPanel(JEditorPane editorPane) {
        this.editorPane = editorPane;
        this.view = new SQLHistoryView(new SQLHistoryModelImpl());
        initSQLHistoryTableData(view);
        initComponents();
        connectionComboBox.addActionListener((HistoryTableModel) sqlHistoryTable.getModel());
        searchTextField.getDocument().addDocumentListener((HistoryTableModel) sqlHistoryTable.getModel());
        sqlHistoryTable.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SQLTableTitle"));
        sqlHistoryTable.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DateTableTitle"));
        // Initialize data for the Connection combo box  
        this.view.updateUrl();
    }

    private void initSQLHistoryTableData(SQLHistoryView localSQLView) {
        List<String> sqlList = localSQLView.getSQLList();
        List<String> dateList = localSQLView.getDateList();

        // Initialize sql column data
        data = new Object[sqlList.size()][2];
        int row = 0;
        for (String sql : sqlList) {
            data[row++][0] = sql.trim().substring(0, 20);
        }             
        // Initialize date column data
        row = 0;
        for (String date : dateList) {
            data[row++][1] = date;
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
        sqlHistoryTable = new javax.swing.JTable();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Connection")); // NOI18N

        connectionComboBox.setActionCommand(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel.connectionComboBox.actionCommand")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Match")); // NOI18N

        searchTextField.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel.searchTextField.text")); // NOI18N
        searchTextField.setMinimumSize(new java.awt.Dimension(20, 22));

        insertSQLButton.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Insert")); // NOI18N
        insertSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSQLButtonActionPerformed(evt);
            }
        });

        sqlHistoryTable.setModel(new HistoryTableModel());
        sqlHistoryTable.setGridColor(java.awt.Color.lightGray);
        jScrollPane1.setViewportView(sqlHistoryTable);
        sqlHistoryTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel.sqlHistoryTable.columnModel.title0")); // NOI18N
        sqlHistoryTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel.sqlHistoryTable.columnModel.title1")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(connectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(searchTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                    .add(insertSQLButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(connectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(searchTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(insertSQLButton)
                .addContainerGap())
        );

        insertSQLButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Search")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_History")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_History")); // NOI18N
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
        }
        new InsertSQLUtility().insert(sqlToInsert, editorPane);
    } catch (BadLocationException ex) {
        Exceptions.printStackTrace(ex);
    }

}//GEN-LAST:event_insertSQLButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionComboBox;
    private javax.swing.JButton insertSQLButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JTable sqlHistoryTable;
    // End of variables declaration//GEN-END:variables
    private class SQLHistoryView {

        SQLHistoryModel model;
        List<SQLHistory> sqlHistoryList;

        public SQLHistoryView(SQLHistoryModel model) {
            this.model = model;
            this.sqlHistoryList = model.getSQLHistoryList();
        }

        public List<SQLHistory> getSQLHistoryList() {
            return sqlHistoryList;
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
                sqlList.add(sqlHistory.getSql());  // NOI18N
            }
            return sqlList;
        }

        public List<String> getDateList() {
            List<String> dateList = new ArrayList<String>();

            for (SQLHistory sqlHistory : sqlHistoryList) {
                dateList.add(DateFormat.getInstance().format(sqlHistory.getDate()));  // NOI18N
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

        public void setFilter() {
            // unused
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
            addListDataListener((HistoryTableModel) sqlHistoryTable.getModel());
        }

        public void removeListDataListener(ListDataListener arg0) {
            removeListDataListener((HistoryTableModel) sqlHistoryTable.getModel());
        }

        public void actionPerformed(ActionEvent arg0) {
        }
    }

    private final class HistoryTableModel extends DefaultTableModel implements ListDataListener, ActionListener, DocumentListener {

        public HistoryTableModel() {
            List<String> sqlList = view.getSQLList();
            List<String> dateList = view.getDateList();
            int row = 0;
            for (String sql : sqlList) {
                data[row++][0] = sql.trim().substring(0, 20);;
            }

            // Initialize data
            row = 0;
            for (String date : dateList) {
                data[row++][1] = date;
            }
        }

        public int getRowCount() {
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
            return data[row][col];
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
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

        public void intervalAdded(ListDataEvent evt) {
            fireChange(evt);
        }

        public void intervalRemoved(ListDataEvent evt) {
            fireChange(evt);
        }

        public void contentsChanged(ListDataEvent evt) {
            fireChange(evt);
        }

        private void fireChange(ListDataEvent evt) {
            String url = (String) ((UrlComboBoxModel) evt.getSource()).getSelectedItem();

            // if the selected item is not All Connections then make sure to only show the corresponding SQL
            if (!url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem"))) {
                List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
                data = null;
                int i = 0;
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    if (url.equals(sqlHistory.getUrl())) {
                        data[i][0] = sqlHistory.getSql().trim().substring(0, 20);
                        ;
                        data[i][1] = sqlHistory.getDate();
                    }
                }
            }
        }

        public void actionPerformed(ActionEvent evt) {
            String url = ((javax.swing.JComboBox) evt.getSource()).getSelectedItem().toString();
            List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();

//            // Refresh table if needed
//            if (!data[0][0].equals(null)) {
//                cleanTable();
//            }
            
            // Reload Table 
            int i = 0;
            for (SQLHistory sqlHistory : sqlHistoryList) {
                if (url.equals(sqlHistory.getUrl())) {
                    setValueAt(sqlHistory.getSql().trim(), i, 0);
                    setValueAt(DateFormat.getInstance().format(sqlHistory.getDate()), i, 1);
                    i++;
                } else if (url.equals(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_URLComboBoxAllConnectionsItem"))) {
                    setValueAt(sqlHistory.getSql().trim(), i, 0);
                    setValueAt(DateFormat.getInstance().format(sqlHistory.getDate()), i, 1);
                    i++;
                }
            }
        }

        public void insertUpdate(DocumentEvent evt) {
            // Read the contents
            try {
                String matchText = read(evt.getDocument());
                List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
                // Clear table
                cleanTable();
                // Restore table data                               
                int i = 0;
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    if (sqlHistory.getSql().trim().indexOf(matchText) != -1) {
                        setValueAt(sqlHistory.getSql().trim(), i, 0);
                        setValueAt(DateFormat.getInstance().format(sqlHistory.getDate()), i, 1);
                        i++;
                    } else {
                        cleanTable();
                    }
                }
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            int i = 0;
            String url = connectionComboBox.getSelectedItem().toString();
            List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
            if (evt.getDocument().equals("")) { // NOI18N
                for (SQLHistory sqlHistory : sqlHistoryList) {
                    if (url.equals(sqlHistory.getUrl())) {
                        setValueAt(sqlHistory.getSql().trim(), i, 0);
                        setValueAt(DateFormat.getInstance().format(sqlHistory.getDate()), i, 1);
                        i++;
                    }
                }
            } else {
                try {
                    String matchText = read(evt.getDocument());
                    i = 0;
                    for (SQLHistory sqlHistory : sqlHistoryList) {
                        if (sqlHistory.getSql().trim().contains(matchText)) {
                            setValueAt(sqlHistory.getSql().trim(), i, 0);
                            setValueAt(DateFormat.getInstance().format(sqlHistory.getDate()), i, 1);
                            i++;
                        } else {
                            cleanTable();
                        }
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }

        public void changedUpdate(DocumentEvent arg0) {
            // unused
        }

        private void cleanTable() {
            List<SQLHistory> sqlHistoryList = view.getSQLHistoryList();
            for (int i = 0; i < sqlHistoryList.size(); i++) {
                setValueAt(null, i, 0);
                setValueAt(null, i, 1);
            }
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

//    private static class SQLHistoryTableRenderer extends JLabel implements TableCellRenderer {
//
//        DateFormat formatter;
//
//        public SQLHistoryTableRenderer() {
//            super();
//        }
//
//        public void setValue(Object sqlHistory) {
////            setToolTipText(((SQLHistory)sqlHistory).getSql());
//        }
//
//        public Component getTableCellRendererComponent(JTable table, Object sqlHistory, boolean isSelected, boolean hasFocus, int nRow, int nCol) {
//            setValue(sqlHistory);
//            return this;
//        }
//    }

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
//
//            if (reformat && start >= 0 && doc instanceof BaseDocument) {  // format the inserted text
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
                doc.insertString(start, s, null);
            } catch (BadLocationException ble) {
                LOGGER.log(Level.WARNING, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_InsertAtLocationError") + ble);
            }
            return start;
        }
    }
}
