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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;


import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

public class DDLBridge extends Object {

    private DriverSpecification drvSpec;

    /** Creates new DDLBridge */
    public DDLBridge(Connection con, String schema, DatabaseMetaData dmd) {
        try {
            SpecificationFactory fac = new SpecificationFactory();
            drvSpec = fac.createDriverSpecification(dmd.getDriverName().trim());
            drvSpec.setMetaData(dmd);
            drvSpec.setSchema(schema);
            
            //workaround for issue #4825200 - it seems there is a timing/thread problem with PointBase driver on Windows
            if (/*Utilities.isWindows() && */dmd.getDatabaseProductName().trim().equals("PointBase")) //NOI18N
                Thread.sleep(60);

            drvSpec.setCatalog(con.getCatalog());
        } catch (Exception exc) {
            org.openide.ErrorManager.getDefault().notify(exc);
        }
    }

    public DriverSpecification getDriverSpecification() {
        return drvSpec;
    }
    
}
