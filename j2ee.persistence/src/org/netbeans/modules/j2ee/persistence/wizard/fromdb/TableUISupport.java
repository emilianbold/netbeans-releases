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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class TableUISupport {

    private TableUISupport() {
    }

    public static void connectAvailable(JList availableTablesList, TableClosure tableClosure) {
        availableTablesList.setModel(new AvailableTablesModel(tableClosure));

        if (!(availableTablesList.getCellRenderer() instanceof AvailableTableRenderer)) {
            availableTablesList.setCellRenderer(new AvailableTableRenderer());
        }
    }

    public static void connectSelected(JList selectedTablesList, TableClosure tableClosure) {
        selectedTablesList.setModel(new SelectedTablesModel(tableClosure));

        if (!(selectedTablesList.getCellRenderer() instanceof SelectedTableRenderer)) {
            selectedTablesList.setCellRenderer(new SelectedTableRenderer());
        }
    }

    public static Set<Table> getSelectedTables(JList list) {
        Set<Table> result = new HashSet<Table>();

        Object[] selectedValues = list.getSelectedValues();
        for (int i = 0; i < selectedValues.length; i++) {
            result.add((Table)selectedValues[i]);
        }

        return result;
    }

    public static void connectClassNames(JTable table, SelectedTables selectedTables) {
        table.setModel(new TableClassNamesModel(selectedTables));
        setRenderer(table.getColumnModel().getColumn(0));
        setRenderer(table.getColumnModel().getColumn(1));
    }

    private static void setRenderer(TableColumn column) {
        if (!(column.getCellRenderer() instanceof TableClassNameRenderer)) {
            column.setCellRenderer(new TableClassNameRenderer());
        }
    }

    private static final class AvailableTablesModel extends AbstractListModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public AvailableTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        public Object getElementAt(int index) {
            return displayTables.get(index);
        }

        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getAvailableTables());
            Collections.sort(displayTables);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }

    private static final class SelectedTablesModel extends AbstractListModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public SelectedTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        public Object getElementAt(int index) {
            return displayTables.get(index);
        }

        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getSelectedTables());
            Collections.sort(displayTables);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }

        public TableClosure getTableClosure() {
            return tableClosure;
        }
    }

    private static final class AvailableTableRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DisabledReason disabledReason = null;
            Object displayName = null;

            if (value instanceof Table) {
                Table tableItem = (Table)value;
                disabledReason = tableItem.getDisabledReason();
                if (disabledReason!= null) {
                    displayName = NbBundle.getMessage(TableUISupport.class, "LBL_TableNameWithDisabledReason", tableItem.getName(), disabledReason.getDisplayName());
                } else {
                    displayName = tableItem.getName();
                }
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setEnabled(disabledReason == null);
            component.setToolTipText(disabledReason != null ? disabledReason.getDescription() : null);

            return component;
        }

    }

    private static final class SelectedTableRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Table table = null;
            Object displayName = null;
            boolean referenced = false;
            TableClosure tableClosure = null;

            if (value instanceof Table) {
                table = (Table)value;
                displayName = table.getName();

                if (list.getModel() instanceof SelectedTablesModel) {
                    SelectedTablesModel model = (SelectedTablesModel)list.getModel();
                    tableClosure = model.getTableClosure();
                    referenced = tableClosure.getReferencedTables().contains(table);
                }
            } else {
                displayName = value;
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setEnabled(!referenced);
            component.setToolTipText(referenced ? getTableTooltip(table, tableClosure) : null); // NOI18N

            return component;
        }

        private static String getTableTooltip(Table table, TableClosure tableClosure) {
            List<Table> tables = new ArrayList<Table>();
            Set<Table> relatedTables;
            String bundleKey;

            if (table.isJoin()) {
                relatedTables = table.getReferencedTables();
                bundleKey = "LBL_RelatedTableJoin"; // NOI18N
            } else {
                relatedTables = table.getReferencedByTables();
                bundleKey = "LBL_RelatedTableRefBy"; // NOI18N
            }
            for (Iterator<Table> i = relatedTables.iterator(); i.hasNext();) {
                Table refTable = i.next();
                if (tableClosure.getSelectedTables().contains(refTable)) {
                    tables.add(refTable);
                }
            }
            return NbBundle.getMessage(TableUISupport.class, bundleKey, createTableList(tables));
        }

        private static String createTableList(List<Table> tables) {
            assert tables.size() > 0;

            if (tables.size() == 1) {
                return tables.iterator().next().getName();
            }

            Collections.sort(tables);

            String separator = NbBundle.getMessage(TableUISupport.class, "LBL_TableListSep");
            Iterator<Table> i = tables.iterator();
            StringBuilder builder = new StringBuilder(i.next().getName());
            String lastTable = i.next().getName();
            while (i.hasNext()) {
                builder.append(separator);
                builder.append(lastTable);
                lastTable = i.next().getName();
            }
            return NbBundle.getMessage(TableUISupport.class, "LBL_TableList", builder.toString(), lastTable);
        }
    }

    private static final class TableClassNamesModel extends AbstractTableModel {

        private SelectedTables selectedTables;
        private final List<Table> tables;

        public TableClassNamesModel(SelectedTables selectedTables) {
            this.selectedTables = selectedTables;
            this.tables = selectedTables.getTables();
        }

        public Table getTableAt(int rowIndex) {
            return tables.get(rowIndex);
        }

        public boolean isValidClass(Table table) {
            return !selectedTables.hasProblem(table);
        }

        public String getProblemDisplayName(Table table) {
            return selectedTables.getProblemDisplayNameForTable(table);
        }

        public int getRowCount() {
            return tables.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return tables.get(rowIndex).getName();

                case 1:
                    Table table = tables.get(rowIndex);
                    return selectedTables.getClassName(table);

                default:
                    assert false;
            }

            return null;
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex != 1) {
                return;
            }

            Table table = tables.get(rowIndex);
            selectedTables.setClassName(table, (String)value);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            Table table = tables.get(rowIndex);
            return !table.isJoin() && columnIndex == 1;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_DatabaseTable");

                case 1:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_ClassName");

                default:
                    assert false;
            }

            return null;
        }
    }

    private static final class TableClassNameRenderer extends DefaultTableCellRenderer {
        private static Color errorForeground;
        private static Color nonErrorForeground;

        static {
            errorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
            if (errorForeground == null) {
                errorForeground = Color.RED;
            }
            nonErrorForeground = UIManager.getColor("Label.foreground"); // NOI18N
        }

        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean joinTable = false;
            boolean validClass = true;
            String problemDisplayName = null;

            if (jTable.getModel() instanceof TableClassNamesModel) {
                TableClassNamesModel model = (TableClassNamesModel)jTable.getModel();
                Table table = model.getTableAt(row);
                joinTable = table.isJoin();
                if (column == 1) {
                    validClass = model.isValidClass(table);
                    if (!validClass) {
                        problemDisplayName = model.getProblemDisplayName(table);
                    }
                }
            }

            Object realValue = null;
            if (joinTable && column == 1) {
                realValue = NbBundle.getMessage(TableUISupport.class, "LBL_JoinTable");
            } else {
                realValue = value;
            }
            JComponent component = (JComponent)super.getTableCellRendererComponent(jTable, realValue, isSelected, hasFocus, row, column);
            component.setEnabled(!joinTable);
            component.setToolTipText(joinTable ? NbBundle.getMessage(TableUISupport.class, "LBL_JoinTableDescription") : problemDisplayName);
            component.setForeground((validClass) ? nonErrorForeground : errorForeground);
           
            return component;
        }
    }
}
