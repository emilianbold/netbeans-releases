/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import org.openide.nodes.Node;
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
        
//        try {
//        } catch(Exception e) {
//            Topmanager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("???")+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
//        }
    }
}
