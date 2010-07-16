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

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.utils.GeneratorUtil;

import com.sun.etl.exception.BaseException;

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
            return GeneratorUtil.getInstance().getEvaluatedString(this);
        } catch (BaseException ignore) {
            return "Unknown";
        }
    }
}

