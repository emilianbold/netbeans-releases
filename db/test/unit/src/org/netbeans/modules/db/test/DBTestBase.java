/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.test;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.test.TestBase;

/**
 * This class is a useful base test class that provides initial setup
 * to get a connecxtion and also a number of utility routines
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public abstract class DBTestBase extends TestBase {

    private static final Logger LOGGER = 
            Logger.getLogger(DBTestBase.class.getName());
    
    // Change this to get rid of output or to see output
    protected static final Level DEBUGLEVEL = Level.FINE;

    protected static final String SCHEMA = "TESTDDL";

    private static String driverClass;
    private static String dbUrl;
    private static String username;
    private static String password;
    private static String dbname;
    protected static String dblocation;
    private static URL driverJarUrl;
    
    private static String DRIVER_PROPERTY = "db.driverclass";
    private static String URL_PROPERTY = "db.url";
    private static String USERNAME_PROPERTY = "db.username";
    private static String PASSWORD_PROPERTY = "db.password";
    private static String DBDIR_PROPERTY = "db.dir";
    private static String DBNAME_PROPERTY = "db.name";

    private static String quoteString = null;
    
    // This defines what happens to identifiers when stored in db
    private static final int RULE_UNDEFINED = -1;
    public static final int LC_RULE = 0; // everything goes to lower case
    public static final int UC_RULE = 1; // everything goes to upper case
    public static final int MC_RULE = 2; // mixed case remains mixed case
    public static final int QUOTE_RETAINS_CASE = 3; // quoted idents retain case

    private static int    unquotedCaseRule = RULE_UNDEFINED;
    private static int    quotedCaseRule = RULE_UNDEFINED;

    private static JDBCDriver jdbcDriver;
    private static DatabaseConnection dbConnection;
    
    protected Connection conn;

    public DBTestBase(String name) {
        super(name);
    }

    protected static JDBCDriver getJDBCDriver() throws Exception{
        if (jdbcDriver == null) {
            jdbcDriver = JDBCDriver.create("derbydriver", "derbydriver", driverClass, new URL[] {driverJarUrl});
            assertNotNull(jdbcDriver);
            JDBCDriverManager.getDefault().addDriver(jdbcDriver);
        }

        return jdbcDriver;

    }

    /**
     * Get the DatabaseConnection for the configured Java DB database.  This
     * method will create and register the connection the first time it is called
     */
    protected static DatabaseConnection getDatabaseConnection() throws Exception {
        if (dbConnection == null) {
            JDBCDriver driver = getJDBCDriver();

            dbConnection = DatabaseConnection.create(driver, dbUrl, username, "APP", password, false);
            ConnectionManager.getDefault().addConnection(dbConnection);
        }

        return dbConnection;
    }

    protected static String getDbUrl() {
        return dbUrl;
    }

    protected static String getDriverClass() {
        return driverClass;
    }

    protected static String getPassword() {
        return password;
    }

    public static String getUsername() {
        return username;
    }

    
    @Override
    protected void setUp() throws Exception {
        driverClass = System.getProperty(DRIVER_PROPERTY,
                "org.apache.derby.jdbc.EmbeddedDriver");
        dbname = System.getProperty(DBNAME_PROPERTY, "ddltestdb");

        clearWorkDir();
        dblocation = System.getProperty(DBDIR_PROPERTY, getWorkDirPath());

        // Add a slash for the Derby URL syntax if we are
        // requesting a specific path for database files
        if ( dblocation.length() > 0 ) {
            dblocation = dblocation + "/";
        }

        LOGGER.log(DEBUGLEVEL, "DB location is " + dblocation);

        dbUrl = System.getProperty(URL_PROPERTY,
                "jdbc:derby:" + dblocation + dbname + ";create=true");

        LOGGER.log(DEBUGLEVEL, "DB URL is " + dbUrl);

        username = System.getProperty(USERNAME_PROPERTY, "testddl");
        password = System.getProperty(PASSWORD_PROPERTY, "testddl");

        driverJarUrl = Class.forName(driverClass).getProtectionDomain().getCodeSource().getLocation();

        try {
            getConnection();
            createSchema();
            setSchema();
            initQuoteString();
        } catch ( SQLException e ) {
            SQLException original = e;
            while ( e != null ) {
                LOGGER.log(Level.SEVERE, null, e);
                e = e.getNextException();
            }
            
            throw original;
        }
    }

    protected Connection getConnection() throws Exception {
        return getConnection(false);
    }
    
    private void shutdownDerby() throws Exception {
        Connection conn = getConnection(true);
        
        try { 
            conn.close();
        } catch ( SQLException sqle ) {
            
        }
    }
    
    private Connection getConnection(boolean shutdown) throws Exception {
        String url;
        
        if ( shutdown ) {
            url = dbUrl + ";shutdown=true";
        } else {
            url = dbUrl;
        }
        
        Class.forName(driverClass);
        conn = DriverManager.getConnection(dbUrl, username, password);
        return conn;
    }
    
    protected void createSchema() throws Exception {
        dropSchema();
        conn.createStatement().executeUpdate("CREATE SCHEMA " + SCHEMA);
    }
    
    protected void dropSchema() throws Exception {
        if ( ! schemaExists(SCHEMA) ) {
            return;
        }
        
        assert (conn != null);

        // drop views first, as they depend on tables
        DatabaseMetaData md = conn.getMetaData();
        
        ResultSet rs = md.getTables(null, SCHEMA, null, 
                new String[] { "VIEW" } );
        Vector views = new Vector();
        while ( rs.next() ) {
            String view = rs.getString(3);
            LOGGER.log(DEBUGLEVEL, "view in schema: " + view);
            views.add(view);
        }
        rs.close();
        
        setSchema();

        Iterator it = views.iterator();        
        while (it.hasNext()) {
            String view = (String)it.next();
            dropView(view);
        }
        
        // drop all tables
        md = conn.getMetaData();
        
        rs = md.getTables(null, SCHEMA, null, null);
        Vector tables = new Vector();
        while ( rs.next() ) {
            String table = rs.getString(3);
            LOGGER.log(DEBUGLEVEL, "table in schema: " + table);
            tables.add(table);
        }
        rs.close();
        
        setSchema();

        it = tables.iterator();        
        while (it.hasNext()) {
            String table = (String)it.next();
            dropTable(table);
        }
        
        // drop schema
        try {
            conn.createStatement().executeUpdate(
                    "DROP SCHEMA " + SCHEMA + " RESTRICT");
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, null, e);
            LOGGER.log(DEBUGLEVEL, "Got an exception when attempting to " +
                    "drop the schema: " + e.getMessage());
        }
    }
    
    protected boolean schemaExists(String schemaName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        
        ResultSet rs  = md.getSchemas();
        
        while ( rs.next() ) {
            if ( schemaName.equals(rs.getString(1))) {
                return true;
            }
        }
    
        return false;
    }
    
    protected void setSchema() throws Exception {
        PreparedStatement stmt = conn.prepareStatement("SET SCHEMA " + SCHEMA);
        stmt.executeUpdate();
    }

    protected void dropView(String viewname) {
        try {
            conn.createStatement().executeUpdate(
                    "DROP VIEW " + viewname);
        } catch ( Exception e ) {
            LOGGER.log(Level.FINE, null, e);
            LOGGER.log(DEBUGLEVEL, "Got exception trying to drop view " +
                    viewname + ": " + e);
        }
    }
    protected void dropTable(String tablename) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE " + tablename);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, null, e);
            LOGGER.log(DEBUGLEVEL, "Got exception trying to drop table " +
                    tablename + ": " + e);
        }
    }
    
    protected void initQuoteString() throws Exception {
        if ( quoteString != null  ) {
            return;
        }
        
        DatabaseMetaData md = conn.getMetaData();
        quoteString = md.getIdentifierQuoteString();
    }
    
    protected String quote(String value) throws Exception {
        if ( value == null  || value.equals("") )
        {
            return value;
        }
        
        if ( quoteString == null ) {
            initQuoteString();
        }
        
        return quoteString + value + quoteString;
    }
    
    protected boolean tableExists(String tablename) throws Exception {
        tablename = fixIdentifier(tablename);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, SCHEMA, tablename, null);
        return rs.next();        
    }
    
    protected boolean columnExists(String tablename, String colname)
            throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getColumns(null, SCHEMA, tablename, colname);
        
        int numrows = printResults(
                rs, "columnExists(" + tablename + ", " + colname + ")");
        
        rs.close();
        
        return numrows > 0;
    }
    
    protected boolean indexExists(String tablename, String indexname)
            throws Exception {
        indexname = fixIdentifier(indexname);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, SCHEMA, tablename, false, false);

        while ( rs.next() ) {
            String idx = rs.getString(6);
            if ( idx.equals(indexname)) {
                return true;
            }
        }
        
        return false;
    }
    
    protected boolean viewExists(String viewName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, SCHEMA, fixIdentifier(viewName),
                new String[] {"VIEW"});
        
        return rs.next();
    }
    
    protected boolean columnInPrimaryKey(String tablename, String colname) 
        throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getPrimaryKeys(null, SCHEMA, tablename);
        
        // printResults(rs, "columnInPrimaryKey(" + tablename + ", " +
        //        colname + ")");
                
        while ( rs.next() ) {
            String pkCol = rs.getString(4);
            if ( pkCol.equals(colname)) {
                return true;
            }
        }
        
        return false;
    }

    protected void printAllTables() throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, SCHEMA, "%", null);
        printResults(rs, "printAllTables()");
    }

    protected boolean columnInIndex(String tablename, String colname, 
            String indexname) throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        indexname = fixIdentifier(indexname);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, SCHEMA, tablename, false, false);

        // printResults(rs, "columnInIndex(" + tablename + ", " + colname + 
        //    ", " + indexname + ")");

        while ( rs.next() ) {
            String ixName = rs.getString(6);
            if ( ixName != null && ixName.equals(indexname)) {
                String ixColName = rs.getString(9);
                if ( ixColName.equals(colname) ) {
                    return true;
                }
            }
        }

        return false;
    }
    
    protected boolean columnInAnyIndex(String tablename, String colname)
            throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, SCHEMA, tablename, false, false);

        // printResults(rs, "columnInIndex(" + tablename + ", " + colname + 
        //    ", " + indexname + ")");

        while ( rs.next() ) {
        String ixName = rs.getString(6);
            String ixColName = rs.getString(9);
            if ( ixColName.equals(colname) ) {
                return true;
            }
        }

        return false;        
    }
    
    protected boolean indexIsUnique(String tablename, String indexName)
            throws Exception {
        tablename = fixIdentifier(tablename);
        indexName = fixIdentifier(indexName);
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, SCHEMA, tablename, false, false);
        
        // TODO - Parse results
        
        rs.close();
        return true;
    }
 
    /**
     * Fix an identifier for a metadata call, as the metadata APIs
     * require identifiers to be in proper case
     */
    public String fixIdentifier(String ident) throws Exception {
        if ( unquotedCaseRule == RULE_UNDEFINED ) {
            getCaseRules();
        }
        
        if ( isQuoted(ident) ) {
            switch ( quotedCaseRule ) {
                case QUOTE_RETAINS_CASE:
                    break;
                case UC_RULE:
                    ident = ident.toUpperCase();
                    break;
                case LC_RULE:
                    ident = ident.toLowerCase();
                    break;
                case MC_RULE:
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Unexpected identifier rule: +" +
                            unquotedCaseRule + ", assuming case is retained");
            }
            
            return ident.substring(1, ident.length() -1);
        } else {
            switch ( unquotedCaseRule ) {
                case UC_RULE:
                    return ident.toUpperCase();
                case LC_RULE:
                    return ident.toLowerCase();
                case MC_RULE:
                    return ident;
                default:
                    LOGGER.log(Level.WARNING, "Unexpected identifier rule: +" +
                            unquotedCaseRule + ", assuming upper case");
                    return ident.toUpperCase();
            }            
        }
    }
    
    protected boolean isQuoted(String ident) {
        assert quoteString != null;
        
        return ident.startsWith(quoteString) && ident.endsWith(quoteString);
    }
    
    public int getUnquotedCaseRule() throws Exception {
        getCaseRules();
        return unquotedCaseRule;
    }
    
    private void getCaseRules() throws Exception {
        assert conn != null;

        DatabaseMetaData md;
        
        try {
            md = conn.getMetaData();
            if ( md.storesUpperCaseIdentifiers() ) {
                unquotedCaseRule = UC_RULE;
            } else if ( md.storesLowerCaseIdentifiers() ) {
                unquotedCaseRule = LC_RULE;
            } else if ( md.storesMixedCaseIdentifiers() ) {
                unquotedCaseRule = MC_RULE;
            } else {
                unquotedCaseRule = UC_RULE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores unquoted identifiers, assuming upper case: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            unquotedCaseRule = UC_RULE;
        }

        try {
            md = conn.getMetaData();
            
            if ( md.storesLowerCaseQuotedIdentifiers() ) {
                quotedCaseRule = LC_RULE;
            } else if ( md.storesUpperCaseQuotedIdentifiers() ) {
                quotedCaseRule = UC_RULE;
            } else if ( md.storesMixedCaseQuotedIdentifiers() ) {
                quotedCaseRule = MC_RULE;
            } else {
                quotedCaseRule = QUOTE_RETAINS_CASE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores quoted identifiers, assuming case is retained: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            quotedCaseRule = QUOTE_RETAINS_CASE;
        }
    }
    
    protected int printResults(ResultSet rs, String queryName) 
            throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numcols = rsmd.getColumnCount();
        int numrows = 0;
        
        LOGGER.log(DEBUGLEVEL, "RESULTS FROM " + queryName);
        assert(rs != null);
        
        StringBuffer buf = new StringBuffer();

        buf.append("|");        
        for ( int i = 1 ; i <= numcols ; i++ ) {
            buf.append(rsmd.getColumnName(i) + "|");
        }
        LOGGER.log(DEBUGLEVEL, buf.toString());
        
        while ( rs.next() ) {
            numrows++;
            buf = new StringBuffer();
            buf.append("|");
            for ( int i = 1 ; i <= numcols ; i++ ) {
                buf.append(rs.getString(i) + "|");
            }
            LOGGER.log(DEBUGLEVEL, buf.toString());
        }
        
        return numrows;
    }
    
    protected void tearDown() throws Exception {
        if ( conn != null ) {
            try {
                conn.close();
                shutdownDerby();
            } catch ( SQLException sqle ) {
                LOGGER.log(Level.INFO, "Got exception closing connection: " +
                    sqle.getMessage());
            }
        }
    }       
}
