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
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import com.netbeans.ddl.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.RootNode;
import com.netbeans.enterprise.modules.db.explorer.actions.DatabaseAction;
import com.netbeans.enterprise.modules.db.explorer.dlg.NewConnectionDialog;

public class ConnectUsingDriverAction extends DatabaseAction
{
	private final static String CLASS_NOT_FOUND = "EXC_ClassNotFound";
	private final static String BUNDLE_PATH = "com.netbeans.enterprise.modules.db.resources.Bundle";

	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {
			DriverNodeInfo info = (DriverNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			ConnectionOwnerOperations nfo = (ConnectionOwnerOperations)info.getParent(nodename);
			Vector drvs = RootNode.getOption().getAvailableDrivers();
			DatabaseConnection cinfo = new DatabaseConnection();
			cinfo.setDriverName(info.getName());
			cinfo.setDriver(info.getURL());
			NewConnectionDialog cdlg = new NewConnectionDialog(drvs, cinfo);
			if (cdlg.run()) nfo.addConnection((DBConnection)cinfo);
		} catch (ClassNotFoundException ex) {	
			String message = MessageFormat.format(NbBundle.getBundle(BUNDLE_PATH).getString(CLASS_NOT_FOUND), new String[] {ex.getMessage()});
			TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to add connection, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}