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

import java.sql.Connection;
import java.sql.ResultSet;
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

        SQLExecutionInfo info;
        
        if (isMySQL()) {
            checkExecution(SQLExecutor.execute(dbconn, "DROP DATABASE IF EXISTS " + getSchema() +";"));
            checkExecution(SQLExecutor.execute(dbconn, "CREATE DATABASE " + getSchema() + ";"));
            checkExecution(SQLExecutor.execute(dbconn, "USE " + getSchema() +";"));
        } else {
            SQLExecutor.execute(dbconn, "DROP TABLE TEST");
        }

        String sql = "CREATE TABLE TEST(id integer primary key)";
        if (isMySQL()) {
            sql += " ENGINE=InnoDB";
            createRentalTable();
        }

        info = SQLExecutor.execute(dbconn, sql);
        checkExecution(info);
    }
    
    @Override
    public void tearDown() throws Exception {
        try {
            if (dbconn.getJDBCConnection() == null || dbconn.getJDBCConnection().isClosed()) {
                return;
            }
        } catch (SQLException e) {
            // do nothing
        }
        
        if (isMySQL()) {
            checkExecution(SQLExecutor.execute(dbconn, "DROP DATABASE IF EXISTS " + getSchema() +";"));
        } else {
            SQLExecutor.execute(dbconn, "DROP TABLE TEST");
        }        
    }

    private void createRentalTable() throws Exception {
        assertTrue(isMySQL());

        String sql = "USE " + getSchema() + "; CREATE TABLE rental ( " +
          "rental_id INT NOT NULL AUTO_INCREMENT, " +
          "rental_date DATETIME NOT NULL, " +
          "inventory_id MEDIUMINT UNSIGNED NOT NULL, " +
          "customer_id SMALLINT UNSIGNED NOT NULL, " +
          "return_date DATETIME DEFAULT NULL, " +
          "staff_id TINYINT UNSIGNED NOT NULL, " +
          "last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
          "PRIMARY KEY (rental_id), " +
          "UNIQUE KEY  (rental_date,inventory_id,customer_id), " +
          "KEY idx_fk_inventory_id (inventory_id), " +
          "KEY idx_fk_customer_id (customer_id), " + 
          "KEY idx_fk_staff_id (staff_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        checkExecution(SQLExecutor.execute(dbconn, sql));
}

    private boolean isMySQL() {
        return dbconn.getDriverClass().equals("com.mysql.jdbc.Driver"); // NOI8N
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
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, "SELECT * FROM TEST;");
        checkExecution(info);
        assertTrue(info.getStatementInfos().size() == 1);

        info = SQLExecutor.execute(dbconn, "SELECT * FROM TEST; SELECT id FROM TEST;");
        checkExecution(info);
        assertTrue(info.getStatementInfos().size() == 2);
    }

    public void testBadExecute() throws Exception {
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, "SELECT * FROM BADTABLE;");

        assertTrue(info.hasExceptions());
    }
        
    public void testDelimiter() throws Exception {
        String sql = "SELECT * FROM TEST;\n--Here is a comment\nDELIMITER ??\n SELECT * FROM TEST??\n " +
                "--Another comment\n DELIMITER ;\nSELECT * FROM TEST;";
        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        checkExecution(info);

        info = SQLExecutor.execute(dbconn,
                "DELIMITER ??\nSELECT * FROM TEST?? DELIMITER ;\nSELECT * FROM TEST;");
        checkExecution(info);

        info = SQLExecutor.execute(dbconn,
                "/** a block comment */\nDELIMITER ??\nSELECT * FROM TEST??");
        checkExecution(info);

        info = SQLExecutor.execute(dbconn,
                "DELIMITER ??\nSELECT * FROM TEST;");

        assertTrue(info.hasExceptions());
    }

    public void testPoundComment() throws Exception {
        checkExecution(SQLExecutor.execute(dbconn, 
                "#This is a comment\nSELECT * FROM TEST; #This is a comment at the end of the line\n" +
                "SELECT * FROM TEST; # Another eol comment"));
    }
    
    private void checkExecution(SQLExecutionInfo info) throws Exception {
        assertNotNull(info);

        if (info.hasExceptions()) {
            for (StatementExecutionInfo stmtinfo : info.getStatementInfos()) {
                if (stmtinfo.hasExceptions()) {
                    System.err.println("The following SQL had exceptions:");
                } else {
                    System.err.println("The following SQL executed cleanly:");
                }
                System.err.println(stmtinfo.getSQL());

                for (Throwable t : stmtinfo.getExceptions()) {
                    t.printStackTrace();
                }
            }

            throw new Exception("Executing SQL generated exceptions - see output for details");
        }        
    }
    
    public void testMySQLStoredFunction() throws Exception {
        if (! isMySQL()) {
            return;
        }

       SQLExecutor.execute(dbconn, "DROP FUNCTION inventory_in_stock");
       SQLExecutor.execute(dbconn, "DROP FUNCTION inventory_held_by_customer");
        
       String sql =
            "DELIMITER $$\n" +
            "CREATE FUNCTION inventory_held_by_customer(p_inventory_id INT) RETURNS INT " +
            "READS SQL DATA " +
            "BEGIN " +
              "DECLARE v_customer_id INT; # Testing comment in this context\n" +
              "DECLARE EXIT HANDLER FOR NOT FOUND RETURN NULL; # Another comment\n" +
              "SELECT customer_id INTO v_customer_id " +
              "FROM rental " +
              "WHERE return_date IS NULL " +
              "AND inventory_id = p_inventory_id; " +
              "RETURN v_customer_id; " +
            "END $$ " +
            "DELIMITER ;\n" +

            "DELIMITER $$\n" +
            "CREATE FUNCTION inventory_in_stock(p_inventory_id INT) RETURNS BOOLEAN " +
            "READS SQL DATA " +
            "BEGIN " +
            "    DECLARE v_rentals INT; #Testing comment in this context\n" +
            "    DECLARE v_out     INT; #Another comment\n" +
            
            "    #AN ITEM IS IN-STOCK IF THERE ARE EITHER NO ROWS IN THE rental TABLE\n" +
            "    #FOR THE ITEM OR ALL ROWS HAVE return_date POPULATED\n" +
            "    SELECT COUNT(*) INTO v_rentals " +
            "    FROM rental " +
            "    WHERE inventory_id = p_inventory_id; " +
            "    IF v_rentals = 0 THEN " +
            "      RETURN TRUE; " +
            "    END IF; " +
            "    SELECT COUNT(rental_id) INTO v_out " +
            "    FROM inventory LEFT JOIN rental USING(inventory_id) " +
            "    WHERE inventory.inventory_id = p_inventory_id " +
            "    AND rental.return_date IS NULL; " +
            "    IF v_out > 0 THEN " +
            "      RETURN FALSE; " +
            "    ELSE " +
            "      RETURN TRUE; " +
            "    END IF; " +
            "END";

        checkExecution(SQLExecutor.execute(dbconn, sql));
    }
    
    public void testNewLines() throws Exception {
        if (! isMySQL()) {
            return;
        }

        // In order for stored procs to be readable when querying them in the
        // database, we have to properly propagate newlines.
        SQLExecutor.execute(dbconn, "DROP FUNCTION inventory_held_by_customer");

        String sql =
            "DELIMITER $$\n" +
            "CREATE FUNCTION inventory_held_by_customer(p_inventory_id INT) RETURNS INT\n" +
            "READS SQL DATA\n" +
            "BEGIN\n" +
              "  DECLARE v_customer_id INT; # Testing comment in this context\n" +
              "  DECLARE EXIT HANDLER FOR NOT FOUND RETURN NULL; # Another comment\n" +
              "  SELECT customer_id INTO v_customer_id\n" +
              "  FROM rental\n" +
              "  WHERE return_date IS NULL\n" +
              "  AND inventory_id = p_inventory_id;\n" +
              "  RETURN v_customer_id;\n" +
            "END$$\n" +
            "DELIMITER ;\n";

        String body = "BEGIN\n" +
            "  DECLARE v_customer_id INT; \n" +
            "  DECLARE EXIT HANDLER FOR NOT FOUND RETURN NULL; \n" +
            "  SELECT customer_id INTO v_customer_id\n" +
            "  FROM rental\n" +
            "  WHERE return_date IS NULL\n" +
            "  AND inventory_id = p_inventory_id;\n" +
            "  RETURN v_customer_id;\n" +
            "END";

        SQLExecutionInfo info = SQLExecutor.execute(dbconn, sql);
        String resultingSQL = info.getStatementInfos().get(0).getSQL();
        if ( ! resultingSQL.contains(body)) {
            System.err.println("Resulting SQL did not contain body - check for newlines:\n" +
                    resultingSQL);
            fail();
        }
        checkExecution(info);

        Connection conn = dbconn.getJDBCConnection();
        ResultSet rs = conn.createStatement().executeQuery("show create function inventory_held_by_customer");
        assertTrue(rs.next());
        String functionText = rs.getString("Create Function");
        assertNotNull(functionText);

        //It looks like the JDBC driver strips off the newlines.  How convenient - it
        // makes the function text completely unreadable when querying the database
        String bodyNoNewlines = body.replace("\n", " ");
        if (! functionText.contains(bodyNoNewlines)) {
            System.err.println("Function text did not contain body (it is likely that newlines did not match): \n" + functionText);
            fail();
        }
    }

}