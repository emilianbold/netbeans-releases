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
import org.netbeans.modules.db.explorer.DbUtilities;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.dlg.AddTableColumnDialog;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableNodeInfo;

public class AddColumnAction extends DatabaseAction {

    static final long serialVersionUID =5894518352294344657L;
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1)
            return true;
        else
            return false;
    }
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;

        DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        final TableNodeInfo nfo = (TableNodeInfo) info.getParent(nodename);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    AddTableColumnDialog dlg = new AddTableColumnDialog((Specification) nfo.getSpecification(), nfo);
                    if (dlg.run()) {
                        nfo.addColumn(dlg.getColumnName());
                    }
                } catch(Exception exc) {
                    DbUtilities.reportError(bundle().getString("ERR_UnableToAddColumn"), exc.getMessage()); // NOI18N
                }
            }
        }, 0);
    }
}
