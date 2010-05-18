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
package org.netbeans.modules.jdbcwizard.builder;

/**
 * Captures metadata information for a database table column.
 * 
 * @author
 */
public class TableColumn {
    private String name = ""; // name of table column

    private String javaType; // Java type - ex. java.lang.String

    private String sqlType; // SQL type - ex. BIGINT, NUMERIC

    private int sqlTypeCode; // SQL type as java.sql.Types enumeration

    private boolean isNullable; // specifies if the column is nullable

    private boolean isSelected; // specifies if the column is selected

    private boolean isPrimaryKey; // specifies if the column is a primary key

    private boolean isForeignKey; // specifies if the column is a foreign key

    private int ordinalPosition = 1; // position of column among others in table

    private int numericPrecision; // numeric precision

    private int numericScale; // numeric scale

    private int numericRadix; // radix (number-base) for numeric values

    private String defaultValue;

    private String value; // added by Neena to hold the value of the column

    // constructors
    /**
     * Constructs an instance of TableColumn.
     */
    public TableColumn() {
        this.name = "";
        this.javaType = "";
        this.sqlType = "";
        this.sqlTypeCode = java.sql.Types.OTHER;

        this.isNullable = false;
        this.isSelected = true;
        this.isPrimaryKey = false;
        this.isForeignKey = false;
    }

    /**
     * Constructs an instance of TableColumn using the given name.
     * 
     * @param newName name of this new instance
     */
    public TableColumn(final String newName) {
        this();
        this.name = newName;
    }

    /**
     * Constructs an instance of TableColumn using the given name and Java type.
     * 
     * @param newName name of new instance
     * @param newJavaType Java type of this new instance
     */
    public TableColumn(final String newName, final String newJavaType) {
        this();
        this.name = newName;
        this.javaType = newJavaType;
    }

    /**
     * Constructs a new instance of TableColumn using the given attributes.
     * 
     * @param newName name of new instance
     * @param newJavaType Java type of new instance
     * @param newIsNullable true if column is nullable, false otherwise
     * @param newIsSelected true if column is selected, false otherwise
     * @param newIsPrimaryKey true if column is PK, false otherwise
     * @param newIsForeignKey true if column if FK, false otherwise
     * @param newSqlTypeCode SQL type code as enumerated in java.sql.Types
     */
    public TableColumn(final String newName, final String newJavaType, final boolean newIsNullable, final boolean newIsSelected,
            final boolean newIsPrimaryKey, final boolean newIsForeignKey, final int newSqlTypeCode) {
        this.name = newName;
        this.javaType = newJavaType;
        this.isNullable = newIsNullable;
        this.isSelected = newIsSelected;
        this.isPrimaryKey = newIsPrimaryKey;
        this.isForeignKey = newIsForeignKey;
        this.sqlTypeCode = newSqlTypeCode;
    }

    public TableColumn(final TableColumn tCol) {
        this.name = tCol.getName();
        this.javaType = tCol.getJavaType();
        this.sqlType = tCol.getSqlType();
        this.sqlTypeCode = tCol.getSqlTypeCode();
        this.isNullable = tCol.getIsNullable();
        this.isSelected = tCol.getIsSelected();
        this.isPrimaryKey = tCol.getIsPrimaryKey();
        this.isForeignKey = tCol.getIsForeignKey();
        this.ordinalPosition = tCol.getOrdinalPosition();
        this.numericPrecision = tCol.getNumericPrecision();
        this.numericScale = tCol.getNumericScale();
        this.numericRadix = tCol.getNumericRadix();
        this.defaultValue = tCol.getDefaultValue();
        this.value = tCol.getValue();
    }

    // getters
    /**
     * Gets name of column.
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets Java type.
     * 
     * @return Java type
     */
    public String getJavaType() {
        return this.javaType;
    }

    /**
     * Gets JDBC SQL type.
     * 
     * @return JDBC SQL type (as String)
     */
    public String getSqlType() {
        return this.sqlType;
    }

    /**
     * Indicates whether column is nullable
     * 
     * @return true if nullable, false otherwise
     */
    public boolean getIsNullable() {
        return this.isNullable;
    }

    /**
     * Indicates whether column is selected
     * 
     * @return true if selected, false otherwise
     */
    public boolean getIsSelected() {
        return this.isSelected;
    }

    /**
     * Indicates whether column is primary key
     * 
     * @return true if PK, false otherwise
     */
    public boolean getIsPrimaryKey() {
        return this.isPrimaryKey;
    }

