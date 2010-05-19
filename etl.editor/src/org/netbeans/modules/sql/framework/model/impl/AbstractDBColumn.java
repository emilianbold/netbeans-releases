/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.utils.GeneratorUtil;
import org.w3c.dom.Element;

import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * Abstract implementation for org.netbeans.modules.model.database.DBColumn and SQLObject interfaces.
 * 
 * @author Sudhendra Seshachala, Jonathan Giron
 * @version $Revision$
 */
public abstract class AbstractDBColumn extends AbstractSQLObject implements SQLDBColumn, Cloneable, Comparable {

    private static transient final Logger mLogger = Logger.getLogger(AbstractDBColumn.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /** Constant for indicating unknown ordinal position for this column. */
    public static final int POSITION_UNKNOWN = Integer.MIN_VALUE;
    /** Constant for column metadata name tag. */
    static final String ELEMENT_TAG = "dbColumn"; // NOI18N
    /** Constant for catalog name tag. */
    protected static final String COLUMN_CATALOGNAME_ATTR = "catalogName"; // NOI18N
    /** Constant for column default value tag. */
    protected static final String COLUMN_DEFAULTVALUE_ATTR = "defaultValue"; // NOI18N
    /** Constant for column isForeignKey name tag. */
    protected static final String COLUMN_INDEXED_ATTR = "indexed"; // NOI18N
    /** Constant for column isForeignKey name tag. */
    protected static final String COLUMN_ISFK_ATTR = "isForeignKey"; // NOI18N
    /** Constant for column isPrimaryKey name tag. */
    protected static final String COLUMN_ISPK_ATTR = "isPrimaryKey"; // NOI18N
    /** Constant for column model name tag. */
    protected static final String COLUMN_MODEL_ATTR = "dbModelName"; // NOI18N
    /** Constant for column name tag. */
    protected static final String COLUMN_NAME_ATTR = "name"; // NOI18N
    /** Constant for column nullable name tag. */
    protected static final String COLUMN_NULLABLE_ATTR = "nullable"; // NOI18N
    /** Constant for column ordinal position tag. */
    protected static final String COLUMN_ORDINAL_POSITION_ATTR = "ordinalPosition"; // NOI18N
    /** Constant for column precision name tag. */
    protected static final String COLUMN_PRECISION_ATTR = "precision"; // NOI18N
    /** Constant for column scale name tag. */
    protected static final String COLUMN_SCALE_ATTR = "scale"; // NOI18N
    /** Constant for schema name tag. */
    protected static final String COLUMN_SCHEMANAME_ATTR = "schemaName"; // NOI18N
    /** Constant for column table name tag. */
    protected static final String COLUMN_TABLENAME_ATTR = "tableName"; // NOI18N
    /** Constant for column type name tag. */
    protected static final String COLUMN_TYPE_ATTR = "type"; // NOI18N
    /** String constant for unknown SQL type */
    protected static final String UNKNOWN_TYPE = "unknown"; // NOI18N

    /* Log4J category name */
    private static final String LOG_CATEGORY = AbstractDBColumn.class.getName();
    /** default value */
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
    /** Cardinal Position */
    protected int ordinalPosition = POSITION_UNKNOWN;
    /** DBTable to which this PK belongs */
    protected DBTable parent;
    /** whether this column is part of a primary key */
    protected boolean pkFlag;
    /** column precision (for numeric types) / width (for char types) */
    protected int precision;
    /** column scale (meaningful only for numeric types) */
    protected int scale;

    /** Constructs default instance of AbstractDBColumn. */
    public AbstractDBColumn() {
        super();
    }

    /**
     * Constructs a new instance of AbstractDBColumn using the given parameters and
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
    public AbstractDBColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        this();

        name = colName;
        jdbcType = sqlJdbcType;

        precision = colPrecision;
        scale = colScale;

        nullable = isNullable;
    }

    /**
     * Constructs a new instance of AbstractDBColumn using the given parameters.
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
    public AbstractDBColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        this(colName, sqlJdbcType, colScale, colPrecision, isNullable);

        pkFlag = isPrimaryKey;
        fkFlag = isForeignKey;
        indexed = isIndexed;
    }

    /**
     * Clone method
     * 
     * @return cloned object
     * @throws CloneNotSupportedException - exception
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
     * Sets the various member variables and collections using the given DBColumn instance
     * as a source object. Concrete implementations should override this method, call
     * super.copyFrom(DBColumn) to pick up member variables defined in this class and then
     * implement its own logic for copying member variables defined within itself.
     * 
     * @param source DBColumn from which to obtain values for member variables and
     *        collections
     */
    public void copyFrom(DBColumn source) {
        name = source.getName();
        jdbcType = source.getJdbcType();

        scale = source.getScale();
        precision = source.getPrecision();

        ordinalPosition = source.getOrdinalPosition();

        pkFlag = source.isPrimaryKey();
        fkFlag = source.isForeignKey();
        indexed = source.isIndexed();
        nullable = source.isNullable();

        parent = source.getParent();

        if (source instanceof SQLDBColumn) {
            super.copyFromSource((SQLDBColumn) source);
            SQLDBColumn sColumn = ((SQLDBColumn) source);
            defaultValue = sColumn.getDefaultValue();
        }
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
    public boolean equals(Object refObj) {
        if (!(refObj instanceof SQLDBColumn)) {
            return false;
        }

        SQLDBColumn refMeta = (SQLDBColumn) refObj;
        boolean result = super.equals(refObj);

        result &= (name != null) ? name.equals(refMeta.getName()) : (refMeta.getName() == null);

        result &= (jdbcType == refMeta.getJdbcType()) && (pkFlag == refMeta.isPrimaryKey()) && (fkFlag == refMeta.isForeignKey()) && (indexed == refMeta.isIndexed()) && (nullable == refMeta.isNullable()) && (scale == refMeta.getScale()) && (precision == refMeta.getPrecision()) && (ordinalPosition == refMeta.getOrdinalPosition());

        result &= (type == refMeta.getObjectType());

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
     * @see SQLObject
     */
    public String getDisplayName() {
        return (displayName != null && displayName.trim().length() != 0) ? displayName.trim() : name.trim();
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
     * @return cardinalPosition to be used
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
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
     * get table qualified name
     * 
     * @return qualified column name prefixed with alias
     */
    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder(50);
        SQLDBTable table = (SQLDBTable) this.getParent();
        if (table != null) {
            buf.append(table.getQualifiedName());
            buf.append(".");
        }

        buf.append(this.getName());

        return buf.toString();
    }

    /**
     * @see org.netbeans.modules.model.database.DBColumn#getScale
     */
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
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (name != null) ? name.hashCode() : 0;
        myHash += ordinalPosition;

        myHash += jdbcType + (10 * scale) + (100 * precision);
        myHash += pkFlag ? 1 : 0;
        myHash += fkFlag ? 2 : 0;
        myHash += indexed ? 4 : 0;
        myHash += nullable ? 8 : 0;

        myHash += type;

        return myHash;
    }

    /**
     * gui property editable check if this column is editable
     * 
     * @return isEditable
     */
    public boolean isEditable() {
        Boolean editable = (Boolean) this.getAttributeObject(ATTR_EDITABLE);
        if (editable != null) {
            return editable.booleanValue();
        }
        return true;
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
     * is this column visible
     * 
     * @return boolean
     */
    public boolean isVisible() {
        Boolean vis = (Boolean) this.getAttributeObject(ATTR_VISIBLE);
        if (vis != null) {
            return vis.booleanValue();
        }

        return true;
    }

    /**
     * Parses the XML content, if any, represented by the given DOM element.
     * 
     * @param columnElement DOM element to be parsed for column content
     * @exception BaseException thrown while parsing XML, or if columnElement is null
     */
    public void parseXML(Element columnElement) throws BaseException {
        if (columnElement == null) {
            throw new BaseException("No <" + ELEMENT_TAG + "> element found.");
        }

        super.parseXML(columnElement);

        if (getId() == null || getId().trim().length() == 0) {
            throw new BaseException("Invalid or missing ID attribute.");
        }

        name = columnElement.getAttribute(COLUMN_NAME_ATTR);
        if (name == null || name.trim().length() == 0) {
            throw new BaseException("Invalid or missing name attribute.");
        }

        String jdbcTypeStr = columnElement.getAttribute(COLUMN_TYPE_ATTR);

        try {
            this.jdbcType = Integer.parseInt(jdbcTypeStr);
        } catch (NumberFormatException e) {
            mLogger.infoNoloc(mLoc.t("EDIT102: Cannot determine JDBC int type for column{0}({1}); will try parsing as string.", name, jdbcTypeStr));
            try {
                this.jdbcType = SQLUtils.getStdJdbcType(jdbcTypeStr);
            } catch (IllegalArgumentException iae) {
                mLogger.infoNoloc(mLoc.t("EDIT103: Cannot determine JDBC int type for column{0}by parsing as string; giving up..", name));
                this.jdbcType = SQLConstants.JDBCSQL_TYPE_UNDEFINED;
            }
        }

        scale = StringUtil.getInt(columnElement.getAttribute(COLUMN_SCALE_ATTR));
        if (scale == Integer.MIN_VALUE) {
            scale = 0;
        }

        precision = StringUtil.getInt(columnElement.getAttribute(COLUMN_PRECISION_ATTR));
        if (precision == Integer.MIN_VALUE) {
            precision = 0;
        }

        ordinalPosition = StringUtil.getInt(columnElement.getAttribute(COLUMN_ORDINAL_POSITION_ATTR));
        if (Integer.MIN_VALUE == ordinalPosition) {
            ordinalPosition = POSITION_UNKNOWN;
        }

        this.pkFlag = "true".equals(columnElement.getAttribute(COLUMN_ISPK_ATTR));
        this.fkFlag = "true".equals(columnElement.getAttribute(COLUMN_ISFK_ATTR));
        this.indexed = "true".equals(columnElement.getAttribute(COLUMN_INDEXED_ATTR));
        this.nullable = "true".equals(columnElement.getAttribute(COLUMN_NULLABLE_ATTR));
        this.defaultValue = columnElement.getAttribute(COLUMN_DEFAULTVALUE_ATTR);
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
     * gui property editable set this column editable property
     * 
     * @param editable - editable
     */
    public void setEditable(boolean editable) {
        this.setAttribute(ATTR_EDITABLE, new Boolean(editable));
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

    /*
     * Setters and non-API helper methods.
     */
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
    public void setParent(DBTable newParent) {
        parent = newParent;

        // set the id if it can be set
        // this is helpful if ancestor relationship is set
        // but sometimes we may have floating tables (tables not added in definition)
        // in that case we can not set id
        if (newParent instanceof SQLObject) {
            try {
                setParentObject(newParent);
                if (newParent.getParent() instanceof SQLDBModel) {
                    SQLDBModel dbModel = (SQLDBModel) newParent.getParent();
                    if (dbModel != null) {
                        SQLDefinition def = (SQLDefinition) dbModel.getParentObject();
                        if (def != null) {
                            this.setId(def.generateId());
                        }
                    }
                }
            } catch (BaseException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT104: could not set parent object or id for column{0}", this.getName()), ex);
            }
        }
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
        pkFlag = newFlag;
    }

    /**
     * Set scale
     * 
     * @param scale - scale
     */
    public void setScale(int theScale) {
        this.scale = theScale;
    }

    /**
     * set this column to be visible
     * 
     * @param visible boolean
     */
    public void setVisible(boolean visible) {
        this.setAttribute(ATTR_VISIBLE, new Boolean(visible));
    }

    /**
     * toString
     * 
     * @return String
     */
    public String toString() {
        String cName = "Unknown";
        try {
            GeneratorUtil eval = GeneratorUtil.getInstance();
            eval.setTableAliasUsed(true);
            cName = eval.getEvaluatedString(this);
            eval.setTableAliasUsed(false);
        } catch (BaseException ignore) {
            // ignore
        }
        return cName;
    }

    /**
     * @see SQLObject#toXMLString(java.lang.String)
     */
    public abstract String toXMLString(String prefix) throws BaseException;

    /**
     * Appends attributes defined in this abstract class to the given StringBuilder.
     * 
     * @param xml StringBuilder to receive XML attribute output
     */
    protected void appendXMLAttributes(StringBuilder xml) {
        if (xml == null) {
            throw new IllegalArgumentException("Must supply non-null StringBuilder ref for parameter xml.");
        }

        if (getId() != null) {
            xml.append(" ").append(ID).append("=\"").append(id.trim()).append("\"");
        }

        xml.append(" ").append(DISPLAY_NAME).append("=\"");

        if (displayName != null) {
            xml.append(displayName.trim());
        }

        xml.append("\"");

        if (name != null && name.trim().length() != 0) {
            xml.append(" ").append(COLUMN_NAME_ATTR).append("=\"").append(name.trim()).append("\"");
        }

        // right out default column value if any
        if (defaultValue != null && defaultValue.trim().length() != 0) {
            xml.append(" ").append(COLUMN_DEFAULTVALUE_ATTR).append("=\"").append(XmlUtil.escapeXML(defaultValue.trim())).append("\"");
        }

        xml.append(" ").append(COLUMN_TYPE_ATTR).append("=\"").append(jdbcType).append("\"");

        xml.append(" ").append(COLUMN_SCALE_ATTR).append("=\"").append(scale).append("\"");

        xml.append(" ").append(COLUMN_PRECISION_ATTR).append("=\"").append(precision).append("\"");

        xml.append(" ").append(COLUMN_ORDINAL_POSITION_ATTR).append("=\"").append(ordinalPosition).append("\"");

        xml.append(" ").append(COLUMN_ISPK_ATTR).append("=\"").append(String.valueOf(pkFlag)).append("\"");

        xml.append(" ").append(COLUMN_ISFK_ATTR).append("=\"").append(String.valueOf(fkFlag)).append("\"");

        xml.append(" ").append(COLUMN_INDEXED_ATTR).append("=\"").append(String.valueOf(indexed)).append("\"");

        xml.append(" ").append(COLUMN_NULLABLE_ATTR).append("=\"").append(String.valueOf(nullable)).append("\"");
    }

    /**
     * Gets String representing tag name for this table class.
     * 
     * @return String representing element tag for this instance
     */
    protected abstract String getElementTagName();
}

