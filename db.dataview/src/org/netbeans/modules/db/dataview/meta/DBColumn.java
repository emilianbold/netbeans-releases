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

/**
 * Represents database table column
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBColumn extends DBObject<DBTable> implements Comparable {

    public static final int POSITION_UNKNOWN = Integer.MIN_VALUE;
    private String defaultValue;
    private boolean foreignKey;
    private boolean indexed;
    private int jdbcType;
    /** name of column */
    private String name;
    private boolean nullable;
    private boolean editable = true;
    private int ordinalPosition = POSITION_UNKNOWN;

    private boolean primaryKey;
    private int precision;
    private int scale;
    private boolean generated;
    private int displaySize;

    /** Constructs default instance of DBColumn. */
    public DBColumn() {
        super();
    }

    /**
     * Constructs a new instance of DBColumn using the given parameters and
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
    public DBColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        this();

        name = colName;
        jdbcType = sqlJdbcType;

        precision = colPrecision;
        scale = colScale;

        nullable = isNullable;
    }

    /**
     * Constructs a new instance of DBColumn using the given parameters.
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
    public DBColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        this(colName, sqlJdbcType, colScale, colPrecision, isNullable);

        primaryKey = isPrimaryKey;
        foreignKey = isForeignKey;
        indexed = isIndexed;
    }

    /**
     * Compares DBColumn with another object for lexicographical ordering. Null objects
     * and those DBColumns with null names are placed at the end of any ordered collection
     * using this method.
     * 
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name
     *         is the same. 1 if the column name is greater than obj to be compared.
     */
    public int compareTo(Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        String myName = getDisplayName();
        myName = (myName == null) ? name : myName;

        String refName = null;
        if (!(refObj instanceof DBColumn)) {
            return -1;
        }

        DBColumn refColumn = (DBColumn) refObj;
        refName = refColumn.getName();

        // compare primary keys
        if (this.isPrimaryKey() && !refColumn.isPrimaryKey()) {
            return -1;
        } else if (!this.isPrimaryKey() && refColumn.isPrimaryKey()) {
            return 1;
        }

        // compare foreign keys
        if (this.isForeignKey() && !refColumn.isForeignKey()) {
            return -1;
        } else if (!this.isForeignKey() && refColumn.isForeignKey()) {
            return 1;
        }

        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * Concrete implementations should override this method and combine the result of
     * super.equals(Object) [calling this method] with its own comparison of member
     * variables declared in its class as its own return value.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    @Override
    public boolean equals(Object refObj) {
        if (!(refObj instanceof DBColumn)) {
            return false;
        }

        DBColumn refMeta = (DBColumn) refObj;
        boolean result = super.equals(refObj);

        result &= (name != null) ? name.equals(refMeta.getName()) : (refMeta.getName() == null);

        result &= (jdbcType == refMeta.getJdbcType()) && (primaryKey == refMeta.isPrimaryKey()) && (foreignKey == refMeta.isForeignKey()) && (indexed == refMeta.isIndexed()) && (nullable == refMeta.isNullable()) && (scale == refMeta.getScale()) && (precision == refMeta.getPrecision()) && (ordinalPosition == refMeta.getOrdinalPosition());

        return result;
    }

    /**
     * Get default value
     * 
     * @return default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @see DBObject
     */
    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.trim().length() != 0) ? displayName.trim() : name.trim();
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets the Ordinal Position
     * 
     * @return cardinalPosition to be used
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    public int getPrecision() {
        return precision;
    }
    
    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    /**
     * get table qualified name
     * 
     * @return qualified column name prefixed with alias
     */
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder(50);
        DBTable table =  this.getParentObject();
        if (table != null) {
            buf.append(table.getFullyQualifiedName());
            buf.append(".");
        }

        String escapeString = table.getEscapeString();
        buf.append(escapeString).append(this.getName()).append(escapeString);

        return buf.toString();
    }

    public int getScale() {
        return scale;
    }

    /**
     * Gets the hashCode for this object. Concrete implementations should override this
     * method and combine the result of super.hashCode() [this method] and its own
     * implementation of hashCode as its own return value.
     * 
     * @return the hashCode of this object.
     */
    @Override
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (name != null) ? name.hashCode() : 0;
        myHash += ordinalPosition;

        myHash += jdbcType + (10 * scale) + (100 * precision);
        myHash += primaryKey ? 1 : 0;
        myHash += foreignKey ? 2 : 0;
        myHash += indexed ? 4 : 0;
        myHash += nullable ? 8 : 0;

        return myHash;
    }

    /**
     * check if this column is editable
     * 
     * @return isEditable
     */
    public boolean isEditable() {
        return editable;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    /**
     * Set default value
     * 
     * @param defaultVal - value
     */
    public void setDefaultValue(String defaultVal) {
        this.defaultValue = defaultVal;
    }

    /**
     * set this column editable property
     * 
     * @param editable - editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Sets whether this column is flagged as part of a foreign key.
     * 
     * @param newFlag true if this column is part of a foreign key; false otherwise
     */
    public void setForeignKey(boolean newFlag) {
        foreignKey = newFlag;
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
     * Sets JDBC type of this column
     * 
     * @param newType new JDBC type value
     */
    public void setJdbcType(int newType) {
        jdbcType = newType;
    }

    /**
     * Set name
     * 
     * @param name - name
     */
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
     * Gets the Ordinal Position
     * 
     * @param cardinalPos to be used
     */
    public void setOrdinalPosition(int cardinalPos) {
        this.ordinalPosition = cardinalPos;
    }

    /**
     * Sets reference to DBTable that owns this DBColumn.
     * 
     * @param newParent new parent of this column.
     */
    public void setParent(DBTable newParent) throws DBException {
        setParentObject(newParent);
    }

    /**
     * Set precision
     * 
     * @param precision - precision
     */
    public void setPrecision(int thePrecision) {
        this.precision = thePrecision;
    }

    /**
     * Sets whether this column is flagged as part of a primary key.
     * 
     * @param newFlag true if this column is part of a primary key; false otherwise
     */
    public void setPrimaryKey(boolean newFlag) {
        primaryKey = newFlag;
    }

    /**
     * Set scale
     * 
     * @param scale - scale
     */
    public void setScale(int theScale) {
        this.scale = theScale;
    }
}

