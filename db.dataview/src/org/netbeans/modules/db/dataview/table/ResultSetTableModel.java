/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.table;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author Ahimanikya Satapathy
 */
public class ResultSetTableModel extends DefaultTableModel {

    private Class[] collumnClasses;
    protected ResultSetJXTable table;

    public static Class<? extends Object> getTypeClass(DBColumn col) {
        int colType = col.getJdbcType();

        if (colType == Types.BIT && col.getPrecision() <= 1) {
            colType = Types.BOOLEAN;
        }

        switch (colType) {
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.TIME:
                return Time.class;
            case Types.DATE:
                return Date.class;
            case Types.TIMESTAMP:
            case -100:
                return Timestamp.class;
            case Types.BIGINT:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return Number.class;

            case Types.CHAR:
            case Types.VARCHAR:
            case -15:
            case -9:
            case -8:
                return String.class;

            case Types.BIT:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return Blob.class;
            case Types.LONGVARCHAR:
            case -16:
            case Types.CLOB:
            case 2011: /*NCLOB */
                return Clob.class;
            case Types.OTHER:
            default:
                return Object.class;
        }
    }

    @SuppressWarnings("rawtypes")
    public ResultSetTableModel(ResultSetJXTable table) {
        super();
        this.table = table;
        collumnClasses = new Class[table.getRSColumnCount()];
        for (int i = 0, I = table.getRSColumnCount(); i < I; i++) {
            collumnClasses[i] = getTypeClass(table.getDBColumn(i));
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (table.dView.getDataViewDBTable() == null) {
            return;
        }
        Object oldVal = getValueAt(row, col);
        if (noUpdateRequired(oldVal, value)) {
            return;
        }
        try {
            if (!DataViewUtils.isSQLConstantString(value, table.getDBColumn(col))) {
                value = DBReadWriteHelper.validate(value, table.getDBColumn(col));
            }
            super.setValueAt(value, row, col);
            handleColumnUpdated(row, col, value);
            fireTableDataChanged();
        } catch (Exception dbe) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(dbe.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        table.revalidate();
        table.repaint();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Object> getColumnClass(int columnIndex) {
        if (collumnClasses[columnIndex] == null) {
            return super.getColumnClass(columnIndex);
        } else {
            return collumnClasses[columnIndex];
        }
    }

    protected boolean noUpdateRequired(Object oldVal, Object value) {
        return oldVal != null && oldVal.toString().equals(value == null ? "" : value.toString()) || (oldVal == null && value == null);
    }

    protected void handleColumnUpdated(int row, int col, Object value) {
        
    }
}
