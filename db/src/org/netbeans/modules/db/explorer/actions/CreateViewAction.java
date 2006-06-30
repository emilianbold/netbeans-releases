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
                String message = MessageFormat.format(bundle().getString("MSG_ViewsAreNotSupported"), new String[] {info.getConnection().getMetaData().getDatabaseProductName().trim()}); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
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
            String message = MessageFormat.format(bundle().getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
