package org.netbeans.modules.iep.editor.ps;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class PartitionPanelTableCellRenderer extends DefaultTableCellRenderer{
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        
        
        boolean editable = table.getModel().isCellEditable(row, column);
        if (value instanceof Boolean) {
            JCheckBox mCB = new JCheckBox();
            mCB.setSelected(((Boolean)value).booleanValue());
            if (hasFocus) {
                mCB.setBorderPainted(true);
                
                    Border border = null;
                    if (isSelected) {
                        border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                    }
                    if (border == null) {
                        border = UIManager.getBorder("Table.focusCellHighlightBorder");
                    }
                    mCB.setBorder(border);

//                if (!isSelected && table.isCellEditable(row, column)) {
//                        Color col;
//                        col = UIManager.getColor("Table.focusCellForeground");
//                        if (col != null) {
//                            mCB.setForeground(col);
//                        }
//                        col = UIManager.getColor("Table.focusCellBackground");
//                        if (col != null) {
//                            mCB.setBackground(col);
//                        }
//                }
                
                
            } else {
                mCB.setBorder(new EmptyBorder(0, 0, 0, 0));
            }

            return mCB;
        }
        
        
        
        
        
        
        super.setEnabled(editable);
        
        return this;
    }
}

