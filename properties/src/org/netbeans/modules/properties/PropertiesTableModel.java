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


package org.netbeans.modules.properties;


import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import javax.swing.JTable;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.datatransfer.*;
import org.openide.actions.InstantiateAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.*;


/** Model for the properties edit table
*
* @author Petr Jiricka
*/
public class PropertiesTableModel extends AbstractTableModel {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** Main dataobject */
    PropertiesDataObject obj;

    /** Listens to changes on the bundle structure */
    private PropertyBundleListener pbl;

    /** Create a data node for a given data object.
    * The provided children object will be used to hold all child nodes.
    * @param obj object to work with
    * @param ch children container for the node
    */
    public PropertiesTableModel (PropertiesDataObject obj) {
        super ();
        this.obj = obj;

        // listener for the BundleStructure
        pbl = new TablePropertyBundleListener();

        obj.getBundleStructure().addPropertyBundleListener(new WeakListenerPropertyBundle(pbl));

        //PENDING move the column corresponding to curNode to the beginning
    }

    class TablePropertyBundleListener implements PropertyBundleListener {
        public void bundleChanged(PropertyBundleEvent evt) {
            // PENDING - should be maybe even finer
            switch (evt.getChangeType()) {
            // structure changed
            case PropertyBundleEvent.CHANGE_STRUCT:
                cancelEditingInTables(getDefaultCancelSelector());
                fireTableStructureChanged(); 
                break;
            // all items changed (keyset)
            case PropertyBundleEvent.CHANGE_ALL:
                cancelEditingInTables(getDefaultCancelSelector());
                // reset all header values as well
                Object list[] = PropertiesTableModel.this.listenerList.getListenerList();
                for (int i = 0; i < list.length; i++) {
                    if (list[i] instanceof JTable) {
                        JTable jt = (JTable)list[i];

                        for (int j=0 ; j < jt.getColumnModel().getColumnCount(); j++) {
                            TableColumn column = jt.getColumnModel().getColumn(j);
                            column.setHeaderValue(jt.getModel().getColumnName(j));
                        }
                    }
                }
                fireTableDataChanged();
                
                break;
            // file changed
            case PropertyBundleEvent.CHANGE_FILE:
                final int index = obj.getBundleStructure().getEntryIndexByFileName(evt.getEntryName());
                if (index == -1) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions"))
                        (new Exception("Changed file not found")).printStackTrace();
                    break;
                }
                cancelEditingInTables(new CancelSelector() {
                                          public boolean doCancelEditing(int row, int column) {
                                              if (!(row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount()))
                                                  return false;
                                              return (column == index + 1);
                                          }
                                      });
                fireTableColumnChanged(index + 1);
                //System.out.println(PropertiesTableModel.this.toString());
                break;
            // one item changed
            case PropertyBundleEvent.CHANGE_ITEM:
                final int index2 = obj.getBundleStructure().getEntryIndexByFileName(evt.getEntryName());
                final int keyIndex = obj.getBundleStructure().getKeyIndexByName(evt.getItemName());
                if (index2 == -1 || keyIndex == -1) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions"))
                        (new Exception("Changed file not found")).printStackTrace();
                    break;
                }
                cancelEditingInTables(new CancelSelector() {
                                          public boolean doCancelEditing(int row, int column) {
                                              if (!(row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount()))
                                                  return false;
                                              return (column == index2 + 1 && row == keyIndex);
                                          }
                                      });
                fireTableCellUpdated(keyIndex, index2 + 1);
                //System.out.println(PropertiesTableModel.this.toString());
                break;
            }
        }
    }  // endof inner class TablePropertyBundleListener

    /** Returns the class for a model. */
    public Class getColumnClass(int columnIndex) {
        return StringPair.class;
    }

    /** Returns the number of rows in the model */
    public int getRowCount() {
        return obj.getBundleStructure().getKeyCount();
    }

    /** Returns the number of columns in the model */
    public int getColumnCount() {
        return obj.getBundleStructure().getEntryCount() + 1;
    }

    /** Returns the value for the given row and column */
    public Object getValueAt(int row, int column) {
        BundleStructure bs = obj.getBundleStructure();
        switch (column) {
        case 0:
            return stringPairForKey(row);//bs.getNthKey(row);
        default:
            Element.ItemElem item;
            try {
                item = bs.getItem(column - 1, row);
            } catch (ArrayIndexOutOfBoundsException aie) {
                item = null;
            }
            return stringPairForValue(item);
            /*
            if (item == null)
              return "";
            else  
              return item.getValue();
            */   
        }
    }

    /* Returns a string pair for a key in an item (may be null). */
    private StringPair stringPairForKey(int row) {
        BundleStructure bs = obj.getBundleStructure();
        Element.ItemElem item = bs.getItem(0, row);
        StringPair sp;
        if (item == null)
            sp = new StringPair("", bs.getNthKey(row), true);
        else
            sp = new StringPair(item.getComment(), bs.getNthKey(row), true);
        if (obj.getBundleStructure().getEntryCount() > 1)
            sp.setCommentEditable(false);
        return sp;
    }

    /* Returns a string pair for a value in an item (may be null). */
    private StringPair stringPairForValue(Element.ItemElem item) {
        if (item == null)
            // item doesnt't exist -> value is null
            return new StringPair(null, null);
        else
            return new StringPair(item.getComment(), item.getValue());
    }

    /** Returns the name for a column */
    public String getColumnName(final int column) {
        String mark;
        if(column == obj.getBundleStructure().getSortIndex()) {
            if(obj.getBundleStructure().getSortOrder())
                // ascending
                mark = " \u25B2 "; // NOI18N
            else
                // descending
                mark = " \u25BC "; // NOI18N
        } else
            mark = " "; // NOI18N
        
        switch (column) {
        case 0:
            return mark+NbBundle.getBundle(PropertiesTableModel.class).getString("LAB_KeyColumnLabel"); // NOI18N
        default:
            if (obj.getBundleStructure().getEntryCount() == 1)
                return mark+NbBundle.getBundle(PropertiesTableModel.class).getString("LBL_ColumnValue"); // NOI18N
            else
                return mark+Util.getPropertiesLabel (obj.getBundleStructure().getNthEntry(column - 1)); // NOI18N
        }
    }

    /** Sets the value at rowIndex and columnIndex */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // PENDING - set comment for all files
        if (columnIndex == 0) {
            BundleStructure bs = obj.getBundleStructure();
            String oldValue = (String)bs.getNthKey(rowIndex);
            if (oldValue == null)
                return;
            String newValue = ((StringPair)aValue).getValue();
            if (newValue == null) { // key can be an empty string
                // remove from all files
                return;
                /*for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {
                  PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
                  if (entry != null) {
                    PropertiesStructure ps = entry.getHandler().getStructure();
                    if (ps != null) {
                      ps.deleteItem(oldValue);
                    }
                  }  
            }*/
            } else {
                // set in all files
                for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {
                    PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            // set the key
                            if (!oldValue.equals(newValue)) {
                                ps.renameItem(oldValue, UtilConvert.escapePropertiesSpecialChars(newValue));
                                // this resorting is necessary only if this column index is same as
                                // column according the sort is performed, REFINE
                                obj.getBundleStructure().sort(-1);
                            }
                            // set the comment
                            if (i == 0) {
                                Element.ItemElem item = ps.getItem(newValue);
                                if (item != null && ((StringPair)aValue).isCommentEditable()) {
                                    // only set if they differ
                                    if (!item.getComment().equals(((StringPair)aValue).getComment()))
                                        item.setComment(((StringPair)aValue).getComment());
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            // property value
            PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(columnIndex - 1);
            String key = obj.getBundleStructure().getNthKey(rowIndex);
            if (entry != null && key != null) {
                PropertiesStructure ps = entry.getHandler().getStructure();
                if (ps != null) {
                    Element.ItemElem item = ps.getItem(key);
                    if (item != null) {
                        item.setValue(UtilConvert.escapeLineContinuationChar(((StringPair)aValue).getValue()));
                        item.setComment(((StringPair)aValue).getComment());
                        // this resorting is necessary only if this column index is same as
                        // column according the sort is performed, REFINE
                        obj.getBundleStructure().sort(-1);
                    }
                    else {
                        if ((((StringPair)aValue).getValue().length() > 0) || (((StringPair)aValue).getComment().length() > 0))  {
                            ps.addItem(key, UtilConvert.escapeLineContinuationChar(((StringPair)aValue).getValue()), ((StringPair)aValue).getComment());
                            // this resorting is necessary only if this column index is same as
                            // column according the sort is performed, REFINE
                            obj.getBundleStructure().sort(-1);
                        }
                    }
                }
            }
        }
    }

    /** Returns true for all cells */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /** Fires a TableModelEvent - change of one column */
    public void fireTableColumnChanged(int index) {
        // reset the header value as well
        Object list[] = listenerList.getListenerList();
        for (int i = 0; i < list.length; i++) {
            if (list[i] instanceof JTable) {
                JTable jt = (JTable)list[i];
                try {
                    TableColumn column = jt.getColumnModel().getColumn(index);
                    column.setHeaderValue(jt.getModel().getColumnName(index));
                } catch (ArrayIndexOutOfBoundsException abe) {
                    // only catch exception
                }
                jt.getTableHeader().repaint();
            }
        }
        fireTableChanged(new TableModelEvent(this, 0, getRowCount() - 1, index));
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("------------------------------ TABLE MODEL DUMP -----------------------\n");
        for (int row = 0; row < getRowCount(); row ++) {
            for (int column = 0; column < getColumnCount(); column ++) {
                StringPair sp = (StringPair)getValueAt(row, column);
                result.append("[" /*+ sp.getComment() + "," */+ sp.getValue() + "]");
                if (column == 0)
                    result.append(" : ");
                else
                    if (column == getColumnCount() - 1)
                        result.append("\n");
                    else
                        result.append(",");
            }
        }
        result.append("---------------------------- END TABLE MODEL DUMP ---------------------\n");
        return result.toString();
    }

    /** Cancels editing in all listening JTables if appropriate */
    private void cancelEditingInTables(CancelSelector can) {
        Object list[] = listenerList.getListenerList();
        for (int i = 0; i < list.length; i++) {
            if (list[i] instanceof JTable) {
                JTable jt = (JTable)list[i];
                if (can.doCancelEditing(jt.getEditingRow(), jt.getEditingColumn())) {
                    TableCellEditor ed = jt.getCellEditor();
                    if (ed != null) {
                        ed.cancelCellEditing();
                        //System.out.println("canceling edit in " + jt);
                    }
                }
            }
        }
    }


    /** Interface which finds out whether editing should be canceled if given cell is edited. */
    private static interface CancelSelector {
        /** Returns whether editing should be canceled for given row and column. */
        public boolean doCancelEditing(int row, int column);
    }

    private CancelSelector getDefaultCancelSelector() {
        return new CancelSelector() {
                   /** Returns whether editing should be canceled for given row and column. */
                   public boolean doCancelEditing(int row, int column) {
                       return (row >= 0 && row < getRowCount() && column >= 0 && column < getColumnCount());
                   }
               };
    }


    /** Object for the value for one cell.
    * Encapsulates up to two values. 
    * It is used to represent either (comment, value) pair of an item, or a key for an item.
    */
    static class StringPair implements Serializable {

        private String comment;
        private String value;
        private boolean keyType;
        private boolean commentEditable;

        static final long serialVersionUID =-463968846283787181L;
        /** Constructs with empty comment and value. */
        public StringPair() {
            this (null, "", false);
        }

        /** Constructs with the given value and no comment. */
        public StringPair(String v) {
            this (null, v, true);
        }

        /** Constructs with the given comment and value. */
        public StringPair(String c, String v) {
            this (c, v, false);
        }

        /** Constructs with the given comment and value. */
        public StringPair(String c, String v, boolean kt) {
            comment = c;
            value   = v;
            keyType = kt;
            commentEditable = true;
        }

        /** Returns the comment associated with this element. */
        public String getComment() {
            return comment;
        }

        /** Returns the value associated with this element. */
        public String getValue() {
            return value;
        }

        public Object clone() {
            String c = (comment == null ? null : new String(comment));
            String v = (value == null ? null : new String(value));
            return new StringPair(c, v);
        }

        public String toString() {
            return value;
        }

        /** Returns the type key/value of the pair. */
        public boolean isKeyType () {
            return keyType;
        }

        /** Returns whether the comment should be allowed to be edited. */
        public boolean isCommentEditable() {
            return commentEditable;
        }

        /** Sets whether the comment should be allowed to be edited. */
        public void setCommentEditable(boolean newEditable) {
            commentEditable = newEditable;
        }

    } // end of inner class
        
}