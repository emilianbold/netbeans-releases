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
import com.netbeans.ddl.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.DatabaseNodeChildren;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;

public class RefTableListNodeInfo extends DatabaseNodeInfo
{
	public void initChildren(Vector children)
	throws DatabaseException
	{
 		try {
			ResultSet rs;
//			DatabaseMetaData dmd = getConnection().getMetaData();
			DatabaseMetaData dmd = getDatabaseAdaptor().getMetaData();
			String catalog = (String)get(DatabaseNode.CATALOG);
			String user = getUser();
			String table = (String)get(DatabaseNode.TABLE);

			rs = dmd.getExportedKeys(catalog,user,table);
			while (rs.next()) {
				DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.EXPORTED_KEY, rs);
				if (info != null) children.add(info);
				else throw new Exception("unable to create node information for exported key");
			}
			rs.close();
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
}
