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
package org.netbeans.modules.sql.framework.codegen.jdbc;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.codegen.AbstractTypeGenerator;
import org.netbeans.modules.sql.framework.codegen.DataType;


/**
 * For JDBC or ANSI standard Database code generations.
 * @author Girish Patil
 * @version $Revision$
 */
public class JdbcTypeGenerator extends AbstractTypeGenerator {

    private static HashMap supportedDataTypes;

    public Map createSupportedDataTypesMap() {
        if (supportedDataTypes == null) {
            supportedDataTypes = new HashMap();
        }
        supportedDataTypes.put(new Integer(Types.BIT), DataType.BIT);
        supportedDataTypes.put(new Integer(Types.BOOLEAN), DataType.BOOLEAN);

        supportedDataTypes.put(new Integer(Types.BIGINT), DataType.BIGINT);
        supportedDataTypes.put(new Integer(Types.SMALLINT), DataType.SMALLINT);
        supportedDataTypes.put(new Integer(Types.TINYINT), DataType.TINYINT);
        supportedDataTypes.put(new Integer(Types.INTEGER), DataType.INTEGER);

        supportedDataTypes.put(new Integer(Types.DECIMAL), DataType.DECIMAL);
        supportedDataTypes.put(new Integer(Types.DOUBLE), DataType.DOUBLE_PRECISION);
        supportedDataTypes.put(new Integer(Types.FLOAT), DataType.FLOAT);
        supportedDataTypes.put(new Integer(Types.NUMERIC), DataType.NUMERIC);
        supportedDataTypes.put(new Integer(Types.REAL), DataType.REAL);

        supportedDataTypes.put(new Integer(Types.DATE), DataType.DATE);
        supportedDataTypes.put(new Integer(Types.TIME), DataType.TIME);
        supportedDataTypes.put(new Integer(Types.TIMESTAMP), DataType.TIMESTAMP);

        supportedDataTypes.put(new Integer(Types.CHAR), DataType.CHAR);
        supportedDataTypes.put(new Integer(Types.LONGVARCHAR), DataType.LONGVARCHAR);
        supportedDataTypes.put(new Integer(Types.VARCHAR), DataType.VARCHAR);

        supportedDataTypes.put(new Integer(Types.JAVA_OBJECT), DataType.JAVA_OBJECT);
        supportedDataTypes.put(new Integer(Types.OTHER), DataType.VARCHAR);

        supportedDataTypes.put(new Integer(Types.BINARY), DataType.BINARY);
        supportedDataTypes.put(new Integer(Types.VARBINARY), DataType.VARBINARY);
        supportedDataTypes.put(new Integer(Types.LONGVARBINARY), DataType.LONGVARBINARY);

        supportedDataTypes.put(new Integer(Types.BLOB), DataType.BLOB);
        supportedDataTypes.put(new Integer(Types.CLOB), DataType.CLOB);
        supportedDataTypes.put(new Integer(Types.ARRAY), DataType.ARRAY);

        supportedDataTypes.put(new Integer(Types.DISTINCT), DataType.DISTINCT);
        supportedDataTypes.put(new Integer(Types.REF), DataType.REF);
        supportedDataTypes.put(new Integer(Types.STRUCT), DataType.STRUCT);
        supportedDataTypes.put(new Integer(Types.DATALINK), DataType.DATALINK);
        supportedDataTypes.put(new Integer(Types.NULL), DataType.NULL);

        return supportedDataTypes;
    }
}
