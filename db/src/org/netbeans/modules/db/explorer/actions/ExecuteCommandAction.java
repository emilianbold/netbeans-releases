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

package org.netbeans.modules.db.explorer.actions;

import java.sql.Connection;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dataview.*;

public class ExecuteCommandAction extends DatabaseAction
{
//  static final long serialVersionUID =-894644054833609687L;
	protected boolean enable(Node[] activatedNodes)
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return false;
		
		ConnectionNodeInfo info = (ConnectionNodeInfo)node.getCookie(ConnectionNodeInfo.class);
		if (info != null) return (info.getConnection() != null);
		return true;
	}

	public void performAction (Node[] activatedNodes) {
		StringBuffer cols = new StringBuffer();
		Node node;
    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");

		if (activatedNodes != null && activatedNodes.length > 0) {
			try {
				node = activatedNodes[0];
				DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
				
				DataViewWindow win = new DataViewWindow(info, "");
				win.open();
			} catch(Exception e) {
				TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("DataViewFetchErrorPrefix") + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
			}
		}				 
	}
}
/*
 * <<Log>>
 *  2    Gandalf-post-FCS1.0.1.0     4/10/00  Radko Najman    
 *  1    Gandalf   1.0         2/10/00  Radko Najman    
 * $
 */
