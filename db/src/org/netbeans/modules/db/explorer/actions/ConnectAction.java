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
import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.*;

public class ConnectAction extends DatabaseAction
{
  static final long serialVersionUID =-6822218300035053411L;
	protected boolean enable(Node[] activatedNodes)
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return false;
		
		DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
		DatabaseNodeInfo nfo = info.getParent(DatabaseNode.CONNECTION);
		if (nfo != null) return (nfo.getConnection() == null);
		return false;
	}

	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		
		try {
			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			ConnectionNodeInfo nfo = (ConnectionNodeInfo)info.getParent(DatabaseNode.CONNECTION);
			Connection connection = nfo.getConnection();
			if (connection != null) return;
			
			String drvurl = (String)nfo.get(DatabaseNodeInfo.DRIVER);
			String dburl = (String)nfo.get(DatabaseNodeInfo.DATABASE);
			String user = (String)nfo.getUser();
			String pwd = (String)nfo.getPassword();
			Boolean rpwd = (Boolean)nfo.get(DatabaseNodeInfo.REMEMBER_PWD);
			boolean remember = ((rpwd != null) ? rpwd.booleanValue() : false);
			if (user == null || pwd == null || !remember) {
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
/*					
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
*/
				} else return;
			}

			nfo.connect();

		} catch (Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to connect, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}		
	}
}
/*
 * <<Log>>
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         7/21/99  Slavek Psenicka update option off
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/21/99  Slavek Psenicka new version
 *  5    Gandalf   1.4         5/14/99  Slavek Psenicka new version
 *  4    Gandalf   1.3         4/23/99  Slavek Psenicka oprava activatedNode[0] 
 *       check
 *  3    Gandalf   1.2         4/23/99  Slavek Psenicka Debug mode
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         3/22/99  Slavek Psenicka 
 * $
 */
