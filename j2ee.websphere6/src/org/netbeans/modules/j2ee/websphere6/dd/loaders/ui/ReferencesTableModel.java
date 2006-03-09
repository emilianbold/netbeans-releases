/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

// Netbeans
import java.util.List;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.CommonRef;
import org.openide.util.NbBundle;
import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.xml.multiview.*;

public class ReferencesTableModel extends org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.InnerTableModel implements  DDXmiConstants {
    
    private List children;
    private BaseBean parent;
    private String hrefType=WEB_APPLICATION;
    private static final String[] columnNames = {
        NbBundle.getMessage(ReferencesTableModel.class,"TTL_ReferenceType"),
        NbBundle.getMessage(ReferencesTableModel.class,"TTL_ReferenceId"),
        NbBundle.getMessage(ReferencesTableModel.class,"TTL_ReferenceJndiName"),
        NbBundle.getMessage(ReferencesTableModel.class,"TTL_ReferenceHref")
    };
    
    private static final int [] columnWidths= {200, 160, 180};
    
    public ReferencesTableModel(XmlMultiViewDataSynchronizer synchronizer)    {
        super(synchronizer,columnNames,columnWidths);
    }
    
    
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    protected BaseBean getParent() {
        return parent;
    }
    
    protected List getChildren() {
        return children;
    }
    public void setHrefType(String value) {
        hrefType=value;
    }
    public String getHrefType(String value) {
        return hrefType;
    }
    public int getColumnCount() {
        return getColumnNames().length;
    }
    
    public int getRowCount() {
        if (children != null) {
            return (children.size());
        } else {
            return (0);
        }
    }
    
    
    public String getColumnName(int column) {
        return getColumnNames()[column];
    }
    
    public boolean isCellEditable(int row, int column) {
        return (false);
    }
    
    public int getRowWithValue(int column, Object value) {
        for(int row = 0; row < getRowCount(); row++) {
            Object obj = getValueAt(row, column);
            if (obj.equals(value)) {
                return (row);
            }
        }
        
        return (-1);
    }
    
    
    
    public void setData(BaseBean parent,BaseBean[] children) {
        this.parent = parent;
        this.children = new java.util.ArrayList();
        if (children==null) return;
        for(int i=0;i<children.length;i++)
            this.children.add(children[i]);
        fireTableDataChanged();
    }
    
    
    public void setValueAt(Object value, int row, int column) {
        CommonRef ref = (CommonRef)getChildren().get(row);
        
        if (column == 0) ;//ref.setXmiId((String)value);
        else if (column == 1) ref.setXmiId((String)value);
        else if(column==2) ref.setJndiName((String)value);
        else if(column==3) ref.setHref((String)value);
    }
    
    
    public Object getValueAt(int row, int column) {
        CommonRef ref = (CommonRef)getChildren().get(row);
        if (column == 0)      return ref.getType();
        else if (column == 1) return ref.getXmiId();
        else if(column == 2)  return ref.getJndiName();
        else if(column == 3)  return ref.getHref();
        return null;
    }
    
    public BaseBean addRow(Object[] values) {
        //try {
        String type=(String)values[0];
        CommonRef ref;
        if(type.equals(BINDING_REFERENCE_TYPE_RESOURCE)) {
            ref = new ResRefBindingsType(hrefType);            
        } else if(type.equals(BINDING_REFERENCE_TYPE_RESOURCE_ENV)) {
            ref = new ResEnvRefBindingsType(hrefType);
        } else if(type.equals(BINDING_REFERENCE_TYPE_EJB)) {
            ref = new EjbRefBindingsType(hrefType);            
        } else {
            return null;
        }
        ref.setXmiId((String)values[1]);
        ref.setJndiName((String)values[2]);
        ref.setHref((String)values[3]);
        ((EjbBindingsType)getParent()).addReferenceBinding(ref);
        getChildren().add(ref);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        return ref;
        //} //catch (ClassNotFoundException ex) {}
        
        //return null;
    }
    
    public int addRow() {
        return -1;
    }
    
    
    public void editRow(int row, Object[] values) {
        CommonRef ref = (CommonRef)getChildren().get(row);
        ref.setXmiId((String)values[1]);
        ref.setJndiName((String)values[2]);
        ref.setHref((String)values[3]);
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        ((EjbBindingsType)getParent()).removeReferenceBinding((CommonRef)getChildren().get(row));
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
        
    }
}