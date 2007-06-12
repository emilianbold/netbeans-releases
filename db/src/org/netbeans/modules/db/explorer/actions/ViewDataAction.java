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

import java.text.MessageFormat;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class ViewDataAction extends QueryAction {
    
    static final long serialVersionUID =-894644054833609687L;

    public ViewDataAction() {
    }
    
    protected boolean enable(Node[] activatedNodes) {
        
        return hasColumnsSelected(activatedNodes);
    }
    
    public void performAction (final Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            final DatabaseNodeInfo info = (DatabaseNodeInfo) activatedNodes[0].getCookie(DatabaseNodeInfo.class);
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        String name = ((DatabaseConnection)info.getDatabaseConnection()).getName();
                        String expression = getDefaultQuery(activatedNodes);
                        SQLEditorSupport.openSQLEditor(ConnectionManager.getDefault().getConnection(name), expression, true);
                    } catch(Exception exc) {
                        String message = MessageFormat.format(bundle().getString("ShowDataError"), new String[] {exc.getMessage()}); // NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            }, 0);
        }
    }

}
