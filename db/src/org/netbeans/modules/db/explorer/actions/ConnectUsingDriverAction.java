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

import java.util.Vector;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.dlg.NewConnectionDialog;

public class ConnectUsingDriverAction extends DatabaseAction {
    static final long serialVersionUID =8245005834483564671L;
    
    public void performAction(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;
        try {
            DriverNodeInfo info = (DriverNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            ConnectionOwnerOperations nfo = (ConnectionOwnerOperations)info.getParent(nodename);
            Vector drvs = RootNode.getOption().getAvailableDrivers();
            DatabaseConnection cinfo = new DatabaseConnection();
            cinfo.setDriverName(info.getName());
            cinfo.setDriver(info.getURL());
            NewConnectionDialog cdlg = new NewConnectionDialog(drvs, cinfo);
            if (cdlg.run()) nfo.addConnection((DBConnection)cinfo);
        } catch (ClassNotFoundException exc) {
            String message = MessageFormat.format(bundle.getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
