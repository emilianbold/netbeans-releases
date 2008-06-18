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
package org.netbeans.modules.db.dataview.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents Database table Index objects.
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBIndex extends DBObject<DBTable> {

    /**
     * Intermediate container class to hold metadata of columns involved in a particular
     * Index.
     */
    public static class Column implements Comparable {

        private String name;
        private int sequence;

        /**
         * Creates a new instance of Index.Column with the given DBColumn and sequence.
         * 
         * @param col new DBColumn
         * @param colSequence sequence of new column w.r.t. other columns
         */
        public Column(DBColumn col, int colSequence) {
            this(col.getName(), colSequence);
        }

        /**
         * Creates a new instance of Index.Column with the given name and sequence.
         * 
         * @param colName name of new column
         * @param colSequence sequence of new column w.r.t. other columns
         */
        public Column(String colName, int colSequence) {
            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException("Must supply non-empty String value for parameter colName.");
            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException("Must supply positive integer value for parameter colSequence.");
            }

            name = colName;
            sequence = colSequence;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative
         * integer, zero, or a positive integer as this object is less than, equal to, or
         * greater than the specified object.
         * <p>
         * Note: this class has a natural ordering that is inconsistent with equals.
         * 
         * @param o the Object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less
         *         than, equal to, or greater than the specified object.
         */
        public int compareTo(Object o) {
            return (this.sequence - ((Column) o).sequence);
        }

        /**
         * Gets name of this column.
         * 
         * @return column name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets sequence of this column with respect to others in the associated index.
         * 
         * @return column index
         */
        public int getSequence() {
            return sequence;
        }
    }

    /* Indicates number of unique values in index */
    private int cardinality;

    /* List of column names in key sequence order. */
    private List<String> columnNames;

    /* Name of this index */
    private String name;

    /* Indicates sort order, if any, of index */
    private String sortSequence;

    /* Type of index, as enumerated in DatabaseMetaData */
    private int type;

    /* Indicates whether index is unique */
    private boolean unique = false;
    private static final String RS_INDEX_NAME = "INDEX_NAME"; // NOI18N

    //private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    private static final String RS_NON_UNIQUE = "NON_UNIQUE"; // NOI18N

    private static final String RS_TYPE = "TYPE"; // NOI18N

    //private static final String RS_ORDINAL = "ORDINAL_POSITION"; // NOI18N
    private static final String RS_ASC_OR_DESC = "ASC_OR_DESC"; // NOI18N

    private static final String RS_CARDINALITY = "CARDINALITY"; // NOI18N


    /**
     * Creates a List of IndexColumn instances from the given ResultSet.
     *
     * @param rs ResultSet containing index metadata as obtained from 
     * DatabaseMetaData
     * @return List of IndexColumn instances based from metadata in rs'
     *
     * @throws SQLException if SQL error occurs while reading in data from
     * given ResultSet
     */
    public static List<DBIndex> createIndexList(ResultSet rs) throws SQLException {
        List<DBIndex> indices = Collections.emptyList();

        if (rs != null && rs.next()) {
            indices = new ArrayList<DBIndex>();
            do {
                DBIndex newIndex = new DBIndex(rs);

                // Ignore statistics indexes as they are relevant only to the
                // DB which sourced this metadata.
                if (newIndex.getType() != DatabaseMetaData.tableIndexStatistic) {
                    indices.add(newIndex);
                }
            } while (rs.next());
        }

        return indices;
    }

    DBIndex(ResultSet rs) throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/sql/framework/model/impl/Bundle", locale); // NO i18n            

            throw new IllegalArgumentException(
                    cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)"); // NOI18N

        }

        name = rs.getString(RS_INDEX_NAME);
        //columnName = rs.getString(RS_COLUMN_NAME);

        unique = !(rs.getBoolean(RS_NON_UNIQUE));
        type = rs.getShort(RS_TYPE);

        //ordinalPosition = rs.getShort(RS_ORDINAL);
        sortSequence = rs.getString(RS_ASC_OR_DESC);
        cardinality = rs.getInt(RS_CARDINALITY);
    }

    public boolean contains(DBColumn col) {
        return (col != null) ? contains(col.getName()) : false;
    }

    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBIndex)) {
            return false;
        }

        DBIndex ref = (DBIndex) refObj;

        boolean result = (name != null) ? name.equals(ref.name) : (ref.name == null);

        result &= (type == ref.type) && (cardinality == ref.cardinality) && (unique == ref.unique);

        result &= (sortSequence != null) ? sortSequence.equals(ref.sortSequence) : (ref.sortSequence == null);

        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames == null);

        return result;
    }

    public int getCardinality() {
        return cardinality;
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public String getColumnName(int iColumn) {
        return columnNames.get(iColumn);
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public String getName() {
        return name;
    }

    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }
        return getSequence(col.getName().trim());
    }

    /**
     * Gets the ordinal position of the column, if any, associated with the given
     * columnName.
     * 
     * @param columnName name of column whose position is desired
     * @return (zero-based) position of given column, or -1 if no column by the given
     *         columnName could be located
     */
    public int getSequence(String columnName) {
        return columnNames.indexOf(columnName);
    }

    public String getSortSequence() {
        return sortSequence;
    }

    public int getType() {
        return type;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        myHash += type + cardinality + (unique ? 1 : 0);
        myHash += (sortSequence != null) ? sortSequence.hashCode() : 0;

        return myHash;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#contains(java.lang.String)
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Sets column names associated with this index from the given List, using the given
     * flag to interpret the type of objects contained in the list.
     * 
     * @param indexColumnNames List of column names (either as Index.Column objects or
     *        String values)
     * @param isStringList true if List contains column names as Strings, false if List
     *        contains Index.Column objects.
     */
    public void setColumnNames(List<String> indexColumnNames, boolean isStringList) {
        if (isStringList) {
            columnNames.addAll(indexColumnNames);
        } else {
            Collections.sort(indexColumnNames);
            Iterator iter = indexColumnNames.iterator();
            while (iter.hasNext()) {
                Column col = (Column) iter.next();
                columnNames.add(col.getName());
            }
        }
    }
}
