package org.netbeans.modules.tbls.editor.table;

import org.netbeans.modules.iep.editor.ps.GUIUtil;

public class ExpressionDefaultMoveableRowTableModel extends DefaultMoveableRowTableModel{

    @Override
    public boolean isCellEditable(int row, int column) {
        //column 3 is size
        if(column == 3) {
            return GUIUtil.isSizeValidForSQLType(row, 2, this);
        }
        
        //column 4 is scale
        if(column == 4) {
            return GUIUtil.isScaleValidForSQLType(row, 2, this);
        }

        return super.isCellEditable(row, column);
    }
    
    @Override
    public Object getValueAt(int row, int column) {
     
        //column 3 is size
        if(column == 3 && !GUIUtil.isSizeValidForSQLType(row, 2, this)) {
            return GUIUtil.getDefaultStringForSize(row, column, this);
        }
        
        //column 4 is scale
        if(column == 4 && !GUIUtil.isScaleValidForSQLType(row, 2, this)) {
            return GUIUtil.getDefaultStringForScale(row, column, this);
        }
        
        return super.getValueAt(row, column);
    }
}
