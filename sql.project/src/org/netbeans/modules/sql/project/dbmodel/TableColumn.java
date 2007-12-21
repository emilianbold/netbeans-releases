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
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;


/**
 * Captures metadata information for a database table column.
 * 
 * @author Susan Chen
 * @version 
 */
public class TableColumn {
    private String name = "";           // name of table column
    private String javaType;            // Java type - ex. java.lang.String
    private String sqlType;             // SQL type - ex. BIGINT, NUMERIC
    private int sqlTypeCode;            // SQL type as java.sql.Types enumeration

    private boolean isNullable;         // specifies if the column is nullable
    private boolean isSelected;         // specifies if the column is selected
    private boolean isPrimaryKey;       // specifies if the column is a primary key
    private boolean isForeignKey;       // specifies if the column is a foreign key

    private int ordinalPosition = 1;        // position of column among others in table
    private int numericPrecision;       // numeric precision
    private int numericScale;           // numeric scale
    private int numericRadix;           // radix (number-base) for numeric values

    private String defaultValue;
    private String value; // added by Neena to hold the value of the column
    
    // constructors
    /**
     * Constructs an instance of TableColumn.
     */
    public TableColumn() {
        name = "";
        javaType = "";
        sqlType = "";
        sqlTypeCode = java.sql.Types.OTHER;

        isNullable = false;
        isSelected = true;
        isPrimaryKey = false;
        isForeignKey = false;
    }

    /**
     * Constructs an instance of TableColumn using the given name.
     *
     * @param newName name of this new instance
     */
    public TableColumn(String newName) {
        this();
        name = newName;
    }

    /**
     * Constructs an instance of TableColumn using the given name and
     * Java type.
     *
     * @param newName name of new instance
     * @param newJavaType Java type of this new instance
     */    
    public TableColumn(String newName, String newJavaType) {
        this();
        name = newName;
        javaType = newJavaType;
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
    public TableColumn(String newName, String newJavaType, boolean newIsNullable, 
                       boolean newIsSelected, boolean newIsPrimaryKey, 
                       boolean newIsForeignKey, int newSqlTypeCode) {
        name = newName;
        javaType = newJavaType;
        isNullable = newIsNullable;
        isSelected = newIsSelected;
        isPrimaryKey = newIsPrimaryKey;
        isForeignKey = newIsForeignKey;
        sqlTypeCode = newSqlTypeCode;
    }

    public TableColumn(TableColumn tCol) {
        name = tCol.getName();
        javaType = tCol.getJavaType();
        sqlType = tCol.getSqlType();
        sqlTypeCode = tCol.getSqlTypeCode();
        isNullable = tCol.getIsNullable();
        isSelected = tCol.getIsSelected();
        isPrimaryKey = tCol.getIsPrimaryKey();
        isForeignKey = tCol.getIsForeignKey();
        ordinalPosition = tCol.getOrdinalPosition();
        numericPrecision = tCol.getNumericPrecision();
        numericScale = tCol.getNumericScale();
        numericRadix = tCol.getNumericRadix();
        defaultValue = tCol.getDefaultValue();
        value = tCol.getValue();
    }
    // getters
    /**
     * Gets name of column.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Gets Java type.
     *
     * @return Java type
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * Gets JDBC SQL type.
     *
     * @return JDBC SQL type (as String)
     */
    public String getSqlType() {
        return sqlType;
    }
    
    /**
     * Indicates whether column is nullable
     *
     * @return true if nullable, false otherwise
     */
    public boolean getIsNullable() {
        return isNullable;
    }
    
    /**
     * Indicates whether column is selected
     *
     * @return true if selected, false otherwise
     */
    public boolean getIsSelected() {
        return isSelected;
    }

    /**
     * Indicates whether column is primary key
     *
     * @return true if PK, false otherwise
     */
    public boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }
    

    /**
     * Indicates whether column is foreign key
     *
     * @return true if FK, false otherwise
     */
    public boolean getIsForeignKey() {
        return isForeignKey;
    }

    /**
     * Gets ordinal position of column among siblings in its table.
     *
     * @return ordinal position
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }
    
    /**
     * Gets precision (for numeric types) or maximum number of characters
     * (for char or date types).
     *
     * @return numeric precision or maximum number of characters, as determined
     * by column type
     */
    public int getNumericPrecision() {
        return numericPrecision;
    }

