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

import java.util.Vector;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.NotifyDescriptor;
import com.netbeans.ide.nodes.*;
import com.netbeans.ddl.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.dlg.NewConnectionDialog;

public class ConnectUsingDriverAction extends DatabaseAction
{
	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {
			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			ConnectionOwnerOperations nfo = (ConnectionOwnerOperations)info.getParent(nodename);
			Vector drvs = RootNode.getOption().getAvailableDrivers();
			DatabaseConnection cinfo = new DatabaseConnection();
			NewConnectionDialog cdlg = new NewConnectionDialog(drvs, cinfo);
//			cdlg.setSelectedDriver((DatabaseDriver)((DatabaseNodeInfo)nfo).get(DatabaseNodeInfo.DRIVER));
			if (cdlg.run()) nfo.addConnection((DBConnection)cinfo);
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to add connection, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}