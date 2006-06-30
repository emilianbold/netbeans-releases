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
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;

public class AddIndexAction extends DatabaseAction {
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            IndexListNodeInfo nfo = (IndexListNodeInfo)info.getParent(nodename);

            String tablename = (String)nfo.get(DatabaseNode.TABLE);
            String columnname = (String)nfo.get(DatabaseNode.COLUMN);

            Specification spec = (Specification)nfo.getSpecification();
            String index = (String)nfo.get(DatabaseNode.INDEX);
            DriverSpecification drvSpec = info.getDriverSpecification();

            // List columns not present in current index
            Vector cols = new Vector(5);

            drvSpec.getColumns(tablename, "%");
            ResultSet rs = drvSpec.getResultSet();
            HashMap rset = new HashMap();
            while (rs.next()) {
                rset = drvSpec.getRow();
                cols.add((String) rset.get(new Integer(4)));
                rset.clear();
            }
            rs.close();

            if (cols.size() == 0)
                throw new Exception(bundle().getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command
            AddIndexDialog dlg = new AddIndexDialog(cols, spec, info);
            dlg.setIndexName(tablename + "_idx"); // NOI18N
            if (dlg.run()) {
                nfo.addIndex(dlg.getIndexName());
            }
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle().getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
