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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.*;
import org.openide.nodes.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;

public class AddToIndexAction extends DatabaseAction {
    static final long serialVersionUID =-1416260930649261633L;
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            DatabaseNodeInfo nfo = info.getParent(nodename);

            String catalog = (String)nfo.get(DatabaseNode.CATALOG);
            String tablename = (String)nfo.get(DatabaseNode.TABLE);
            String columnname = (String)nfo.get(DatabaseNode.COLUMN);

            Connection con = nfo.getConnection();
            DatabaseMetaData dmd = info.getSpecification().getMetaData();
            Specification spec = (Specification)nfo.getSpecification();
            DriverSpecification drvSpec = info.getDriverSpecification();
            String index = (String)nfo.get(DatabaseNode.INDEX);

            // List columns used in current index (do not show)
            HashSet ixrm = new HashSet();

            drvSpec.getIndexInfo(catalog, dmd, tablename, false, false);
            while (drvSpec.rs.next()) {
                String ixname = drvSpec.rs.getString("INDEX_NAME"); // NOI18N
                if (ixname != null) {
                    String colname = drvSpec.rs.getString("COLUMN_NAME"); // NOI18N
                    if (ixname.equals(index))
                        ixrm.add(colname);
                }
            }
            drvSpec.rs.close();

            // List columns not present in current index
            Vector cols = new Vector(5);

            drvSpec.getColumns(catalog, dmd, tablename, null);
            while (drvSpec.rs.next()) {
                String colname = drvSpec.rs.getString("COLUMN_NAME"); // NOI18N
                if (!ixrm.contains(colname))
                    cols.add(colname);
            }
            drvSpec.rs.close();
            if (cols.size() == 0)
                throw new Exception(bundle.getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command

            LabeledComboDialog dlg = new LabeledComboDialog(bundle.getString("AddToIndexTitle"), bundle.getString("AddToIndexLabel"), cols); // NOI18N
            if (dlg.run()) {
                CreateIndex icmd = spec.createCommandCreateIndex(tablename);
                icmd.setIndexName(index);
                Iterator enu = ixrm.iterator();
                while (enu.hasNext())
                    icmd.specifyColumn((String)enu.next());

                icmd.specifyColumn((String)dlg.getSelectedItem());
                spec.createCommandDropIndex(index).execute();
                icmd.execute();
                nfo.refreshChildren();
//				((DatabaseNodeChildren)nfo.getNode().getChildren()).createSubnode(info,true);
            }

        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
