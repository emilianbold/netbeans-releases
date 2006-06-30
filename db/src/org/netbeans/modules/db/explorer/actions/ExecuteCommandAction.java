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

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.nodes.Node;

public class ExecuteCommandAction extends DatabaseAction {

    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return false;

        ConnectionNodeInfo info = (ConnectionNodeInfo)node.getCookie(ConnectionNodeInfo.class);
        if (info != null)
            return (info.getConnection() != null);
        
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            Node node = activatedNodes[0];
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            String name = ((DatabaseConnection)info.getDatabaseConnection()).getName();
            SQLEditorSupport.openSQLEditor(ConnectionManager.getDefault().getConnection(name), "", false); // NOI18N
        }
    }
}
