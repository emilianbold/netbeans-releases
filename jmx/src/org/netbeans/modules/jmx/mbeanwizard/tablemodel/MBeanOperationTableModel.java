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
import org.netbeans.modules.jmx.MBeanOperation;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;


/**
 * Class implementing the table model for the mbean operation table
 *
 */
public class MBeanOperationTableModel extends AbstractJMXTableModel {
    public static final int IDX_METH_NAME          = 0;
    public static final int IDX_METH_TYPE          = 1;
    public static final int IDX_METH_PARAM         = 2;
    public static final int IDX_METH_EXCEPTION     = 3;
    public static final int IDX_METH_DESCRIPTION   = 4;
    
    private int lastIndex;
    
    /**
     * Constructor
     */
    public MBeanOperationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanOperationTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[5];
        
        String sn = bundle.getString("LBL_MethodName");// NOI18N
        String st = bundle.getString("LBL_MethodReturnType");// NOI18N
        String sp = bundle.getString("LBL_MethodParamType");// NOI18N
        String se = bundle.getString("LBL_MethodExceptionType");// NOI18N
        String sd = bundle.getString("LBL_MethodDescription");// NOI18N
        
        columnNames[IDX_METH_NAME]          = sn;
        columnNames[IDX_METH_TYPE]          = st;
        columnNames[IDX_METH_PARAM]         = sp;
        columnNames[IDX_METH_EXCEPTION]     = se;
        columnNames[IDX_METH_DESCRIPTION]   = sd;
        
        lastIndex = 0;
    }
    
    /**
     * Instantiates a new operation; called when a line is added to the
     * table
     * @return MBeanOperation the created operation
     */
    public MBeanOperation createNewOperation() {
        MBeanOperation oper = new MBeanOperation(
                WizardConstants.METH_NAME_DEFVALUE + lastIndex,
                WizardConstants.VOID_RET_TYPE,
                null,
                null,
                WizardConstants.METH_DESCR_DEFVALUE_PREFIX + lastIndex +
                WizardConstants.METH_DESCR_DEFVALUE_SUFFIX);
        lastIndex++;
        return oper;
    }
    
    public void clear() {
        data.clear();
    }
    
    /**
     * Returns the operation at index index
     * @return MBeanOperation the operation at index index
     */
    public MBeanOperation getOperation(int index) {
        return (MBeanOperation)data.get(index);
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperation oper = (MBeanOperation)data.get(row);
        switch(col) {
            case 0: return oper.getName();
            case 1: return oper.getReturnTypeName();
            case 2: return oper.getParametersList();
            case 3: return oper.getExceptionsList();
            case 4: return oper.getDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanMethodTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperation oper = (MBeanOperation)data.get(rowIndex);
            switch(columnIndex) {
                case 0: oper.setName((String)aValue);
                break;
                case 1: oper.setReturnTypeName((String)aValue);
                break;
                case 2: oper.setParametersList(
                        (ArrayList<MBeanOperationParameter>)aValue);
                break;
                case 3: oper.setExceptionsList(
                        (ArrayList<MBeanOperationException>)aValue);
                break;
                case 4: oper.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanMethodTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperation mbo = createNewOperation();
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
    public void addRow(MBeanOperation op) {
        MBeanOperation mbo = new MBeanOperation(
                op.getName(),
                op.getReturnTypeName(),
                op.getParametersList(),
                op.getExceptionsList(),
                op.getDescription());
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
