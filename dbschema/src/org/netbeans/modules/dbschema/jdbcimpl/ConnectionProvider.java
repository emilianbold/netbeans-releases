/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
 
package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.ResourceBundle;

public class ConnectionProvider {
    
	final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle"); // NOI18N

    private Connection con;
    private DatabaseMetaData dmd;
  
    private String driver;
    private String url;
    private String username;
    private String password;
    private String schema;
    
    /** Creates new ConnectionProvider */
    public ConnectionProvider(Connection con, String driver) throws SQLException{
        this.con = con;
        this.driver = driver;
        dmd = con.getMetaData();
    }
    
    public ConnectionProvider(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    
        Class.forName(driver);
        con = DriverManager.getConnection(url, username, password);
        dmd = con.getMetaData();
    }
  
    public Connection getConnection() {
        return con;
    }
  
    public DatabaseMetaData getDatabaseMetaData() throws SQLException {
        return dmd;
    }

    public String getDriver() {
        return driver;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void closeConnection() {
        if (con != null)
            try {
                con.close();
            } catch (SQLException exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    System.out.println(bundle.getString("UnableToCloseConnection")); //NOI18N
                con = null;
            }
    }
}
