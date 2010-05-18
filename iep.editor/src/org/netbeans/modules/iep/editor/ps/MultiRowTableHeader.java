/**
 * 
 */
package org.netbeans.modules.iep.editor.ps;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author pVarghese
 *
 * This is the object which manages the header of the JTable and
 * also provides functionality for groupable headers.
 *
 */
public class MultiRowTableHeader extends JTableHeader {
    /**
     * Identifies the UI class which draws the header.
     */    
    private static final String uiClassID = "MultiRowTableHeaderUI";
    
    /**
     * Constructs a GroupableTableHeader which is initialized with cm as the
     * column model. If cm is null this method will initialize the table header
     * with a default TableColumnModel.
     * @param model the column model for the table
     */    
    public MultiRowTableHeader(MultiRowTableColumnModel model) {
        super(model);
        setUI(new MultiRowTableHeaderUI());
        setReorderingAllowed(false);
    }
    
    
    /**
     * Sets the margins correctly for all groups within
     * the header.
     */    
    public void setColumnMargin() {
        int columnMargin = getColumnModel().getColumnMargin();
        Iterator iter = ((MultiRowTableColumnModel)columnModel).columnGroupIterator();
        while (iter.hasNext()) {
            ColumnGroup cGroup = (ColumnGroup)iter.next();
            cGroup.setColumnMargin(columnMargin);
        }
    }

}
