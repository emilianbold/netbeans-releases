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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class JDBCStubUtilTest extends TestCase {
    
    public JDBCStubUtilTest(String name) {
        super(name);
    }
    
    public void testColumnsResultSet() throws Exception {
        ResultSet rs = JDBCStubUtil.columnsResultSet(
                new String[] { "REAL_COL", "VARCHAR_COL" },
                new String[] { "REAL", "VARCHAR" },
                new int[] { Types.REAL, Types.VARCHAR },
                new int[] { 10, 20 },
                new int[] { 6, 0 },
                new int[] { DatabaseMetaData.columnNoNulls, DatabaseMetaData.columnNullable }
        );
        
        rs.next();
        assertEquals("REAL_COL", rs.getString("COLUMN_NAME"));
        assertEquals("REAL", rs.getString("TYPE_NAME"));
        assertEquals(Types.REAL, rs.getInt("DATA_TYPE"));
        assertEquals(10, rs.getInt("COLUMN_SIZE"));
        assertEquals(6, rs.getInt("DECIMAL_DIGITS"));
        assertEquals(DatabaseMetaData.columnNoNulls, rs.getInt("NULLABLE"));
        
        rs.next();
        assertEquals("VARCHAR_COL", rs.getString("COLUMN_NAME"));
        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
        assertEquals(Types.VARCHAR, rs.getInt("DATA_TYPE"));
        assertEquals(20, rs.getInt("COLUMN_SIZE"));
        assertEquals(0, rs.getInt("DECIMAL_DIGITS"));
        assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
    }
}
