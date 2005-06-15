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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;

import org.openide.util.Utilities;

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
