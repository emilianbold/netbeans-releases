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

import java.io.InputStream;
import java.util.*;
import java.sql.*;
import com.netbeans.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import com.netbeans.ddl.util.PListReader;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.DatabaseNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;

public class ProcedureNodeInfo extends DatabaseNodeInfo 
{
  static final long serialVersionUID =-5984072379104199563L;
	public DatabaseDriver getDatabaseDriver()
	{
		return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
	}
/*
	public void setDatabaseDriver(DatabaseDriver drv)
	{
		put(DatabaseNodeInfo.NAME, drv.getName());
		put(DatabaseNodeInfo.URL, drv.getURL());
		put(DatabaseNodeInfo.DBDRIVER, drv);
	}
*/
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String name = (String)get(DatabaseNode.PROCEDURE);
      
      ResultSet rs = getDriverSpecification().getProcedureColumns(catalog, dmd, name, null);
			if (rs != null) {
        while (rs.next()) {
          DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE_COLUMN, rs);
          if (info != null) {
            Object ibase = null;
            String itype = "unknown";
            int type = ((Integer)info.get("type")).intValue();
            switch (type) {
              case DatabaseMetaData.procedureColumnIn: 
                ibase = info.get("iconbase_in"); 
                itype = "in";
                break;
              case DatabaseMetaData.procedureColumnOut: 
                ibase = info.get("iconbase_out"); 
                itype = "out";
                break;
              case DatabaseMetaData.procedureColumnInOut: 
                ibase = info.get("iconbase_inout"); 
                itype = "in/out";
                break;
              case DatabaseMetaData.procedureColumnReturn: 
                ibase = info.get("iconbase_return"); 
                itype = "return";
                break;
              case DatabaseMetaData.procedureColumnResult: 
                ibase = info.get("iconbase_result"); 
                itype = "result";
                break;
            }
            if (ibase != null) info.put("iconbase", ibase);
            info.put("type", itype);
            children.add(info);
          } else throw new Exception("unable to create node information for procedure column");
        }
        rs.close();
      }
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}	
}

/*
 * <<Log>>
 *  11   Gandalf   1.10        12/15/99 Radko Najman    driver adaptor
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         11/15/99 Radko Najman    MS ACCESS
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  6    Gandalf   1.5         9/13/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka adaptor changes
 *  4    Gandalf   1.3         8/19/99  Slavek Psenicka English
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
