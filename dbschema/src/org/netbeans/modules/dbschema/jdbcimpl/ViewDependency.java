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
import java.util.*;

public class ViewDependency {
    private Connection con;
    private String user;
    private String view;
    private DatabaseMetaData dmd;
    private LinkedList tables;
    private LinkedList columns;

    /** Creates new ViewDependency */
    public ViewDependency(ConnectionProvider cp, String user, String view) throws SQLException {
        con = cp.getConnection();
        this.user = user;
        this.view = view;
        dmd = cp.getDatabaseMetaData();
        
        tables = new LinkedList();
        columns = new LinkedList();
    }
    
    public LinkedList getTables() {
        return tables;
    }
    
    public LinkedList getColumns() {
        return columns;
    }

    public void constructPK() {
        try {
            String database = dmd.getDatabaseProductName();
            if (database==null){
                return;
            }  
            database = database.trim();
            if (database.equalsIgnoreCase("Oracle")) {
                getOraclePKTable(user, view);
                getOracleViewColumns();
                return;
            }
            
            if (database.equalsIgnoreCase("Microsoft SQL Server")) {
                getMSSQLServerPKTable(user, view);
                getMSSQLServerViewColumns();
                return;
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void getOraclePKTable(String user, String view) throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        
        String query = "select OWNER, REFERENCED_OWNER, REFERENCED_NAME, REFERENCED_TYPE from ALL_DEPENDENCIES where NAME = ? AND OWNER = ?";

        stmt = con.prepareStatement(query);
        stmt.setString(1, view);
        stmt.setString(2, user);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            String type = rs.getString(4).trim();
            if (type.equalsIgnoreCase("TABLE")) {
                tables.add(rs.getString(3).trim());
                continue;
            }

            if (type.equalsIgnoreCase("VIEW"))
                getOraclePKTable(rs.getString(2), rs.getString(3));
        }
        rs.close();
        stmt.close();
    }

    private void getMSSQLServerPKTable(String user, String view) throws SQLException {
        CallableStatement cs;
        ResultSet rs;
        String name;
        int pos;
        
        cs = con.prepareCall("{call sp_depends(?)}");
        cs.setString(1, view);
        try {
            rs = cs.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString(2).trim().toLowerCase();
                name = rs.getString(1).trim();
                pos = name.lastIndexOf(".");
                name = name.substring(pos + 1);

                if (type.indexOf("table") != -1)
                    if (! tables.contains(name)) {
                        tables.add(name);
                        continue;
                    }

                if (type.equals("view"))
                    getMSSQLServerPKTable(user, name);
            }
            rs.close();
        } catch (Exception exc) {
            //no result set produced or unexpected driver error
            rs = null;
            return;
        }
    }
    
    private void getOracleViewColumns() throws SQLException {
        PreparedStatement stmt;
        ResultSet rs;
        String text = null;
        int startPos, endPos;
        
        String query = "select TEXT from ALL_VIEWS where VIEW_NAME = ?";

        stmt = con.prepareStatement(query);
        stmt.setString(1, view);
        rs = stmt.executeQuery();
        
        if (rs.next())
            text = rs.getString(1).trim();
        rs.close();
        stmt.close();
        
        if (text == null)
            return;
        
        startPos = text.indexOf(" ");
        endPos = text.toLowerCase().indexOf("from");
        text = text.substring(startPos, endPos).trim();
        
        StringTokenizer st = new StringTokenizer(text, ",");
        String colName;
        while (st.hasMoreTokens()) {
            colName = st.nextToken().trim();
            if (colName.startsWith("\""))
                colName = colName.substring(1, colName.length() - 1);
            columns.add(colName.toLowerCase());
        }
    }

    private void getMSSQLServerViewColumns() throws SQLException {
        CallableStatement cs;
        ResultSet rs;
        String text = null;
        int startPos, endPos;
        
        cs = con.prepareCall("{call sp_helptext(?)}");
        cs.setString(1, view);
        try {
            rs = cs.executeQuery();
            
            if (rs != null)
                while (rs.next())
                    text += rs.getString(1).trim();
            rs.close();
            cs.close();
            
            if (text == null)
                return;

            startPos = text.toLowerCase().indexOf("select") + 6;
            endPos = text.toLowerCase().indexOf("from");
            text = text.substring(startPos, endPos).trim();

            StringTokenizer st = new StringTokenizer(text, ",");
            String colName;
            while (st.hasMoreTokens()) {
                colName = st.nextToken().trim();
                if (colName.startsWith("\""))
                    colName = colName.substring(1, colName.length() - 1);
                columns.add(colName.toLowerCase());
            }
        } catch (Exception exc) {
            //no result set produced or unexpected driver error
            rs = null;
            return;
        }
    }
    
}
