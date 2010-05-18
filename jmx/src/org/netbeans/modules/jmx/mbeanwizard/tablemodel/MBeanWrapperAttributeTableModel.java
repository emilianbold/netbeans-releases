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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.mbeanwizard.tablemodel;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperAttribute;
import org.netbeans.modules.jmx.MBeanAttribute;

/**
 *
 * @author an156382
 */
public class MBeanWrapperAttributeTableModel extends MBeanAttributeTableModel {

    //other idx inherited
    public static final int IDX_ATTR_SELECTION        = 0;

    private int firstEditableRow = 0;

    /**
     * Creates a new instance of MBeanWrapperAttributeTableModel 
     */
    public MBeanWrapperAttributeTableModel() {
        
        bundle = NbBundle.getBundle(MBeanAttributeTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[5];
        
        String ss = bundle.getString("LBL_AttrSelection");// NOI18N
        String sn = bundle.getString("LBL_AttrName");// NOI18N
        String st = bundle.getString("LBL_AttrType");// NOI18N
        String sa = bundle.getString("LBL_AttrAccess");// NOI18N
        String sd = bundle.getString("LBL_AttrDescription");// NOI18N
        
        columnNames[IDX_ATTR_SELECTION]              = ss;
        columnNames[super.IDX_ATTR_NAME +1]          = sn;
        columnNames[super.IDX_ATTR_TYPE +1]          = st;
        columnNames[super.IDX_ATTR_ACCESS +1]        = sa;
        columnNames[super.IDX_ATTR_DESCRIPTION +1]   = sd;
        
        lastIndex=0; 
    }
    
    public void setFirstEditableRow(int row) {
        firstEditableRow = row;
    }
    
    public int getFirstEditableRow() {
        return firstEditableRow;
    }
    
    /**
     * Overridden method from superclass 
     */
    public Object getValueAt(int row, int col) {
        
    MBeanWrapperAttribute attr = (MBeanWrapperAttribute)data.get(row);
        switch(col) {
            case 0: return attr.isSelected();
            case 1: return attr.getName();
            case 2: return attr.getTypeName();
            case 3: return attr.getAccess();
            case 4: return attr.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanWrapperAttributeTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        
        if (rowIndex < this.size()){
            MBeanWrapperAttribute attr = (MBeanWrapperAttribute)data.get(rowIndex);
            switch(columnIndex) {
                case 0: attr.setSelected((Boolean)aValue);
                break;
                case 1: attr.setName((String)aValue);
                break;
                case 2: attr.setTypeName((String)aValue);
                break;
                case 3: attr.setAccess((String)aValue);
                break;
                case 4: attr.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanWrapperAttributeTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    public MBeanWrapperAttribute getWrapperAttribute(int row) {
        return (MBeanWrapperAttribute) data.get(row);
    }
    
    public void addRow(MBeanAttribute attr) {
        MBeanWrapperAttribute mba = new MBeanWrapperAttribute(
                true,
                attr.getName(),
                attr.getTypeName(),
                attr.getAccess(),
                attr.getDescription(), attr.getTypeMirror());
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    public void addRow() {
        MBeanWrapperAttribute mba = new MBeanWrapperAttribute(
                true,
                WizardConstants.ATTR_NAME_DEFVALUE + lastIndex,
                WizardConstants.STRING_OBJ_FULLNAME,
                WizardConstants.ATTR_ACCESS_READ_WRITE,
                WizardConstants.ATTR_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.ATTR_DESCR_DEFVALUE_SUFFIX, null);
        lastIndex++;
        data.add(mba);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
