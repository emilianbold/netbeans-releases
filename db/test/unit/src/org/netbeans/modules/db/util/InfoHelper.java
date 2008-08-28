/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
