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
                throw new Exception(bundle.getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command
            AddIndexDialog dlg = new AddIndexDialog(cols);
            dlg.setIndexName(tablename + "_idx"); // NOI18N
            if (dlg.run()) {
                CreateIndex icmd = spec.createCommandCreateIndex(tablename);
                icmd.setIndexName(dlg.getIndexName());
                icmd.setIndexType(dlg.getIndexType());
                Iterator enu = dlg.getSelectedColumns().iterator();
                while (enu.hasNext())
                    icmd.specifyColumn((String)enu.next());

                icmd.execute();
                nfo.addIndex(dlg.getIndexName());
            }
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
