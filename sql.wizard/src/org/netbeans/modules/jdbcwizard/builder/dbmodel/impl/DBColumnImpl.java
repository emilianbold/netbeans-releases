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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

import java.util.logging.Logger;

import org.netbeans.modules.jdbcwizard.builder.DBMetaData;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;

/**
 * Interface describing column metadata for JDBC data sources providing information in a database or
 * database-like format.
 * 
 * @author
 */
public class DBColumnImpl implements DBColumn, Cloneable, Comparable {


    /** String constant for unknown SQL type */
    public static final String UNKNOWN_TYPE = "unknown";

    /** Constant for this jdbc type not being assigned. */
    public static final int NOT_ASSIGNED = -562471;

    /* Log4J category name */
    private static final String LOG_CATEGORY = DBColumnImpl.class.getName();

    public void setName(final String name) {
        this.name = name;
    }

    /** name of column */
    protected String name;

    /** whether this column is part of a primary key */
    protected boolean pkFlag;

    /** whether this column is part of a foreign key */
    protected boolean fkFlag;

    /** whether this column is indexed */
    protected boolean indexed;

    /** whether this column can accept null as a valid value */
    protected boolean nullable;

    /** editable */
    protected boolean editable = true;
    
    /** insert editable */
    protected boolean insertEditable = true;

    /** selected */
    protected boolean insertSelected = true;

    protected boolean updateSelected = true;

    protected boolean chooseSelected = true;

    protected boolean pollSelected = true;

    /** JDBC SQL type, as enumerated in java.sql.Types */
    protected int jdbcType;

    /** column precision (for numeric types) / width (for char types) */
    protected int precision;

    /** column scale (meaningful only for numeric types) */
    protected int scale;

    /* DBTable to which this PK belongs */
    protected DBTable parent;

    /* Java name of this column */
    protected String javaName;

    /* Java type for this column */
    protected String javaType;

    /* Default Value */
    protected String defaultValue;

    /* Cardinal Position */
    protected int cardinalPosition;

