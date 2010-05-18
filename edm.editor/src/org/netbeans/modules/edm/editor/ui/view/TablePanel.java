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
package org.netbeans.modules.edm.editor.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;


import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.graph.components.MashupTopPanel;
import org.netbeans.modules.edm.model.MashupCollaborationModel;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.openide.util.NbBundle;

/**
 * This class is used to define columns for input and output table
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TablePanel extends JPanel {

    class ActionAdapter implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String actionCmd = e.getActionCommand();
            if (actionCmd.equals("Add")) {
                ((TableColumnModel) tbl.getModel()).addEmptyRow();
            } else if (actionCmd.equals("Remove")) {
                int[] rows = tbl.getSelectedRows();
                TableColumnModel tModel = (TableColumnModel) tbl.getModel();

                ArrayList columns = new ArrayList();
                for (int i = 0; i < rows.length; i++) {
                    if (tModel.isCellEditable(rows[i], 0)) {
                        columns.add(tModel.getColumns().get(rows[i]));
                    } else {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(TablePanel.class, "MSG_Remove_of_Flat_File"),
                                NotifyDescriptor.WARNING_MESSAGE));
                    }
                }

                // now delete columns
                for (int i = 0; i < columns.size(); i++) {
                    ((TableColumnModel) tbl.getModel()).removeRow((ColumnWrapper) columns.get(i));
                }
            }
        }
    }

    class ColumnWrapper {
        private SQLDBColumn column;
        private String columnOldName;
        private boolean isNew = false;

        ColumnWrapper(SQLDBColumn column) {
            this.column = column;
            this.columnOldName = column.getName();
        }

        public SQLDBColumn getColumn() {
            return column;
        }

        public String getColumnOldName() {
            return this.columnOldName;
        }

        public String getDefaultValue() {
            return column.getDefaultValue();
        }

        public int getJdbcType() {
            return column.getJdbcType();
        }

        public String getJdbcTypeString() {
            return column.getJdbcTypeString();
        }

        public String getName() {
            return column.getName();
        }

        public int getPrecision() {
            return column.getPrecision();
        }

        public int getScale() {
            return column.getScale();
        }

        public boolean isNew() {
            return isNew;
        }

        public void setDefaultValue(String defaultVal) {
            column.setDefaultValue(defaultVal);
        }

        public void setJdbcType(int newType) {
            column.setJdbcType(newType);
        }

        public void setName(String name) {
            column.setName(name);
            column.setDisplayName(name);
        }

        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }

        public void setPrecision(int precision) {
            column.setPrecision(precision);
        }

        public void setScale(int scale) {
            column.setScale(scale);
        }
    }

    class IntegerInputVerifier extends InputVerifier {

        /**
         * Checks whether the JComponent's input is valid. This method should have no side
         * effects. It returns a boolean indicating the status of the argument's input.
         * 
         * @param input the JComponent to verify
         * @return <code>true</code> when valid, <code>false</code> when invalid
         * @see javax.swing.JComponent#setInputVerifier
         * @see javax.swing.JComponent#getInputVerifier
         */
        public boolean verify(JComponent input) {
            JTextField tf = (JTextField) input;
            String text = tf.getText();
            try {
                Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Please_Enter_an_Integer"));
                return false;
            }
            return true;
        }
    }

    class TableColumnModel extends AbstractTableModel {
        // private AbstractDBTable table;
        // local list for table column
        private List columnList = new ArrayList();

        TableColumnModel(SQLDBTable table) {
            // this.table = table;
            Iterator it = table.getColumnList().iterator();
            while (it.hasNext()) {
                SQLDBColumn column = (SQLDBColumn) it.next();
                ColumnWrapper colWrapper = new ColumnWrapper(column);
                columnList.add(colWrapper);
            }

            // add default runtime outputs
            if (table.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                addFlatFileTableRuntimeArgument();
            }

            // if there are no columns then add one empty row
            if (columnList.size() == 0) {
                addEmptyRow();
            }
        }

        public void addEmptyRow() {
            boolean added = false;

            if (table.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                SourceColumn column = SQLModelObjectFactory.getInstance().createSourceColumn(generateUniqueColumnName(), Types.VARCHAR, 0, 0, true);
                ColumnWrapper colWrapper = new ColumnWrapper(column);
                columnList.add(colWrapper);
                added = true;
            }

            if (added) {
                fireTableRowsInserted(columnList.size(), columnList.size());
            }
        }

        /**
         * Returns the number of columns in the model. A <code>JTable</code> uses this
         * method to determine how many columns it should create and display by default.
         * 
         * @return the number of columns in the model
         * @see #getRowCount
         */
        public int getColumnCount() {
            return headers.length;
        }

        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName
         */
        public String getColumnName(int col) {
            return headers[col];
        }

        public List getColumns() {
            return columnList;
        }

        public ColumnWrapper getColumnWrapper(String columnName) {
            Iterator it = columnList.iterator();

            while (it.hasNext()) {
                ColumnWrapper colWrapper = (ColumnWrapper) it.next();
                if (colWrapper.getColumnOldName().equals(columnName)) {
                    return colWrapper;
                }
            }
            return null;
        }

        /**
         * Returns the number of rows in the model. A <code>JTable</code> uses this
         * method to determine how many rows it should display. This method should be
         * quick, as it is called frequently during rendering.
         * 
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        public int getRowCount() {
            return columnList.size();
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         * 
         * @param rowIndex the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            ColumnWrapper colWrapper = (ColumnWrapper) columnList.get(rowIndex);
            Object retValue = null;

            switch (columnIndex) {
                case 0:
                    retValue = colWrapper.getName();
                    break;
                case 1:
                    retValue = colWrapper.getDefaultValue();
                    break;
                case 2:
                    retValue = colWrapper.getJdbcTypeString();
                    break;
                case 3:
                    retValue = new Integer(colWrapper.getPrecision());
                    break;
                case 4:
                    retValue = new Integer(colWrapper.getScale());
                    break;
            }

            return retValue;
        }

        /**
         * Indicates whether given cell is editable.
         * 
         * @param row row number of cell
         * @param col column number of cell
         * @return true if all cells are editable, false otherwise
         */
        public boolean isCellEditable(int row, int col) {
            ColumnWrapper colWrapper = (ColumnWrapper) columnList.get(row);
            SQLDBColumn column = colWrapper.getColumn();

            // we do not allow name to be edited
            if (col == 0) {
                return column.isEditable();
            }
            return true;
        }

        public void removeRow(ColumnWrapper colWrapper) {
            int idx = columnList.indexOf(colWrapper);
            if (idx != -1) {
                columnList.remove(idx);
                fireTableRowsDeleted(idx, idx);
                // removeRow(idx);
            }
        }

        public void removeRow(int row) {
            Iterator it = columnList.iterator();
            int cnt = 0;

            while (it.hasNext()) {
                if (cnt == row) {
                    it.remove();
                    fireTableRowsDeleted(row, row);
                }
                cnt++;
            }

            // if all rows are removed then add one empty one
            if (columnList.size() == 0) {
                addEmptyRow();
            }
        }

        /**
         * Sets the specified cell to the desired value
         * 
         * @param value desired value of the cell
         * @param row row number
         * @param col column number
         */
        public void setValueAt(Object value, int row, int col) {

            ColumnWrapper colWrapper = (ColumnWrapper) columnList.get(row);

            int val = 0;
            boolean set = false;

            switch (col) {
                case 0:
                    String name = (String) value;

                    // if column name exist show message to user
                    if (!name.equals(colWrapper.getName()) && isArgNameExist(name)) {
                        TablePanel.showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Column") + name + NbBundle.getMessage(TablePanel.class, "MSG_already_exist"));
                        return;
                    }
                    // set the new name in column
                    colWrapper.setName((String) value);
                    set = true;
                    break;
                case 1:
                    colWrapper.setDefaultValue((String) value);
                    set = true;
                    break;
                case 2:
                    colWrapper.setJdbcType(SQLUtils.getStdJdbcType((String) value));
                    set = true;
                    break;
                case 3:
                    try {
                        val = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Please_Enter_an_Integer"));
                        return;
                    }

                    colWrapper.setPrecision(val);
                    set = true;
                    break;
                case 4:
                    try {
                        val = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Please_Enter_an_Integer"));
                        return;
                    }

                    colWrapper.setScale(val);
                    set = true;
            }

            if (set) {
                fireTableCellUpdated(row, col);
            }
        }

        private void addFlatFileTableRuntimeArgument() {            

            List sTables = collabModel.getSQLDefinition().getSourceTables();
            createFlatFileRuntimeArgument(sTables);

        }

        private void createFlatFileRuntimeArgument(List tables) {
            Iterator it = tables.iterator();

            while (it.hasNext()) {
                SQLDBTable table1 = (SQLDBTable) it.next();
                String argName = table1.getFlatFileLocationRuntimeInputName();
                if (argName != null && !isArgNameExist(argName)) {
                    SQLDBColumn column = SQLObjectUtil.createRuntimeInputArg(table1, argName);
                    ColumnWrapper colWrapper = new ColumnWrapper(column);

                    // if runtime output does not contain this then add
                    if (!table1.getColumns().containsKey(column.getName())) {
                        columnList.add(colWrapper);

                        fireTableRowsInserted(columnList.size(), columnList.size());
                    }
                }
            }
        }

        private String generateUniqueColumnName() {
            int cnt = 0;
            String cName = "arg_" + cnt;
            while (isArgNameExist(cName)) {
                cName = "arg_" + cnt++;
            }
            return cName;
        }

        private boolean isArgNameExist(String name) {
            Iterator it = columnList.iterator();
            while (it.hasNext()) {
                ColumnWrapper colWrapper = (ColumnWrapper) it.next();
                SQLDBColumn column = colWrapper.getColumn();
                if (name.equals(column.getName())) {
                    return true;
                }
            }

            return false;
        }

    }

    private static void showMessage(String msg) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
    }

    private boolean firstCreation = false;
    private String[] headers = new String[] {"Argument Name","Default Value","SQL Type",
                                                    "Precision/Length", "Scale"};
    private SQLDBTable initialTable;
    private SQLDBTable table;

    private int tableType;
    private JTable tbl;
    private MashupCollaborationModel collabModel;
    
    /** Creates a new instance of TableColumnPanel */
    public TablePanel(int tableType) {
        this.tableType = tableType;
        String tableName = "";        
        collabModel = MashupDataObjectProvider.getProvider()
                                                .getActiveDataObject().getModel();
        if (collabModel == null) {
            return;
        }
        RuntimeDatabaseModel runtimeDbModel = collabModel.getRuntimeDbModel();

        if (tableType == SQLConstants.RUNTIME_INPUT) {
            if (runtimeDbModel != null && runtimeDbModel.getRuntimeInput() != null) {
                initialTable = runtimeDbModel.getRuntimeInput();
                table = SQLModelObjectFactory.getInstance().createRuntimeInput(runtimeDbModel.getRuntimeInput());

            } else {
                table = SQLModelObjectFactory.getInstance().createRuntimeInput();
                initialTable = table;
                firstCreation = true;
            }
            tableName += ":RuntimeInput";
        }
        table.setDisplayName(tableName);
        initGui();
    }
    
    /** Creates a new instance of TableColumnPanel */
    public TablePanel(int tableType, MashupCollaborationModel colmodel) {
        this.tableType = tableType;
        String tableName = "";

        collabModel = colmodel;
        
        if (collabModel == null) {
            return;
        }
        RuntimeDatabaseModel runtimeDbModel = collabModel.getRuntimeDbModel();

        if (tableType == SQLConstants.RUNTIME_INPUT) {
            if (runtimeDbModel != null && runtimeDbModel.getRuntimeInput() != null) {
                initialTable = runtimeDbModel.getRuntimeInput();
                table = SQLModelObjectFactory.getInstance().createRuntimeInput(runtimeDbModel.getRuntimeInput());

            } else {
                table = SQLModelObjectFactory.getInstance().createRuntimeInput();
                initialTable = table;
                firstCreation = true;
            }
            tableName += ":RuntimeInput";
        }

        table.setDisplayName(tableName);
        initGui();
    }

    public void showTablePanel() {
        String title = "";
        if (this.tableType == SQLConstants.RUNTIME_INPUT) {
            title = NbBundle.getMessage(TablePanel.class, "TITLE_Add_Input_Runtime_Arguments");
        } else {
            title = NbBundle.getMessage(TablePanel.class, "TITLE_Add_Output_Runtime_Arguments");
        }

        DialogDescriptor dd = new DialogDescriptor(this, title);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This is the dialog to add Input/Output Runtime arguments");
        dlg.setSize(450, 400);
        dlg.setVisible(true);

        if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
            commitTableEditingValue();

            if (collabModel != null && firstCreation) {
                try {
                    addFirstTimeRuntimeArgs();
                    collabModel.addObject(initialTable);
                } catch (org.netbeans.modules.edm.model.EDMException ex) {
                    showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Can_not_create_Runtime")+ ex.getMessage());
                }
                // update the table already on canvas
            } else {
                try {
                    if (initialTable.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                        checkForDanglingReference();
                    } 
                } catch (org.netbeans.modules.edm.model.EDMException ex) {
                    showMessage(NbBundle.getMessage(TablePanel.class, "MSG_Can_not_update_Runtime") + ex.getMessage());
                }
            }
            collabModel.setDirty(true);
        }
    }

    private void addFirstTimeRuntimeArgs() {
        TableColumnModel tModel = (TableColumnModel) tbl.getModel();
        List columns = tModel.getColumns();
        Iterator it = columns.iterator();
        int colId = 1;

        while (it.hasNext()) {
            ColumnWrapper colWrapper = (ColumnWrapper) it.next();
            SQLDBColumn column = colWrapper.getColumn();
            column.setOrdinalPosition(colId++);
            initialTable.addColumn(column);
        }
    }

    private void addNewRuntimeArgs() {
        ArrayList newColumnList = new ArrayList();

        TableColumnModel tModel = (TableColumnModel) tbl.getModel();
        List columns = tModel.getColumns();
        Iterator it = columns.iterator();
        int colId = 1;

        while (it.hasNext()) {
            ColumnWrapper colWrapper = (ColumnWrapper) it.next();
            SQLDBColumn column = colWrapper.getColumn();
            column.setOrdinalPosition(colId++);
            if (initialTable.getColumn(column.getName()) == null && initialTable.getColumn(colWrapper.getColumnOldName()) == null) {
                newColumnList.add(column);
                initialTable.addColumn(column);
            }
        }
        MashupTopPanel edmView = null;
        try {
            edmView = MashupDataObjectProvider.getProvider().getActiveDataObject().getMashupEditorTopPanel();
        } catch (Exception ex) {
            // ignore
        }
    }

    // This method checks for dangling references of any runtime arguments which are
    // deleted
    private void checkForDanglingReference() throws EDMException {
        List colList = initialTable.getColumnList();
        Iterator it = colList.iterator();
        if (collabModel == null) {
            // TODO log this
            return;
        }
        MashupTopPanel etlView = null;
        try {
            etlView = MashupDataObjectProvider.getProvider().getActiveDataObject().getMashupEditorTopPanel();
        } catch (Exception ex) {
            // ignore
        }

        // maintain a list of columns which were there in initialTable and
        // no nolonger is available in newTable meaning they are deleted and so we need
        // to check if any filter has reference to these deleted columns
        ArrayList deletedColumnList = new ArrayList();

        TableColumnModel tModel = (TableColumnModel) tbl.getModel();

        while (it.hasNext()) {
            SourceColumn column = (SourceColumn) it.next();
            // check if initialTable column also exist in newTable
            // if exist then delete this column from newTable and add the column from
            // initialTable
            ColumnWrapper newColumn = tModel.getColumnWrapper(column.getName());
            if (newColumn != null) {
                // delete column key (if column name has changed then key will be
                // dangling)and add new key
                initialTable.deleteColumn(column.getName());

                // column may be updated so set new value
                column.copyFrom(newColumn.getColumn());

                // add column again which is updated
                initialTable.addColumn(column);

            } else {
                // newTable does not have column so we need to see if it is been
                // referenced elsewhere
                deletedColumnList.add(column);
            }
        }

        // now go throw the list of deleted columns and
        // check if it is referenced elsewhere
        it = deletedColumnList.iterator();
        while (it.hasNext()) {
            SourceColumn column = (SourceColumn) it.next();
            collabModel.removeDanglingColumnReference(column);
            initialTable.deleteColumn(column.getName());
        }

        // add new columns in table gui
        addNewRuntimeArgs();

    }

    // this is to commit the last edited value
    // if user press ok and has focus on a cell which he is
    // editing then this value needs to be commited
    // its a focus problem solution
    private void commitTableEditingValue() {
        Component editor = tbl.getEditorComponent();

        if (editor != null && editor instanceof JTextField) {
            JTextField textEditor = (JTextField) editor;
            String val = textEditor.getText();

            int row = tbl.getEditingRow();
            int col = tbl.getEditingColumn();
            if (row != -1 && col != -1) {
                tbl.getModel().setValueAt(val, row, col);
            }
        }
    }

    private void initGui() {
        // set layout

        this.setLayout(new BorderLayout());
        // create a panel to hold jtable at CENTER of this component
        JPanel tableColumnPanel = new JPanel();
        tableColumnPanel.setLayout(new BorderLayout());
        tableColumnPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(TablePanel.class, "TITLE_Define_Columns")),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        // create a panel to hold add remove button
        JPanel tableButtonPanel = new JPanel();
        FlowLayout fLayout = new FlowLayout();
        fLayout.setAlignment(FlowLayout.LEFT);
        tableButtonPanel.setLayout(fLayout);

        ActionAdapter aAdapter = new ActionAdapter();

        JButton addColumnButton = new JButton(NbBundle.getMessage(TablePanel.class, "LBL_Add"));//(nbBundle1.substring(15));
        addColumnButton.getAccessibleContext().setAccessibleName("Add Button");
        addColumnButton.getAccessibleContext().setAccessibleDescription("Add Button");//(nbBundle1.substring(15));
        addColumnButton.setActionCommand("Add");
        addColumnButton.setMnemonic('A');
        addColumnButton.addActionListener(aAdapter);
        JButton removeColumnButton = new JButton(NbBundle.getMessage(TablePanel.class, "LBL_Remove"));
        removeColumnButton.getAccessibleContext().setAccessibleName("Remove Button");
        removeColumnButton.getAccessibleContext().setAccessibleDescription("Remove Button");
        removeColumnButton.setActionCommand("Remove");
        removeColumnButton.setMnemonic('R');
        removeColumnButton.addActionListener(aAdapter);

        tableButtonPanel.add(addColumnButton);
        tableButtonPanel.add(removeColumnButton);

        if (table.getObjectType() == SQLConstants.RUNTIME_INPUT) {
            // add add remove button panel to column panel
            tableColumnPanel.add(tableButtonPanel, BorderLayout.NORTH);
        }

        // create a table
        tbl = new JTable();
        JScrollPane sPane = new JScrollPane(tbl);
        tbl.setModel(new TableColumnModel(table));
        tbl.getAccessibleContext().setAccessibleDescription("JTable");
        tbl.getAccessibleContext().setAccessibleName("JTable");
        // set up table column editors and renderers
        setColumnRendererAndEditor();

        // set SQL Type column size
        TableColumn column = tbl.getColumnModel().getColumn(2);
        column.setMinWidth(40);
        column.setMaxWidth(100);
        column.setPreferredWidth(90);

        // set precision column size
        column = tbl.getColumnModel().getColumn(3);
        column.setMinWidth(40);
        column.setMaxWidth(100);
        column.setPreferredWidth(60);

        // set scale column size
        column = tbl.getColumnModel().getColumn(4);
        column.setMinWidth(40);
        column.setMaxWidth(100);
        column.setPreferredWidth(40);

        // add table to table panel
        tableColumnPanel.add(sPane, BorderLayout.CENTER);

        // add table panel to CENTER in this component
        this.add(tableColumnPanel, BorderLayout.CENTER);
    }

    private void setColumnRendererAndEditor() {
        JTextField editField = new JTextField();
        TableColumn tblCol;
        DefaultTableCellRenderer renderer;

        // for column name
        tblCol = tbl.getColumnModel().getColumn(0);
        tblCol.setCellEditor(new DefaultCellEditor(editField));

        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(NbBundle.getMessage(TablePanel.class, "TOOLTIP_Click_To_Enter_Column_Name"));
        tblCol.setCellRenderer(renderer);

        // for default column value
        tblCol = tbl.getColumnModel().getColumn(1);
        tblCol.setCellEditor(new DefaultCellEditor(editField));

        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(NbBundle.getMessage(TablePanel.class, "TOOLTIP_Click_To_Enter_Default_Column_Value"));
        tblCol.setCellRenderer(renderer);

        // for sql type column
        tblCol = tbl.getColumnModel().getColumn(2);
        tblCol.setCellEditor(new DefaultCellEditor(new JComboBox(new Vector(SQLUtils.getSupportedLiteralTypes()))));

        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(NbBundle.getMessage(TablePanel.class, "TOOLTIP_Click_To_Select_Sql_Type"));
        tblCol.setCellRenderer(renderer);

        // for precision / length column
        tblCol = tbl.getColumnModel().getColumn(3);
        tblCol.setCellEditor(new DefaultCellEditor(editField));

        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(NbBundle.getMessage(TablePanel.class, "TOOLTIP_Click_To_Enter_Precision/Length"));
        tblCol.setCellRenderer(renderer);

        // for scale column
        tblCol = tbl.getColumnModel().getColumn(4);
        tblCol.setCellEditor(new DefaultCellEditor(editField));

        renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(NbBundle.getMessage(TablePanel.class, "TOOLTIP_Click_To_Enter_Scale"));
        tblCol.setCellRenderer(renderer);
    }


}

