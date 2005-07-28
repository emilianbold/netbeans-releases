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

import java.text.MessageFormat;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class DisconnectAction extends DatabaseAction {
    static final long serialVersionUID =-5994051723289754485L;

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) 
            return false;
        
        for (int i = 0; i < activatedNodes.length; i++) {
            Node node = activatedNodes[i];
            DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
            if (info != null) {
                DatabaseNodeInfo nfo = info.getParent(DatabaseNode.CONNECTION);
                if (nfo != null && nfo.getConnection() == null)
                    return false;
            } else
                return false;
        }
        return true;
    }
    
    protected int mode() {
        return MODE_ALL;
    }

    public void performAction (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0)
            return;
        
        final Node[] nodes = activatedNodes;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                for (int i = 0; i < nodes.length; i++) {
                    Node node = nodes[i];
                    try {
                        DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
                        ConnectionNodeInfo nfo = (ConnectionNodeInfo) info.getParent(DatabaseNode.CONNECTION);
                        nfo.disconnect();
                    } catch(Exception exc) {
                        String message = MessageFormat.format(bundle().getString("ERR_UnableToDisconnect"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            }
        }, 0);
    }
}
