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

import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;

public class RefreshChildrenAction extends DatabaseAction {
    static final long serialVersionUID =-2858583720506557569L;
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo nfo = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            nfo.refreshChildren();
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("RefreshChildrenErrorPrefix"), new String[] {exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
