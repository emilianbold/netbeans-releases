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

package org.netbeans.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class ProcedureListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-7911927402768472443L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            DatabaseMetaData dmd = getSpecification().getMetaData();
            String catalog = (String) get(DatabaseNode.CATALOG);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getProcedures(catalog, dmd, null);

            if (drvSpec.rs != null) {
                while (drvSpec.rs.next()) {
                    DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE, drvSpec.rs);
                    if (info != null) {
                        info.put(DatabaseNode.PROCEDURE, info.getName());
                        children.add(info);
                    } else
                        throw new Exception(bundle.getString("EXC_UnableToCreateProcedureNodeInfo")); //NOI18N
                }
                drvSpec.rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /* refresh list of procedure nodes */
    public void refreshChildren() throws DatabaseException {

        Vector charr = new Vector();
        DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

        put(DatabaseNodeInfo.CHILDREN, charr);
        chil.remove(chil.getNodes());
        initChildren(charr);
        Enumeration en = charr.elements();
        while(en.hasMoreElements()) {
                DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
                chil.add(new Node[] {subnode});
        }
    }
}
