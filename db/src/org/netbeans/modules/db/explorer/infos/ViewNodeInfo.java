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

import java.io.IOException;
import java.sql.*;
import java.util.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class ViewNodeInfo extends DatabaseNodeInfo
{
  static final long serialVersionUID =8370676447530973161L;
	public void initChildren(Vector children)
	throws DatabaseException
	{				
 		try {
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String view = (String)get(DatabaseNode.VIEW);
                
			// Columns
			DriverSpecification drvSpec = getDriverSpecification();
      drvSpec.getColumns(catalog, dmd, view, null);
      
      if (drvSpec.rs != null) {
        while (drvSpec.rs.next()) {
          DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEWCOLUMN, drvSpec.rs);
          if (nfo != null) children.add(nfo);
        }
        drvSpec.rs.close();
      }
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setProperty(String key, Object obj)
	{
		try {
			if (key.equals("remarks")) setRemarks((String)obj);		
			put(key, obj);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setRemarks(String rem)
	throws DatabaseException
	{
		String viewname = (String)get(DatabaseNode.VIEW);
		Specification spec = (Specification)getSpecification();
		try {
			AbstractCommand cmd = spec.createCommandCommentView(viewname, rem);
			cmd.execute();		
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void delete()
	throws IOException
	{
		try {
			String code = getCode();
			String table = (String)get(DatabaseNode.TABLE);
			Specification spec = (Specification)getSpecification();
			AbstractCommand cmd = spec.createCommandDropView(getName());
			cmd.execute();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}	
}
/*
 * <<Log>>
 *  12   Gandalf   1.11        1/25/00  Radko Najman    new driver adaptor 
 *       version
 *  11   Gandalf   1.10        12/15/99 Radko Najman    driver adaptor
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         11/15/99 Radko Najman    MS ACCESS
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  6    Gandalf   1.5         9/13/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka adaptor changes
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
