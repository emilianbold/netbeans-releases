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
//			ResultSet rs = dmd.getIndexInfo(catalog,nfo.getUser(),tablename, true, false);

//je to BARBARSTVI, po beta 6 rozumne prepsat
ResultSet rs;
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	rs = dmd.getIndexInfo(catalog, null, tablename, true, false);
else
	rs = dmd.getIndexInfo(catalog, dmd.getUserName(), tablename, true, false);
	
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
//			rs = dmd.getColumns(catalog, nfo.getUser(), tablename, null);

//je to BARBARSTVI, po beta 6 rozumne prepsat
if (dmd.getDatabaseProductName().trim().equals("ACCESS"))
	rs = dmd.getColumns(catalog, null, tablename, null);
else
	rs = dmd.getColumns(catalog, dmd.getUserName(), tablename, null);
	
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
/*
 * <<Log>>
 *  9    Gandalf   1.8         11/15/99 Radko Najman    MS ACCESS
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  6    Gandalf   1.5         9/13/99  Slavek Psenicka 
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka adaptor changes
 *  4    Gandalf   1.3         7/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
