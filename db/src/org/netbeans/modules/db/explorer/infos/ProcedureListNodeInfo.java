/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;

public class ProcedureListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-7911927402768472443L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getProcedures("%");
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    
                    //workaround for issue #21409 (http://db.netbeans.org/issues/show_bug.cgi?id=21409)
                    if (drvSpec.getDBName().indexOf("Oracle") != -1) {
                        String pac = (String) rset.get(new Integer(1));
                        if (pac != null)
                            rset.put(new Integer(3), pac + "." + rset.get(new Integer(3)));
                    }

                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE, rset);
                    if (info != null) {
                        info.put(DatabaseNode.PROCEDURE, info.getName());
                        children.add(info);
                    } else
                        throw new Exception(bundle().getString("EXC_UnableToCreateProcedureNodeInfo")); //NOI18N
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

}
