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
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dataview.*;

public class ViewDataAction extends DatabaseAction {
    static final long serialVersionUID =-894644054833609687L;
    
    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return false;

        ConnectionNodeInfo info = (ConnectionNodeInfo)node.getCookie(ConnectionNodeInfo.class);
        if (info != null)
            return (info.getConnection() != null);
        
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        String expression = ""; //NOI18N
        StringBuffer cols = new StringBuffer();
        Node node;

        if (activatedNodes != null && activatedNodes.length>0) {
            try {

                node = activatedNodes[0];
                DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
                String onome = info.getName();
                if (info instanceof TableNodeInfo || info instanceof ViewNodeInfo) {
                    Enumeration enum = info.getChildren().elements();
                    while (enum.hasMoreElements()) {
                        DatabaseNodeInfo nfo = (DatabaseNodeInfo)enum.nextElement();
                        if (nfo instanceof ColumnNodeInfo || nfo instanceof ViewColumnNodeInfo) {
                            if (cols.length()>0) cols.append(", "); //NOI18N
                            cols.append(nfo.getName());
                        }
                    }

                    expression = "select "+cols.toString()+" from "+onome; //NOI18N

                } else if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
                    onome = (info instanceof ViewColumnNodeInfo)?info.getView():info.getTable();
                    for (int i = 0; i<activatedNodes.length; i++) {
                        node = activatedNodes[i];
                        info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
                        if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
                            if (cols.length()>0) cols.append(", "); //NOI18N
                            cols.append(info.getName());
                        }
                    }

                    expression = "select "+cols.toString()+" from "+onome; //NOI18N

                }

                DataViewWindow win = new DataViewWindow(info, expression);
                win.open();
                win.executeCommand();
            } catch(Exception exc) {
                String message = MessageFormat.format(bundle.getString("ShowDataError"), new String[] {exc.getMessage()}); // NOI18N
                TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }
}
