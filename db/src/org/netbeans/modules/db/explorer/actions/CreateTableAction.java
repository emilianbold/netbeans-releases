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

import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.dlg.CreateTableDialog;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableOwnerOperations;

public class CreateTableAction extends DatabaseAction {
    static final long serialVersionUID =-7008851466327604724L;
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        
        final DatabaseNodeInfo xnfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        final String nodeName = node.getName();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    TableOwnerOperations nfo = (TableOwnerOperations) xnfo.getParent(nodename);
                    Specification spec = (Specification) xnfo.getSpecification();
                    CreateTableDialog dlg = new CreateTableDialog(spec, (DatabaseNodeInfo) nfo);
                    if (dlg.run())
                        try {
                            nfo.addTable(dlg.getTableName());
                        } catch ( DatabaseException de ) {
                            //PENDING
                        }
                } catch(Exception exc) {
                    String message = MessageFormat.format(bundle().getString("EXC_UnableToCreateTable"), new String[] {nodeName, exc.getMessage()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }, 0);       
    }
}
