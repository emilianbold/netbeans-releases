/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.iep.editor.ps.SelectPanel;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class IEPAttributeTableModel extends AbstractTableModel {

    private List<PlaceholderSchemaAttribute> mAttrList = new ArrayList<PlaceholderSchemaAttribute>();
    
    public IEPAttributeTableModel() {
        
    }
    
    public int getRowCount() {
        return this.mAttrList.size();
    }

    public List<PlaceholderSchemaAttribute> getAttributeList() {
        return this.mAttrList;
    }
            
    public int getColumnCount() {
        return 5;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    
    @Override
    public String getColumnName(int column) {
        String columnName = "";
        
        switch(column) {
                
                case 0:
                 columnName = NbBundle.getMessage(SelectPanel.class, "SelectPanel.ATTRIBUTE_NAME");   
                 break;
                 
                case 1:
                 columnName = NbBundle.getMessage(SelectPanel.class, "SelectPanel.DATA_TYPE");
                break;
                
                case 2:
                 columnName = NbBundle.getMessage(SelectPanel.class, "SelectPanel.SIZE");    
                break;
                
                case 3:
                 columnName = NbBundle.getMessage(SelectPanel.class, "SelectPanel.SCALE");   
                break;
                
                case 4:
                 columnName = NbBundle.getMessage(SelectPanel.class, "SelectPanel.COMMENT");    
                break;
            }
        
        return columnName;
    }
    
    
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        String val = "";
        PlaceholderSchemaAttribute sa = null;
        
        if(rowIndex <= mAttrList.size()) {
            sa = mAttrList.get(rowIndex);
            
            switch(columnIndex) {
                
                case 0:
                 val = sa.getAttributeName();   
                 break;
                 
                case 1:
                 val = sa.getAttributeType();
                break;
                
                case 2:
                 val = sa.getAttributeSize();    
                break;
                
                case 3:
                 val = sa.getAttributeScale();   
                break;
                
                case 4:
                 val = sa.getAttributeComment();    
                break;
            }
        }
        
        return val;
        
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PlaceholderSchemaAttribute sa = null;
        
        if(rowIndex <= mAttrList.size()) {
            sa = mAttrList.get(rowIndex);
            
            switch(columnIndex) {
                
                case 0:
                 sa.setAttributeName((String) aValue);
                 sa.setAXIComponent(null);
                 break;
                 
                case 1:
                 sa.setAttributeType((String) aValue);
                 sa.setAXIComponent(null);
                break;
                
                case 2:
                 sa.setAttributeSize((String) aValue);
                 sa.setAXIComponent(null);
                break;
                
                case 3:
                 sa.setAttributeScale((String) aValue);   
                 sa.setAXIComponent(null);
                break;
                
                case 4:
                 sa.setAttributeComment((String) aValue);    
                break;
            }
        }
    }

    public void addNewRow() {
        PlaceholderSchemaAttribute attr = new PlaceholderSchemaAttribute();
        this.mAttrList.add(attr);
        
        this.fireTableDataChanged();
    }
    
    public void addRow(PlaceholderSchemaAttribute rowData) {
        this.mAttrList.add(rowData);
        
        this.fireTableDataChanged();
    }
    
    public void insertRow(int rowIndex, PlaceholderSchemaAttribute rowData) {
        if(rowIndex <= this.mAttrList.size()) {
            this.mAttrList.add(rowIndex, rowData);
            
            this.fireTableDataChanged();
        }
    }
    
    public void removeRow(int rowIndex) {
        if(rowIndex < this.mAttrList.size()) {
            this.mAttrList.remove(rowIndex);
            
            this.fireTableDataChanged();
        }
    }
    
    public void removeRow(PlaceholderSchemaAttribute rowData) {
        if(this.mAttrList.contains(rowData)) {
            this.mAttrList.remove(rowData);
            this.fireTableDataChanged();
        }
    }
    
    public void clear() {
        this.mAttrList.clear();
        this.fireTableDataChanged();
    }
    
    public PlaceholderSchemaAttribute getMatchingRow(AXIComponent component) {
        Iterator<PlaceholderSchemaAttribute> it = this.mAttrList.iterator();
        
        while(it.hasNext()) {
            PlaceholderSchemaAttribute attr = it.next();
            AXIComponent comp = attr.getAXIComponent();
            if(comp != null && component.equals(comp)) {
                return attr;
            }
        }
        
        return null;
    }
    
    public PlaceholderSchemaAttribute getRowData(int rowIndex) {
        PlaceholderSchemaAttribute rowData = null;
       
        if(rowIndex < this.mAttrList.size()) {
            rowData = this.mAttrList.get(rowIndex);
        }
        return rowData;
    }
    
    
    
}
