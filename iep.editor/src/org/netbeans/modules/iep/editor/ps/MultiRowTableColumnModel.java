/**
 * 
 */
package org.netbeans.modules.iep.editor.ps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;


/**
 * @author pVarghese
 *
 */
public class MultiRowTableColumnModel extends DefaultTableColumnModel {
    /**
     * Hold the list of ColumnGroups which define what group each normal
     * column is within, if any.
     */    
    protected ArrayList columnGroups = new ArrayList();
    

    /**
     * Add a new columngroup.
     * @param columnGroup new ColumnGroup
     */    
    public void addColumnGroup(ColumnGroup columnGroup) {
        columnGroups.add(columnGroup);
    }
    
    /**
     * Provides an Iterator to iterate over the
     * ColumnGroup list.
     * @return Iterator over ColumnGroups
     */    
    public Iterator columnGroupIterator() {
        return columnGroups.iterator();
    }
    
    /**
     * Returns a ColumnGroup specified by an index.
     * @param index index of ColumnGroup
     * @return ColumnGroup
     */    
    public ColumnGroup getColumnGroup(int index) {
        if(index >= 0 && index < columnGroups.size()) {
            return (ColumnGroup)columnGroups.get(index);
        }
        return null;
    }
    
    /**
     * Provides and iterator for accessing the ColumnGroups
     * associated with a column.
     * @param col Column
     * @return ColumnGroup iterator
     */    
    public Iterator getColumnGroups(TableColumn col) {
        if (columnGroups.isEmpty()) return null;
        Iterator iter = columnGroups.iterator();
        while (iter.hasNext()) {
            ColumnGroup cGroup = (ColumnGroup)iter.next();
            Vector v_ret = (Vector)cGroup.getColumnGroups(col,new Vector());
            if (v_ret != null) {
                return v_ret.iterator();
            }
        }
        return null;
    }

}
