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
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;

public class AddDriverAction extends DatabaseAction {
    static final long serialVersionUID =-109193000951395612L;
    public void performAction(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;
        
        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            DriverOperations nfo = (DriverOperations)info.getParent(nodename);
            AddDriverDialog dlg = new AddDriverDialog();
            if (dlg.run()) nfo.addDriver(dlg.getDriver());
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToAddDriver"), new String[] {exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
