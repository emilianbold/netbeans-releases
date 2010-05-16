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
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.netbeans.modules.wsdlextensions.jdbc.builder.xsd;

import java.util.HashMap;

/**
 * TypeUtil
 * 
 * @author
 */
public class TypeUtil {

    public static HashMap builtInTypes = new HashMap();

    public TypeUtil() {
    }

    static {
        // NOTE: CLOB and SLOB not supported
        TypeUtil.builtInTypes.put("byte[]", "xsd:base64Binary");
        TypeUtil.builtInTypes.put("boolean", "xsd:boolean");
        TypeUtil.builtInTypes.put("byte", "xsd:byte");
        TypeUtil.builtInTypes.put("java.util.Calendar", "xsd:dateTime");
        TypeUtil.builtInTypes.put("java.math.BigDecimal", "xsd:decimal");
        TypeUtil.builtInTypes.put("double", "xsd:double");
        TypeUtil.builtInTypes.put("java.lang.Double", "xsd:double");
        TypeUtil.builtInTypes.put("float", "xsd:float");
        TypeUtil.builtInTypes.put("java.lang.Float", "xsd:float");
        TypeUtil.builtInTypes.put("byte[]", "xsd:hexBinary");
        TypeUtil.builtInTypes.put("int", "xsd:int");
        TypeUtil.builtInTypes.put("java.math.BigInteger", "xsd:integer");
        TypeUtil.builtInTypes.put("java.lang.Integer", "xsd:integer");

        TypeUtil.builtInTypes.put("long", "xsd:long");
        TypeUtil.builtInTypes.put("java.lang.Long", "xsd:long");

        TypeUtil.builtInTypes.put("javax.xml.namespace.QName", "xsd:QName");
        TypeUtil.builtInTypes.put("short", "xsd:short");
        TypeUtil.builtInTypes.put("java.lang.Short", "xsd:short");

        TypeUtil.builtInTypes.put("java.lang.String", "xsd:string");
        TypeUtil.builtInTypes.put("java.sql.Time", "xsd:string");
        TypeUtil.builtInTypes.put("java.sql.Timestamp", "xsd:string");
        TypeUtil.builtInTypes.put("java.sql.Date", "xsd:string");

        // temporary for demo
        TypeUtil.builtInTypes.put("java.sql.Blob", "xsd:base64Binary");
        TypeUtil.builtInTypes.put("java.sql.Clob", "xsd:string");
        // added by abey for Procedure with parameter of type RefCursor
        TypeUtil.builtInTypes.put("java.sql.ResultSet", "xsd:ResultSet");

    }

    /** Map SQL type to Java type */
    public static final HashMap SQLTOJAVATYPES = new HashMap();
    static {
        TypeUtil.SQLTOJAVATYPES.put("ARRAY", "java.sql.Array"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("BIGINT", "long"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("BINARY", "byte[]"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("BIT", "boolean"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("BLOB", "java.sql.Blob"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("BOOLEAN", "boolean"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("CHAR", "java.lang.String"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("CLOB", "java.sql.Clob"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("DATALINK", "java.net.URL"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("DATE", "java.sql.Date"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("DECIMAL", "java.math.BigDecimal"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("DISTINCT", "java.lang.String"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("DOUBLE", "double"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("FLOAT", "double"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("INTEGER", "int"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("JAVA_OBJECT", "java.lang.Object"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("LONGVARBINARY", "java.sql.Blob"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("LONGVARCHAR", "java.sql.Clob"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("NULL", "java.lang.String"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("NUMERIC", "java.math.BigDecimal"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("OTHER", "java.lang.String"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("REAL", "float"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("REF", "java.sql.Ref"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("SMALLINT", "short"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("STRUCT", "java.sql.Struct"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("TIME", "java.sql.Time"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("TIMESTAMP", "java.sql.Timestamp"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("TINYINT", "byte"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("VARBINARY", "byte[]"); // NOI18N
        TypeUtil.SQLTOJAVATYPES.put("VARCHAR", "java.lang.String"); // NOI18N
        // added abey for Procedure ResultSets
        TypeUtil.SQLTOJAVATYPES.put("RESULTSET", "java.sql.ResultSet"); // NOI18N
    }

    public static String getSQLTypeDescription(final int type) {
        // returns a String representing the passed in numeric
        // SQL type
        switch (type) {
        case java.sql.Types.ARRAY:
            return "ARRAY";
        case java.sql.Types.BIGINT:
            return "BIGINT";
        case java.sql.Types.BINARY:
            return "BINARY";
        case java.sql.Types.BIT:
            return "BIT";
        case java.sql.Types.BLOB:
            return "BLOB";
        case 16:
            // case java.sql.Types.BOOLEAN:
            return "BOOLEAN";
        case java.sql.Types.CHAR:
            return "CHAR";
        case java.sql.Types.CLOB:
            return "CLOB";
        case 70:
            // case java.sql.Types.DATALINK:
            return "DATALINK";
        case java.sql.Types.DATE:
            return "DATE";
        case java.sql.Types.DECIMAL:
            return "DECIMAL";
        case java.sql.Types.DOUBLE:
            return "DOUBLE";
        case java.sql.Types.FLOAT:
            return "FLOAT";
        case java.sql.Types.INTEGER:
            return "INTEGER";
        case java.sql.Types.JAVA_OBJECT:
            return "JAVA_OBJECT";
        case java.sql.Types.LONGVARBINARY:
            return "LONGVARBINARY";
        case java.sql.Types.LONGVARCHAR:
            return "LONGVARCHAR";
        case java.sql.Types.NULL:
            return "NULL";
        case java.sql.Types.NUMERIC:
            return "NUMERIC";
        case java.sql.Types.OTHER:
            return "OTHER";
        case java.sql.Types.REAL:
            return "REAL";
        case java.sql.Types.REF:
            return "REF";
        case java.sql.Types.SMALLINT:
            return "SMALLINT";
        case java.sql.Types.STRUCT:
            return "STRUCT";
        case java.sql.Types.TIME:
            return "TIME";
        case java.sql.Types.TIMESTAMP:
            return "TIMESTAMP";
        case java.sql.Types.TINYINT:
            return "TINYINT";
        case java.sql.Types.VARBINARY:
            return "VARBINARY";
        case java.sql.Types.VARCHAR:
            return "VARCHAR";
        }
        // all others default to OTHER
        return "OTHER";
    }

    public static boolean isBuiltInType(final String type) {
        return TypeUtil.builtInTypes.get(type) != null;
    }

}
