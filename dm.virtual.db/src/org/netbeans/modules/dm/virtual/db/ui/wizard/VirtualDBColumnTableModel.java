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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.axiondb.DataType;
import org.axiondb.types.BigDecimalType;
import org.axiondb.types.TimeType;
import org.axiondb.types.TimestampType;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;

import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class VirtualDBColumnTableModel extends RowEntryTableModel {

    public static class ColumnEntry implements RowEntryTableModel.RowEntry {

        private VirtualDBColumn column;

        public ColumnEntry(VirtualDBColumn aColumn) {
            if (aColumn == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_Null_FFDBColRef"));
            }

            column = aColumn;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null) {
                return false;
            }

            ColumnEntry anEntry = (ColumnEntry) o;
            return (column != null) ? column.equals(anEntry.column) : (anEntry.column == null);
        }

        public VirtualDBColumn getColumn() {
            return column;
        }

        public List validateColumnDefinition() {
            List errorList = new ArrayList();

            String columnName = column.getName();
            if (columnName == null || columnName.trim().length() == 0) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_nofieldname");
                errorList.add(msg);
            } else if (!VirtualDBUtil.isValid(columnName, NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_ColName_Valid"))) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_illegalfieldname");
                errorList.add(msg);
            }

            int precLength = column.getPrecision();
            if (precLength <= 0) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_badfieldsize");
                errorList.add(msg);
            }

            int sqlType = Integer.MIN_VALUE;
            if (column.getJdbcTypeString() == null) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_nosqltype");
                errorList.add(msg);
            } else {
                sqlType = column.getJdbcType();
            }

            int scale = column.getScale();
            if (column.getScale() < 0) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_badfieldscale");
                errorList.add(msg);
            } else if (Types.NUMERIC == sqlType && scale > precLength) {
                String msg = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_scaleexceedsprecision");
                errorList.add(msg);
            }
            if (!errorList.isEmpty()) {
                String header = NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_fieldtablemodel_fieldheader", new Integer(column.getOrdinalPosition()));
                errorList.add(0, header);
            }

            if (column.getJdbcType() == Types.NUMERIC) {
                try {
                    DataType type = new BigDecimalType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add(NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_Invalid_NumericType"));
                }
            }

            if (column.getJdbcType() == Types.TIMESTAMP) {
                try {
                    DataType type = new TimestampType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add(NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_Invalid_TimestampType"));
                }
            }

            if (column.getJdbcType() == Types.TIME) {
                try {
                    DataType type = new TimeType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add(NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_Invalid_TimeType"));
                }
            }

            return errorList;
        }

        public String getName() {
            return column.getName();
        }

        public Object getValue(int index) {
            switch (index) {
                case COLUMN_ID:
                    return new Integer(column.getCardinalPosition());

                case COLUMN_PRECLENGTH:
                    return new Integer(column.getPrecision());

                case COLUMN_NAME:
                    return column.getName();

                case COLUMN_JDBCTYPE:
                    return column.getJdbcTypeString();

                case COLUMN_SCALE:
                    if (isEditable(index)) {
                        return new Integer(column.getScale());
                    }
                    return "";

                case COLUMN_ISNULLABLE:
                    return Boolean.valueOf(column.isNullable());

                case COLUMN_ISPK:
                    return Boolean.valueOf(column.isPrimaryKey());

                case COLUMN_DAFAULT:
                    return column.getDefaultValue();

                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int hashCode() {
            return column.hashCode();
        }

        public boolean isEditable(int index) {
            if (index == COLUMN_SCALE) {
                // Allow scale only for numeric datatype
                return (Types.NUMERIC == column.getJdbcType());
            }
            if (index == COLUMN_ISNULLABLE) {
                return (!column.isPrimaryKey());
            }
            return (index != 0);
        }

        public boolean isValid() {
            return (validateColumnDefinition().isEmpty());
        }

        public void setEditable(int index, boolean newState) {
            if (index < COLUMN_ID || index > COLUMN_DAFAULT) {
                throw new IndexOutOfBoundsException();
            }
        }

        public void setValue(int index, Object newValue) {
            switch (index) {
                case COLUMN_ID:
                    column.setCardinalPosition(((Integer) newValue).intValue());
                    break;

                case COLUMN_PRECLENGTH:
                    column.setPrecision(Integer.parseInt((String) newValue));
                    break;

                case COLUMN_NAME:
                    column.setName(newValue.toString());
                    break;

                case COLUMN_JDBCTYPE:
                    column.setJdbcType(VirtualDBUtil.getStdJdbcType(newValue.toString()));
                    break;

                case COLUMN_SCALE:
                    int scaleValue = isEditable(index) ? Integer.parseInt((String) newValue) : 0;
                    column.setScale(scaleValue);
                    break;

                case COLUMN_ISNULLABLE:
                    if (column.isPrimaryKey()) {
                        column.setNullable(false);
                    } else {
                        column.setNullable(((Boolean) newValue).booleanValue());
                    }
                    break;

                case COLUMN_ISPK:
                    boolean pkFlag = ((Boolean) newValue).booleanValue();
                    column.setPrimaryKey(pkFlag);
                    if (pkFlag) {
                        column.setNullable(false);
                    }
                    break;

                case COLUMN_DAFAULT:
                    column.setDefaultValue(newValue.toString());
                    break;

                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }
    public static final int COLUMN_DAFAULT = 7;
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ISNULLABLE = 5;
    public static final int COLUMN_ISPK = 6;
    public static final int COLUMN_JDBCTYPE = 3;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_PRECLENGTH = 1;
    public static final int COLUMN_SCALE = 4;

    public VirtualDBColumnTableModel(Collection columns, List columnNames) {
        super();

        editable = new boolean[columnNames.size()];
        Arrays.fill(editable, true);

        columnHeaders = (String[]) columnNames.toArray(new String[columnNames.size()]);
        setRowEntries(columns);
    }

    public VirtualDBColumnTableModel(VirtualDBTable aTable, List columnNames) {
        this(aTable.getColumnList(), columnNames);
    }

    public List convertToFieldEntries(Collection columns) {
        List rowEntries = new ArrayList(columns.size());
        if (columns != null && !columns.isEmpty()) {
            Iterator iter = columns.iterator();
            while (iter.hasNext()) {
                VirtualDBColumn field = (VirtualDBColumn) iter.next();
                rowEntries.add(new ColumnEntry(field));
            }
        }
        return rowEntries;
    }

    @Override
    public synchronized List getRowEntries() {
        return super.getRowEntries();
    }

    @Override
    public synchronized List getRowEntries(int[] indices) {
        return super.getRowEntries(indices);
    }

    public List getRowIdsWithInvalidNames() {
        List rowlist = Collections.EMPTY_LIST;

        List entries = getRowEntries();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            VirtualDBColumnTableModel.ColumnEntry fieldEntry = (VirtualDBColumnTableModel.ColumnEntry) it.next();
            String name = fieldEntry.getValue(2).toString();
            if (!VirtualDBUtil.isValid(name, NbBundle.getMessage(VirtualDBColumnTableModel.class, "MSG_Name_Valid"))) {
                if (rowlist == Collections.EMPTY_LIST) {
                    rowlist = new ArrayList(entries.size());
                }

                // NOTE: Ordinal position is an Integer object.
                rowlist.add(fieldEntry.getValue(0));
            }
        }

        return rowlist;
    }

    public boolean hasZeroLengthColumns() {
        boolean hasZeroLengthColumn = false;

        List entries = getRowEntries();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            VirtualDBColumnTableModel.ColumnEntry fieldEntry = (VirtualDBColumnTableModel.ColumnEntry) it.next();
            if (((Integer) fieldEntry.getValue(1)).intValue() == 0) {
                hasZeroLengthColumn = true;
                break;
            }
        }

        return hasZeroLengthColumn;
    }

    public void setEntryEditable(int index, boolean newValue) {
        List entries = getRowEntries();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            VirtualDBColumnTableModel.ColumnEntry columnEntry = (VirtualDBColumnTableModel.ColumnEntry) iter.next();
            columnEntry.setEditable(index, newValue);
        }
    }

    @Override
    public synchronized void setRowEntries(Collection columns) {
        super.setRowEntries(this.convertToFieldEntries(columns));
    }

    public void updateColumns(VirtualDBTable table) {
        table.deleteAllColumns();
        Iterator iter = getRowEntries().iterator();
        while (iter.hasNext()) {
            ColumnEntry entry = (ColumnEntry) iter.next();
            table.addColumn(entry.getColumn());
        }
    }
}

