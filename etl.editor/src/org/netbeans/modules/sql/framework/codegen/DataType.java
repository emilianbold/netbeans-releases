/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.codegen;

import java.sql.Types;

/**
 * A class to represents jdbc type and actual keyword for that jdbc type in the database
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 * @link http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
 */
public class DataType {

    public static final DataType BIT = new DataType(Types.BIT, "bit");
    public static final DataType BOOLEAN = new DataType(Types.BOOLEAN, "boolean");
    public static final DataType INTEGER = new DataType(Types.INTEGER, "integer");
    public static final DataType BIGINT = new DataType(Types.BIGINT, "bigint");
    public static final DataType TINYINT = new DataType(Types.TINYINT, "tinyint");
    public static final DataType SMALLINT = new DataType(Types.SMALLINT, "smallint");
    public static final DataType DOUBLE = new DataType(Types.DOUBLE, "double");
    public static final DataType DOUBLE_PRECISION = new DataType(Types.DOUBLE, "double precision");
    public static final DataType FLOAT = new DataType(Types.FLOAT, "float");
    public static final DataType DECIMAL = new DataType(Types.DECIMAL, "decimal");
    public static final DataType NUMERIC = new DataType(Types.NUMERIC, "numeric");
    public static final DataType NUMEBER = new DataType(Types.NUMERIC, "number");
    public static final DataType REAL = new DataType(Types.REAL, "real");
    public static final DataType DATE = new DataType(Types.DATE, "date");
    public static final DataType TIME = new DataType(Types.TIME, "time");
    public static final DataType TIMESTAMP = new DataType(Types.TIMESTAMP, "timestamp");
    public static final DataType LONGVARCHAR = new DataType(Types.LONGVARCHAR, "longvarchar");
    public static final DataType LONG_VARCHAR = new DataType(Types.LONGVARCHAR, "long varchar");
    public static final DataType VARCHAR = new DataType(Types.VARCHAR, "varchar");
    public static final DataType CHAR = new DataType(Types.CHAR, "char");
    public static final DataType JAVA_OBJECT = new DataType(Types.JAVA_OBJECT, "java_object");
    public static final DataType BINARY = new DataType(Types.BINARY, "binary");
    public static final DataType VARBINARY = new DataType(Types.VARBINARY, "varbinary");
    public static final DataType LONGVARBINARY = new DataType(Types.LONGVARBINARY, "longvarbinary");
    public static final DataType LONG_VARBINARY = new DataType(Types.LONGVARBINARY, "long varbinary");
    public static final DataType BLOB = new DataType(Types.BLOB, "blob");
    public static final DataType CLOB = new DataType(Types.CLOB, "clob");
    public static final DataType ARRAY = new DataType(Types.ARRAY, "array");
    public static final DataType DISTINCT = new DataType(Types.DISTINCT, "distinct");
    public static final DataType REF = new DataType(Types.REF, "ref");
    public static final DataType STRUCT = new DataType(Types.STRUCT, "struct");
    public static final DataType DATALINK = new DataType(Types.DATALINK, "datalink");
    public static final DataType NULL = new DataType(Types.NULL, "null");
    public static final DataType SYBASE_IMAGE = new DataType(Types.LONGVARBINARY, "image");
    public static final DataType SYBASE_TEXT = new DataType(Types.LONGVARCHAR, "text");
    public static final DataType SYBASE_DATETIME = new DataType(Types.TIMESTAMP, "datetime");
    public static final DataType ORACLE_RAW = new DataType(Types.VARBINARY, "raw");
    public static final DataType ORACLE_LONGRAW = new DataType(Types.LONGVARBINARY, "long raw");
    public static final DataType ORACLE_VARRAY = new DataType(Types.ARRAY, "varray");
    public static final DataType ORACLE_OBJECT = new DataType(Types.STRUCT, "object");
    public static final DataType ORACLE_LONG = new DataType(Types.LONGVARCHAR, "long");
    private String dataTypeName = null;
    private int jdbcType = -1;

    public DataType(int newJdbcType, String dTypeName) {
        this.jdbcType = newJdbcType;
        this.dataTypeName = dTypeName;
    }

    public int getJdbcType() {
        return this.jdbcType;
    }

    public void setJdbcType(int newJdbcType) {
        this.jdbcType = newJdbcType;
    }

    public String getDataTypeName() {
        return this.dataTypeName;
    }

    public void setDataTypeName(String dTypeName) {
        this.dataTypeName = dTypeName;
    }

    @Override
    public String toString() {
        return dataTypeName;
    }
}