    /**
     * Indicates whether column is foreign key
     * 
     * @return true if FK, false otherwise
     */
    public boolean getIsForeignKey() {
        return this.isForeignKey;
    }

    /**
     * Gets ordinal position of column among siblings in its table.
     * 
     * @return ordinal position
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * Gets precision (for numeric types) or maximum number of characters (for char or date types).
     * 
     * @return numeric precision or maximum number of characters, as determined by column type
     */
    public int getNumericPrecision() {
        return this.numericPrecision;
    }

    /**
     * Gets numeric scale of this column. Meaningful only for numeric columns.
     * 
     * @return numeric scale of column; meaningless for non-numeric columns
     */
    public int getNumericScale() {
        return this.numericScale;
    }

    /**
     * Gets radix (number base) of this column, e.g., 10 (decimal), 2 (binary). Meaningful only for
     * numeric columns.
     * 
     * @return numeric radix of column; meaningless for non-numeric columns
     */
    public int getNumericRadix() {
        return this.numericRadix;
    }

    /**
     * Gets SQL type code.
     * 
     * @param newCode SQL code
     */
    public int getSqlTypeCode() {
        return this.sqlTypeCode;
    }

    /**
     * Gets String representation of default value for this column (may be null).
     * 
     * @return default value, or null if none was defined
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Gets String representation of value for this column (may be null).
     * 
     * @return value, or null if none was defined
     */
    public String getValue() {
        return this.value;
    }

    // setters

    /**
     * Sets column name.
     * 
     * @param newName new name of column
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Sets Java type of column.
     * 
     * @param newJavaType new Java type of column
     */
    public void setJavaType(final String newJavaType) {
        this.javaType = newJavaType;
    }

    /**
     * Sets JDBC SQL type of column
     * 
     * @param newSqlType new JDBC SQL type (expressed as String representation)
     */
    public void setSqlType(final String newSqlType) {
        this.sqlType = newSqlType;
    }

    /**
     * Sets nullability of column
     * 
     * @param newIsNullable true if nullable; false otherwise
     */
    public void setIsNullable(final boolean newIsNullable) {
        this.isNullable = newIsNullable;
    }

    /**
     * Sets selection state of column.
     * 
     * @param newIsSelected true if selected; false otherwise
     */
    public void setIsSelected(final boolean newIsSelected) {
        this.isSelected = newIsSelected;
    }

    /**
     * Sets whether column is a primary key. Note that multiple columns may participate in a
     * composite PK.
     * 
     * @param newIsPrimaryKey true if PK; false otherwise
     */
    public void setIsPrimaryKey(final boolean newIsPrimaryKey) {
        this.isPrimaryKey = newIsPrimaryKey;
    }

    /**
     * Sets whether column is a foreign key. Note that multiple columns may participate in a
     * composite FK.
     * 
     * @param newIsForeignKey true if FK; false otherwise
     */
    public void setIsForeignKey(final boolean newIsForeignKey) {
        this.isForeignKey = newIsForeignKey;
    }

    /**
     * Sets ordinal position of column.
     * 
     * @param newPosition new ordinal position of column
     */
    public void setOrdinalPosition(final int newPosition) {
        if (newPosition <= 0) {
            throw new IllegalArgumentException("Must supply positive integer value for newPosition.");
        }
        this.ordinalPosition = newPosition;
    }

    /**
     * Sets numeric precision or maximum character width of column.
     * 
     * @param newNumericPrecision new precision of column
     */
    public void setNumericPrecision(final int newNumericPrecision) {
        this.numericPrecision = newNumericPrecision;
    }

    /**
     * Sets numeric scale of column.
     * 
     * @param newNumericScale new scale of column
     */
    public void setNumericScale(final int newNumericScale) {
        this.numericScale = newNumericScale;
    }

    /**
     * Sets numeric radix of column.
     * 
     * @param newNumericRadix new radix of column
     */
    public void setNumericRadix(final int newNumericRadix) {
        this.numericRadix = newNumericRadix;
    }

    /**
     * Sets default value, if any, of column.
     * 
     * @param newDefault default value; set to null to indicate that no default exists.
     */
    public void setDefaultValue(final String newDefault) {
        this.defaultValue = newDefault;
    }

    /**
     * Sets value, if any, of column.
     * 
     * @param newDefault default value; set to null to indicate that no default exists.
     */
    public void setValue(final String newValue) {
        this.value = newValue;
    }

    /**
     * Sets SQL type code.
     * 
     * @param newCode SQL code
     */
    public void setSqlTypeCode(final int newCode) {
        this.sqlTypeCode = newCode;
    }
}
