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

import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import com.netbeans.enterprise.modules.db.explorer.DatabaseDriver;
import com.netbeans.enterprise.modules.db.explorer.dlg.AddDriverDialog;

public class AddDriverAction extends DatabaseAction
{
	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {
			DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
			DriverOperations nfo = (DriverOperations)info.getParent(nodename);
			AddDriverDialog dlg = new AddDriverDialog();
			if (dlg.run()) nfo.addDriver(dlg.getDriver());
		} catch(Exception e) {
			e.printStackTrace();
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to add driver, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}
/*
 * <<Log>>
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka oprava activatedNode[0] 
 *       check
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
