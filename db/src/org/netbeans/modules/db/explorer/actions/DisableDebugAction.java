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

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class DisableDebugAction extends DatabaseAction {
    static final long serialVersionUID =-7189966255551930161L;
    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length > 0)
            node = activatedNodes[0];
        else
            return false;

        DatabaseNodeInfo nfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        if (nfo != null)
            return nfo.isDebugMode();
        else
            return false;
    }

    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length > 0)
            node = activatedNodes[0];
        else
            return;

        DatabaseNodeInfo nfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        nfo.setDebugMode(false);
    }
}
