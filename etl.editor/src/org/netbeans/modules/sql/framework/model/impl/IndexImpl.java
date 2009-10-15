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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.Index;

/**
 * Implements Index interface.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class IndexImpl implements Cloneable, Index {

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

    /** Name of attribute used for marshalling out cardinality value to XML */
    public static final String CARDINALITY_ATTR = "cardinality"; // NOI18N

    /** List of column names in key sequence order. */
    public static final String COLUMNS_ATTR = "columns"; // NOI18N

    /** Document element tag name for marshalling out this object to XML */
    public static final String ELEMENT_TAG = "index"; // NOI18N

    /** Name of attribute used for marshalling out index name to XML */
    public static final String NAME_ATTR = "name"; // NOI18N

    /** Name of attribute used for marshalling out sort order value to XML */
    public static final String SORTORDER_ATTR = "sortOrder"; // NOI18N

    /** Name of attribute used for marshalling out index type to XML */
    public static final String TYPE_ATTR = "type"; // NOI18N

    /** Name of attribute used for marshalling out uniqueness flag to XML */
    public static final String UNIQUE_ATTR = "unique"; // NOI18N

    /* Indicates number of unique values in index */
    private int cardinality;

    /* List of column names in key sequence order. */
    private List<String> columnNames;

    /* (optional) DOM element used to construct this instance of Index */
    private transient Element element;

    /* Name of this index */
    private String name;

    /* DBTable to which this Index belongs */
    private DBTable parent;

    /* Indicates sort order, if any, of index */
    private String sortSequence;

    /* Type of index, as enumerated in DatabaseMetaData */
    private int type;

    /* Indicates whether index is unique */
    private boolean unique = false;

    private static final String
        RS_INDEX_NAME = "INDEX_NAME"; // NOI18N
    
    private static final String
        RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
        
    private static final String
        RS_NON_UNIQUE = "NON_UNIQUE"; // NOI18N
    
    private static final String
        RS_TYPE = "TYPE"; // NOI18N
    
    private static final String
        RS_ORDINAL = "ORDINAL_POSITION"; // NOI18N
    
    private static final String
        RS_ASC_OR_DESC = "ASC_OR_DESC"; // NOI18N
    
    private static final String
        RS_CARDINALITY = "CARDINALITY"; // NOI18N
    
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
    public static List<IndexImpl> createIndexList(ResultSet rs) throws SQLException {
        List<IndexImpl> indices = Collections.emptyList();
        
        if (rs != null && rs.next()) {
            indices = new ArrayList<IndexImpl>();
            do {
                IndexImpl newIndex = new IndexImpl(rs);
                
                // Ignore statistics indexes as they are relevant only to the
                // DB which sourced this metadata.
                if (newIndex.getType() != DatabaseMetaData.tableIndexStatistic) {
                    indices.add(newIndex);
                }
            } while (rs.next());
        }
        
        return indices;
    }

    IndexImpl(ResultSet rs) throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/sql/framework/model/impl/Bundle", locale); // NO i18n            
            throw new IllegalArgumentException(
                cMessages.getString("ERROR_VALID_RS")+"(ERROR_VALID_RS)"); // NOI18N
        }
        
        name = rs.getString(RS_INDEX_NAME);
        //columnName = rs.getString(RS_COLUMN_NAME);
        
        unique = !(rs.getBoolean(RS_NON_UNIQUE));
        type = rs.getShort(RS_TYPE);
        
        //ordinalPosition = rs.getShort(RS_ORDINAL);
        sortSequence = rs.getString(RS_ASC_OR_DESC);
        cardinality = rs.getInt(RS_CARDINALITY);
    }

    /**
     * Creates a new instance of IndexImpl, using the given keyElement as a source for
     * reconstituting its contents. Caller must invoke parseXML() after this constructor
     * returns in order to unmarshal and reconstitute the instance object.
     * 
     * @param keyElement DOM element containing XML marshalled version of a IndexImpl
     *        instance
     * @see #parseXML
     */
    public IndexImpl(Element keyElement) {
        this();
        element = keyElement;
    }

    /**
     * Creates a new instance of Index, cloning the contents of the given Index
     * implementation instance.
     * 
     * @param src Index instance to be cloned
     */
    public IndexImpl(Index src) {
        this();
        copyFrom(src);
    }

    /**
     * Creates a new instance of Index with the given key name and attributes.
     * 
     * @param indexName name of this Index, must be non-empty
     * @param indexType type of Index, as enumerated in java.sql.DatabaseMetaData; one of
     *        tableIndexClustered, tableIndexHashed, or tableIndexOther
     * @param isUnique true if index enforces uniqueness, false otherwise
     * @param sortOrder 'A' for ascending, 'D' for descending, null if undefined
     * @param indexCardinality cardinality of this index
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public IndexImpl(String indexName, int indexType, boolean isUnique, String sortOrder, int indexCardinality) {
        this();

        if (indexName == null) {
            throw new IllegalArgumentException("Must supply non-empty String ref for indexName param.");
        }

        name = indexName;
        type = indexType;
        unique = isUnique;
        sortSequence = sortOrder;
        cardinality = indexCardinality;
    }

    /**
     * Creates a new instance of Index with the given key name and attributes, and
     * referencing the column names in the given List.
     * 
     * @param indexName name of this Index, must be non-empty
     * @param indexType type of Index, as enumerated in java.sql.DatabaseMetaData; one of
     *        tableIndexClustered, tableIndexHashed, or tableIndexOther
     * @param isUnique true if index enforces uniqueness, false otherwise
     * @param sortOrder 'A' for ascending, 'D' for descending, null if undefined
     * @param indexCardinality cardinality of this index
     * @param indexColumnNames List of Column objects, or column names in sequential
     *        order, depending on state of isStringList
     * @param isStringList true if indexColumnName contains column names in sequential
     *        order, false if it contains Column objects which need to be sorted in
     *        sequential order.
     * @see java.sql.DatabaseMetaData#tableIndexClustered
     * @see java.sql.DatabaseMetaData#tableIndexHashed
     * @see java.sql.DatabaseMetaData#tableIndexOther
     */
    public IndexImpl(String indexName, int indexType, boolean isUnique, String sortOrder, int indexCardinality, List indexColumnNames,
            boolean isStringList) {
        this(indexName, indexType, isUnique, sortOrder, indexCardinality);
        setColumnNames(indexColumnNames, isStringList);
    }

    /*
     * IMPLEMENTATION OF Index
     */

    /* Private no-arg constructor */
    private IndexImpl() {
        name = null;
        columnNames = new ArrayList<String>();
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    @Override
    public Object clone() {
        try {
            IndexImpl impl = (IndexImpl) super.clone();
            impl.columnNames = new ArrayList<String>(this.columnNames);

            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * @see org.netbeans.modules.model.database.Index#contains(DBColumn)
     */
    public boolean contains(DBColumn col) {
        return (col != null) ? contains(col.getName()) : false;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#contains(java.lang.String)
     */
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

        if (!(refObj instanceof IndexImpl)) {
            return false;
        }

        IndexImpl ref = (IndexImpl) refObj;

        boolean result = (name != null) ? name.equals(ref.name) : (ref.name == null);

        result &= (type == ref.type) && (cardinality == ref.cardinality) && (unique == ref.unique);

        result &= (sortSequence != null) ? sortSequence.equals(ref.sortSequence) : (ref.sortSequence == null);

        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames == null);

        return result;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getCardinality
     */
    public int getCardinality() {
        return cardinality;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getColumnCount
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getColumnName
     */
    public String getColumnName(int iColumn) {
        return columnNames.get(iColumn);
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getColumnNames
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getName
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getParent
     */
    public DBTable getParent() {
        return parent;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getSequence(DBColumn)
     */
    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return getSequence(col.getName().trim());
    }

    /*
     * Setter and non-API helper methods
     */

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

    /**
     * @see org.netbeans.modules.model.database.Index#getSortSequence
     */
    public String getSortSequence() {
        return sortSequence;
    }

    /**
     * @see org.netbeans.modules.model.database.Index#getType
     */
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
     * Parses the XML content, if any, represented by the DOM element member variable.
     * 
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    public void parseXML() throws BaseException {
        if (this.element == null) {
            throw new BaseException("No <" + ELEMENT_TAG + "> element found.");
        }

        this.name = element.getAttribute(NAME_ATTR);

        String val = element.getAttribute(TYPE_ATTR);
        try {
            this.type = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.type = 0;
        } catch (NullPointerException e) {
            this.type = 0;
        }

        val = element.getAttribute(UNIQUE_ATTR);
        try {
            this.unique = Boolean.valueOf(val).booleanValue();
        } catch (NullPointerException e) {
            this.type = 0;
        }

        this.sortSequence = element.getAttribute(SORTORDER_ATTR);

        val = element.getAttribute(CARDINALITY_ATTR);
        try {
            this.cardinality = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.cardinality = 0;
        } catch (NullPointerException e) {
            this.cardinality = 0;
        }

        String colNames = element.getAttribute(COLUMNS_ATTR);
        columnNames.addAll(StringUtil.createStringListFrom(colNames));
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

    /**
     * Replaces the current List of column names with the contents of the given String
     * array.
     * 
     * @param newColNames array of names to supplant current list of column names
     */
    public void setColumnNames(String[] newColNames) {
        if (newColNames == null) {
            throw new IllegalArgumentException("Must supply non-null String[] for param newColNames.");
        }

        columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            columnNames.add(newColNames[i]);
        }
    }

    /**
     * Sets reference to SQLTable that owns this primary key.
     * 
     * @param newParent new parent of this primary key.
     */
    public void setParent(DBTable newParent) {
        parent = newParent;
    }

    /**
     * Gets the default XML representation of index metadata.
     * 
     * @return XML representation of the index metadata.
     */
    public synchronized String toXMLString() {
        return toXMLString(null);
    }

    /**
     * Gets the XML representation of index metadata, using the given String as a prefix
     * for successive elements.
     * 
     * @param prefix start-of-line prefix for the XML representation.
     * @return XML representation of the index metadata.
     */
    public synchronized String toXMLString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buf = new StringBuilder(100);

        buf.append(prefix).append("<").append(ELEMENT_TAG).append(" ");
        if (name != null && name.trim().length() != 0) {
            buf.append(NAME_ATTR).append("=\"").append(name.trim()).append("\" ");
        }

        buf.append(TYPE_ATTR).append("=\"").append(type).append("\" ");

        buf.append(UNIQUE_ATTR).append("=\"").append(unique).append("\" ");

        if (sortSequence != null && sortSequence.trim().length() != 0) {
            buf.append(SORTORDER_ATTR).append("=\"").append(sortSequence).append("\" ");
        }

        buf.append(CARDINALITY_ATTR).append("=\"").append(cardinality).append("\" ");

        if (columnNames.size() != 0) {
            buf.append(COLUMNS_ATTR).append("=\"");
            for (int i = 0; i < columnNames.size(); i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append((columnNames.get(i)).trim());
            }
            buf.append("\" ");
        }

        buf.append("/>\n");

        return buf.toString();
    }

    private void copyFrom(Index src) {
        name = src.getName();
        parent = src.getParent();

        columnNames.clear();
        columnNames.addAll(src.getColumnNames());

        type = src.getType();
        unique = src.isUnique();
        sortSequence = src.getSortSequence();
        cardinality = src.getCardinality();
    }
}
