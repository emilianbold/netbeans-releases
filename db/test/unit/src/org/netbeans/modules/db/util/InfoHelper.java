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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.IndexListNodeInfo;
import org.netbeans.modules.db.explorer.infos.IndexNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

/**
 * This class provides utility routines to help construct Info nodes
 * to be used in testing
 * 
 * @author David
 */
public class InfoHelper {
    
    private Specification spec;
    private DriverSpecification drvSpec;
    private Connection conn;
    
    public InfoHelper(Specification spec, DriverSpecification drvSpec,
            Connection conn) {
        this.spec = spec;
        this.drvSpec = drvSpec;
        this.conn = conn;
    }
    
    public DatabaseNodeInfo getTableInfo(String tablename) throws Exception {
        drvSpec.getTables(tablename, null);
        ResultSet rs = drvSpec.getResultSet();
        assert rs != null;
        
        HashMap rset = new HashMap();
        rs.next();
        rset = drvSpec.getRow();
        assert rset != null;

        ConnectionNodeInfo connNodeInfo = (ConnectionNodeInfo)
                DatabaseNodeInfo.createNodeInfo(
                        null, DatabaseNodeInfo.CONNECTION);
        connNodeInfo.setDriverSpecification(drvSpec);
        connNodeInfo.setSpecification(spec);
        connNodeInfo.setConnection(conn);

        DatabaseNodeInfo tableInfo =
                DatabaseNodeInfo.createNodeInfo(
                    connNodeInfo, 
                    DatabaseNode.TABLE,
                    rset);
        
        rs.close();
        
        return tableInfo;
    }
}
