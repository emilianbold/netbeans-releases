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

package org.netbeans.api.db.explorer.support;

import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;

/**
 *
 * @author Libor Kotouc, Andrei Badea
 */
public class DatabaseExplorerUIsTest extends TestBase {

    private DatabaseConnection dbconn1 = null;
    private DatabaseConnection dbconn2 = null;

    public DatabaseExplorerUIsTest(String testName) {
        super(testName);
    }

    private void initConnections() throws Exception {
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
        JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers("sun.jdbc.odbc.JdbcOdbcDriver")[0];
        dbconn1 = DatabaseConnection.create(driver, "db", "dbuser", "dbschema", "dbpassword", true);
        dbconn2 = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn1);
        ConnectionManager.getDefault().addConnection(dbconn2);
        assertEquals(2, ConnectionManager.getDefault().getConnections().length);
    }

    private JComboBox connect() {
        JComboBox combo = new JComboBox();
        DatabaseExplorerUIs.connect(combo, ConnectionManager.getDefault());
        return combo;
    }

    public void testEmptyComboboxContent() {
        JComboBox combo = connect();

        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 1);
    }

    public void testComboboxWithConnections() throws Exception {
        initConnections();
        JComboBox combo = connect();

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 4);
        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));
    }
}
