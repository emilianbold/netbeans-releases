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

package com.netbeans.enterprise.modules.db.explorer.actions;

import java.util.*;
import java.sql.*;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import org.openide.*;
import org.openide.nodes.*;
import com.netbeans.ddl.adaptors.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class AddIndexAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		
		try {

			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			IndexListNodeInfo nfo = (IndexListNodeInfo)info.getParent(nodename);

			String catalog = (String)nfo.get(DatabaseNode.CATALOG);
			String tablename = (String)nfo.get(DatabaseNode.TABLE);
			String columnname = (String)nfo.get(DatabaseNode.COLUMN);

			Connection con = nfo.getConnection();
//			DatabaseMetaData dmd = con.getMetaData();
			DatabaseMetaData dmd = info.getSpecification().getMetaData();
			Specification spec = (Specification)nfo.getSpecification();
			String index = (String)nfo.get(DatabaseNode.INDEX);

			// List columns not present in current index

			Vector cols = new Vector(5);
//			ResultSet rs = dmd.getColumns(catalog, nfo.getUser(), tablename, null);
			ResultSet rs = dmd.getColumns(catalog, dmd.getUserName(), tablename, null);
			while (rs.next()) cols.add(rs.getString("COLUMN_NAME"));
			rs.close();
			if (cols.size() == 0) throw new Exception("no usable column in place");

			// Create and execute command
			
			AddIndexDialog dlg = new AddIndexDialog(cols);
			dlg.setIndexName(tablename+"_idx");
			if (dlg.run()) {

				CreateIndex icmd = spec.createCommandCreateIndex(tablename);
				icmd.setIndexName(dlg.getIndexName());
				Iterator enu = dlg.getSelectedColumns().iterator();
				while (enu.hasNext()) {
					icmd.specifyColumn((String)enu.next());
				}
				
				icmd.execute();
				nfo.addIndex(dlg.getIndexName());
			}

		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to perform operation "+node.getName()+", "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}
/*
 * <<Log>>
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  6    Gandalf   1.5         9/13/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/13/99  Slavek Psenicka 
 *  4    Gandalf   1.3         9/8/99   Slavek Psenicka adaptor changes
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