    /**
     * Gets numeric scale of this column.  Meaningful only for numeric columns.
     *
     * @return numeric scale of column; meaningless for non-numeric columns
     */    
    public int getNumericScale() {
        return numericScale;
    }
    
    /**
     * Gets radix (number base) of this column, e.g., 10 (decimal), 2 (binary).
     * Meaningful only for numeric columns.
     *
     * @return numeric radix of column; meaningless for non-numeric columns
     */    
    public int getNumericRadix() {
        return numericRadix;
    }

    /**
     * Gets SQL type code.
     *
     * @param newCode SQL code
     */
    public int getSqlTypeCode() {
        return sqlTypeCode;
    }

    /**
     * Gets String representation of default value for this column (may be 
     * null).
     *
     * @return default value, or null if none was defined
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets String representation of  value for this column (may be
     * null).
     *
     * @return  value, or null if none was defined
     */
    public String getValue() {
        return value;
    }

    // setters

    /**
     * Sets column name.
     *
     * @param newName new name of column
     */
    public void setName(String newName) {
        name = newName;
    }
    
    /**
     * Sets Java type of column.
     *
     * @param newJavaType new Java type of column
     */
    public void setJavaType(String newJavaType) {
        javaType = newJavaType;
    }
    
    /**
     * Sets JDBC SQL type of column
     * 
     * @param newSqlType new JDBC SQL type (expressed as String representation)
     */
    public void setSqlType(String newSqlType) {
        sqlType = newSqlType;
    }

    /**
     * Sets nullability of column
     *
     * @param newIsNullable true if nullable; false otherwise
     */    
    public void setIsNullable(boolean newIsNullable) {
        isNullable = newIsNullable;
    }
    
    /**
     * Sets selection state of column.
     *
     * @param newIsSelected true if selected; false otherwise
     */    
    public void setIsSelected(boolean newIsSelected) {
        isSelected = newIsSelected;
    }
    
    /**
     * Sets whether column is a primary key.  Note that multiple columns
     * may participate in a composite PK.
     *
     * @param newIsPrimaryKey true if PK; false otherwise
     */    
    public void setIsPrimaryKey(boolean newIsPrimaryKey) {
        isPrimaryKey = newIsPrimaryKey;
    }
    
    /**
     * Sets whether column is a foreign key.  Note that multiple columns
     * may participate in a composite FK.
     *
     * @param newIsForeignKey true if FK; false otherwise
     */    
    public void setIsForeignKey(boolean newIsForeignKey) {
        isForeignKey = newIsForeignKey;
    }

    /**
     * Sets ordinal position of column.
     *
     * @param newPosition new ordinal position of column
     */    
    public void setOrdinalPosition(int newPosition) {
        if (newPosition <= 0) {
            throw new IllegalArgumentException(
                "Must supply positive integer value for newPosition.");
        }
        ordinalPosition = newPosition;
    }

    /**
     * Sets numeric precision or maximum character width of column.
     *
     * @param newNumericPrecision new precision of column
     */    
    public void setNumericPrecision(int newNumericPrecision) {
        numericPrecision = newNumericPrecision;
    }

    /**
     * Sets numeric scale of column.
     *
     * @param newNumericScale new scale of column
     */     
    public void setNumericScale(int newNumericScale) {
        numericScale = newNumericScale;
    }

    /**
     * Sets numeric radix of column.
     *
     * @param newNumericRadix new radix of column
     */
    public void setNumericRadix(int newNumericRadix) {
        numericRadix = newNumericRadix;
    }

    /**
     * Sets default value, if any, of column.
     *
     * @param newDefault default value; set to null to indicate that no
     * default exists.
     */
    public void setDefaultValue(String newDefault) {
        defaultValue = newDefault;
    }

    /**
     * Sets value, if any, of column.
     *
     * @param newDefault default value; set to null to indicate that no
     * default exists.
     */
    public void setValue(String newValue) {
        value = newValue;
    }

    /**
     * Sets SQL type code.
     *
     * @param newCode SQL code
     */
    public void setSqlTypeCode(int newCode) {
        sqlTypeCode = newCode;
    }
}
