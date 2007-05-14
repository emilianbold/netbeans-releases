/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.ForeignKey;
import org.netbeans.modules.model.database.PrimaryKey;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Implements ForeignKey interface.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ForeignKeyImpl implements Cloneable, ForeignKey {

    /**
     * Object wrapper to bind the name and sequence ID of a table column participating in
     * a foreign key, along with its associated primary key column.
     */
    public static class Column implements Comparable {
        private String name;
        private String pkColumnName;

        private int sequence;

        /**
         * Constructs a new instance of Column with the given name, sequence, and
         * associated primary key column
         * 
         * @param colName name of new Column
         * @param colSequence one-based value indicating this column's sequential order
         * @param pkColName name of PK column associated wtih this new (FK) Column
         *        instance in a composite key; should be 1 if this column is the only one
         *        in the PK
         */
        public Column(String colName, int colSequence, String pkColName) {
            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException("Must supply non-empty String value for parameter colName.");
            }

            if (pkColName == null || pkColName.trim().length() == 0) {
                throw new IllegalArgumentException("Must supply non-empty String value for parameter pkColName.");
            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException("Must supply positive integer value for parameter colSequence.");
            }

            name = colName.trim();
            sequence = colSequence;

            pkColumnName = pkColName.trim();
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
         * Gets name of this Column.
         * 
         * @return column name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets name of associated Primary Key column.
         * 
         * @return name of associated PK column
         */
        public String getPKColumnName() {
            return pkColumnName;
        }

        /**
         * Gets sequence of this Column within its containing PK.
         * 
         * @return this column's sequence relative to other columns in the containing PK.
         */
        public int getSequence() {
            return sequence;
        }
    }

    /** Document element tag name for marshalling out this object to XML */
    public static final String ELEMENT_TAG = "foreignKey"; // NOI18N

    /** Name of attribute used for marshalling out FK column names to XML */
    public static final String FK_COLUMNS_ATTR = "fkColumns"; // NOI18N

    /** Name of attribute used for marshalling out FK name to XML */
    public static final String FK_NAME_ATTR = "name"; // NOI18N

    /** Name of attribute used for marshalling out catalog name of PK table to XML */
    public static final String PK_CATALOG_ATTR = "pkCatalog"; // NOI18N

    /** Name of attribute used for marshalling out PK column names to XML */
    public static final String PK_COLUMNS_ATTR = "pkColumns"; // NOI18N

    /** Name of attribute used for marshalling out deferrability rule to XML */
    public static final String PK_DEFER_ATTR = "deferRule"; // NOI18N

    /** Name of attribute used for marshalling out delete rule to XML */
    public static final String PK_DELETE_ATTR = "deleteRule"; // NOI18N

    /** Name of attribute used for marshalling out PK name to XML */
    public static final String PK_NAME_ATTR = "pkName"; // NOI18N

    /** Name of attribute used for marshalling out schema name of PK table to XML */
    public static final String PK_SCHEMA_ATTR = "pkSchema"; // NOI18N

    /** Name of attribute used for marshalling out PK table name to XML */
    public static final String PK_TABLE_ATTR = "pkTable"; // NOI18N

    /** Name of attribute used for marshalling out update rule to XML */
    public static final String PK_UPDATE_ATTR = "updateRule"; // NOI18N

    /** RCS ID */
    static final String RCS_ID = "$Id$";

    /*
     * deferrability cascade rule; holds constant value as defined in
     * java.sql.DatabaseMetaData
     */
    private int deferrability;

    /* delete cascade rule; holds constant value as defined in java.sql.DatabaseMetaData */
    private int deleteRule;

    /* (optional) DOM element used to construct this instance of ForeignKey */
    private transient Element element;

    /* List of column names for this foreign key in key sequence order. */
    private List fkColumnNames = new ArrayList();

    /* Name of this key; may be null */
    private String fkName;

    /* DBTable to which this PK belongs */
    private DBTable parent;

    /* catalog name, if any, of PK table associated with this FK */
    private String pkCatalog;

    /*
     * List of column names of corresponding primary key columns, in key sequence order.
     */
    private List pkColumnNames = new ArrayList();

    /* Name of corresponding primary key; may be null */
    private String pkName;

    /* schema name, if any, of PK table associated with this FK */
    private String pkSchema;

    /* name of PK table associated with this FK */
    private String pkTable;

    /* update cascade rule; holds constant value as defined in java.sql.DatabaseMetaData */
    private int updateRule;

    /**
     * Creates a new instance of ForeignKey with the given key name and referencing the
     * column names in the given List.
     * 
     * @param fkTable DBTable that owns this FK instance
     * @param foreignKeyName name, if any, of this ForeignKeyImpl
     * @param primaryKeyName name, if any, of PK associated with this ForeignKeyImpl
     * @param primaryKeyTable table owning associated PK
     * @param primaryKeySchema schema containing table which owns associated PK; may be
     *        null
     * @param primaryKeyCatalog catalog containing table which owns associated PK; may be
     *        null
     * @param updateFlag update cascade rule
     * @param deleteFlag delete cascade rule
     * @param deferFlag flag indicating deferrability of application of cascade rules
     */
    public ForeignKeyImpl(DBTable fkTable, String foreignKeyName, String primaryKeyName, String primaryKeyTable, String primaryKeySchema,
            String primaryKeyCatalog, int updateFlag, int deleteFlag, int deferFlag) {
        parent = fkTable;
        fkName = foreignKeyName;
        pkName = primaryKeyName;

        pkTable = primaryKeyTable;
        pkSchema = primaryKeySchema;
        pkCatalog = primaryKeyCatalog;

        updateRule = updateFlag;
        deleteRule = deleteFlag;
        deferrability = deferFlag;
    }

    /**
     * Creates a new instance of ForeignKeyImpl, using the keyElement as a source for
     * reconstituting its contents. Caller must invoke parseXml() after this constructor
     * returns in order to unmarshal and reconstitute the instance object.
     * 
     * @param keyElement DOM element containing XML marshalled version of a ForeignKeyImpl
     *        instance
     */
    public ForeignKeyImpl(Element keyElement) {
        if (keyElement == null) {
            throw new IllegalArgumentException("Must supply non-null org.w3c.dom.Element ref for keyElement.");
        }

        element = keyElement;
    }

    /**
     * Creates a new instance of ForeignKeyImpl, cloning the contents of the given
     * ForeignKey implementation instance.
     * 
     * @param src ForeignKey to be cloned
     */
    public ForeignKeyImpl(ForeignKey src) {
        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null ForeignKey instance for src.");
        }

        copyFrom(src);
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    public Object clone() {
        try {
            ForeignKeyImpl impl = (ForeignKeyImpl) super.clone();
            impl.pkColumnNames = new ArrayList(this.pkColumnNames);
            impl.fkColumnNames = new ArrayList(this.fkColumnNames);

            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#contains
     */
    public boolean contains(DBColumn fkCol) {
        return contains(fkCol.getName());
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#contains(java.lang.String)
     */
    public boolean contains(String fkColumnName) {
        return fkColumnNames.contains(fkColumnName);
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof ForeignKeyImpl)) {
            return false;
        }

        ForeignKeyImpl ref = (ForeignKeyImpl) refObj;

        boolean result = (fkName != null) ? fkName.equals(ref.fkName) : (ref.fkName == null);

        result &= (pkName != null) ? pkName.equals(ref.pkName) : (ref.pkName == null);

        result &= (pkTable != null) ? pkTable.equals(ref.pkTable) : (ref.pkTable == null);

        result &= (pkSchema != null) ? pkSchema.equals(ref.pkSchema) : (ref.pkSchema == null);

        result &= (pkCatalog != null) ? pkCatalog.equals(ref.pkCatalog) : (ref.pkCatalog == null);

        result &= (updateRule == ref.updateRule) && (deleteRule == ref.deleteRule) && (deferrability == ref.deferrability);

        result &= (pkColumnNames != null) ? pkColumnNames.equals(ref.pkColumnNames) : (ref.pkColumnNames != null);

        result &= (fkColumnNames != null) ? fkColumnNames.equals(ref.fkColumnNames) : (ref.fkColumnNames != null);

        return result;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getColumnCount
     */
    public int getColumnCount() {
        return fkColumnNames.size();
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getColumnName
     */
    public String getColumnName(int iColumn) {
        return (String) fkColumnNames.get(iColumn);
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getColumnNames
     */
    public List getColumnNames() {
        return Collections.unmodifiableList(fkColumnNames);
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getDeferrability
     */
    public int getDeferrability() {
        return deferrability;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getDeleteRule
     */
    public int getDeleteRule() {
        return deleteRule;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getMatchingPKColumn
     */
    public String getMatchingPKColumn(String fkColumnName) {
        ListIterator it = fkColumnNames.listIterator();
        while (it.hasNext()) {
            String colName = (String) it.next();
            if (colName.equals(fkColumnName.trim())) {
                return (String) pkColumnNames.get(it.previousIndex());
            }
        }

        return null;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getName
     */
    public String getName() {
        return fkName;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getParent
     */
    public DBTable getParent() {
        return parent;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getPKCatalog
     */
    public String getPKCatalog() {
        return pkCatalog;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getPKColumnNames
     */
    public List getPKColumnNames() {
        return Collections.unmodifiableList(pkColumnNames);
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getPKName
     */
    public String getPKName() {
        return pkName;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getPKSchema
     */
    public String getPKSchema() {
        return pkSchema;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getPKTable
     */
    public String getPKTable() {
        return pkTable;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getSequence
     */
    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return fkColumnNames.indexOf(col.getName().trim());
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#getUpdateRule
     */
    public int getUpdateRule() {
        return updateRule;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = (fkName != null) ? fkName.hashCode() : 0;

        myHash += (pkName != null) ? pkName.hashCode() : 0;
        myHash += (pkTable != null) ? pkTable.hashCode() : 0;
        myHash += (pkSchema != null) ? pkSchema.hashCode() : 0;
        myHash += (pkCatalog != null) ? pkCatalog.hashCode() : 0;

        myHash += updateRule + deleteRule + deferrability;

        myHash += (fkColumnNames != null) ? fkColumnNames.hashCode() : 0;
        myHash += (pkColumnNames != null) ? pkColumnNames.hashCode() : 0;

        return myHash;
    }

    /**
     * Parses the XML content, if any, represented by the DOM element member varaible.
     * 
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    public void parseXML() throws BaseException {
        if (this.element == null) {
            throw new BaseException("No <" + ELEMENT_TAG + "> element found.");
        }

        this.fkName = element.getAttribute(FK_NAME_ATTR);
        this.pkName = element.getAttribute(PK_NAME_ATTR);

        this.pkTable = element.getAttribute(PK_TABLE_ATTR);
        this.pkSchema = element.getAttribute(PK_SCHEMA_ATTR);
        this.pkCatalog = element.getAttribute(PK_CATALOG_ATTR);

        String val = element.getAttribute(PK_UPDATE_ATTR);
        try {
            updateRule = Integer.parseInt(val);
        } catch (Exception e) {
            updateRule = 0;
        }

        val = element.getAttribute(PK_DELETE_ATTR);
        try {
            deleteRule = Integer.parseInt(val);
        } catch (Exception e) {
            deleteRule = 0;
        }

        val = element.getAttribute(PK_DEFER_ATTR);
        try {
            deferrability = Integer.parseInt(val);
        } catch (Exception e) {
            deferrability = 0;
        }

        String pkColNames = element.getAttribute(PK_COLUMNS_ATTR);
        pkColumnNames.addAll(StringUtil.createStringListFrom(pkColNames));

        String fkColNames = element.getAttribute(FK_COLUMNS_ATTR);
        fkColumnNames.addAll(StringUtil.createStringListFrom(fkColNames));
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#references
     */
    public boolean references(DBTable aTable) {
        return (aTable != null) ? references(aTable.getName(), aTable.getSchema(), aTable.getCatalog()) : false;
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#references
     */
    public boolean references(PrimaryKey pk) {
        if (pk == null) {
            return false;
        }

        List targetColNames = pk.getColumnNames();
        DBTable targetTable = pk.getParent();

        return references(targetTable) && targetColNames.containsAll(pkColumnNames) && pkColumnNames.containsAll(targetColNames);
    }

    /**
     * @see org.netbeans.modules.model.database.ForeignKey#references
     */
    public boolean references(String pkTableName, String pkSchemaName, String pkCatalogName) {         
        if(pkCatalogName.equals("")) {
            pkCatalogName = null;
        }
        if(pkSchemaName.equals("")) {
            pkSchemaName = null;
        }    
        if(pkTableName.equals("")) {
            pkTableName = null;
        }          
        
        boolean tableMatches = (pkTableName != null) ? pkTableName.equals(pkTable) : (pkTable == null);

        boolean schemaMatches = (pkSchemaName != null) ? pkSchemaName.equals(pkSchema) : (pkSchema == null);

        boolean catalogMatches = (pkCatalogName != null) ? pkCatalogName.equals(pkCatalog) : (pkCatalog == null);

        return tableMatches && schemaMatches && catalogMatches;
    }

    /**
     * Sets names of columns participating in this ForeignKeyImpl, using the given array
     * of ForeignKeyImpl.Column instances.
     * 
     * @param columns array of ForeignKeyImpl.Columns, each of which contains the name of
     *        the foreign key column, its (one-based) position in a composite key, and its
     *        corresponding primary key column.
     * @see org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl.Column
     */
    public void setColumnNames(Column[] columns) {
        fkColumnNames.clear();
        pkColumnNames.clear();

        if (columns == null) {
            return;
        }

        for (int i = 0; i < columns.length; i++) {
            Column col = columns[i];
            fkColumnNames.add(col.getName());
            pkColumnNames.add(col.getPKColumnName());
        }
    }

    /**
     * Sets names of columns participating in this ForeignKeyImpl, using the given List of
     * ForeignKeyImpl.Column instances.
     * 
     * @param columns List of ForeignKeyImpl.Column instances, each of which contains the
     *        name of the foreign key column, its (one-based) position in a composite key,
     *        and its corresponding primary key column.
     * @see org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl.Column
     */
    public void setColumnNames(List columns) {
        fkColumnNames.clear();
        pkColumnNames.clear();

        if (columns == null) {
            return;
        }

        for (Iterator it = columns.iterator(); it.hasNext();) {
            Column col = (Column) it.next();
            fkColumnNames.add(col.getName());
            pkColumnNames.add(col.getPKColumnName());
        }
    }

    /**
     * Sets names of columns participating in this ForeignKeyImpl, using the given Lists
     * of foreign key column names and corresponding names of primary key columns.
     * 
     * @param fkColumns List of Strings representing the names of columns that are part of
     *        this ForeignKey, in sequential order.
     * @param pkColumns List of Strings representing the names of corresponding primary
     *        key columns, in sequential order.
     * @see org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl.Column
     */
    public void setColumnNames(List fkColumns, List pkColumns) {
        fkColumnNames.clear();
        pkColumnNames.clear();

        if (fkColumns == null || pkColumns == null) {
            return;
        }

        if (fkColumns.size() != pkColumns.size()) {
            throw new IllegalArgumentException("Sizes of fkColumns and pkColumns lists must be identical!");
        }

        for (ListIterator it = fkColumns.listIterator(); it.hasNext();) {
            String myFkName = (String) it.next();
            String myPkName = (String) pkColumns.get(it.previousIndex());

            if (myFkName != null && myPkName != null) {
                fkColumnNames.add(myFkName);
                pkColumnNames.add(myPkName);
            }
        }
    }

    /**
     * Sets reference to DBTable that owns this foreign key.
     * 
     * @param newParent new parent of this foreign key.
     */
    public void setParent(DBTable newParent) {
        parent = newParent;
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * default prefix.
     * 
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString() {
        return toXMLString(null);
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * given prefix String.
     * 
     * @param prefix String used to prefix each new line of the XML output
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buf = new StringBuilder(100);

        buf.append(prefix).append("<").append(ELEMENT_TAG).append(" ");
        if (fkName != null && fkName.trim().length() != 0) {
            buf.append(FK_NAME_ATTR).append("=\"").append(fkName.trim()).append("\" ");
        }

        if (pkName != null && pkName.trim().length() != 0) {
            buf.append(PK_NAME_ATTR).append("=\"").append(pkName.trim()).append("\" ");
        }

        if (pkTable != null && pkTable.trim().length() != 0) {
            buf.append(PK_TABLE_ATTR).append("=\"").append(pkTable.trim()).append("\" ");
        }

        if (pkSchema != null && pkSchema.trim().length() != 0) {
            buf.append(PK_SCHEMA_ATTR).append("=\"").append(pkSchema.trim()).append("\" ");
        }

        if (pkCatalog != null && pkCatalog.trim().length() != 0) {
            buf.append(PK_CATALOG_ATTR).append("=\"").append(pkCatalog.trim()).append("\" ");
        }

        buf.append(PK_UPDATE_ATTR).append("=\"").append(updateRule).append("\" ");

        buf.append(PK_DELETE_ATTR).append("=\"").append(deleteRule).append("\" ");

        buf.append(PK_DEFER_ATTR).append("=\"").append(deferrability).append("\" ");

        if (fkColumnNames.size() != 0) {
            buf.append(FK_COLUMNS_ATTR).append("=\"");
            buf.append(StringUtil.createDelimitedStringFrom(fkColumnNames));
            buf.append("\" ");
        }

        if (pkColumnNames.size() != 0) {
            buf.append(PK_COLUMNS_ATTR).append("=\"");
            buf.append(StringUtil.createDelimitedStringFrom(pkColumnNames));
            buf.append("\" ");
        }

        buf.append("/>\n");

        return buf.toString();
    }

    /*
     * Copies contents of given ForeignKey implementation. @param src ForeignKey whose
     * contents are to be copied
     */
    private void copyFrom(ForeignKey src) {
        parent = src.getParent();

        fkName = src.getName();
        fkColumnNames.clear();
        fkColumnNames.addAll(src.getColumnNames());

        pkName = src.getPKName();
        pkCatalog = src.getPKCatalog();
        pkSchema = src.getPKSchema();
        pkTable = src.getPKTable();
        pkColumnNames.clear();
        pkColumnNames.addAll(src.getPKColumnNames());

        // Set cascade attributes
        updateRule = src.getUpdateRule();
        deleteRule = src.getDeleteRule();
        deferrability = src.getDeferrability();
    }
}

