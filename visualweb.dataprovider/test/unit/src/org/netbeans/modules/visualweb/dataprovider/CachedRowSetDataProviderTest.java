/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataprovider;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.sql.rowset.CachedRowSetXImpl;
import java.io.File;
import junit.framework.TestCase;
import com.sun.data.provider.impl.CachedRowSetDataProvider;


import java.beans.Beans;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * This class makes sure that the rowset behaves correctly around what should
 * happen if the rowset is executed, not executed, modified, etc.
 * 
 * @author David
 */
public class CachedRowSetDataProviderTest extends TestCase {
    
    private static final Logger LOGGER = 
            Logger.getLogger(CachedRowSetDataProviderTest.class.getName());
    
    private static final String DBURL = "jdbc:derby:mydb;create=true";
    private static final String TABLENAME = "mytable";
    private static final String IDNAME = "id";
    private static final String COL1NAME = "col1"; 
    private static final String COL2NAME = "col2";
    private static final String ROWID = "row1";  
    private static final int NUMROWS = 10;
    
    public CachedRowSetDataProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        try {
            super.setUp();
            
            // Comment this out to turn off debugging
            LOGGER.setLevel(Level.FINE);

            initDatabase();
        } catch ( SQLException sqle ) {
            reportSQLException(sqle);
        } catch ( Throwable t ) {
            LOGGER.log(Level.SEVERE, "Failed to set up test", t);
            throw new Exception(t);
        }
    }
    
    private void reportSQLException(SQLException sqle) {
        LOGGER.log(Level.SEVERE, null, sqle);
        
        if ( sqle.getNextException() != null ) {
            reportSQLException(sqle.getNextException());
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        // Remove the test database so it's not left lying around
        String userdir = System.getProperty("user.dir");
        File dbdir = new File (userdir + "/" + "mydb");
        
        LOGGER.log(Level.INFO, "userdir is " + userdir);
        
        deleteRecursively(dbdir);
        
        File logfile = new File(userdir + "/derby.log");
        if ( logfile.exists() ) { 
            logfile.delete();
        }
    }
    
    private void deleteRecursively(File file) throws Exception {
        if ( !file.exists() ) {
            return;
        }
        
        if ( ! file.isDirectory() ) {
            file.delete();
            return;
        }
        
        File [] children = file.listFiles();
        
        for ( int i = 0 ; i < children.length ; i++ ) {
            deleteRecursively(children[i]);
        }
        
        file.delete();
    }
    
    private void initDatabase() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection(DBURL);
        
        try {
            conn.prepareStatement("DROP TABLE " + TABLENAME).execute();
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.FINE, null, sqle);
        }
        
        String create = "CREATE TABLE " + TABLENAME + "(" + 
                IDNAME + " int primary key, " +
                COL1NAME + " varchar(255),  " +
                COL2NAME + " varchar(255))";
        
        conn.prepareStatement(create).execute();
        
        PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO " + TABLENAME  + " VALUES(?, ?, ?)");
        
        for ( int i = 1; i <= NUMROWS ; i++) {
            insert.setInt(1, i);
            insert.setString(2, "col1_" + i);
            insert.setString(3, "col2_" + i);
            
            insert.execute();
        }            
    }
        
    
    /**
     * Make sure the data provider detects a change to the rowset command 
     * 
     * @throws java.lang.Exception
     */
    public void testCommandChange() throws Exception {
       CachedRowSetXImpl rowset = new CachedRowSetXImpl(); 
       
       CachedRowSetDataProvider provider = new CachedRowSetDataProvider();
       provider.setCachedRowSet(rowset);

       rowset.setUrl("jdbc:derby:mydb;create=true");
       
       /** 
        * Select only one row and one extra column
        */
       rowset.setCommand("SELECT " + IDNAME + ", " + COL1NAME +
               " FROM " + TABLENAME +
               " WHERE " + IDNAME + " = 2");
       
       rowset.setTableName(TABLENAME);
       
       checkRows(provider, 1, 2);
       
       rowset.setCommand("SELECT " + IDNAME + " FROM " +
               TABLENAME );
       
       
       checkRows(provider, NUMROWS, 1);
       
       provider.close();
    }
    
    public void testInsertRow() {
        try {
            int newRowCount = 0;
            int currentRowCount = 0;
            CachedRowSetXImpl rowset = new CachedRowSetXImpl();
            Beans beans = new Beans();
            beans.setDesignTime(true);
            CachedRowSetDataProvider provider = new CachedRowSetDataProvider();
            provider.setCachedRowSet(rowset);
            currentRowCount = getRowCount(rowset);
            
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            Connection conn = DriverManager.getConnection(DBURL);           

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO " + TABLENAME + " VALUES(?, ?, ?)");

            for (int i = 1; i <= NUMROWS; i++) {
                insert.setInt(1, i);
                insert.setString(2, "col1_" + i);
                insert.setString(3, "col2_" + i);

                insert.execute();
            }
            
            newRowCount = getRowCount(rowset);           
            assertEquals(newRowCount, currentRowCount + 1);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (SQLException sqle) {
            LOGGER.log(Level.FINE, null, sqle);
        }

    }
    
    public void testRevertChanges() {
        int newRowCount = 0;
        int currentRowCount = 0;
        
        try {
            CachedRowSetXImpl rowset = new CachedRowSetXImpl();
            Beans beans = new Beans();
            beans.setDesignTime(true);
            CachedRowSetDataProvider provider = new CachedRowSetDataProvider();
            provider.setCachedRowSet(rowset);

            /* Select only one row and one extra column
             */
            rowset.setCommand("SELECT " + IDNAME + ", " + COL1NAME + " FROM " + TABLENAME + " WHERE " + IDNAME + " = 1");

            rowset.setTableName(TABLENAME);

            //commit changes
            rowset.execute();
            provider.commitChanges();
            currentRowCount = getRowCount(rowset);   

            // add a new row
            FieldKey fk = new FieldKey(IDNAME);
            RowKey rk = new RowKey(ROWID);
            provider.setValue(fk, rk, beans);
            provider.appendRow();
            newRowCount = getRowCount(rowset);   
            // test revert changes
            provider.revertChanges();      
            assertEquals(newRowCount, currentRowCount);

        } catch (SQLException sqle) {   
            LOGGER.log(Level.FINE, null, sqle);
        }            
    }
    
        
    private void checkRows(CachedRowSetDataProvider provider,
            int expectedRows, int expectedFields) {
        int numrows = provider.getRowCount();
        assert(numrows == expectedRows);
        
        FieldKey[] keys = provider.getFieldKeys();
        int numkeys = keys.length;
        
        assert(numkeys == expectedFields);
        
        provider.cursorFirst();

        for ( int i = 0 ; i < numrows ; i++ ) {
            for ( int j = 0 ; j < numkeys ; j++ ) {
                Object value = provider.getValue(keys[j]);
                assert(value != null);
            }
        }
    }
    
    private int getRowCount(CachedRowSetXImpl rowset) {

        CachedRowSetDataProvider provider = new CachedRowSetDataProvider();
        provider.setCachedRowSet(rowset);

        // Select only one row and one extra column
        try {
            rowset.setUrl("jdbc:derby:mydb;create=true");
            rowset.setCommand("SELECT " + IDNAME + ", " + COL1NAME + " FROM " + TABLENAME + " WHERE " + IDNAME + " = 2");
            rowset.setTableName(TABLENAME);

            //commit changes
            rowset.execute();
            
        } catch (SQLException sqle) {
            LOGGER.log(Level.FINE, null, sqle);
        }
        
        return provider.getRowCount();
    }


}

