/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.db.dataview.output;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import org.netbeans.modules.db.dataview.table.JXTableRowHeader;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.text.CloneableEditorSupport;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Nithya Radhakrishanan
 * @author Ahimanikya Satapathy
 *
 */
class InsertRecordDialog extends javax.swing.JDialog {

    private final DataView dataView;
    InsertRecordTableUI jTable1;
    private JXTableRowHeader rowHeader;

    public InsertRecordDialog(DataView dataView) {
        super(WindowManager.getDefault().getMainWindow(), true);
        this.dataView = dataView;
        jTable1 = new InsertRecordTableUI(dataView) {

            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (rowIndex != -1 && columnIndex != -1) {
                    removeBtn.setEnabled(true);
                }
                AWTEvent awtEvent = EventQueue.getCurrentEvent();
                if (awtEvent instanceof KeyEvent) {
                    KeyEvent keyEvt = (KeyEvent) awtEvent;
                    if (keyEvt.getSource() != this) {
                        return;
                    }
                    if (rowIndex == 0 && columnIndex == 0 && KeyStroke.getKeyStrokeForEvent(keyEvt).equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
                        DefaultTableModel model = (DefaultTableModel) getModel();
                        model.addRow(createNewRow());
                        rowIndex = getRowCount() - 1; //Otherwise the selection switches to the first row
                        editCellAt(rowIndex, 0);
                    } else if (KeyStroke.getKeyStrokeForEvent(keyEvt).equals(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT + KeyEvent.VK_TAB, 0))) {
                        editCellAt(rowIndex, columnIndex);
                    } else {
                        editCellAt(rowIndex, columnIndex);
                    }
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        initComponents();
        addInputFields();
        jTable1.addKeyListener(new TableKeyListener());

        jTable1.getModel().addTableModelListener(new TableListener());

        jSplitPane1.setBottomComponent(null);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                executeBtnActionPerformed(null);
            }
        };

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); // NOI18N
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER"); // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction); // NOI18N
        getRootPane().getActionMap().put("ENTER", enterAction); // NOI18N

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 50) / 2, (screenSize.height - 200) / 2, (screenSize.width - 50) / 2, (screenSize.height - 50) / 2);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextArea1 = new javax.swing.JTextArea();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        btnPanel = new javax.swing.JPanel();
        previewBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        executeBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.title")); // NOI18N
        setBackground(java.awt.Color.white);
        setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        setForeground(java.awt.Color.black);
        setLocationByPlatform(true);
        setModal(true);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(jTextArea1.getFont());
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(jTextArea1, java.awt.BorderLayout.NORTH);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "insertRecodrDialog.jTextArea")); // NOI18N
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "insertRecord.textarea.desc")); // NOI18N

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setLastDividerLocation(250);
        jSplitPane1.setRequestFocusEnabled(false);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)));
        jScrollPane1.setFont(jScrollPane1.getFont());

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setForeground(new java.awt.Color(204, 204, 255));
        jPanel3.setFont(jPanel3.getFont().deriveFont(jPanel3.getFont().getSize()+1f));
        jPanel3.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanel3);

        jSplitPane1.setTopComponent(jScrollPane1);

        jScrollPane2.setFont(jScrollPane2.getFont());

        jEditorPane1.setContentType(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.jEditorPane1.contentType")); // NOI18N
        jEditorPane1.setEditable(false);
        jEditorPane1.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-sql"));
        jEditorPane1.setFont(jEditorPane1.getFont());
        jEditorPane1.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.jEditorPane1.toolTipText")); // NOI18N
        jEditorPane1.setOpaque(false);
        jScrollPane2.setViewportView(jEditorPane1);

        jSplitPane1.setBottomComponent(jScrollPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        btnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 20, 10));
        btnPanel.setFont(btnPanel.getFont());
        btnPanel.setPreferredSize(new java.awt.Dimension(550, 50));
        btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        previewBtn.setFont(previewBtn.getFont());
        previewBtn.setMnemonic('S');
        previewBtn.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N
        previewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewBtnActionPerformed(evt);
            }
        });
        btnPanel.add(previewBtn);
        previewBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N
        previewBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N

        addBtn.setMnemonic('A');
        addBtn.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.addBtn.text_1")); // NOI18N
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.addBtn.toolTipText")); // NOI18N
        addBtn.setMaximumSize(previewBtn.getMaximumSize());
        addBtn.setMinimumSize(previewBtn.getMinimumSize());
        addBtn.setPreferredSize(previewBtn.getPreferredSize());
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });
        btnPanel.add(addBtn);

        removeBtn.setMnemonic('R');
        removeBtn.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.removeBtn.text_1")); // NOI18N
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.removeBtn.toolTipText")); // NOI18N
        removeBtn.setEnabled(false);
        removeBtn.setMaximumSize(previewBtn.getMaximumSize());
        removeBtn.setMinimumSize(previewBtn.getMinimumSize());
        removeBtn.setPreferredSize(previewBtn.getPreferredSize());
        removeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBtnActionPerformed(evt);
            }
        });
        btnPanel.add(removeBtn);

        executeBtn.setFont(executeBtn.getFont());
        executeBtn.setMnemonic('O');
        executeBtn.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N
        executeBtn.setMaximumSize(previewBtn.getMaximumSize());
        executeBtn.setMinimumSize(previewBtn.getMinimumSize());
        executeBtn.setPreferredSize(previewBtn.getPreferredSize());
        executeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeBtnActionPerformed(evt);
            }
        });
        btnPanel.add(executeBtn);
        executeBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N
        executeBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N

        cancelBtn.setFont(cancelBtn.getFont());
        cancelBtn.setMnemonic('C');
        cancelBtn.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N
        cancelBtn.setMaximumSize(previewBtn.getMaximumSize());
        cancelBtn.setMinimumSize(previewBtn.getMinimumSize());
        cancelBtn.setPreferredSize(previewBtn.getPreferredSize());
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        btnPanel.add(cancelBtn);
        cancelBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N
        cancelBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N

        getContentPane().add(btnPanel, java.awt.BorderLayout.SOUTH);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleParent(null);
    }// </editor-fold>//GEN-END:initComponents

