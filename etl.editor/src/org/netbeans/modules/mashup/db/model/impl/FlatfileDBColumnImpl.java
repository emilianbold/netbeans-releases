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
package org.netbeans.modules.mashup.db.model.impl;

import java.sql.Types;
import java.util.Map;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.w3c.dom.Element;

import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Implements FlatfileDBColumn interface.
 * 
 * @author Jonathan Giron
 * @author Girish Patil
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileDBColumnImpl implements FlatfileDBColumn, Cloneable, Comparable {
    /** Constants used in XML tags * */
    private static final String ATTR_CARDINAL_POSITION = "cardinalPosition";
    private static final String ATTR_INDEXED = "indexed";
    private static final String ATTR_IS_FOREIGN_KEY = "isForeignKey";
    private static final String ATTR_IS_PRIMARY_KEY = "isPrimaryKey";
    private static final String ATTR_JDBC_TYPE = "jdbcType";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NULLABLE = "nullable";
    private static final String ATTR_PARENT = "parent";
    private static final String ATTR_PRECISION = "precision";
    private static final String ATTR_SCALE = "scale";
    private static final String END_QUOTE_SPACE = "\" ";
    private static final String EQUAL_START_QUOTE = "=\"";
    private static final String LOG_CATEGORY = FlatfileDBColumnImpl.class.getName();
    private static final String QUOTE = "\"";

    private static final String TAG_STCDB_COLUMN = "stcdbColumn";

    /* Cardinal Position */
    protected int cardinalPosition;

    /* Default Value */
    protected String defaultValue;

    /** whether this column is part of a foreign key */
    protected boolean fkFlag;

    /** whether this column is indexed */
    protected boolean indexed;

    /** JDBC SQL type, as enumerated in java.sql.Types */
    protected int jdbcType;

    /** name of column */
    protected String name;

    /** whether this column can accept null as a valid value */
    protected boolean nullable;

    /** DBTable to which this PK belongs */
    protected FlatfileDBTable parent;

    /** whether this column is part of a primary key */
    protected boolean pkFlag;

    /** column precision (for numeric types) / width (for char types) */
    protected int precision;

    /** column scale (meaningful only for numeric types) */
    protected int scale;

    private boolean isSelected; // specifies if the column is selected

    public FlatfileDBColumnImpl() {
    }

    /**
     * Creates a new instance of FlatfileDBColumnImpl, cloning the contents of the given
     * DBColumn implementation instance.
     * 
     * @param src DBColumn instance to be cloned
     */
    public FlatfileDBColumnImpl(DBColumn src) {
        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBColumn instance for src.");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of FlatfileDBColumnImpl using the given parameters and
     * assuming that the column is not part of a foreign key or primary key, and that it
     * accepts null values.
     * 
     * @param colName name of this column
     * @param sqlJdbcType JDBC type of this column
     * @param colScale scale of this column
     * @param colPrecision precision of this column
     * @param isNullable true if nullable, false otherwise
     * @see java.sql.Types
     */
    public FlatfileDBColumnImpl(String colName, int sqlJdbcType, int colPrecision, int colScale, boolean isNullable) {
        name = colName;
        jdbcType = sqlJdbcType;

        precision = colPrecision;
        scale = colScale;

        nullable = isNullable;
    }

    /**
     * Constructs a new instance of FlatfileDBColumnImpl using the given parameters.
     * 
     * @param colName name of this column
     * @param sqlJdbcType JDBC type of this column
     * @param colScale scale of this column
     * @param colPrecision precision of this column
     * @param isPrimaryKey true if part of a primary key, false otherwise
     * @param isForeignKey true if part of a foreign key, false otherwise
     * @param isIndexed true if indexed, false otherwise
     * @param isNullable true if nullable, false otherwise
     * @see java.sql.Types
     */
    public FlatfileDBColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        this(colName, sqlJdbcType, colPrecision, colScale, isNullable);

        pkFlag = isPrimaryKey;
        fkFlag = isForeignKey;
        indexed = isIndexed;
    }

    /**
     * Clone a deep copy of DBColumn.
     * 
     * @return a copy of DBColumn.
     */
    public Object clone() {
        try {
            FlatfileDBColumn column = (FlatfileDBColumn) super.clone();

            return column;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Compares DBColumn with another object for lexicographical ordering. Null objects
     * and those DBColumns with null names are placed at the end of any ordered collection
     * using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if this column is before refObj 0 if this column and refObj are in the
     *         same position. 1 if this column name is after refObj
     * @throws ClassCastException if refObj is not comparable to FlatfileDBColumnImpl
     */
    public int compareTo(Object refObj) {
        if (null == refObj) {
            return -1;
        } else if (this == refObj) {
            return 0;
        }

        FlatfileDBColumnImpl col = (FlatfileDBColumnImpl) refObj;
        return cardinalPosition - col.cardinalPosition;
    }

    /**
     * Sets the various member variables and collections using the given DBColumn instance
     * as a source object.
     * 
     * @param source DBColumn from which to obtain values for member variables and
     *        collections
     */
    public void copyFrom(DBColumn source) {
        name = source.getName();
        jdbcType = source.getJdbcType();

        scale = source.getScale();
        precision = source.getPrecision();

        pkFlag = source.isPrimaryKey();
        fkFlag = source.isForeignKey();
        indexed = source.isIndexed();
        nullable = source.isNullable();

        cardinalPosition = source.getOrdinalPosition();
        defaultValue = source.getDefaultValue();

        try {
			parent = (FlatfileDBTable) source.getParent();
		} catch (RuntimeException e) {
			// TODO log this
		}
    }

    /**
     * Gets debug output as a String, using the given String as a prefix for each output
     * line.
     * 
     * @param prefix String to prepend to each new line of debug output
     * @return debug output
     */
    public String debugOutput(String prefix) {
        StringBuilder out = new StringBuilder(100);

        out.append(prefix).append("Column name: " + getName() + "\n");
        out.append(prefix).append("Position: " + getOrdinalPosition() + "\n");
        out.append(prefix).append("JDBC type: " + getJdbcType() + "\n");
        out.append(prefix).append("Precision/width: " + getPrecision() + "\n");
        out.append(prefix).append("Scale: " + getScale() + "\n");
        out.append(prefix).append("Nullable? " + isNullable() + "\n");
        out.append(prefix).append("Indexed? " + isIndexed() + "\n");
        out.append(prefix).append("Selected? " + isSelected() + "\n");

        return out.toString();
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

        if (!(refObj instanceof FlatfileDBColumn)) {
            return false;
        }

        FlatfileDBColumnImpl refMeta = (FlatfileDBColumnImpl) refObj;

        boolean result = (name != null) ? name.equals(refMeta.name) : (refMeta.name == null);

        result &= (jdbcType == refMeta.jdbcType) && (pkFlag == refMeta.pkFlag) && (fkFlag == refMeta.fkFlag) && (indexed == refMeta.indexed)
            && (nullable == refMeta.nullable) && (getScale() == refMeta.getScale()) && (precision == refMeta.precision);

        result &= (parent != null) ? parent.equals(refMeta.parent) : (refMeta.parent == null);

        return result;
    }

    public int getCardinalPosition() {
        return this.cardinalPosition;
    }

    /**
     * Gets the SQL create statement to create a column representing this flatfile field.
     * 
     * @return SQL statement fragment to create of a column representing this field
     */
    public String getCreateStatementSQL() {
        // Default value if jdbcType is unrecognized.
        int sqlTypeCode = java.sql.Types.VARCHAR;

        if (SQLUtils.isStdJdbcType(this.jdbcType)) {
            // Map existing integer and float datatypes to numeric
            // because AxionDB does not support precision or scale
            // specifications for those datatypes.
            switch (jdbcType) {
                case java.sql.Types.FLOAT:
                case java.sql.Types.INTEGER:
                    sqlTypeCode = Types.NUMERIC;
                    break;

                case java.sql.Types.CHAR:
                    sqlTypeCode = Types.VARCHAR;
                    break;

                default:
                    sqlTypeCode = this.jdbcType;
                    break;
            }
        }

        StringBuilder buffer = new StringBuilder(100);
        buffer.append("\"").append(this.name).append(END_QUOTE_SPACE);
        buffer.append(SQLUtils.getStdSqlType(sqlTypeCode));

        if (this.precision > 0) {
            switch (sqlTypeCode) {
                case java.sql.Types.VARCHAR:
                    buffer.append("(").append(this.precision).append(")");
                    break;

                case java.sql.Types.NUMERIC:
                    int extraLen = ((getScale() == 0) ? 0 : 1) + 1;
                    buffer.append("(").append(this.precision - extraLen);
                    if (getScale() > 0) {
                        buffer.append(", ").append(getScale());
                    }
                    buffer.append(")");
                    break;

                default:
                    // Append nothing.
                    break;
            }
        }

        if (defaultValue != null) {
            buffer.append(" DEFAULT ");
            switch (sqlTypeCode) {
                case java.sql.Types.VARCHAR:
                case java.sql.Types.DATE:
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    buffer.append("'").append(defaultValue).append("'");
                    break;

                default:
                    if (!StringUtil.isNullString(defaultValue)) {
                        buffer.append(defaultValue);
                    }
                    break;
            }
        }

        if (!isNullable() && !isPrimaryKey()) {
            buffer.append(" NOT NULL");
        }

        return buffer.toString();
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
     * @see org.netbeans.modules.model.database.DBColumn#getJdbcType
     */
    public int getJdbcType() {
        return jdbcType;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#getJdbcTypeString
     */
    public String getJdbcTypeString() {
        return SQLUtils.getStdSqlType(jdbcType);
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#getName
     */
    public String getName() {
        return this.name;
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
     * @see org.netbeans.modules.model.database.DBColumn#getParent
     */
    public DBTable getParent() {
        return parent;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#getPrecision
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Gets Map of current properties associated with this field.
     * 
     * @return unmodifiable Map of current properties.
     */
    public Map getProperties() {
        return ((FlatfileDBTable) this.getParent()).getProperties();
    }

    /**
     * Gets property string associated with the given name.
     * 
     * @param propName property key
     * @return property associated with propName, or null if no such property exists.
     */
    public String getProperty(String propName) {
        return ((FlatfileDBTable) this.getParent()).getProperty(propName);
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#getScale
     */
    public int getScale() {
        switch (jdbcType) {
            case Types.NUMERIC:
                return scale;

            default:
                return 0;
        }
    }

    /**
     * Returns the hashCode for this object.
     * 
     * @return the hashCode of this object.
     */
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;
        myHash += jdbcType + (10 * getScale()) + (100 * precision);

        myHash += pkFlag ? 1 : 0;
        myHash += fkFlag ? 2 : 0;
        myHash += indexed ? 4 : 0;
        myHash += nullable ? 8 : 0;

        myHash += (parent != null) ? parent.hashCode() : 0;

        return myHash;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#isForeignKey
     */
    public boolean isForeignKey() {
        return fkFlag;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#isIndexed
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#isNullable
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#isPrimaryKey
     */
    public boolean isPrimaryKey() {
        return pkFlag;
    }

    /**
     * Indicates whether column is selected
     * 
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return isSelected;
    }

    public void parseXML(Element xmlElement) {
        Map attrs = TagParserUtility.getNodeAttributes(xmlElement);
        String str = null;

        this.name = (String) attrs.get(ATTR_NAME);
        str = (String) attrs.get(ATTR_JDBC_TYPE);

        if (str != null) {
            try {
                this.jdbcType = Integer.parseInt(str);
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_JDBC_TYPE);
            }
        }

        str = (String) attrs.get(ATTR_SCALE);
        if (str != null) {
            try {
                this.scale = Integer.parseInt(str);
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_SCALE);
            }
        }

        str = (String) attrs.get(ATTR_PRECISION);
        if (str != null) {
            try {
                this.precision = Integer.parseInt(str);
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_PRECISION);
            }
        }

        str = (String) attrs.get(ATTR_CARDINAL_POSITION);
        if (str != null) {
            try {
                this.cardinalPosition = Integer.parseInt(str);
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_CARDINAL_POSITION);
            }
        }

        str = (String) attrs.get(ATTR_IS_PRIMARY_KEY);
        if (str != null) {
            try {
                this.pkFlag = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_IS_PRIMARY_KEY);
            }
        }

        str = (String) attrs.get(ATTR_IS_FOREIGN_KEY);
        if (str != null) {
            try {
                this.fkFlag = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_IS_FOREIGN_KEY);
            }
        }

        str = (String) attrs.get(ATTR_INDEXED);
        if (str != null) {
            try {
                this.indexed = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_INDEXED);
            }
        }

        str = (String) attrs.get(ATTR_NULLABLE);
        if (str != null) {
            try {
                this.nullable = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                Logger.print(Logger.DEBUG, LOG_CATEGORY, "parseXML()", ATTR_NULLABLE);
            }
        }
    }

    public void setCardinalPosition(int theCardinalPosition) {
        this.cardinalPosition = theCardinalPosition;
    }

    /**
     * sets the default value
     * 
     * @param default value to be set
     */
    public void setDefaultValue(String defValue) {
        this.defaultValue = defValue;
    }

    /**
     * Sets whether this column is flagged as part of a foreign key.
     * 
     * @param newFlag true if this column is part of a foreign key; false otherwise
     */
    public void setForeignKey(boolean newFlag) {
        fkFlag = newFlag;
    }

    /**
     * Sets whether this column is flagged as indexed.
     * 
     * @param newFlag true if this column is indexed; false otherwise
     */
    public void setIndexed(boolean newFlag) {
        indexed = newFlag;
    }

    /**
     * Indicates whether this DBColumn references the given DBColumn in a FK -> PK
     * relationship.
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
    /**
     * Sets SQL type code.
     * 
     * @param newCode SQL code
     * @throws FlatfileDBException if newCode is not a recognized SQL type code
     */
    public void setJdbcType(int newType) {
        if (SQLUtils.isStdJdbcType(newType)) {
            jdbcType = newType;
        } else {
            // Map legacy JDBC types to values which are acceptable to current
            // Axion implementation.
            switch (newType) {
                case java.sql.Types.FLOAT:
                case java.sql.Types.INTEGER:
                    jdbcType = java.sql.Types.NUMERIC;
                    break;

                case java.sql.Types.CHAR:
                    jdbcType = java.sql.Types.VARCHAR;
                    break;

                case java.sql.Types.DATE:
                    jdbcType = java.sql.Types.TIMESTAMP;
                    break;

                default:
                    jdbcType = newType;
            }
        }
    }

    public void setName(String theName) {
        this.name = theName;
    }

    /**
     * Sets whether this column is flagged as nullable.
     * 
     * @param newFlag true if this column is nullable; false otherwise
     */
    public void setNullable(boolean newFlag) {
        nullable = newFlag;
    }

    /**
     * Sets reference to DBTable that owns this DBColumn.
     * 
     * @param newParent new parent of this column.
     */
    public void setParent(FlatfileDBTable newParent) {
        parent = newParent;
    }

    public void setPrecision(int thePrecision) {
        this.precision = thePrecision;
    }

    /**
     * Sets whether this column is flagged as part of a primary key.
     * 
     * @param newFlag true if this column is part of a primary key; false otherwise
     */
    public void setPrimaryKey(boolean newFlag) {
        pkFlag = newFlag;
    }

    public void setScale(int theScale) {
        this.scale = theScale;
    }

    /**
     * Marshall this object to XML string.
     * 
     * @param prefix
     * @return XML string
     */
    public String toXMLString(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("<");
        sb.append(TAG_STCDB_COLUMN);
        sb.append(getAttributeNameValues());
        sb.append("/>\n");
        return sb.toString();
    }

    private String getAttributeNameValues() {
        StringBuilder sb = new StringBuilder(" ");
        sb.append(ATTR_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.name);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_JDBC_TYPE);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.jdbcType);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_SCALE);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.scale);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_PRECISION);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.precision);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_CARDINAL_POSITION);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.cardinalPosition);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_IS_PRIMARY_KEY);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.pkFlag);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_IS_FOREIGN_KEY);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.fkFlag);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_INDEXED);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.indexed);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_NULLABLE);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.nullable);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_PARENT);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.parent.getName());
        sb.append(QUOTE);

        return sb.toString();
    }

}

