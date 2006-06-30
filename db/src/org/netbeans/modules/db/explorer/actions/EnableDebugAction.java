/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class EnableDebugAction extends DatabaseAction {
    static final long serialVersionUID =-4578856899499264469L;

    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length > 0)
            node = activatedNodes[0];
        else
            return false;

        DatabaseNodeInfo nfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        if (nfo != null)
            return !nfo.isDebugMode();
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
        nfo.setDebugMode(true);
    }
}
