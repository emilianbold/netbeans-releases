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

package org.netbeans.api.db.explorer;

import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseConnectionTest extends TestBase {
    
    protected void setUp() throws Exception {
        super.setUp();
        Util.deleteConnectionFiles();
    }
    
    public DatabaseConnectionTest(String testName) {
        super(testName);
    }
    
    public void testConnectionsRemovedWhenFilesDeleted() throws Exception{
        JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers("sun.jdbc.odbc.JdbcOdbcDriver")[0];
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn);
        
        assertTrue(ConnectionManager.getDefault().getConnections().length > 0);
        
        Util.deleteConnectionFiles();
        
        assertTrue(ConnectionManager.getDefault().getConnections().length == 0);
    }

    public void testSameDatabaseConnectionReturned() throws Exception {
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
        
        JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers("sun.jdbc.odbc.JdbcOdbcDriver")[0];
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn);
        
        assertEquals(dbconn, ConnectionManager.getDefault().getConnections()[0]);
    }
}
