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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ElementOrTypeTableModel.java
 *
 * Created on August 30, 2006, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 *
 * @author radval
 */
public class PartAndElementOrTypeTableModel extends AbstractTableModel {
    
    private List<PartAndElementOrType> mPartAndElementOrTypeList = new ArrayList();
    private Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
    
    /** Creates a new instance of ElementOrTypeTableModel */
    public PartAndElementOrTypeTableModel(Map<String, String> namespaceToPrefixMap) {
        this.namespaceToPrefixMap = namespaceToPrefixMap;
    }
    
    public List<PartAndElementOrTypeTableModel.PartAndElementOrType> getPartAndElementOrType() {
        return this.mPartAndElementOrTypeList;
    }
    
    public String getColumnName(int column) {
        if(column == 0) {
            return NbBundle.getMessage(PartAndElementOrTypeTableModel.class, "PartAndElementOrTypeTableMode.Column1.text"); 
        } else {
            return NbBundle.getMessage(PartAndElementOrTypeTableModel.class, "PartAndElementOrTypeTableMode.Column2.text");
        }
    }
 
    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return mPartAndElementOrTypeList.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        PartAndElementOrType partAndElementOrType = mPartAndElementOrTypeList.get(rowIndex);
        if(columnIndex == 0) {
            return partAndElementOrType.getPartName();
        } else if(columnIndex == 1) {
            return partAndElementOrType.getElementOrType();
        }
        
        return "Missing Value";
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) 
            return;
        PartAndElementOrTypeTableModel.PartAndElementOrType item = this.mPartAndElementOrTypeList.get(rowIndex);
        if(columnIndex == 0) {
            String partName = (String) aValue;
            String oldPartName = (String) this.getValueAt(rowIndex, 0);
            if (oldPartName != null && oldPartName.equals(partName)) return;
            if(isPartNameExists(partName)) {
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message("Part \""+ partName + " is already exist ", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else if(!isValidPartName(partName)) {
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message("Name \"" + partName + "\" is not a valid NCName", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                
            } else  {
            
                item.setPartName(partName);
            }
        } else if(columnIndex == 1) {
            if(aValue instanceof ElementOrType) {
                ElementOrType eorT = (ElementOrType) aValue;
                item.setElementOrType(eorT);
            }
            
            //TODO: if user types strings make sure it is valid type 
            //and convert it to ElementOrType and set it 
        }
        fireTableCellUpdated(rowIndex, columnIndex);
        
    }
    
     public boolean isCellEditable(int row, int col) {
            return true;
     }

     /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }   
     
    public void removeSelectedRow(int row) {
        int size = this.mPartAndElementOrTypeList.size();
        if(row >= size) {
            throw new IllegalArgumentException("can not delete row "+ row+1 +"total rows are"+ size);
        }
        
        this.mPartAndElementOrTypeList.remove(row);
        super.fireTableRowsDeleted(row, row);
    }
    
    public void addNewRow() {
        //Get the schemacomponent representing the xsd:string
        GlobalSimpleType stringSimpleType = null;
        Schema schema = SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema();
        Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
        for (GlobalSimpleType st : simpleTypes) {
            if (st.getName().equals("string")) {
                stringSimpleType = st;
                break;
            }
        }
        
        
        PartAndElementOrType p = new PartAndElementOrType(generateUniquePartName(),
                new ElementOrType(stringSimpleType, namespaceToPrefixMap));
        this.mPartAndElementOrTypeList.add(p);
        int row = this.mPartAndElementOrTypeList.indexOf(p);
        super.fireTableRowsInserted(row, row);
    } 
    
    private String generateUniquePartName() {
        String newNamePrefix = "part";
        int counter = 1;
        String generatedName = newNamePrefix + counter++;
        
        while(isPartNameExists(generatedName)) {
            generatedName = newNamePrefix + counter++;
        }
        
        return generatedName;
    }
    
    private boolean isPartNameExists(String newPartName) {
        Iterator<PartAndElementOrType> it = this.mPartAndElementOrTypeList.iterator();
        while(it.hasNext()) {
            PartAndElementOrType row = it.next();
            String partName = row.getPartName();
            //if name exists then create another name
            if(partName != null && partName.equals(newPartName)) {
                return true;
            } 
        }
        
        return false;
    }
    
    private boolean isValidPartName(String newPartName) {
        return org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(newPartName);
    }
    
    public class PartAndElementOrType {
        
        private String mPartName;
        
        private ElementOrType mElementOrType;
        
        public PartAndElementOrType(String partName, ElementOrType elementOrType) {
            this.mPartName = partName;
            this.mElementOrType = elementOrType;
        }
        
        public String getPartName() {
            return this.mPartName;
        }
        
        public void setPartName(String partName) {
            this.mPartName = partName;
        }
        
        public ElementOrType getElementOrType() {
            return this.mElementOrType;
        }
        
        public void setElementOrType(ElementOrType elementOrType) {
            this.mElementOrType = elementOrType;
        }
    }
    
}
