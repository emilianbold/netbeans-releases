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

import java.sql.*;
import java.text.MessageFormat;

import org.netbeans.lib.ddl.impl.*;
import org.openide.*;
import org.openide.nodes.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.infos.*;

public class CreateViewAction extends DatabaseAction {
    static final long serialVersionUID =-1640355770860785644L;
    
    public void performAction(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            
            if (! info.getDriverSpecification().areViewsSupported()) {
                String message = MessageFormat.format(bundle.getString("MSG_ViewsAreNotSupported"), new String[] {info.getConnection().getMetaData().getDatabaseProductName().trim()}); // NOI18N
                TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }
            
            ViewListNodeInfo nfo = (ViewListNodeInfo)info.getParent(nodename);
            Specification spec = (Specification)nfo.getSpecification();

            // Create and execute command
            AddViewDialog dlg = new AddViewDialog(spec, info);
            if (dlg.run()) {
                nfo.addView(dlg.getViewName());
            }
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
