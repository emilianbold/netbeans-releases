/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;
import com.netbeans.ddl.impl.*;
import org.openide.nodes.Node;
import com.netbeans.ddl.adaptors.*;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class IndexListNodeInfo extends DatabaseNodeInfo
{
  static final long serialVersionUID =5809643799834921044L;
  
  public void initChildren(Vector children) throws DatabaseException {
    try {
      DatabaseMetaData dmd = getSpecification().getMetaData();
      String catalog = (String)get(DatabaseNode.CATALOG);
      String table = (String)get(DatabaseNode.TABLE);

      DriverSpecification drvSpec = getDriverSpecification();
      drvSpec.getIndexInfo(catalog, dmd, table, true, false);

//      boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);
      boolean jdbcOdbcBridge = (((((String)get(DatabaseNode.DRIVER)).trim().equals("sun.jdbc.odbc.JdbcOdbcDriver")) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);
      
      if (drvSpec.rs != null) {
        Set ixmap = new HashSet();
        while (drvSpec.rs.next()) {
          if (jdbcOdbcBridge)
            drvSpec.rsTemp.next();
          if (drvSpec.rs.getString("INDEX_NAME") != null) {
            IndexNodeInfo info;
            if (jdbcOdbcBridge)
              info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rsTemp);
            else
              info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rs);
            
            if (info != null) {
              if (!ixmap.contains(info.getName())) {
                ixmap.add(info.getName());
                info.put("index", info.getName());
                children.add(info);
              }
            } else
              throw new Exception("unable to create node information for index");
          }
        }
        drvSpec.rs.close();
        if (jdbcOdbcBridge)
          drvSpec.rsTemp.close();
      }
    } catch (Exception e) {
      throw new DatabaseException(e.getMessage());	
    }
  }
	
	public void addIndex(String name)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String table = (String)get(DatabaseNode.TABLE);

      DriverSpecification drvSpec = getDriverSpecification();
      drvSpec.getIndexInfo(catalog, dmd, table, true, false);
       boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);

			if (drvSpec.rs != null) {
        while (drvSpec.rs.next()) {
          if (jdbcOdbcBridge) drvSpec.rsTemp.next();
          String findex = drvSpec.rs.getString("INDEX_NAME");
          if (findex != null) {
            if (findex.equals(name)) {
              IndexNodeInfo info;
              if (jdbcOdbcBridge)
                info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rsTemp);
              else
                info = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEX, drvSpec.rs);
              
              if (info != null) ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
            } 
          }
        }
        drvSpec.rs.close();
        if (jdbcOdbcBridge) drvSpec.rsTemp.close();
      }
 		} catch (Exception e) {
 			e.printStackTrace();
			throw new DatabaseException(e.getMessage());	
		}
	}
}

/*
 * <<Log>>
 *  15   Gandalf-post-FCS1.13.1.0    4/10/00  Radko Najman    
 *  14   Gandalf   1.13        1/26/00  Radko Najman    JDBC-ODBC bridge HACK
 *  13   Gandalf   1.12        1/25/00  Radko Najman    new driver adaptor 
 *       version
 *  12   Gandalf   1.11        12/15/99 Radko Najman    driver adaptor
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         11/15/99 Radko Najman    MS ACCESS
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/13/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka adaptor changes
 *  4    Gandalf   1.3         8/19/99  Slavek Psenicka English
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
