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

import java.io.*;
import java.beans.*;
import java.util.*;
import java.sql.*;
import com.netbeans.ddl.impl.*;
import com.netbeans.ide.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.*;

public class ConnectAction extends DatabaseAction
{
	protected boolean enable(Node[] activatedNodes)
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return false;
		ConnectionOperations nfo = (ConnectionOperations)findInfo((DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class));
		Connection connection = (Connection)((DatabaseNodeInfo)nfo).getConnection();
		return (connection == null);
	}

	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {
			ConnectionNodeInfo nfo = (ConnectionNodeInfo)findInfo((DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class));
			Connection connection = nfo.getConnection();
			if (connection != null) return;
			String drvurl = (String)nfo.get(DatabaseNodeInfo.DRIVER);
			String dburl = (String)nfo.get(DatabaseNodeInfo.DATABASE);
			String user = (String)nfo.getUser();
			String pwd = (String)nfo.getPassword();
			if (user == null || pwd == null || !nfo.containsKey("rememberspassword")) {
				ConnectDialog dlg = new ConnectDialog(user);
				if (dlg.run()) {
					user = dlg.getUser();
					nfo.setUser(user);
					pwd = dlg.getPassword();		
					nfo.setPassword(pwd);
					if (dlg.rememberPassword()) {
						nfo.put(DatabaseNodeInfo.REMEMBER_PWD, new Boolean(true));
					}
					
					// Update option
					
					DatabaseConnection con = new DatabaseConnection(drvurl, dburl, user, null);
					Vector cons = RootNode.getOption().getConnections();
					int idx = cons.indexOf(con);
					if (idx != -1) {
						con = (DatabaseConnection)cons.elementAt(idx);
						con.setUser(user);
						if (dlg.rememberPassword()) {
							con.setPassword(pwd);
							con.setRememberPassword(true);
						} else {
							con.setPassword(null);
							con.setRememberPassword(false);
						}
					}
				} else return;
			}

			nfo.connect();

		} catch (Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to connect, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}		
	}
}