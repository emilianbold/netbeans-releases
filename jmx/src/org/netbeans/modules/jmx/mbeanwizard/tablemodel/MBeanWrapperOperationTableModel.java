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
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperOperation;
import org.netbeans.modules.jmx.MBeanOperation;

/**
 *
 * @author an156382
 */
public class MBeanWrapperOperationTableModel extends MBeanOperationTableModel {

    //other idx inherited
    public static final int IDX_METH_SELECTION        = 0;
    private int firstEditableRow = 0;
    
    /** Creates a new instance of MBeanWrapperOperationTableModel */
    public MBeanWrapperOperationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanOperationTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[6];
        
        String ss = bundle.getString("LBL_MethodSelected");// NOI18N
        String sn = bundle.getString("LBL_MethodName");// NOI18N
        String st = bundle.getString("LBL_MethodReturnType");// NOI18N
        String sp = bundle.getString("LBL_MethodParamType");// NOI18N
        String se = bundle.getString("LBL_MethodExceptionType");// NOI18N
        String sd = bundle.getString("LBL_MethodDescription");// NOI18N
        
        columnNames[IDX_METH_SELECTION]              = ss;
        columnNames[super.IDX_METH_NAME +1]          = sn;
        columnNames[super.IDX_METH_TYPE +1]          = st;
        columnNames[super.IDX_METH_PARAM +1]         = sp;
        columnNames[super.IDX_METH_EXCEPTION +1]     = se;
        columnNames[super.IDX_METH_DESCRIPTION +1]   = sd;
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanWrapperOperation oper = (MBeanWrapperOperation)data.get(row);
        switch(col) {
            case 0: return oper.isSelected();
            case 1: return oper.getName();
            case 2: return oper.getReturnTypeName();
            case 3: return oper.getParametersList();
            case 4: return oper.getExceptionsList();
            case 5: return oper.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanWrapperOperationTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanWrapperOperation oper = (MBeanWrapperOperation)data.get(rowIndex);
            switch(columnIndex) {
                case 0: oper.setSelected((Boolean)aValue);
                break;
                case 1: oper.setName((String)aValue);
                break;
                case 2: oper.setReturnTypeName((String)aValue);
                break;
                case 3: //do nothing (already saved by the popup)
                break;
                case 4: //do nothing (already saved by the popup)
                break;
                case 5: oper.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanWrapperOperationTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    public void addRow(MBeanOperation op) {
        MBeanWrapperOperation mbo = new MBeanWrapperOperation(
                true,
                op.getName(),
                op.getReturnTypeName(),
                op.getParametersList(),
                op.getExceptionsList(),
                op.getDescription());
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    public void addRow() {
        MBeanOperation op = createNewOperation();
        MBeanWrapperOperation mbwo = new MBeanWrapperOperation(
                true,
                op.getName(),
                op.getReturnTypeName(),
                op.getParametersList(),
                op.getExceptionsList(),
                op.getDescription());
        data.add(mbwo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    /**
     * Returns the operation at index index
     * @return MBeanWrapperOperation the operation at index index
     */
    public MBeanWrapperOperation getWrapperOperation(int index) {
        return (MBeanWrapperOperation) data.get(index);
    }

    public int getFirstEditableRow() {
        return this.firstEditableRow;
    }
    
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
}
