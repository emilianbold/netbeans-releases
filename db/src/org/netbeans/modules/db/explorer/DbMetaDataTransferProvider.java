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

import java.awt.datatransfer.DataFlavor;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * This interface is a means to communicate with the db/dbapi module, which
 * contains the actual data flavor and classes for transfer. The db/dbapi module
 * puts an implementation of this interface in the default lookup.
 *
 * @author Andrei Badea
 */
public interface DbMetaDataTransferProvider {

    /**
     * Returns the data flavor representing database connections.
     */
    DataFlavor getConnectionDataFlavor();

    /**
     * Returns the data flavor representing database tables.
     */
    DataFlavor getTableDataFlavor();
    
    /**
     * Returns the data flavor representing database views.
     */
    DataFlavor getViewDataFlavor();

    /**
     * Returns the data flavor representing columns of database tables.
     */
    DataFlavor getColumnDataFlavor();

    /**
     * Returns an object which encapsulates a database connection.
     */
    Object createConnectionData(DatabaseConnection dbconn, JDBCDriver jdbcDriver);
    
    /**
     * Returns an object which encapsulates a database table.
     */
    Object createTableData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName);
    
    /**
     * Returns an object which encapsulates a database view.
     */
    Object createViewData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName);
    
    /**
     * Returns an object which encapsulates a column of a database table.
     */
    Object createColumnData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName);
}