private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.addRow(jTable1.createNewRow());
}//GEN-LAST:event_addBtnActionPerformed

private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed
    jTable1.removeRows();
    removeBtn.setEnabled(false);
}//GEN-LAST:event_removeBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    public class TableListener implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            if (SwingUtilities.isEventDispatchThread()) {
                refreshSQL();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        refreshSQL();
                    }
                });
            }
        }
    }

    private void executeBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (jTable1.isEditing()) {
            jTable1.getCellEditor().stopCellEditing();
        }
        // Get out of AWT thread because SQLExecutionHelper does calls to AWT
        // and we need to wait here to show possible exceptions.
        new Thread("Inserting values") {  //NOI18N

            @Override
            public void run() {
                String insertSQL = null;
                SQLStatementGenerator stmtBldr = dataView.getSQLStatementGenerator();
                SQLExecutionHelper execHelper = dataView.getSQLExecutionHelper();
                for (int i = 0; i < jTable1.getRowCount(); i++) {
                    boolean wasException = false;
                    try {
                        Object[] insertedRow = getInsertValues(i);
                        insertSQL = stmtBldr.generateInsertStatement(insertedRow);
                        RequestProcessor.Task task = execHelper.executeInsertRow(insertSQL, insertedRow);
                        task.waitFinished();
                        wasException = dataView.hasExceptions();
                    } catch (DBException ex) {
                        NotifyDescriptor nd = new NotifyDescriptor.Exception(ex);
                        DialogDisplayer.getDefault().notify(nd);
                        wasException = true;
                    }
                    if (wasException) {
                        // remove i already inserted
                        for (int j = 0; j < i; j++) {
                            ((DefaultTableModel) jTable1.getModel()).removeRow(0);
                        }
                        // return without closing
                        return;
                    }
                }
                // close dialog
                dispose();
            }
        }.start();
    }

    private void previewBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand().equalsIgnoreCase(NbBundle.getMessage(InsertRecordDialog.class, "LBL_show_sql"))) {
            jSplitPane1.setDividerLocation(jSplitPane1.getHeight() / 2);
            jSplitPane1.setBottomComponent(jScrollPane2);
            refreshSQL();
            previewBtn.setText(NbBundle.getMessage(InsertRecordDialog.class, "LBL_hide_sql"));
        } else {
            jSplitPane1.setBottomComponent(null);
            previewBtn.setText(NbBundle.getMessage(InsertRecordDialog.class, "LBL_show_sql"));
        }
    }

    public void refreshSQL() {
        try {
            jEditorPane1.setContentType("text/x-sql"); // NOI18N
            String sqlText = "";
            if (jSplitPane1.getBottomComponent() != null) {
                SQLStatementGenerator stmtBldr = dataView.getSQLStatementGenerator();
                for (int i = 0; i < jTable1.getRowCount(); i++) {
                    String sql = stmtBldr.generateRawInsertStatement(getInsertValues(i));
                    sqlText = sqlText + sql + "\n";
                }
                jEditorPane1.setText(sqlText);
            }
        } catch (DBException ex) {
            jEditorPane1.setContentType("text/html"); // NOI18N
            String str = "<html> <body><font color=" + "#FF0000" + ">" + ex.getMessage().replaceAll("\\n", "<br>") + "</font></body></html>";
            jEditorPane1.setText(str);//ex.getMessage());
            return;
        }
    }

    private void addInputFields() {
        List<Object[]> rows = new ArrayList<Object[]>();
        rows.add(jTable1.createNewRow());
        jScrollPane1.setViewportView(jTable1);
        rowHeader = new JXTableRowHeader(jTable1);
        final Component order[] = new Component[]{rowHeader, jTable1};
        FocusTraversalPolicy policy = new FocusTraversalPolicy() {

            List componentList = Arrays.asList(order);

            public Component getFirstComponent(Container focusCycleRoot) {
                return order[0];
            }

            public Component getLastComponent(Container focusCycleRoot) {
                return order[order.length - 1];
            }

            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {              
                if (aComponent instanceof JXTableRowHeader) {
                    int rowIndex = jTable1.getRowCount() - 1;
                    jTable1.editCellAt(rowIndex, 0);
                    jTable1.setRowSelectionInterval(rowIndex, 0);
                }
                return jTable1;
            }

            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                int index = componentList.indexOf(aComponent);
                return order[(index - 1 + order.length) % order.length];
            }

            public Component getDefaultComponent(Container focusCycleRoot) {
                return order[0];
            }
        };
        setFocusTraversalPolicy(policy);
        jScrollPane1.setRowHeaderView(rowHeader);
        jScrollPane1.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader());
        jTable1.createTableModel(rows, rowHeader);
    }

    private Object[] getInsertValues(int row) throws DBException {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        Object[] insertData = new Object[jTable1.getRSColumnCount()];
        for (int i = 0, I = jTable1.getRSColumnCount(); i < I; i++) {
            DBColumn col = jTable1.getDBColumn(i);
            Object val = model.getValueAt(row, i);

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (DataViewUtils.isSQLConstantString(val)) {
                insertData[i] = val;
            } else { // ELSE literals
                insertData[i] = DBReadWriteHelper.validate(val, col);
            }

        }
        return insertData;
    }

    private class TableKeyListener implements KeyListener {

        public TableKeyListener() {
        }

        public void keyTyped(KeyEvent e) {
            processKeyEvents(e);
        }

        public void keyPressed(KeyEvent e) {
            processKeyEvents(e);
        }

        public void keyReleased(KeyEvent e) {
            processKeyEvents(e);
        }
    }

    private void processKeyEvents(KeyEvent e) {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

        if (KeyStroke.getKeyStrokeForEvent(e).equals(copy)) {
            copy();
        } else if (KeyStroke.getKeyStrokeForEvent(e).equals(paste)) {
            paste();
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE) {
            jTable1.removeRows();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            setFocusable(false);
        } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {
            control0Event();
        } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_1) {
            control1Event();
        } else if (KeyStroke.getKeyStrokeForEvent(e).equals(tab)) {
        }
    }
    private Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private void copy() {
        StringBuffer strBuffer = new StringBuffer();
        int numcols = jTable1.getSelectedColumnCount();
        int numrows = jTable1.getSelectedRowCount();
        int[] rowsselected = jTable1.getSelectedRows();
        int[] colsselected = jTable1.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length) &&
                (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] && numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                strBuffer.append(jTable1.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1) {
                    strBuffer.append("\t");
                }
            }
            strBuffer.append("\n");
        }
        StringSelection stringSelection = new StringSelection(strBuffer.toString());
        clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(stringSelection, stringSelection);
    }

    private void paste() {
        String rowstring, value;
        int startRow = (jTable1.getSelectedRows())[0];
        int startCol = (jTable1.getSelectedColumns())[0];
        try {
            String trstring = (String) (clipBoard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            if (jTable1.getSelectedRows().length < st1.countTokens()) {
                int rowCnt = st1.countTokens() - jTable1.getSelectedRows().length;
                for (int cnt = 0; cnt < rowCnt; cnt++) {
                    addBtnActionPerformed(null);
                }
            }
            for (int i = 0; st1.hasMoreTokens(); i++) {
                rowstring = st1.nextToken();
                StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                for (int j = 0; st2.hasMoreTokens(); j++) {
                    value = st2.nextToken();
                    if (startRow + i < jTable1.getRowCount() && startCol + j < jTable1.getColumnCount()) {
                        jTable1.setValueAt(value, startRow + i, startCol + j);
                    }
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(InsertRecordDialog.class.getName()).info("Failed to paste the contents " + ex);
        }
    }

    private void control0Event() {
        int row = jTable1.getSelectedRow();
        int col = jTable1.getSelectedColumn();
        if (row == -1) {
            return;
        }
        jTable1.editCellAt(row, col);
        TableCellEditor editor = jTable1.getCellEditor();
        if (editor != null) {
            DBColumn dbcol = dataView.getDataViewDBTable().getColumn(col);
            if (dbcol.isGenerated() || !dbcol.isNullable()) {
                Toolkit.getDefaultToolkit().beep();
                editor.stopCellEditing();
            } else {
                editor.getTableCellEditorComponent(jTable1, null, jTable1.isRowSelectionAllowed, row, col);
                jTable1.setValueAt(null, row, col);
                editor.stopCellEditing();
            }
            jTable1.setRowSelectionInterval(row, row);
        }
    }

    private void control1Event() {
        int row = jTable1.getSelectedRow();
        int col = jTable1.getSelectedColumn();
        if (row == -1) {
            return;
        }
        jTable1.editCellAt(row, col);
        TableCellEditor editor = jTable1.getCellEditor();
        if (editor != null) {
            DBColumn dbcol = dataView.getDataViewDBTable().getColumn(col);
            Object val = jTable1.getValueAt(row, col);
            if (dbcol.isGenerated() || !dbcol.hasDefault()) {
                Toolkit.getDefaultToolkit().beep();
                editor.stopCellEditing();
            } else if (val != null && val instanceof String && ((String) val).equals("<DEFAULT>")) {
                editor.getTableCellEditorComponent(jTable1, "", jTable1.isRowSelectionAllowed, row, col);
                jTable1.setValueAt(null, row, col);
                editor.stopCellEditing();
            } else {
                editor.getTableCellEditorComponent(jTable1, "<DEFAULT>", jTable1.isRowSelectionAllowed, row, col);
                jTable1.setValueAt("<DEFAULT>", row, col);
                editor.stopCellEditing();
            }
            jTable1.setRowSelectionInterval(row, row);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton executeBtn;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton previewBtn;
    private javax.swing.JButton removeBtn;
    // End of variables declaration//GEN-END:variables
}
