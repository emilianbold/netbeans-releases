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
package org.netbeans.modules.dm.virtual.db.model;

import java.sql.Types;
import java.util.Map;

/**
 * @author Ahimanikya Satapathy
 */
public class VirtualDBColumn extends VirtualDBObject implements Cloneable, Comparable {

    public static final int POSITION_UNKNOWN = Integer.MIN_VALUE;
    protected String defaultValue;
    protected boolean fkFlag;
    protected int jdbcType;
    protected String name;
    protected boolean nullable;
    protected int ordinalPosition = POSITION_UNKNOWN;
    protected VirtualDBTable parent;
    protected boolean pkFlag;
    protected int precision;
    protected int scale;
    private static final String END_QUOTE_SPACE = "\" ";

    public VirtualDBColumn() {
        super();
    }

    public VirtualDBColumn(String colName, int sqlJdbcType, int colPrecision, int colScale, boolean isNullable) {
        this();

        name = colName;
        jdbcType = sqlJdbcType;

        precision = colPrecision;
        scale = colScale;

        nullable = isNullable;
    }

    public VirtualDBColumn(String colName, int sqlJdbcType, int colPrecision, int colScale, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        this(colName, sqlJdbcType, colPrecision, colScale, isNullable);

        pkFlag = isPrimaryKey;
        fkFlag = isForeignKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.trim().length() != 0) ? displayName.trim() : name.trim();
    }

    public int getJdbcType() {
        return jdbcType;
    }

    public String getJdbcTypeString() {
        return VirtualDBUtil.getStdSqlType(jdbcType);
    }

    public String getName() {
        return this.name;
    }

    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    public VirtualDBTable getParent() {
        return parent;
    }

    public int getPrecision() {
        return precision;
    }

    public String getQualifiedName() {
        StringBuilder buf = new StringBuilder(50);
        VirtualDBTable table = (VirtualDBTable) this.getParent();
        if (table != null) {
            buf.append(table.getQualifiedName());
            buf.append(".");
        }

        buf.append(this.getName());

        return buf.toString();
    }

    @Override
    public int hashCode() {
        int myHash = super.hashCode();

        myHash += (name != null) ? name.hashCode() : 0;
        myHash += ordinalPosition;

        myHash += jdbcType + (10 * scale) + (100 * precision);
        myHash += pkFlag ? 1 : 0;
        myHash += fkFlag ? 2 : 0;
        myHash += nullable ? 4 : 0;

        myHash += type;

        return myHash;
    }

    public boolean isForeignKey() {
        return fkFlag;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return pkFlag;
    }

    public void setDefaultValue(String defaultVal) {
        this.defaultValue = defaultVal;
    }

    public void setForeignKey(boolean newFlag) {
        fkFlag = newFlag;
    }

    public void setName(String theName) {
        this.name = theName;
    }

    public void setNullable(boolean newFlag) {
        nullable = newFlag;
    }

    public void setOrdinalPosition(int cardinalPos) {
        this.ordinalPosition = cardinalPos;
    }

    public void setParent(VirtualDBTable newParent) {
        parent = newParent;

        // set the id if it can be set
        // this is helpful if ancestor relationship is set
        // but sometimes we may have floating tables (tables not added in definition)
        // in that case we can not set id
        if (newParent instanceof VirtualDBObject) {
            try {
                setParentObject(newParent);
            } catch (Exception ex) {
            }
        }
    }

    public void setPrecision(int thePrecision) {
        this.precision = thePrecision;
    }

    public void setPrimaryKey(boolean newFlag) {
        pkFlag = newFlag;
    }

    public void setScale(int theScale) {
        this.scale = theScale;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object clone() {
        try {
            VirtualDBColumn column = (VirtualDBColumn) super.clone();

            return column;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public int compareTo(Object refObj) {
        if (null == refObj) {
            return -1;
        } else if (this == refObj) {
            return 0;
        }

        VirtualDBColumn col = (VirtualDBColumn) refObj;
        return ordinalPosition - col.ordinalPosition;
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof VirtualDBColumn)) {
            return false;
        }

        VirtualDBColumn refMeta = (VirtualDBColumn) refObj;
        boolean result = (name != null) ? name.equals(refMeta.name) : (refMeta.name == null);
        result &= (jdbcType == refMeta.jdbcType) && (pkFlag == refMeta.pkFlag) && (fkFlag == refMeta.fkFlag) && (nullable == refMeta.nullable) && (getScale() == refMeta.getScale()) && (precision == refMeta.precision);
        result &= (parent != null) ? parent.equals(refMeta.parent) : (refMeta.parent == null);
        return result;
    }

    public int getCardinalPosition() {
        return this.ordinalPosition;
    }

    public String getCreateStatementSQL() {
        // Default value if jdbcType is unrecognized.
        int sqlTypeCode = java.sql.Types.VARCHAR;

        if (VirtualDBUtil.isStdJdbcType(this.jdbcType)) {
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
        buffer.append(VirtualDBUtil.getStdSqlType(sqlTypeCode));

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
                case java.sql.Types.TIME:
                case java.sql.Types.TIMESTAMP:
                    buffer.append("(").append(this.precision).append(")");
                    break;
                case java.sql.Types.DATE:
                default:
                    // Append nothing.
                    break;
            }
        }

        if (!VirtualDBUtil.isNullString(defaultValue)) {
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

    public Map getProperties() {
        return ((VirtualDBTable) this.getParent()).getProperties();
    }

    public String getProperty(String propName) {
        return ((VirtualDBTable) this.getParent()).getProperty(propName);
    }

    public int getScale() {
        switch (jdbcType) {
            case Types.NUMERIC:
                return scale;
            default:
                return 0;
        }
    }

    public void setCardinalPosition(int theCardinalPosition) {
        this.ordinalPosition = theCardinalPosition;
    }

    public void setJdbcType(int newType) {
        if (VirtualDBUtil.isStdJdbcType(newType)) {
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
}
