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
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class AddToIndexAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		
		try {

			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			DatabaseNodeInfo nfo = info.getParent(nodename);

			String catalog = (String)nfo.get(DatabaseNode.CATALOG);
			String tablename = (String)nfo.get(DatabaseNode.TABLE);
			String columnname = (String)nfo.get(DatabaseNode.COLUMN);

			Connection con = nfo.getConnection();
//			DatabaseMetaData dmd = con.getMetaData();
			DatabaseMetaData dmd = info.getSpecification().getMetaData();
			Specification spec = (Specification)nfo.getSpecification();
			String index = (String)nfo.get(DatabaseNode.INDEX);

			// List columns used in current index (do not show)

			HashSet ixrm = new HashSet();
			ResultSet rs = dmd.getIndexInfo(catalog,nfo.getUser(),tablename, true, false);
			while (rs.next()) {
				String ixname = rs.getString("INDEX_NAME");
				if (ixname != null) {
					String colname = rs.getString("COLUMN_NAME");
					if (ixname.equals(index)) ixrm.add(colname);
				}
			}
			rs.close();

			// List columns not present in current index

			Vector cols = new Vector(5);
			rs = dmd.getColumns(catalog, nfo.getUser(), tablename, null);
			while (rs.next()) {
				String colname = rs.getString("COLUMN_NAME");
				if (!ixrm.contains(colname)) cols.add(colname);
			}
			rs.close();
			if (cols.size() == 0) throw new Exception("no usable column in place");
			
			// Create and execute command
			
			LabeledComboDialog dlg = new LabeledComboDialog("Add to index", "Column:", cols);
			if (dlg.run()) {

				CreateIndex icmd = spec.createCommandCreateIndex(tablename);
				icmd.setIndexName(index);
				Iterator enu = ixrm.iterator();
				while (enu.hasNext()) {
					icmd.specifyColumn((String)enu.next());
				}
				
				icmd.specifyColumn((String)dlg.getSelectedItem());
				spec.createCommandDropIndex(index).execute();
				icmd.execute();
				nfo.refreshChildren();
//				((DatabaseNodeChildren)nfo.getNode().getChildren()).createSubnode(info,true);
			}

		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to perform operation "+node.getName()+", "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}