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

import java.util.ResourceBundle;
import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.*;

public class DropViewAction extends DatabaseAction
{
  static final long serialVersionUID =2634594290357298187L;
	public void performAction(Node[] activatedNodes) 
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0)
      node = activatedNodes[0];
		else
      return;
		ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
//		try {			
//		} catch(Exception e) {
//			TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("DropIndexErrorPrefix")+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
//		}
	}
}
/*
 * <<Log>>
 *  7    Gandalf-post-FCS1.5.1.0     4/10/00  Radko Najman    
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka oprava activatedNode[0] 
 *       check
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
