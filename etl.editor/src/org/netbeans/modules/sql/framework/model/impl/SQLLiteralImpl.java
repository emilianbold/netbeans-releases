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

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.utils.EvaluatorUtil;

import com.sun.sql.framework.exception.BaseException;

/**
 * Represents a string or number literal value.
 * 
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public class SQLLiteralImpl extends AbstractSQLObject implements SQLLiteral {

    static {
        VALID_TYPE_NAMES = SQLUtils.getSupportedLiteralTypes();
    }

    /** Array of Strings representing available SQL datatypes */
    protected static final List VALID_TYPE_NAMES;

    /**
     * Returns list of Strings representing valid SQL datatypes for an SQLLiteral object.
     * 
     * @return read-only List of valid SQL datatypes
     */
    public static final List getValidTypeNames() {
        return Collections.unmodifiableList(VALID_TYPE_NAMES);
    }

    /** Creates a new default instance of SQLLiteral */
    public SQLLiteralImpl() {
        type = SQLConstants.LITERAL;
    }

    /**
     * Constructs a new instance of SQLLiteral, copying the contents of the given
     * SQLLiteral.
     * 
     * @param src SQLLiteral whose contents are to be copied to this new instance
     */
    public SQLLiteralImpl(SQLLiteral src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Cannot create SQLLiteral using copy constructor - src is null");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of SQLLiteral with the given display name, value, and
     * JDBC type.
     * 
     * @param name display name of new instance
     * @param value value of new instance
     * @param jdbcType JDBC type of new instance
     * @throws BaseException if name is null or empty, or value is null.
     */
    public SQLLiteralImpl(String name, String value, int jdbcType) throws BaseException {
        this();

        if (value == null) {
            String msg = "null value";
            throw new BaseException(msg);
        }

        setDisplayName(name);
        setJdbcType(jdbcType);
        setValue(value);
    }

    public Object clone() {
        return new SQLLiteralImpl(this);
    }

    public void copyFrom(SQLLiteral src) {
        super.copyFromSource(src);

        // If src is VisibleSQLLiteral, ensure type is set to non-visible variety as value
        // of type is cloned from src by AbstractSQLObject and overrides the value set in
        // constructor. Comparing src introduces side-effect, make non-visible literal
        // only if this is not visible literal instead.
        if (!(this instanceof VisibleSQLLiteral)) {
            type = SQLConstants.LITERAL;
        }
    }

    /**
     * Gets JDBC type of this literal.
     * 
     * @return JDBC type
     */
    public int getJdbcType() {
        String aType = (String) getAttributeObject(ATTR_TYPE);
        if (aType != null && aType.equals(VARCHAR_UNQUOTED_STR)) {
            return VARCHAR_UNQUOTED;

        }
        return SQLUtils.getStdJdbcType(aType);
    }

    public int getPrecision() {
        return 0;
    }

    /**
     * Gets value of this literal.
     * 
     * @return current value
     */
    public String getValue() {
        return (String) getAttributeObject(ATTR_VALUE);
    }

    /**
     * Sets JDBC type of this literal.
     * 
     * @param jdbcType new JDBC type
     */
    public void setJdbcType(int jdbcType) {
        if (jdbcType == VARCHAR_UNQUOTED) {
            setAttribute(ATTR_TYPE, VARCHAR_UNQUOTED_STR);
        } else {
            setAttribute(ATTR_TYPE, SQLUtils.getStdSqlType(jdbcType));
        }
    }

    /**
     * Sets value of this literal to the given value.
     * 
     * @param val new value
     */
    public void setValue(String val) {
        setAttribute(ATTR_VALUE, val);
    }

    public String toString() {
        try {
            return EvaluatorUtil.getInstance().getEvaluatedString(this);
        } catch (BaseException ignore) {
            return "Unknown";
        }
    }
}

