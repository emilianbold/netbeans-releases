/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.api.sql.execute;

import java.sql.SQLException;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.core.test.TestBase;

/**
 *
 * @author David Van Couvering
 */
public class SQLExecutorTest extends TestBase {
    
    private DatabaseConnection dbconn;

    public SQLExecutorTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        dbconn = getDatabaseConnection();

        ConnectionManager.getDefault().disconnect(dbconn);
        
        ConnectionManager.getDefault().connect(dbconn);

        assertNotNull(dbconn.getJDBCConnection());
        assert(! dbconn.getJDBCConnection().isClosed());

        try {
            dbconn.getJDBCConnection().createStatement().execute("DROP TABLE test");
        } catch (SQLException sqle) {
            System.out.println("Got an exception dropping table: " + sqle.getMessage());
        }

        String sql = "CREATE TABLE test(id integer primary key); " +
                "INSERT INTO test VALUES(1); " +
                "SELECT * FROM TEST";

        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        checkExecution(info, sql);
    }

    public void testExecuteOnClosedConnection() throws Exception {
        DatabaseConnection broken = getDatabaseConnection();

        ConnectionManager.getDefault().disconnect(broken);

        try {
            SQLExecutor.execute(broken, "SELECT ydayaday");
            fail("No exception when executing on a closed connection");
        } catch (DatabaseException dbe) {
            // expected
        }
    }

    public void testExecute() throws Exception {
        String sql = "INSERT INTO TEST VALUES(1); INSERT INTO TEST VALUES(3);";

        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        assertNotNull(info);
        assertTrue(info.hasExceptions());
        assertTrue(info.getExceptions().size() == 1);
        assertNotNull(info.getExceptions().get(0));
    }
        
    public void testDelimiter() throws Exception {
        String sql = "SELECT * FROM TEST;\n--Here is a comment\nDELIMITER ??\n SELECT * FROM TEST??\n " +
                "--Another comment\n DELIMITER ;\nSELECT * FROM TEST;";
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        checkExecution(info, sql);

        info = SQLExecutor.execute(dbconn,
                "DELIMITER ??\nSELECT * FROM TEST?? DELIMITER ;\nSELECT * FROM TEST;");
        checkExecution(info, sql);

        info = SQLExecutor.execute(dbconn,
                "/** a block comment */\nDELIMITER ??\nSELECT * FROM TEST??");
        checkExecution(info, sql);

        info = SQLExecutor.execute(dbconn,
                "DELIMITER ??\nSELECT * FROM TEST;");

        assertTrue(info.hasExceptions());
    }
    
    private void checkExecution(SQLExecutionInfo info, String sql) throws Exception {
        assertNotNull(info);

        if (info.hasExceptions()) {
            for (Throwable t : info.getExceptions()) {
                t.printStackTrace();
            }
            throw new Exception("Executing SQL '" + sql + "' generated exceptions - see output for details");
        }        
    }

}