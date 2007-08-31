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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.CreateView;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.TableColumn;
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

    private static int    identRule = RULE_UNDEFINED;
    private static int    quotedIdentRule = RULE_UNDEFINED;

    protected static SpecificationFactory specfactory;
    
    protected Connection conn;
    protected Specification spec;
    protected DriverSpecification drvSpec;

    static {
        try {
            specfactory = new SpecificationFactory();

            driverClass = System.getProperty(DRIVER_PROPERTY, 
                    "org.apache.derby.jdbc.EmbeddedDriver");
            dbname = System.getProperty(DBNAME_PROPERTY, "ddltestdb");
            
            dblocation = System.getProperty(DBDIR_PROPERTY, "");
            
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
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }

    public DBTestBase(String name) {
        super(name);
    }

    
    public void setUp() throws Exception {
        try {
            getConnection();
            createSchema();
            setSchema();
            initQuoteString();
            spec = (Specification)specfactory.createSpecification(conn);
            
            drvSpec = specfactory.createDriverSpecification(
                    spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals(
                    "jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - copied from mainline code
                drvSpec.setMetaData(conn.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            
            drvSpec.setCatalog(conn.getCatalog());
            drvSpec.setSchema(SCHEMA);
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
    
    private void initQuoteString() throws Exception {
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
        if ( identRule == RULE_UNDEFINED ) {
            getIdentRules();
        }
        
        if ( isQuoted(ident) ) {
            switch ( quotedIdentRule ) {
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
                            identRule + ", assuming case is retained");
            }
            
            return ident.substring(1, ident.length() -1);
        } else {
            switch ( identRule ) {
                case UC_RULE:
                    return ident.toUpperCase();
                case LC_RULE:
                    return ident.toLowerCase();
                case MC_RULE:
                    return ident;
                default:
                    LOGGER.log(Level.WARNING, "Unexpected identifier rule: +" +
                            identRule + ", assuming upper case");
                    return ident.toUpperCase();
            }            
        }
    }
    
    protected boolean isQuoted(String ident) {
        assert quoteString != null;
        
        return ident.startsWith(quoteString) && ident.endsWith(quoteString);
    }
    
    private void getIdentRules() throws Exception {
        assert conn != null;

        DatabaseMetaData md;
        
        try {
            md = conn.getMetaData();
            if ( md.storesUpperCaseIdentifiers() ) {
                identRule = UC_RULE;
            } else if ( md.storesLowerCaseIdentifiers() ) {
                identRule = LC_RULE;
            } else if ( md.storesMixedCaseIdentifiers() ) {
                identRule = MC_RULE;
            } else {
                identRule = UC_RULE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores unquoted identifiers, assuming upper case: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            identRule = UC_RULE;
        }

        try {
            md = conn.getMetaData();
            
            if ( md.storesLowerCaseQuotedIdentifiers() ) {
                quotedIdentRule = LC_RULE;
            } else if ( md.storesUpperCaseQuotedIdentifiers() ) {
                quotedIdentRule = UC_RULE;
            } else if ( md.storesMixedCaseQuotedIdentifiers() ) {
                quotedIdentRule = MC_RULE;
            } else {
                quotedIdentRule = QUOTE_RETAINS_CASE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores quoted identifiers, assuming case is retained: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            quotedIdentRule = QUOTE_RETAINS_CASE;
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
    
    protected void createBasicTable(String tablename, String pkeyName) 
            throws Exception {
        dropTable(tablename);
        CreateTable cmd = spec.createCommandCreateTable(tablename);
        cmd.setObjectOwner(SCHEMA);
                
        // primary key
        TableColumn col = cmd.createPrimaryKeyColumn(pkeyName);
        col.setColumnType(Types.INTEGER);
        col.setNullAllowed(false);
        
        cmd.execute();
    }
    
    protected void createView(String viewName, String query) throws Exception {
        CreateView cmd = spec.createCommandCreateView(viewName);
        cmd.setQuery(query);
        cmd.setObjectOwner(SCHEMA);
        cmd.execute();
        
        assertFalse(cmd.wasException());        
    }

    protected void createSimpleIndex(String tablename, 
            String indexname, String colname) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
        xcmd.setIndexName(indexname);

        // *not* unique
        xcmd.setIndexType(new String());

        xcmd.setObjectOwner(SCHEMA);
        xcmd.specifyColumn(fixIdentifier(colname));

        xcmd.execute();        
    }
    
    /**
     * Adds a basic column.  Non-unique, allows nulls.
     */
    protected void addBasicColumn(String tablename, String colname,
            int type, int size) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        AddColumn cmd = spec.createCommandAddColumn(tablename);
        cmd.setObjectOwner(SCHEMA);
        TableColumn col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(type);
        col.setColumnSize(size);
        col.setNullAllowed(true);
        
        cmd.execute();
        if ( cmd.wasException() ) {
            throw new Exception("Unable to add column");
        }
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
