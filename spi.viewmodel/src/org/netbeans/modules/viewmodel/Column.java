/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.beans.PropertyEditor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author   Jan Jancura
 */
public class Column extends PropertySupport.ReadWrite {
    
    private PropertyEditor propertyEditor;
    private ColumnModel columnModel;
    private TreeTable treeTable;
    
    
    Column (
        ColumnModel columnModel,
        TreeTable treeTable
    ) {
        super (
            columnModel.getID (),
            columnModel.getType () == null ? 
                String.class : 
                columnModel.getType (),
            columnModel.getDisplayName (),
            columnModel.getShortDescription ()
        );
        this.columnModel = columnModel;
        this.treeTable = treeTable;
        setValue (
            "ComparableColumnTTV", 
            Boolean.valueOf (columnModel.isSortable ())
        );
        if (columnModel.getType () == null)
            // Default column!
            setValue (
                "TreeColumnTTV", 
                Boolean.TRUE
            );
        Character mnemonic = columnModel.getDisplayedMnemonic();
        if (mnemonic != null) {
            setValue("ColumnMnemonicCharTTV", mnemonic); // NOI18N
        }
        this.propertyEditor = columnModel.getPropertyEditor ();
    }

    int getColumnWidth () {
        return columnModel.getColumnWidth ();
    }
    
    void setColumnWidth (int width) {
        columnModel.setColumnWidth (width);
    }
    
    int getOrderNumber () {
        Object o = getValue ("OrderNumberTTV");
        if (o == null) return -1;
        return ((Integer) o).intValue ();
    }
    
    boolean isDefault () {
        return columnModel.getType () == null;
    }
    
    public Object getValue () {
        return null;
    }
    
    public void setValue (Object obj) {
    }

    public Object getValue (String propertyName) {
        if ("OrderNumberTTV".equals (propertyName)) 
            if (columnModel.getCurrentOrderNumber () != -1)
                return new Integer (columnModel.getCurrentOrderNumber ());
        if ("InvisibleInTreeTableView".equals (propertyName)) 
            return Boolean.valueOf (!columnModel.isVisible ());
        if ("SortingColumnTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSorted ());
        if ("DescendingOrderTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSortedDescending ());
        return super.getValue (propertyName);
    }
    
    public void setValue (String propertyName, Object newValue) {
        if ("OrderNumberTTV".equals (propertyName)) 
            columnModel.setCurrentOrderNumber (
                ((Integer) newValue).intValue ()
            );
        else
        if ("InvisibleInTreeTableView".equals (propertyName)) {
            columnModel.setVisible (
                !((Boolean) newValue).booleanValue ()
            );
            treeTable.updateColumnWidths ();
        } else
        if ("SortingColumnTTV".equals (propertyName)) 
            columnModel.setSorted (
                ((Boolean) newValue).booleanValue ()
            );
        else
        if ("DescendingOrderTTV".equals (propertyName)) 
            columnModel.setSortedDescending (
                ((Boolean) newValue).booleanValue ()
            );
        else
        super.setValue (propertyName, newValue);
    }

    public PropertyEditor getPropertyEditor () {
        return propertyEditor;
    }
}