    /**
     * @param sqlType
     */
    public void setSqlType(final String sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * @return
     */
    public String getSqlType() {
        return this.sqlType;
    }

    protected String sqlType;

    private static final Logger mLogger = Logger.getLogger(DBColumnImpl.LOG_CATEGORY);

    /**
     * @param colName
     * @param sqlJdbcType
     * @param colScale
     * @param colPrecision
     * @param isNullable
     */
    public DBColumnImpl(final String colName, final int sqlJdbcType, final int colScale, final int colPrecision, final boolean isNullable) {
        this.name = colName;
        this.jdbcType = sqlJdbcType;
        this.precision = colPrecision;
        this.scale = colScale;
        this.nullable = isNullable;
    }

    /**
     * @param colName
     * @param sqlJdbcType
     * @param colScale
     * @param colPrecision
     * @param isPrimaryKey
     * @param isForeignKey
     * @param isIndexed
     * @param isNullable
     */
    public DBColumnImpl(final String colName, final int sqlJdbcType, final int colScale, final int colPrecision, final boolean isPrimaryKey,
            final boolean isForeignKey, final boolean isIndexed, final boolean isNullable) {
        this(colName, sqlJdbcType, colScale, colPrecision, isNullable);

        this.pkFlag = isPrimaryKey;
        this.fkFlag = isForeignKey;
        this.indexed = isIndexed;
    }

    /**
     * Creates a new instance of DBColumnImpl, cloning the contents of the given DBColumn
     * implementation instance.
     * 
     * @param src DBColumn instance to be cloned
     */
    public DBColumnImpl() {
    }

    /**
     * @see com.stc.model.database.DBColumn#getName
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.stc.model.database.DBColumn#isPrimaryKey
     */
    public boolean isPrimaryKey() {
        return this.pkFlag;
    }

    /**
     * @see com.stc.model.database.DBColumn#isForeignKey
     */
    public boolean isForeignKey() {
        return this.fkFlag;
    }

    /**
     * @see com.stc.model.database.DBColumn#isIndexed
     */
    public boolean isIndexed() {
        return this.indexed;
    }

    /**
     * @see com.stc.model.database.DBColumn#isNullable
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * @see com.stc.model.database.DBColumn#getParent
     */
    public DBTable getParent() {
        return this.parent;
    }

    /**
     * @see com.stc.model.database.DBColumn#getJdbcType
     */
    public int getJdbcType() {
        return this.jdbcType;
    }

    /**
     * @see com.stc.model.database.DBColumn#getJdbcTypeString
     */
    public String getJdbcTypeString() {
        return DBMetaData.getSQLTypeDescription(this.jdbcType);
    }

    /**
     * @see com.stc.model.database.DBColumn#getScale
     */
    public int getScale() {
        return this.scale;
    }

    /**
     * @see com.stc.model.database.DBColumn#getPrecision
     */
    public int getPrecision() {
        return this.precision;
    }

    /**
     * Indicates whether this DBColumn references the given DBColumn in a FK -> PK relationship.
     * 
     * @param column PK whose relationship to this column is to be checked
     * @return true if this column is a FK reference to column; false otherwise
     */
    // public boolean references(DBColumn column);
    /**
     * Indicates whether this DBColumn is referenced by the given DBColumn in a FK -> PK
     * relationship.
     * 
     * @param column potential FK reference to be checked
     * @return true if column is referenced as a PK by the given column, false otherwise
     */
    // public boolean isReferencedBy(DBColumn column);
    /*
     * Setters and non-API helper methods.
     */
    public void setPrecision(final int newPrec) {
        this.precision = newPrec;
    }

    public void setScale(final int newScale) {
        this.scale = newScale;
    }

    public void setJdbcType(final int newType) {
        this.jdbcType = newType;
    }

    public void setPrimaryKey(final boolean newFlag) {
        this.pkFlag = newFlag;
    }

    public void setForeignKey(final boolean newFlag) {
        this.fkFlag = newFlag;
    }

    public void setIndexed(final boolean newFlag) {
        this.indexed = newFlag;
    }

    public void setNullable(final boolean newFlag) {
        this.nullable = newFlag;
    }

    public void setParent(final DBTable newParent) {
        this.parent = newParent;
    }

    /**
     * Clone a deep copy of DBColumn.
     * 
     * @return a copy of DBColumn.
     */
    public Object clone() {
        try {
            final DBColumnImpl column = (DBColumnImpl) super.clone();

            return column;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(final Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBColumn)) {
            return false;
        }

        final DBColumnImpl refMeta = (DBColumnImpl) refObj;

        boolean result = this.name != null ? this.name.equals(refMeta.name) : refMeta.name == null;

        result &= this.jdbcType == refMeta.jdbcType && this.pkFlag == refMeta.pkFlag && this.fkFlag == refMeta.fkFlag
                && this.indexed == refMeta.indexed && this.nullable == refMeta.nullable && this.scale == refMeta.scale
                && this.precision == refMeta.precision;

        result &= this.parent != null ? this.parent.equals(refMeta.parent) : refMeta.parent == null;

        return result;
    }

    /**
     * Returns the hashCode for this object.
     * 
     * @return the hashCode of this object.
     */
    public int hashCode() {
        int myHash = this.name != null ? this.name.hashCode() : 0;
        myHash += this.jdbcType + 10 * this.scale + 100 * this.precision;

        myHash += this.pkFlag ? 1 : 0;
        myHash += this.fkFlag ? 2 : 0;
        myHash += this.indexed ? 4 : 0;
        myHash += this.nullable ? 8 : 0;

        myHash += this.parent != null ? this.parent.hashCode() : 0;

        return myHash;
    }

    /**
     * Compares DBColumn with another object for lexicographical ordering. Null objects and those
     * DBColumns with null names are placed at the end of any ordered collection using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name is the
     *         same. 1 if the column name is greater than obj to be compared.
     */
    public int compareTo(final Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        final String refName = ((DBColumn) refObj).getName();
        return this.name != null ? this.name.compareTo(refName) : refName != null ? 1 : -1;
    }

    /*
     * Sets the various member variables and collections using the given DBColumn instance as a
     * source object. @param source DBColumn from which to obtain values for member variables and
     * collections
     */
    public void copyFrom(final DBColumn source) {
        this.name = source.getName();
        this.jdbcType = source.getJdbcType();

        this.scale = source.getScale();
        this.precision = source.getPrecision();

        this.pkFlag = source.isPrimaryKey();
        this.fkFlag = source.isForeignKey();
        this.indexed = source.isIndexed();
        this.nullable = source.isNullable();

        this.parent = source.getParent();
    }

    /**
     * Gets Java name for this table.
     * 
     * @return normalized Java name for this table
     */
    public String getJavaName() {
        return this.javaName != null ? this.javaName : this.name;
    }

    /**
     * Sets Java name for this table.
     * 
     * @param newName new normalized Java name for this table, or null if original name is to be
     *            used.
     */
    public void setJavaName(final String newName) {
        this.javaName = newName;
    }

    /**
     * Gets Java type for this table.
     * 
     * @return normalized Java type for this table
     */
    public String getJavaType() {
        return this.javaType;
    }

    /**
     * Sets Java type for this table.
     * 
     * @param newType new normalized Java name for this table
     */
    public void setJavaType(final String newType) {
        this.javaType = newType;
    }

    /**
     * Gets the default value
     * 
     * @return defaultValue
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Gets the Ordinal Position
     * 
     * @param cardinalPosition to be used
     */
    public int getOrdinalPosition() {
        return this.cardinalPosition;
    }

    /**
     * sets the default value
     * 
     * @param defaultValue to be set
     */
    public void setDefaultValue(final String defValue) {
        this.defaultValue = defValue;
    }

    /**
     * Gets the Ordinal Position
     * 
     * @param cardinalPosition to be used
     */
    public void setOrdinalPosition(final int cardinalPos) {
        this.cardinalPosition = cardinalPos;
    }

    /**
     * Gets the status of selection of the column
     * 
     * @return seleted
     */
    public boolean isInsertSelected() {
        return this.insertSelected;
    }

    /**
     * Gets the status of selection of the column
     * 
     * @return seleted
     */
    public boolean isUpdateSelected() {
        return this.updateSelected;
    }

    /**
     * Gets the status of selection of the column
     * 
     * @return seleted
     */
    public boolean isChooseSelected() {
        return this.chooseSelected;
    }

    /**
     * Gets the status of selection of the column
     * 
     * @return seleted
     */
    public boolean isPollSelected() {
        return this.pollSelected;
    }

    public boolean isSelected() {
        return this.insertSelected && this.updateSelected && this.chooseSelected && this.pollSelected;
    }

    /**
     * Gets the status of editing
     * 
     * @return editable
     */
    public boolean isEditable() {
        return this.editable;
    }
    
    /**
     * Gets the status of Insert editing
     * 
     * @return InsertEditable
     */
    public boolean isInsertEditable() {
        return this.insertEditable;
    }

    /**
     * Sets status of editing the column table
     * 
     * @param the editing status is from now that of cedit
     */

    public void setEditable(final boolean cedit) {
        this.editable = cedit;
    }
    
    /**
     * Sets status of editing the column table at Insert tab
     * 
     * @param the editing status is from now that of cedit
     */

    public void setInsertEditable(final boolean cedit) {
        this.insertEditable = cedit;
    }

    /**
     * Sets status of selection of the column table
     * 
     * @param the selection status is set from now on to that of cselect
     */

    public void setInsertSelected(final boolean cselect) {
        this.insertSelected = cselect;
    }

    /**
     * Sets status of selection of the column table
     * 
     * @param the selection status is set from now on to that of cselect
     */

    public void setUpdateSelected(final boolean cselect) {
        this.updateSelected = cselect;
    }

    /**
     * Sets status of selection of the column table
     * 
     * @param the selection status is set from now on to that of cselect
     */

    public void setChooseSelected(final boolean cselect) {
        this.chooseSelected = cselect;
    }

    /**
     * Sets status of selection of the column table
     * 
     * @param the selection status is set from now on to that of cselect
     */

    public void setPollSelected(final boolean cselect) {
        this.pollSelected = cselect;
    }

    public void setSelected(final boolean cselect) {
        this.setInsertSelected(cselect);
        this.setUpdateSelected(cselect);
        this.setChooseSelected(cselect);
        this.setPollSelected(cselect);
    }

}
