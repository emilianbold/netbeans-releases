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

import java.sql.Connection;
import com.netbeans.ide.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ddl.impl.Specification;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.dlg.AddTableColumnDialog;

public class AddColumnAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		
		try {

			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			TableNodeInfo nfo = (TableNodeInfo)info.getParent(nodename);
			AddTableColumnDialog dlg = new AddTableColumnDialog((Specification)nfo.getSpecification(), nfo);
			if (dlg.run()) nfo.addColumn(dlg.getColumnName());

		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to add column, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}