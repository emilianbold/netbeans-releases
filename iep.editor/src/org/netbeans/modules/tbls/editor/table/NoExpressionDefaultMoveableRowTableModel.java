package org.netbeans.modules.tbls.editor.table;

import org.netbeans.modules.iep.editor.ps.GUIUtil;

public class NoExpressionDefaultMoveableRowTableModel extends DefaultMoveableRowTableModel {

    @Override
    public boolean isCellEditable(int row, int column) {
//      column 2 is size
        if(column == 2) {
            return GUIUtil.isSizeValidForSQLType(row, 1, this);
        }
        
        //column 3 is scale
        if(column == 3) {
            return GUIUtil.isScaleValidForSQLType(row, 1, this);
        }

        return super.isCellEditable(row, column);
    }
    
    @Override
    public Object getValueAt(int row, int column) {
     
        //column 2 is size
        if(column == 2 && !GUIUtil.isSizeValidForSQLType(row, 1, this)) {
            return GUIUtil.getDefaultStringForSize(row, column, this);
        }
        
        //column 3 is scale
        if(column == 3 && !GUIUtil.isScaleValidForSQLType(row, 1, this)) {
            return GUIUtil.getDefaultStringForScale(row, column, this);
        }
        
        return super.getValueAt(row, column);
    }
}
