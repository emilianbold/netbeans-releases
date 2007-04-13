package org.netbeans.modules.swingapp.actiontable;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.swingapp.*;

// End of variables declaration                   

public class ActionTableModel extends AbstractTableModel {
    
    final String[] columnNames = new String[]{
            "Name","Text","Accel",
            "Class","Method",
            "Icon","Task"};
    final Class[] columnClasses = new Class[] {
        String.class, String.class, String.class, 
        String.class, String.class, 
        Icon.class, Boolean.class};
    
    
    private List<ProxyAction> actions;

    public static int ICON_COLUMN = 5;
    public static int TASK_COLUMN = 6;
    public static int METHOD_COLUMN = 4;
    
    public ActionTableModel(List<ProxyAction> actions) {
        super();
        this.actions = actions;
    }
    
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }
    
    public int getColumnCount() {
        return columnClasses.length;
    }
    
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    
    public int getRowCount() {
        return actions.size();
    }
    
    public ProxyAction getAction(int row) {
        return actions.get(row);
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProxyAction act = actions.get(rowIndex);
        if (columnIndex == 0) {
            return act.getId();
        }
        if (columnIndex == 1) {
            return act.getValue(ProxyAction.NAME);
        }
        if (columnIndex == 2) {
            StringBuffer sb = new StringBuffer();
            KeyStroke key = (KeyStroke) act.getValue(ProxyAction.ACCELERATOR_KEY);
            if(key == null) { return null; }
            if ((key.getModifiers()  & InputEvent.META_DOWN_MASK) > 0) { sb.append("Meta-"); }
            if ((key.getModifiers()  & InputEvent.ALT_DOWN_MASK) > 0) { sb.append("Alt-"); }
            if ((key.getModifiers()  & InputEvent.CTRL_DOWN_MASK) > 0) { sb.append("Ctrl-"); }
            if ((key.getModifiers()  & InputEvent.SHIFT_DOWN_MASK) > 0) { sb.append("Shift-"); }
            //sb.append(key.getKeyChar());
            //sb.append(key.getKeyCode());
            sb.append(KeyEvent.getKeyText(key.getKeyCode()));
            //sb.append(":"+key.toString());
            return sb.toString();
        }
        if (columnIndex == 3) {
            return act.getClassname();
        }
        if (columnIndex == METHOD_COLUMN) {
            return act.getId() + "()";
        }
        if (columnIndex == ICON_COLUMN) {
            int iconCount = 0;
            if (act.getValue(ProxyAction.SMALL_ICON) != null) {
                iconCount++;
            }
            return (Icon) act.getValue(ProxyAction.SMALL_ICON);
        }
        if (columnIndex == TASK_COLUMN) {
            return Boolean.valueOf(act.isTaskEnabled());
        }
        if (columnIndex == 7) {
            return "--";
        }
        return "asdf";
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    
    
    // ========== action specific methods =========
    public void updateAction(ProxyAction action) {
        for(int i=0; i<actions.size(); i++) {
            ProxyAction a = actions.get(i);
            if( a == action) {
                fireTableRowsUpdated(i,i);
                break;
            }
        }
    }

}