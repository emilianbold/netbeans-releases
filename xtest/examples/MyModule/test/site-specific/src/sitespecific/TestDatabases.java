/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package sitespecific;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/** Example of site specific test case.
 */
public class TestDatabases extends NbTestCase {
    
    public TestDatabases(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(TestDatabases.class));
    }
    
    /** URL of database */
    public String url;
    /** driver class */
    public String driver;
    /** username */
    public String username;
    /** password */
    public String password;
    
    /** Sets connection properties from system properties.
     */
    public void setUp() {
        driver = System.getProperty("driver");
        if(driver==null) {
            fail("Property driver hasn't been set");
        }
        url = System.getProperty("url");
        if(url==null) {
            fail("Property url hasn't been set");
        }
        username = System.getProperty("username");
        if(username==null) {
            fail("Property username hasn't been set");
        }
        password = System.getProperty("password");
        if(password==null) {
            fail("Property password hasn't been set");
        }
    }
    
    
    /** Tries to establish connection to a database specified by driver,
     * url, username and password properties. These properties have to be set
     * before test execution.
     * It also writes connection properties to log file.
     */
    public void testConnection() {
        log("driver="+driver);
        log("url="+url);
        log("username="+username);
        log("password="+password);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            fail("Driver not found: "+e.getMessage());
        }
        Connection con;
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            fail("Connection not established: "+e.getMessage());
        }
    }
}
