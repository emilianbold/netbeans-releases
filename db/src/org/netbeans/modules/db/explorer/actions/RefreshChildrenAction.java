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
  static final long serialVersionUID =-2858583720506557569L;
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
/*
 * <<Log>>
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
