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

import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import com.netbeans.ddl.impl.*;
import org.openide.*;
import org.openide.nodes.Node;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;

public class RefreshChildrenAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;

		ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");		
		try {
			DatabaseNodeInfo nfo = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			nfo.refreshChildren();
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("RefreshChildrenErrorPrefix")+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}