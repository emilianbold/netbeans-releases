package org.netbeans.modules.tbls.editor.table;

public class ReadOnlyNoExpressionDefaultMoveableRowTableModel extends NoExpressionDefaultMoveableRowTableModel {

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
