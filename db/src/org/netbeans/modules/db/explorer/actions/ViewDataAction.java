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

import java.util.Enumeration;
import java.sql.Connection;
import org.openide.*;
import org.openide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.dataview.*;

public class ViewDataAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		StringBuffer cols = new StringBuffer();
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) {
			try {

				node = activatedNodes[0];
				DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
				String onome = info.getName();
				if (info instanceof TableNodeInfo || info instanceof ViewNodeInfo) {
					Enumeration enum = info.getChildren().elements();
					while (enum.hasMoreElements()) {
						DatabaseNodeInfo nfo = (DatabaseNodeInfo)enum.nextElement();
						if (nfo instanceof ColumnNodeInfo || nfo instanceof ViewColumnNodeInfo) {
							if (cols.length()>0) cols.append(", ");
							cols.append(nfo.getName());
						}
					}
				} else if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
					onome = info.getTable();
					for (int i = 0; i<activatedNodes.length; i++) {
						node = activatedNodes[i];
						info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
						if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
							if (cols.length()>0) cols.append(", ");
							cols.append(info.getName());
						}
					}
				} else throw new Exception("unable to view data from "+info.getClass());
				
				DataViewWindow win = new DataViewWindow(info.getDatabaseConnection(), info.getUser(), "select "+cols.toString()+" from "+onome);
				win.open();
				win.fetch();
				
			} catch(Exception e) {
				TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to show data, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
			}
		}				 
	}
}