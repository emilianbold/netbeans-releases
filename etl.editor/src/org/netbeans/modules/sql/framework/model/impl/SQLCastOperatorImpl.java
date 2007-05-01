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

import java.sql.Types;
import java.util.List;

import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLUtils;
import com.sun.sql.framework.utils.Attribute;

/**
 * Overrides parent implementation to ensure return value of getJdbcType() reflects the
 * datatype to which the user opts to cast the input.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public final class SQLCastOperatorImpl extends SQLGenericOperatorImpl implements SQLCastOperator {

    private static final String ATTR_PRECISION = "precision"; // NOI18N
    private static final String ATTR_SCALE = "scale"; // NOI18N
    /**
     * Constant representing argument name for input that corresponds to the cast
     * datatype.
     */
    private static final String COLUMN_INPUT = "column"; // NOI18N
    private static final int DEFAULT_NUMERIC_PRECISION = 22;

    private static final Integer DEFAULT_SCALE = new Integer(0);

    private static final int DEFAULT_TEXT_PRECISION = 2048;
    private static final String TYPE_INPUT = "type"; // NOI18N

    /**
     * Constructs a default instance of SQLCastOperatorImpl
     */
    public SQLCastOperatorImpl() {
        super();
        initialize();
    }

    /**
     * @param src
     * @throws BaseException
     */
    public SQLCastOperatorImpl(SQLCastOperator src) throws BaseException {
        super(src);
        initialize();
    }

    /**
     * @param newName
     * @param aType
     * @throws BaseException
     */
    public SQLCastOperatorImpl(String newName, String aType) throws BaseException {
        super(newName, aType);
        initialize();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLConnectableObject#addInput(java.lang.String,
     *      org.netbeans.modules.sql.framework.model.SQLObject)
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        super.addInput(argName, newInput);
        if (TYPE_INPUT.equals(argName)) {
            setDefaultAttributesIfMissing(getJdbcType());
        }
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            SQLCastOperatorImpl op = new SQLCastOperatorImpl(this);
            return op;
        } catch (BaseException ex) {
            throw new CloneNotSupportedException("Cannot create clone of " + this.getOperatorType());
        }
    }

    /**
     * Overrides parent implementation to return datatype represented by user's selection
     * in the static GUI field.
     * 
     * @return JDBC type that user has specified for casting
     * @see org.netbeans.modules.sql.framework.model.SQLObject#getJdbcType()
     */
    public int getJdbcType() {
        SQLObject obj = this.getSQLObject(TYPE_INPUT);
        if (obj instanceof SQLLiteral) {
            String castType = ((SQLLiteral) obj).getValue();
            int castJdbcType = SQLUtils.getStdJdbcType(castType);
            if (castJdbcType != SQLConstants.JDBCSQL_TYPE_UNDEFINED) {
                return castJdbcType;
            }
        }

        return super.getJdbcType();
    }

    public int getPrecision() {
        Object obj = getAttributeObject(ATTR_PRECISION);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }

        return 0;
    }

    public int getScale() {
        Object obj = getAttributeObject(ATTR_SCALE);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }

        return 0;
    }

    /**
     * Overrides default implementation to treat precision and scale arguments as
     * attributes rather than as inputs.
     * 
     * @see org.netbeans.modules.sql.framework.model.SQLOFunction#setArguments(java.util.List)
     */
    public void setArguments(List opArgs) throws BaseException {
        if (operatorDefinition == null) {
            throw new BaseException("Operator Definition is null.");
        }

        if (opArgs != null && opArgs.size() > 1) {
            SQLObject columnValue = (SQLObject) opArgs.get(0);
            setArgument(COLUMN_INPUT, columnValue);

            SQLObject typeValue = (SQLObject) opArgs.get(1);
            setArgument(TYPE_INPUT, typeValue);

            if (SQLUtils.isPrecisionRequired(getJdbcType())) {
                try {
                    String precisionStr = (String) opArgs.get(2);
                    setPrecision(Integer.parseInt(precisionStr));
                } catch (IndexOutOfBoundsException e) {
                    throw new BaseException("Required precision argument is missing.");
                }
            }

            if (SQLUtils.isScaleRequired(getJdbcType())) {
                try {
                    String scaleStr = (String) opArgs.get(3);
                    setScale(Integer.parseInt(scaleStr));
                } catch (IndexOutOfBoundsException ignore) {
                    // scale is optional
                }
            }
        }
    }

    /**
     * Overrides parent implementation to set datatype to the given value.
     * 
     * @param newType new JDBC type for this cast-as operator.
     * @see org.netbeans.modules.sql.framework.model.SQLObject#setJdbcType(int)
     */
    public void setJdbcType(int newType) {
        String typeStr = SQLUtils.getStdSqlType(newType);

        SQLObject obj = this.getSQLObject(TYPE_INPUT);
        if (obj instanceof SQLLiteral) {
            ((SQLLiteral) obj).setValue(typeStr);
        } else {
            try {
                this.setArgument(TYPE_INPUT, typeStr);
            } catch (BaseException ignore) {
                // Should not happen - String is an accepted type for setArgument
            }
        }

        setDefaultAttributesIfMissing(newType);
    }

    public void setPrecision(int newValue) {
        if (newValue != getPrecision()) {
            setAttribute(ATTR_PRECISION, new Integer(newValue));
        }
    }

    public void setScale(int newValue) {
        if (newValue != getScale()) {
            setAttribute(ATTR_SCALE, new Integer(newValue));
        }
    }

    private void initialize() {
        this.type = SQLConstants.CAST_OPERATOR;
    }

    private void setDefaultAttributesIfMissing(int jdbcType) {
        Attribute precisionAttr = getAttribute(ATTR_PRECISION);
        Attribute scaleAttr = getAttribute(ATTR_SCALE);

        int precision = 0;
        switch (jdbcType) {
            case Types.NUMERIC:
                precision = DEFAULT_NUMERIC_PRECISION;
                break;

            case Types.CHAR:
            case Types.VARCHAR:
                precision = DEFAULT_TEXT_PRECISION;
                break;

            default:
                break;
        }

        if (precisionAttr == null) {
            setAttribute(ATTR_PRECISION, new Integer(precision));
        }

        if (scaleAttr == null) {
            setAttribute(ATTR_SCALE, DEFAULT_SCALE);
        }
    }
}
