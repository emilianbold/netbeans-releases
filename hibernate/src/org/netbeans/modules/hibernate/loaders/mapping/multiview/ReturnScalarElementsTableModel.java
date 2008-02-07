/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.hibernate.loaders.mapping.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.multiview.*;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.Resultset;

/**
 * 
 * @author Dongmei Cao
 */
public class ReturnScalarElementsTableModel extends AbstractTableModel {

    private static final String[] columnNames = { "Column", "Type"
        //NbBundle.getMessage(ImportElementsTableModel.class, "LBL_Meta_Attribute"),
        //NbBundle.getMessage(ImportElementsTableModel.class, "LBL_Meta_Inherit"),
        //NbBundle.getMessage(ImportElementsTableModel.class, "LBL_Meta_Value")
    
    };
    // Matches the attribute name used in org.netbeans.modules.hibernate.mapping.model.HibernateMapping
    private static final String[] attrNames = new String[]{"Column", "Type"};
    private Resultset resultset;

    public ReturnScalarElementsTableModel(Resultset resultset) {
        this.resultset = resultset;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Object getValueAt(int row, int column) {

        if (resultset == null) {
            return null;
        } else {
                return resultset.getAttributeValue(Resultset.RETURN_SCALAR, row, attrNames[column]);
        }
    }

    public int getRowCount() {
        if (resultset == null) {
            return 0;
        } else {
            return resultset.sizeReturnScalar();
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    public void addRow(String column, String type) {

        int index = resultset.addReturnScalar(true);
        resultset.setAttributeValue(Resultset.RETURN_SCALAR, index, attrNames[0], column);
        resultset.setAttributeValue(Resultset.RETURN_SCALAR, index, attrNames[1], type);

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public void editRow(int row, String column, String type) {
        resultset.setAttributeValue(Resultset.RETURN_SCALAR, row, attrNames[0], column);
        resultset.setAttributeValue(Resultset.RETURN_SCALAR, row, attrNames[1], type);

        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        resultset.removeReturnScalar(row);

        fireTableRowsDeleted(row, row);
    }
}
