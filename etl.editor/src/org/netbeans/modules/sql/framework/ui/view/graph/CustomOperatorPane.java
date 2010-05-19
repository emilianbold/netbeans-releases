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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetColumn;

import com.sun.etl.jdbc.SQLUtils;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Configures type, precision and scale (as appropriate) of a cast as operator.
 *
 * @author Wei
 * @version $Revision$
 */
public class CustomOperatorPane extends JPanel {

    private static String[] headers = new String[]{"Argument Name", "SQL Type"};
    private List args;
    private JTextField nameField = new JTextField(20);
    private TableColumnModel tableModel = null;
    private static transient final Logger mLogger = Logger.getLogger(CustomOperatorPane.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public CustomOperatorPane() {
        initArguments();
        this.tableModel = new TableColumnModel(this.args);
        this.initComponents();
    }

    public CustomOperatorPane(List args) {
        this.args = args;
        if (args.isEmpty()) {
            initArguments();
        }
        this.tableModel = new TableColumnModel(this.args);
        this.initComponents();
    }

    /**
     * Overloaded constructor which takes input args and return type and
     * generates the custom operator wizard
     * 
     * @param inputArgs
     * @param retType
     */
    public CustomOperatorPane(List inputArgs, SQLOperatorArg retType) {
        this.args = new ArrayList();
        this.setArgList(inputArgs);
        this.setReturnType(retType);
        this.tableModel = new TableColumnModel(this.args);
        this.initComponents();
    }

    /**
     * Returns a list of arguments
     * 
     * @return List
     */
    public List getArgList() {
        ArrayList inputArgs = new ArrayList();
        List list = this.tableModel.getColumns();
        int num = list.size() - 1;
        for (int i = 0; i < num; i++) {
            ColumnWrapper wrapper = (ColumnWrapper) list.get(i);
            SQLOperatorArg arg = new SQLOperatorArg(wrapper.getName(), wrapper.getJdbcType());
            inputArgs.add(arg);
        }
        return inputArgs;
    }

    /**
     * setter for argument list does the transformation of SQLOperatorArg to
     * ColumnWrapper before populating the list of args.
     * 
     * @param args
     */
    public void setArgList(List args) {
        this.args.clear();
        Iterator iter = args.iterator();
        TargetColumn inputArg = null;
        while (iter.hasNext()) {
            SQLOperatorArg arg = (SQLOperatorArg) iter.next();
            inputArg = SQLModelObjectFactory.getInstance().createTargetColumn(arg.getArgName(),
                    arg.getJdbcType(), 0, 0, true);
            this.args.add(new ColumnWrapper(inputArg));
        }
    }

    /**
     * Returns the SourceColumn as return type
     */
    public SQLOperatorArg getReturnType() {
        int size = this.tableModel.getColumns().size();
        ColumnWrapper wrapper = (ColumnWrapper) this.tableModel.getColumns().get(size - 1);
        return new SQLOperatorArg(wrapper.getName(), wrapper.getJdbcType());
    }

    /**
     * sets the return type used for custom functions
     * @param retType
     */
    public void setReturnType(SQLOperatorArg retType) {
        SourceColumn retArg = SQLModelObjectFactory.getInstance().createSourceColumn(retType.getArgName(), retType.getJdbcType(), 0, 0, true);
        this.args.add(new ColumnWrapper(retArg));
    }

    public String getFunctionName() {
        String name = this.nameField.getText().trim();
        return (name.length() == 0) ? "userFx" : name;
    }

    /**
     * populates the name value in the textfield
     * @param name
     */
    public void setFunctionName(String name) {
        this.nameField.setText(name);
    }

    private void initArguments() {
        TargetColumn arg1 = SQLModelObjectFactory.getInstance().createTargetColumn("arg1", Types.NUMERIC, 0, 0, true);
        SourceColumn retArg = SQLModelObjectFactory.getInstance().createSourceColumn("return", Types.NUMERIC, 0, 0, true);
        this.args.add(new ColumnWrapper(arg1));
        this.args.add(new ColumnWrapper(retArg));
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        String nbBundle1 = mLoc.t("BUND413: Function Name");
        JLabel nameLabel = new JLabel(nbBundle1.substring(15)); //NOI18N
        nameLabel.getAccessibleContext().setAccessibleName(nbBundle1.substring(15));
        this.add(nameLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        nameField.setText("userFx");
        this.add(nameField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        String nbBundle4 = mLoc.t("BUND151: Add");
        JButton addButton = new JButton(nbBundle4.substring(15));
        addButton.getAccessibleContext().setAccessibleName(nbBundle4.substring(15));
        this.add(addButton,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));

        String nbBundle2 = mLoc.t("BUND152: Remove");
        JButton removeButton = new JButton(nbBundle2.substring(15));
        removeButton.getAccessibleContext().setAccessibleName(nbBundle2.substring(15));
        this.add(removeButton,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        String nbBundle3 = mLoc.t("BUND416: Arguments");
        JLabel argLabel = new JLabel(nbBundle3.substring(15));
        argLabel.getAccessibleContext().setAccessibleName(nbBundle3.substring(15));

        this.add(argLabel,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        final JTable table = new JTable();
        table.setModel(this.tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumn typeColumn = table.getColumnModel().getColumn(1);
        typeColumn.setCellEditor(new DefaultCellEditor(new JComboBox(new Vector(SQLUtils.getSupportedLiteralTypes()))));
        this.add(new JScrollPane(table),
                new GridBagConstraints(0, 3, 2, 2, 0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));



        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                tableModel.addEmptyRow();
            }
        });

        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = table.getSelectedRow();
                if (index >= 0) {
                    String argName = (String) tableModel.getValueAt(index, 0);
                    tableModel.removeRow(index);
                }
            }
        });
    }

    private class TableColumnModel extends AbstractTableModel {
        // local list for table column
        private List columnList = new ArrayList();

        TableColumnModel(List columnList) {
            if (columnList != null) {
                this.columnList.addAll(columnList);
            }
        }

        public void addEmptyRow() {
            String argName = "arg" + columnList.size();
            TargetColumn arg1 = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.NUMERIC, 0, 0, true);
            this.columnList.add(columnList.size() - 1, new ColumnWrapper(arg1));
            this.fireTableDataChanged();
        }

        public void removeRow(int row) {
            if (columnList.size() <= 2) {
                return;
            }
            if (row == columnList.size() - 1) {
                return;
            }
            columnList.remove(row);
            this.fireTableRowsDeleted(row, row);
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
                    retValue = colWrapper.getJdbcTypeString();
                    break;
                case 2:
                    retValue = new Integer(colWrapper.getPrecision());
                    break;
                case 3:
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
            return col != 0;
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
                case 1:
                    colWrapper.setJdbcType(com.sun.etl.jdbc.SQLUtils.getStdJdbcType((String) value));
                    set = true;
                    break;
                case 2:
                    try {
                        val = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    colWrapper.setPrecision(val);
                    set = true;
                    break;
                case 3:
                    try {
                        val = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    colWrapper.setScale(val);
                    set = true;
            }

            if (set) {
                fireTableCellUpdated(row, col);
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
}
