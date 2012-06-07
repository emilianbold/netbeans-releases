package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen.CommandLineLexer;
import org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen.CommandLineParser;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author Rastislav Komara
 * xxx: Wrong usage of generic types -> heap pollution.
 */
public class VMOptionsTableModel extends AbstractTableModel {
    private List<JavaVMOption<?>> rows = new ArrayList<JavaVMOption<?>>();
    private String[] columns = {"name", "value"}; //NOI18N
    private static final UserPropertyNode USER_PROPERTY_NODE = new UserPropertyNode() {
        @Override
        public void setName(String name) {
        }

        @Override
        public void setValue(OptionValue<Map.Entry<String, String>> value) {
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };

    public int getRowCount() {
        return rows.size() + 1; //we are providing one additional row for user to insert custom -D properties.
    }

    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        if (0 <= column && column < columns.length) {
            return org.openide.util.NbBundle.getMessage(VMOptionsTableModel.class, "VMOptionTableModel." + columns[column] + ".text");  //NOI18N
        }
        throw new IllegalStateException("Column index out of range."); //NOI18N
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowOk(rowIndex)) {
            return rows.get(rowIndex);
        } else if (rowIndex == rows.size()) {
            return USER_PROPERTY_NODE;
        }
        throw new IllegalArgumentException("Row index out of range."); //NOI18N
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        setValue(rowIndex, (OptionValue<Object>) aValue);
    }

    public <T> void setValue(int row, OptionValue<T> value) {
        if (rowOk(row)) {
            JavaVMOption<OptionValue<T>> option = (JavaVMOption<OptionValue<T>>) rows.get(row);
            if (option == null) throw new IllegalStateException("The selected row contains null option  ."); //NOI18N
            option.setValue(value);
            fireTableRowsUpdated(row, row);
        } else if (row == rows.size()) {
            UserPropertyNode upn = new UserPropertyNode();
            upn.setValue((OptionValue<Map.Entry<String, String>>) value);
            rows.add(upn);
            fireTableRowsInserted(rows.size() - 2, rows.size() - 1);
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) return true;
        if (rowIndex == rows.size()) return true;
        if (rowOk(rowIndex)) {
            JavaVMOption<?> option = rows.get(rowIndex);
            return option instanceof UserPropertyNode;
        }
        return false;
    }

    private boolean rowOk(int rowIndex) {
        return 0 <= rowIndex && rowIndex < rows.size();
    }


    public void fill(String str) throws Exception {
        rows.clear();
        final CommandLineLexer lexer = new CommandLineLexer(new ANTLRStringStream(str));
        final CommonTokenStream cts = new CommonTokenStream(lexer);
        final CommandLineParser parser = new CommandLineParser(cts);
        parser.setTreeAdaptor(new VMOptionTreeAdaptor());
        rows.addAll(parser.parse());
        Collections.sort(rows);
        fireTableDataChanged();
    }

    public List<JavaVMOption<?>> getValidOptions() {
        List<JavaVMOption<?>> result = new LinkedList<JavaVMOption<?>>();
        for (JavaVMOption<?> row : rows) {
            final OptionValue<?> value = row.getValue();
            if (value != null && value.isPresent()) {
                result.add(row);
            }
        }
        return result;
    }
}
