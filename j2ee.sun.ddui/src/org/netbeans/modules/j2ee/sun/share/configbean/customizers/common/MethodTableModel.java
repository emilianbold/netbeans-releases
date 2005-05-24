/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MethodTableModel.java
 *
 * Created on February 2, 2005, 11:38 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import java.awt.Component;
  
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author Rajeshwar Patil
 */
public abstract class MethodTableModel extends AbstractTableModel {

    protected List ddMethods;
    protected List methods;


    //Map of java.util.Method to BaseBean(i.e. Method)
    private Map methodToDDMethodMap;


    /** Creates a new instance of MethodTableModel */
    public MethodTableModel(List methods, List ddMethods) {
        if (methods != null) {
            this.methods = new ArrayList();
            for(int i = 0;i < methods.size(); i++) {
                    this.methods.add(methods.get(i));
            }
        }

        if (ddMethods != null) {
            this.ddMethods = new ArrayList();
            for(int i = 0;i < ddMethods.size(); i++) {
                    this.ddMethods.add(ddMethods.get(i));
            }
        }
        setMap();
        fireTableDataChanged();
    }


    public void setData(List methods, List ddMethods) {
        if (methods != null) {
            this.methods = new ArrayList();
            for(int i = 0;i < methods.size(); i++) {
                    this.methods.add(methods.get(i));
            }
        }

        if (ddMethods != null) {
            this.ddMethods = new ArrayList();
            for(int i = 0;i < ddMethods.size(); i++) {
                    this.ddMethods.add(ddMethods.get(i));
            }
        }

        //printList(this.methods);
        //printList(this.ddMethods);

        setMap();
        fireTableDataChanged();
    }

    
    public MethodTableModel(List methods) {
        this(methods, null);
    }


    protected MethodTableModel() {
    }


    public int getColumnCount() {
        return getColumnNames().length;
    }

    
    public String getColumnName(int column) {
        return getColumnNames()[column];    
    }


    public Class getColumnClass(int column) {
        switch(column){
            case 0: {
                return String.class;
            }
            case 1: {
                return Boolean.class;
            }
            default: {
                return getColumnType(column);
            }
        }
    }


    public int getRowCount() {
        if (methods != null){
                return methods.size();
        } else {
                return 0;
        }
    }


    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 0) {
            return false;
        } else {
            return true;
        }
    }


    public Object getValueAt(int row, int col) {
        switch(col){
            case 0: {
                //Method Name
                Object method = methods.get(row);
                if(method != null){
                    ///revert later(post Netbeans 4.1); since method is a mockup object and 
                    //does not have method-signature available to display in tooltip
                    //using row number instead
                    ///return getValueAt(col, method);
                    return getValueAt(col, method, row);
                }else{
                    return null;
                }
            }
            case 1: {
                //Enability Flag
                if(methodToDDMethodMap != null){
                    if(methodToDDMethodMap.containsKey(methods.get(row))){
                        return new Boolean("true");                     //NOI18N
                    }
                }
                return new Boolean("false");                            //NOI18N
            } 
            default: {
                   if(methodToDDMethodMap != null){ 
                       Object ddMethod = methodToDDMethodMap.get(methods.get(row));
                       if(ddMethod != null){
                            return getDDValueAt(col, ddMethod);
                       }
                   } else return null;
            }
        }
        return null;
    }


    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        switch(col){
            case 0: {
            }
            break;
            case 1: {
                Object method = methods.get(row);
                Object ddMethod = null;
                if(methodToDDMethodMap != null){
                    ddMethod = methodToDDMethodMap.get(method);
                }
                Boolean enable = (Boolean)value;
                if(enable.toString().equals("true")){                   //NOI18N
                    if(ddMethod == null){
                        enableMethod(method);
                    }
                }else{
                    if(ddMethod != null){
                        disableMethod(method, ddMethod);
                    }
                }
            }
            break;
            default: {
                if(methodToDDMethodMap  != null){
                    Object ddMethod = methodToDDMethodMap.get(methods.get(row));
                    if(ddMethod != null){
                        setDDValueAt(col, ddMethod, value);
                    }
                }
            }
            break;
        }
        fireTableCellUpdated(row, col);
    }


    protected abstract String[] getColumnNames();

    protected abstract Class getColumnType(int column);

    ///revert later(post Netbeans 4.1); since method is a mockup object and 
    //does not have unique method name available to display, using row number instead
    ///protected abstract Object getValueAt(int column, Object method);///revert later
    protected abstract Object getValueAt(int column, Object method, int row);

    protected abstract Object getDDValueAt(int column, Object ddMethod);

    protected abstract void setDDValueAt(int column, Object ddMethod, Object value);

    protected abstract Object getDDMethod(Object method);

    protected abstract void addDDMethod(Object ddMethod);

    protected abstract void removeDDMethod(Object ddMethod);

    protected abstract boolean areEqual(Object ddMethod, Object method);


    protected void setMap(){
        //assert(methods != null);
        methodToDDMethodMap = new Hashtable();
        if(methods == null) return;
        Object method = null;
        if(ddMethods != null){
            for(int i=0; i<ddMethods.size(); i++){
                Object ddMethod = ddMethods.get(i);
                method = getMethod(ddMethod, methods);
                assert(method  != null);
                if(method != null){
                    methodToDDMethodMap.put(method, ddMethod);
                }
            }
        }
    }


    private void enableMethod(Object method){
        Object ddMethod = getDDObject(method);
        if(methodToDDMethodMap == null){
            methodToDDMethodMap = new Hashtable();
        } 
        methodToDDMethodMap.put(method, ddMethod);
        addDDMethod(ddMethod);
        fireTableDataChanged();
    }


    private Object getDDObject(Object method){
        return getDDMethod(method);
    }


    private void disableMethod(Object method, Object ddMethod){
        if(methodToDDMethodMap != null){
            methodToDDMethodMap.remove(method);
            removeDDMethod(ddMethod);
        }
        fireTableDataChanged();
    }


    private Object getMethod(Object ddMethod, List methods){
        for(int i=0; i<methods.size(); i++){
            if(areEqual(ddMethod, methods.get(i))){
                return methods.get(i);
            }
        }
        return null; 
    }


   private void printList(List list){
       if(list != null){
           System.out.println("printList list --" + list);                                              //NOI18N
           System.out.println("printList list toString -- " + list.toString());                         //NOI18N
           int size = list.size();
           for(int i=0; i<size; i++){
               System.out.println("printList item no: i -- " + list.get(i));                            //NOI18N
               System.out.println("printList item no: i toSring() -- " + list.get(i).toString());       //NOI18N
           }
       }
   }
}
