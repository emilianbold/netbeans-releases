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

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.modules.db.explorer.DbUtilities;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.modules.db.explorer.dlg.ColumnItem;
import org.netbeans.modules.db.explorer.dlg.LabeledComboDialog;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class AddToIndexAction extends DatabaseAction {
    static final long serialVersionUID =-1416260930649261633L;
    
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

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

            Specification spec = (Specification)nfo.getSpecification();
            DriverSpecification drvSpec = info.getDriverSpecification();
            String index = (String)nfo.get(DatabaseNode.INDEX);

            // List columns used in current index (do not show)
            HashSet ixrm = new HashSet();

            drvSpec.getIndexInfo(tablename, false, true);
            ResultSet rs = drvSpec.getResultSet();
            HashMap rset = new HashMap();
            boolean isUQ = false;
            String ixname;
            while (rs.next()) {
                rset = drvSpec.getRow();
                ixname = (String) rset.get(new Integer(6));

                if (!index.equals(ixname))
                    continue;

                String colname = (String) rset.get(new Integer(9));
                ixrm.add(colname);

                String val = (String) rset.get(new Integer(4));
                if (val.equals("1"))
                    isUQ = false;
                else
                    isUQ = !(Boolean.valueOf(val).booleanValue());
                
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
                throw new Exception(bundle().getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command

            LabeledComboDialog dlg = new LabeledComboDialog(bundle().getString("AddToIndexTitle"), bundle().getString("AddToIndexLabel"), cols); // NOI18N
            String selectedCol;
            if (dlg.run()) {
                AddToIndexDDL ddl = new AddToIndexDDL(spec, 
                        (String)info.get(DatabaseNodeInfo.SCHEMA),
                        tablename);

                selectedCol = (String)dlg.getSelectedItem();
                ixrm.add(selectedCol);
                ddl.execute(index, isUQ, ixrm);
            }

        } catch(Exception exc) {
            DbUtilities.reportError(bundle().getString("ERR_UnableToAddColumn"), exc.getMessage()); // NOI18N
        }
    }
}
