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
        
//        try {
//        } catch(Exception e) {
//            TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("???")+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
//        }
    }
}
