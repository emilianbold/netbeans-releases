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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import org.openide.nodes.Node;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.sql.visualeditor.VisualSQLEditorSupport;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

/**
 *
 * @author Jim Davidson
 */

public class DesignQueryAction extends QueryAction {
    
    protected boolean enable(Node[] activatedNodes) {
        
        return hasColumnsSelected(activatedNodes);
    }
    
    public void performAction(final Node[] activatedNodes) {
        
        if (activatedNodes != null && activatedNodes.length > 0) {
            
            final DatabaseNodeInfo info = (DatabaseNodeInfo) activatedNodes[0].getCookie(DatabaseNodeInfo.class);
            String name = ((DatabaseConnection)info.getDatabaseConnection()).getName();
            String expression = getDefaultQuery(activatedNodes);
            
            VisualSQLEditorSupport.openVisualSQLEditor(ConnectionManager.getDefault().getConnection(name), expression); 
        }
    }
}

