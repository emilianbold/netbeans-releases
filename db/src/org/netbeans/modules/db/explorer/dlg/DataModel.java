/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.DefaultCaret;
import java.io.InputStream;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.util.CommandBuffer;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.util.PListReader;
import javax.swing.event.TableModelEvent;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.util.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

/**
* Table data model
* Uses a vector of objects to store the data
*/
public class DataModel extends AbstractTableModel
{
    /** Column data */
    private Vector data;
    
    transient private Vector primaryKeys = new Vector();
    transient private Vector uniqueKeys = new Vector();

    static final long serialVersionUID =4162743695966976536L;
    public DataModel()
    {
        super();
        data = new Vector(1);
    }

    public Vector getData()
    {
        return data;
    }

    public int getColumnCount()
    {
        return ColumnItem.getProperties().size();
    }

    public int getRowCount()
    {
        return data.size();
    }

    public Object getValue(String pname, int row)
    {
        ColumnItem xcol = (ColumnItem)data.elementAt(row);
        return xcol.getProperty(pname);
    }

    public Object getValueAt(int row, int col)
    {
        return getValue((String)ColumnItem.getColumnNames().elementAt(col), row);
    }

    public void setValue(Object val, String pname, int row)
    {
        if( row < getRowCount() ) {
            int srow = row, erow = row;
            ColumnItem xcol = (ColumnItem)data.elementAt(row);
            xcol.setProperty(pname, val);
            if (pname.equals(ColumnItem.PRIMARY_KEY) && val.equals(Boolean.TRUE)) {

                if (xcol.allowsNull()) xcol.setProperty(ColumnItem.NULLABLE, Boolean.FALSE);
                if (!xcol.isIndexed()) xcol.setProperty(ColumnItem.INDEX, Boolean.TRUE);
                if (!xcol.isUnique()) xcol.setProperty(ColumnItem.UNIQUE, Boolean.TRUE);
                /*for (int i=0; i<data.size();i++) {
                    ColumnItem eitem = (ColumnItem)data.elementAt(i);
                    if (i!=row && eitem.isPrimaryKey()) {
                        eitem.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                        if (i<row) srow = i; else erow = i;
                    }
                }*/
                primaryKeys.add(xcol);
            }
            
            if (pname.equals(ColumnItem.PRIMARY_KEY) && val.equals(Boolean.FALSE)) {
                primaryKeys.remove((ColumnItem)data.elementAt(row));
            }

            if (pname.equals(ColumnItem.NULLABLE)) {
                if (val.equals(Boolean.TRUE)) {
                    //xcol.setProperty(ColumnItem.UNIQUE, Boolean.FALSE);
                    //xcol.setProperty(ColumnItem.INDEX, Boolean.FALSE);
                    xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                }
            }

            if (pname.equals(ColumnItem.INDEX)) {
                if (val.equals(Boolean.FALSE)) {
                    if (xcol.isUnique()) xcol.setProperty(ColumnItem.UNIQUE, Boolean.FALSE);
                    if (xcol.isPrimaryKey()) xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                    //xcol.setProperty(ColumnItem.UNIQUE, Boolean.FALSE);
                    //xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                } //else xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
            }

            if (pname.equals(ColumnItem.UNIQUE)) {
                if (val.equals(Boolean.TRUE)) {
                    if (!xcol.isIndexed()) xcol.setProperty(ColumnItem.INDEX, Boolean.TRUE);
                } else {
                    xcol.setProperty(ColumnItem.PRIMARY_KEY, Boolean.FALSE);
                    xcol.setProperty(ColumnItem.INDEX, Boolean.FALSE);
                }
            }

            fireTableRowsUpdated(srow, erow);
        }
    }

    public void setValueAt(Object val, int row, int col)
    {
        if( ( row < getRowCount() ) && ( col < getColumnCount() ) ){
            String pname = (String)ColumnItem.getColumnNames().elementAt(col);
            setValue(val, pname, row);
        }
    }

    public String getColumnName(int col)
    {
        return (String)ColumnItem.getColumnTitles().elementAt(col);
    }

    public Class getColumnClass(int c)
    {
        return getValueAt(0,c).getClass();
    }

    public boolean isCellEditable(int row, int col)
    {
        return true;
    }

    public boolean isTablePrimaryKey()
    {
        return primaryKeys.size()>1;
    }
    
    public Vector getTablePrimaryKeys()
    {
        return primaryKeys;
    }

    public Vector getTableUniqueKeys()
    {
        return uniqueKeys;
    }

    public boolean isTableUniqueKey()
    {
        return uniqueKeys.size()>1;
    }

    /**
    * Add a row to the end of the model.  
    * Notification of the row being added will be generated.
    * @param object Object to add
    */
    public void addRow(Object object)
    {
        data.addElement(object);
        fireTableChanged(new TableModelEvent(this, getRowCount()-1, getRowCount()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    /**
    * Insert a row at <i>row</i> in the model.  
    * Notification of the row being added will be generated.
    * @param row The row index of the row to be inserted
    * @param object Object to add
    * @exception ArrayIndexOutOfBoundsException If the row was invalid.
    */
    public void insertRow(int row, Object object)
    {
        data.insertElementAt(object, row);
        fireTableRowsInserted(row, row);
    }

    /**
    * Remove the row at <i>row</i> from the model.  Notification
    * of the row being removed will be sent to all the listeners.
    * @param row The row index of the row to be removed
    * @exception ArrayIndexOutOfBoundsException If the row was invalid.
    */
    public void removeRow(int row)
    {
        if (row < data.size()) {
            data.removeElementAt(row);
            fireTableRowsDeleted(row, row);
        }
    }
}

/*
 * <<Log>>
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/21/99  Slavek Psenicka array bounds check
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
