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

package org.netbeans.modules.db.mysql.sakila.test;

import java.io.File;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import org.junit.Before;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author David
 */
public class TestBase extends NbTestCase  {
    private String database;
    private String user;
    private String password;
    private String host;
    private String port;
    private static final String JARPATH="nbinst:///modules/ext/mysql-connector-java-5.1.6-bin.jar";

    private JDBCDriver jdbcDriver;
    private DatabaseConnection dbconn;

    public TestBase(String testName) {
        super(testName);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);

        // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
        // finds the driver jar file if the user is using the nbinst protocol
        File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File clusterDir = jarFile.getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", clusterDir.getAbsolutePath());
        
        getProperties();
    }

    protected DatabaseConnection getDatabaseConnection() throws Exception {
        JDBCDriver driver = getJDBCDriver();

        if (dbconn == null) {
            String url = "jdbc:mysql://" + host + ":" + port + "/";
            if (database != null) {
                url += database;
            }
            dbconn = DatabaseConnection.create(driver, url, user, null, password, true);
            ConnectionManager.getDefault().addConnection(dbconn);
            try {
                ConnectionManager.getDefault().connect(dbconn);
            } catch (DatabaseException e) {
                System.err.println("Unable to connect to the database");
                System.err.println("Please make sure the properties are set correctly in private.properties");
                System.err.println("Here are the possible values you can set:");
                System.err.println("");
                System.err.println("test-unit-sys-prop.db.user=<database-user> [default is root]");
                System.err.println("test-unit-sys-prop.db.password=<database-password> [default is none]");
                System.err.println("test-unit-sys-prop.db.host=<database-host> [default is localhost]");
                System.err.println("test-unit-sys-prop.db.port=<database-port> [default is 3306]");
                System.err.println("");
                throw e;
            }
            assertTrue(dbconn.getJDBCConnection() != null);
            assertFalse(dbconn.getJDBCConnection().isClosed());
        }

        return dbconn;
    }

    protected DatabaseConnection refreshDatabaseConnection() throws Exception {
        ConnectionManager.getDefault().removeConnection(dbconn);
        dbconn = null;
        return getDatabaseConnection();
    }

    protected JDBCDriver getJDBCDriver() throws Exception {
        if (jdbcDriver == null) {
            String driverClassName = "com.mysql.jdbc.Driver";
            jdbcDriver = JDBCDriver.create(driverClassName, driverClassName,
                    driverClassName, new URL[]{ new URL(JARPATH)});
            JDBCDriverManager.getDefault().addDriver(jdbcDriver);
        }

        return jdbcDriver;
    }

    protected void getProperties() throws Exception {
        user = System.getProperty("db.user", "root");
        password = System.getProperty("db.password", null);
        host = System.getProperty("db.host", "localhost");
        port = System.getProperty("db.port", "3306");

        // Make sure we the jar URL is valid
        assertNotNull(new URL(JARPATH).openStream());

    }

    public void checkExecution(SQLExecutionInfo info) throws Exception {
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

    public boolean tableExists(DatabaseConnection dbconn, String database, String tablename) throws Exception {
        DatabaseMetaData md = dbconn.getJDBCConnection().getMetaData();
        ResultSet rs = md.getTables(null, database, tablename, null);
        return rs.next();        
    }

    public void setDatabase(String dbname) {
        this.database = dbname;
    }

}
