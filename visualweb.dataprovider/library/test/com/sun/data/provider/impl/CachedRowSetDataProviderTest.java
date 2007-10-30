/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.data.provider.impl;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.sql.rowset.CachedRowSetXImpl;
import junit.framework.TestCase;


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
    
    private static final int NUMROWS = 10;
    
    public CachedRowSetDataProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        try {
            super.setUp();
            initDatabase();
        } catch ( Throwable t ) {
            LOGGER.log(Level.SEVERE, "Failed to set up test", t);
            throw new Exception(t);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void initDatabase() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection(DBURL);
        
        try {
            conn.prepareStatement("DROP TABLE " + TABLENAME).execute();
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, null, sqle);
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
       rowset.setUrl("jdbc:derby:mydb;create=true");
       
       CachedRowSetDataProvider provider = new CachedRowSetDataProvider();
       provider.setCachedRowSet(rowset);
       
       /** 
        * Select only one row and one extra column
        */
       rowset.setCommand("SELECT " + IDNAME + ", " + COL1NAME +
               " FROM " + TABLENAME +
               " WHERE " + IDNAME + " = 2");
       rowset.setTableName(TABLENAME);
       
       FieldKey[] keys = provider.getFieldKeys();
       

       checkRows(provider, 1, 2);
       
       rowset.setCommand("SELECT " + IDNAME + " FROM " +
               TABLENAME );
       
       
       checkRows(provider, this.NUMROWS, 1);
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


}

