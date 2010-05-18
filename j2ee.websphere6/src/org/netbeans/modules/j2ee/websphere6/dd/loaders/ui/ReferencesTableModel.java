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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
