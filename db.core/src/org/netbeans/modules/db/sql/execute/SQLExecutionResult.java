/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Returns the result of executing a list of SQL statements.
 *
 * @author Andrei Badea
 */
public class SQLExecutionResult {
    
    /**
     * The executed statement.
     */
    private final Statement statement;
    
    /**
     * The ResultSet returned by the statement execution.
     */
    private final ResultSet resultSet;
    
    /**
     * The number of the rows affected by the statement execution.
     */
    private final int rowCount;
    
    public SQLExecutionResult(Statement statement, ResultSet resultSet) {
        this.statement = statement;
        this.resultSet = resultSet;
        this.rowCount = 0;
    }
    
    public SQLExecutionResult(Statement statement, int rowCount) {
        this.statement = statement;
        this.rowCount = rowCount;
        this.resultSet = null;
    }
    
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    public Statement getStatement() {
        return statement;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        statement.close();
    }
}
