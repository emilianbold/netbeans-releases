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

import java.beans.*;
import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.infos.*;

public class AddConnectionAction extends DatabaseAction {

    static final long serialVersionUID =5370365696803042542L;
    public void performAction (Node[] activatedNodes) {
        
        try {

            Node n[] = TopManager.getDefault().getPlaces().nodes().environment().getChildren().findChild("Databases").getChildren().findChild("Drivers").getChildren().getNodes(); //NOI18N
            Node node;
            if (n != null && n.length>0)
                node = n[0];
            else
                return;

            SystemAction[] actArr = node.getActions();
            for(int i=0; i<actArr.length; i++)
                if(actArr[i] instanceof ConnectUsingDriverAction)
                    ((DatabaseAction)actArr[i]).performAction(new Node[] {node});

        } catch (Exception e) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToPerformOperation"), new String[] {e.getMessage(), ""}); //NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }

    }
}
