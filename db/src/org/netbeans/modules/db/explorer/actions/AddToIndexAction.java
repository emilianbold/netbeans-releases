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

            String tablename = (String)nfo.get(DatabaseNode.TABLE);
            String columnname = (String)nfo.get(DatabaseNode.COLUMN);

            Specification spec = (Specification)nfo.getSpecification();
            DriverSpecification drvSpec = info.getDriverSpecification();
            String index = (String)nfo.get(DatabaseNode.INDEX);

            // List columns used in current index (do not show)
            HashSet ixrm = new HashSet();

            drvSpec.getIndexInfo(tablename, false, false);
            ResultSet rs = drvSpec.getResultSet();
            HashMap rset = new HashMap();
            boolean isUQ = false;
            while (rs.next()) {
                rset = drvSpec.getRow();
                String ixname = (String) rset.get(new Integer(6));
                if (ixname != null) {
                    String colname = (String) rset.get(new Integer(9));
                    if (ixname.equals(index))
                        ixrm.add(colname);
                }
                isUQ = !Boolean.valueOf( (String)rset.get( new Integer(4) ) ).booleanValue();
                rset.clear();
            }
            rs.close();

            // List columns not present in current index
            Vector cols = new Vector(5);

            drvSpec.getColumns(tablename, "%");
            rs = drvSpec.getResultSet();
            while (rs.next()) {
                rset = drvSpec.getRow();
                String colname = (String) rset.get(new Integer(4));               
                if (!ixrm.contains(colname))
                    cols.add(colname);
                rset.clear();
            }
            rs.close();
            if (cols.size() == 0)
                throw new Exception(bundle.getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command

            LabeledComboDialog dlg = new LabeledComboDialog(bundle.getString("AddToIndexTitle"), bundle.getString("AddToIndexLabel"), cols); // NOI18N
            if (dlg.run()) {
                CreateIndex icmd = spec.createCommandCreateIndex(tablename);
                icmd.setIndexName(index);
                icmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
                if(isUQ)
                    icmd.setIndexType(ColumnItem.UNIQUE);
                else
                    icmd.setIndexType(new String());
                Iterator enu = ixrm.iterator();
                while (enu.hasNext())
                    icmd.specifyColumn((String)enu.next());

                icmd.specifyColumn((String)dlg.getSelectedItem());
                DropIndex dicmd = spec.createCommandDropIndex(index);
                dicmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
                dicmd.setTableName(tablename);
                dicmd.execute();
                icmd.execute();
                info.getParent(DatabaseNode.TABLE).refreshChildren();
//				((DatabaseNodeChildren)nfo.getNode().getChildren()).createSubnode(info,true);
            }

        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToPerformOperation"), new String[] {node.getName(), exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
