/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.beans.PropertyEditor;
import javax.swing.SwingUtilities;
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
        if ("OrderNumberTTV".equals (propertyName)) {
            if (!columnModel.isVisible()) return -1;
            int index = columnModel.getCurrentOrderNumber();
            if (index != -1) {
                index = treeTable.getColumnVisibleIndex(this, index);
            }
            //System.err.println("Get order of "+this.getDisplayName()+" => "+index);
            return new Integer(index);
        }
        if ("InvisibleInTreeTableView".equals (propertyName)) 
            return Boolean.valueOf (!columnModel.isVisible ());
        if ("SortingColumnTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSorted ());
        if ("DescendingOrderTTV".equals (propertyName)) 
            return Boolean.valueOf (columnModel.isSortedDescending ());
        return super.getValue (propertyName);
    }
    
    public void setValue (String propertyName, Object newValue) {
        if ("OrderNumberTTV".equals (propertyName)) {
            int index = ((Integer) newValue).intValue();
            //System.err.println("Set order of "+this.getDisplayName()+" <= "+newValue);
            if (index != -1) {
                index = treeTable.getColumnGlobalIndex(this, index);
                columnModel.setCurrentOrderNumber(index);
            }
        } else
        if ("InvisibleInTreeTableView".equals (propertyName)) {
            columnModel.setVisible (
                !((Boolean) newValue).booleanValue ()
            );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    treeTable.updateColumnWidths ();
                }
            });
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

