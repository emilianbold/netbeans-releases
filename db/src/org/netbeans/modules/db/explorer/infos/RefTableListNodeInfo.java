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

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class RefTableListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =318942800614012305L;
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getExportedKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.EXPORTED_KEY, rset);
                    rset.clear();
                    if (info != null)
                        children.add(info);
                    else
                        throw new Exception(bundle.getString("EXC_UnableToCreateExportedKeyNodeInfo"));
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
