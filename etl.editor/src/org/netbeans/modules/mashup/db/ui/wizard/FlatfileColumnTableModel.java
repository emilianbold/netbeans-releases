/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mashup.db.ui.wizard;

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
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;

import com.sun.sql.framework.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileColumnTableModel extends RowEntryTableModel {

    private static transient final Logger mLogger = Logger.getLogger(FlatfileColumnTableModel.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Implementation of RowEntry interface that wraps around the content of a
     * FlatfileDBColumn instance.
     */
    public static class ColumnEntry implements RowEntryTableModel.RowEntry {
        /* column data model */
        private FlatfileDBColumn column;

        /**
         * Creates an instance of ColumnEntry that represents the given flatfile column
         * and exists within a flatfile of the given type.
         * 
         * @param aColumn FlatfileDBColumn that this RowEntry will represent
         */
        public ColumnEntry(FlatfileDBColumn aColumn) {
            if (aColumn == null) {
                throw new IllegalArgumentException("Must supply non-null FlatfileDBColumn reference.");
            }

            column = aColumn;
        }

        /**
         * Overrides default implementation.
         * 
         * @param o Object to be compared against this for equality
         * @return true if o is functionally equivalent to this, false otherwise
         */
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null) {
                return false;
            }

            ColumnEntry anEntry = (ColumnEntry) o;
            return (column != null) ? column.equals(anEntry.column) : (anEntry.column == null);
        }

        /**
         * Gets reference to underlying FlatfileDBColumn instance.
         * 
         * @return FlatfileDBColumn that this RowEntry represents
         */
        public FlatfileDBColumn getColumn() {
            return column;
        }

        /**
         * Returns List of Strings describing errors, if any, associated with this
         * RowEntry.
         * 
         * @return List, possibly empty, of errors associated with the contents of this
         *         RowEntry
         */
        public List validateColumnDefinition() {
            List errorList = new ArrayList();

            String columnName = column.getName();
            String nbBundle1 = mLoc.t("BUND210: Missing column name - please enter a unique string value.");
            String nbBundle2 = mLoc.t("BUND211: Invalid column name - must start with an alphabetical character and contain alphanumeric characters and/or underscores.");
            String nbBundle3 = mLoc.t("BUND212: Invalid length - please enter a non-zero, positive integer value.");
            if (columnName == null || columnName.trim().length() == 0) {
                String msg = nbBundle1.substring(15);
                errorList.add(msg);
            } else if (!StringUtil.isValid(columnName, "[A-Za-z]+[A-Za-z0-9_$#]*")) {
                String msg = nbBundle2.substring(15);
                errorList.add(msg);
            }

            int precLength = column.getPrecision();
            if (precLength <= 0) {
                String msg = nbBundle3.substring(15);
                errorList.add(msg);
            }

            int sqlType = Integer.MIN_VALUE;
            String nbBundle4 = mLoc.t("BUND213: Invalid data type - please select from the list of available types.");
            if (column.getJdbcTypeString() == null) {
                String msg =  nbBundle4.substring(15);
                errorList.add(msg);
            } else {
                sqlType = column.getJdbcType();
            }

            int scale = column.getScale();
            String nbBundle5 = mLoc.t("BUND214: Invalid scale - please enter a non-negative integer value.");
            String nbBundle6 = mLoc.t("BUND215: Scale exceeds precision - please enter a smaller non-negative integer value.");
            if (column.getScale() < 0) {
                String msg = nbBundle5.substring(15);
                errorList.add(msg);
            } else if (Types.NUMERIC == sqlType && scale > precLength) {
                String msg = nbBundle6.substring(15);
                errorList.add(msg);
            }
            String nbBundle7 = mLoc.t("BUND216: Column #{0}:",new Integer(column.getOrdinalPosition()));
            if (!errorList.isEmpty()) {
                String header = nbBundle7.substring(15);
                errorList.add(0, header);
            }

            if (column.getJdbcType() == Types.NUMERIC) {
                try {
                    DataType type = new BigDecimalType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add("Invalid default value for numeric type");
                }
            }

            if (column.getJdbcType() == Types.TIMESTAMP) {
                try {
                    DataType type = new TimestampType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add("Invalid default value for timestamp type");
                }
            }

            if (column.getJdbcType() == Types.TIME) {
                try {
                    DataType type = new TimeType();
                    type.convert(column.getDefaultValue());
                } catch (Exception e) {
                    errorList.add("Invalid default value for time type");
                }
            }

            return errorList;
        }

        /**
         * Gets name of the flatfile column underlying this RowEntry.
         * 
         * @return column name
         */
        public String getName() {
            return column.getName();
        }

        /**
         * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel.RowEntry#getValue
         */
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

        /**
         * Overrides default implementation.
         * 
         * @return computed hash code
         */
        public int hashCode() {
            return column.hashCode();
        }

        /**
         * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel.RowEntry#isEditable
         */
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

        /**
         * Indicates whether contents of this RowEntry instance are valid.
         * 
         * @return true if contents are valid, false otherwise.
         */
        public boolean isValid() {
            return (validateColumnDefinition().isEmpty());
        }

        /**
         * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel.RowEntry#setEditable
         */
        public void setEditable(int index, boolean newState) {
            if (index < COLUMN_ID || index > COLUMN_DAFAULT) {
                throw new IndexOutOfBoundsException();
            }
        }

        /**
         * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel.RowEntry#setValue
         */
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
                    column.setJdbcType(SQLUtils.getStdJdbcType(newValue.toString()));
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

    /**
     * Create new instance of FlatfileColumnTableModel representing the given Collection
     * of FlatfileDBColumn instances and using header information in the given Map.
     * 
     * @param columns Collection of FlatfileDBColumns containing model data
     * @param columnNames List of display header names for the various table columns
     */
    public FlatfileColumnTableModel(Collection columns, List columnNames) {
        super();

        editable = new boolean[columnNames.size()];
        Arrays.fill(editable, true);

        columnHeaders = (String[]) columnNames.toArray(new String[columnNames.size()]);
        setRowEntries(columns);
    }

    /**
     * Create new instance of FlatfileColumnTableModel representing the given Flatfile
     * instance and using header information in the given Map.
     * 
     * @param aTable FlatfileDBTable containing our data
     * @param columnNames List of display header names for the various table columns
     */
    public FlatfileColumnTableModel(FlatfileDBTable aTable, List columnNames) {
        this(aTable.getColumnList(), columnNames);
    }

    /**
     * Converts the FlatfileDBColumn instances in the given Collection to ColumnEntry
     * instances that are compatible with this model.
     * 
     * @param columns Collection of FlatfileDBColumn instances to be converted
     * @return List of ColumnEntry instances based on the contents of <code>columns</code>
     */
    public List convertToFieldEntries(Collection columns) {
        List rowEntries = new ArrayList(columns.size());
        if (columns != null && !columns.isEmpty()) {
            Iterator iter = columns.iterator();
            while (iter.hasNext()) {
                FlatfileDBColumn field = (FlatfileDBColumn) iter.next();
                rowEntries.add(new ColumnEntry(field));
            }
        }
        return rowEntries;
    }

    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel#getRowEntries
     */
    public synchronized List getRowEntries() {
        return super.getRowEntries();
    }

    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel#getRowEntries(int[])
     */
    public synchronized List getRowEntries(int[] indices) {
        return super.getRowEntries(indices);
    }

    /**
     * Gets List of ordinal position IDs of fields, if any, which have invalid names.
     * 
     * @return List (possibly empty) containing Integer instances, each of which holds the
     *         ordinal position of a column with an invalid name.
     */
    public List getRowIdsWithInvalidNames() {
        List rowlist = Collections.EMPTY_LIST;

        List entries = getRowEntries();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            FlatfileColumnTableModel.ColumnEntry fieldEntry = (FlatfileColumnTableModel.ColumnEntry) it.next();
            String name = fieldEntry.getValue(2).toString();
            if (!StringUtil.isValid(name, "[A-Za-z]+[A-Za-z_]*")) {
                if (rowlist == Collections.EMPTY_LIST) {
                    rowlist = new ArrayList(entries.size());
                }

                // NOTE: Ordinal position is an Integer object.
                rowlist.add(fieldEntry.getValue(0));
            }
        }

        return rowlist;
    }

    /**
     * Indicates whether the model contains at least one column with a column length value
     * of zero.
     * 
     * @return true if at least one column in this model has a length value of zero; false
     *         otherwise.
     */
    public boolean hasZeroLengthColumns() {
        boolean hasZeroLengthColumn = false;

        List entries = getRowEntries();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            FlatfileColumnTableModel.ColumnEntry fieldEntry = (FlatfileColumnTableModel.ColumnEntry) it.next();
            if (((Integer) fieldEntry.getValue(1)).intValue() == 0) {
                hasZeroLengthColumn = true;
                break;
            }
        }

        return hasZeroLengthColumn;
    }

    /**
     * Sets editability of column names in this model.
     * 
     * @param newValue true if column name is editable, false otherwise
     */
    public void setEntryEditable(int index, boolean newValue) {
        List entries = getRowEntries();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            FlatfileColumnTableModel.ColumnEntry columnEntry = (FlatfileColumnTableModel.ColumnEntry) iter.next();
            columnEntry.setEditable(index, newValue);
        }
    }

    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.RowEntryTableModel#setRowEntries(List)
     */
    public synchronized void setRowEntries(Collection columns) {
        super.setRowEntries(this.convertToFieldEntries(columns));
    }

    /**
     * Updates the given FlatfileDBTable instance to hold FlatfileDBColumn instances which
     * are defined by this model's current set of row entries.
     * 
     * @param table FlatfileDBTable whose columns will be populated with the contents of
     *        this model
     */
    public void updateColumns(FlatfileDBTable table) {
        table.deleteAllColumns();
        Iterator iter = getRowEntries().iterator();
        while (iter.hasNext()) {
            ColumnEntry entry = (ColumnEntry) iter.next();
            table.addColumn(entry.getColumn());
        }
    }
}

