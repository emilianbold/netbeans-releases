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

import com.netbeans.ide.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.dlg.AddDriverDialog;

public class AddDriverAction extends DatabaseAction
{
	public void performAction(Node[] activatedNodes) 
	{
		Node node = activatedNodes[0];
		try {
			DriverOperations nfo = (DriverOperations)findInfo((DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class));
			AddDriverDialog dlg = new AddDriverDialog();
			if (dlg.run()) nfo.addDriver(dlg.getDriver());
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to add driver, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}