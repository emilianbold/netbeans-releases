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
import com.netbeans.ide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class ViewNodeInfo extends DatabaseNodeInfo
{
	public void initChildren(Vector children)
	throws DatabaseException
	{				
 		try {
 			
			ResultSet rs;
			DatabaseMetaData dmd = getConnection().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String user = getUser();
			String view = (String)get(DatabaseNode.VIEW);
                
			// Columns

			rs = dmd.getColumns(catalog,user,view,null);
			while (rs.next()) {
				DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEWCOLUMN, rs);
				if (nfo != null) children.add(nfo);
			}
			rs.close();

		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	public void setProperty(String key, Object obj)
	{
		try {
			if (key.equals("remarks")) setRemarks((String)obj);		
			put(key, obj);
		} catch (Exception e) {
			System.out.println("unable to set "+key+" = "+obj);
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