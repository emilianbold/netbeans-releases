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
package org.netbeans.modules.mashup.db.model.impl;

import java.sql.Types;
import java.util.Map;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.w3c.dom.Element;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn;

/**
 * Implements FlatfileDBColumn interface.
 *
 * @author Jonathan Giron
 * @author Girish Patil
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileDBColumnImpl extends AbstractDBColumn implements FlatfileDBColumn, Cloneable, Comparable {

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
    private boolean isSelected; // specifies if the column is selected
    private static transient final Logger mLogger = Logger.getLogger(FlatfileDBColumnImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public FlatfileDBColumnImpl() {
        super();
    }

    /**
     * Creates a new instance of FlatfileDBColumnImpl, cloning the contents of the given
     * DBColumn implementation instance.
     *
     * @param src DBColumn instance to be cloned
     */
    public FlatfileDBColumnImpl(DBColumn src) {
        this();
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
        super(colName, sqlJdbcType, colScale, colPrecision, isNullable);
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
    public FlatfileDBColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey, boolean isIndexed, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
    }

    /**
     * Clone a deep copy of DBColumn.
     *
     * @return a copy of DBColumn.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof FlatfileDBColumn)) {
            return false;
        }

        FlatfileDBColumnImpl refMeta = (FlatfileDBColumnImpl) refObj;
        boolean result = (name != null) ? name.equals(refMeta.name) : (refMeta.name == null);
        result &= (jdbcType == refMeta.jdbcType) && (pkFlag == refMeta.pkFlag) && (fkFlag == refMeta.fkFlag) && (indexed == refMeta.indexed) && (nullable == refMeta.nullable) && (getScale() == refMeta.getScale()) && (precision == refMeta.precision);
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
                case java.sql.Types.DATE:
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    buffer.append("(").append(this.precision).append(")");
                    break;
                default:
                    // Append nothing.
                    break;
            }
        }

        if (!StringUtil.isNullString(defaultValue)) {
            buffer.append(" DEFAULT ");
            switch (sqlTypeCode) {
                case java.sql.Types.VARCHAR:
                case java.sql.Types.DATE:
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    buffer.append("'").append(defaultValue).append("'");
                    break;
                default:
                    buffer.append(defaultValue);
                    break;
            }
        }

        if (!isNullable() && !isPrimaryKey()) {
            buffer.append(" NOT NULL");
        }

        return buffer.toString();
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
    @Override
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
    @Override
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
     * Indicates whether column is selected
     *
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void parseXML(Element xmlElement) {
        Map attrs = TagParserUtility.getNodeAttributes(xmlElement);
        String str = null;

        this.name = (String) attrs.get(ATTR_NAME);
        str = (String) attrs.get(ATTR_JDBC_TYPE);

        if (str != null) {
            try {
                this.jdbcType = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_JDBC_TYPE), ex);
            }
        }

        str = (String) attrs.get(ATTR_SCALE);
        if (str != null) {
            try {
                this.scale = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_SCALE), ex);
            }
        }

        str = (String) attrs.get(ATTR_PRECISION);
        if (str != null) {
            try {
                this.precision = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_PRECISION), ex);
            }
        }

        str = (String) attrs.get(ATTR_CARDINAL_POSITION);
        if (str != null) {
            try {
                this.cardinalPosition = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_CARDINAL_POSITION), ex);
            }
        }

        str = (String) attrs.get(ATTR_IS_PRIMARY_KEY);
        if (str != null) {
            try {
                this.pkFlag = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_IS_PRIMARY_KEY), ex);
            }
        }

        str = (String) attrs.get(ATTR_IS_FOREIGN_KEY);
        if (str != null) {
            try {
                this.fkFlag = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_IS_FOREIGN_KEY), ex);
            }
        }

        str = (String) attrs.get(ATTR_INDEXED);
        if (str != null) {
            try {
                this.indexed = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_INDEXED), ex);
            }
        }

        str = (String) attrs.get(ATTR_NULLABLE);
        if (str != null) {
            try {
                this.nullable = Boolean.valueOf(str).booleanValue();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_NULLABLE), ex);
            }
        }
    }

    public void setCardinalPosition(int theCardinalPosition) {
        this.cardinalPosition = theCardinalPosition;
    }

    /**
     * Sets SQL type code.
     *
     * @param newCode SQL code
     * @throws FlatfileDBException if newCode is not a recognized SQL type code
     */
    @Override
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

    protected String getElementTagName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
