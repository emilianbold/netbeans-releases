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

package org.netbeans.modules.db.explorer;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Column;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Connection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Table;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.View;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * 
 * @author Andrei Badea
 */
public abstract class DatabaseMetaDataTransferAccessor {
    
    public static DatabaseMetaDataTransferAccessor DEFAULT;
    
    static {
        Class c = DatabaseMetaDataTransferAccessor.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns an object which encapsulates a database connection.
     */
    public abstract Connection createConnectionData(DatabaseConnection dbconn, JDBCDriver jdbcDriver);
    
    /**
     * Returns an object which encapsulates a database table.
     */
    public abstract Table createTableData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName);
    
    /**
     * Returns an object which encapsulates a database view.
     */
    public abstract View createViewData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName);
    
    /**
     * Returns an object which encapsulates a column of a database table.
     */
    public abstract Column createColumnData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName);
}
