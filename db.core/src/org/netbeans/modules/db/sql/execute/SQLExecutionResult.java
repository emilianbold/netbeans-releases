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

package org.netbeans.modules.db.sql.execute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Encapsulates the result of the execution of a single SQL statement.
 *
 * @author Andrei Badea
 */
public class SQLExecutionResult {

    /**
     * The info about the executed statement.
     */
    private final StatementInfo statementInfo;
    
    /**
     * The executed statement.
     */
    private final Statement statement;

    /**
     * The ResultSet returned by the statement execution.
     */
    private final ResultSet resultSet;

    /**
     * The exception (if any) which occurred while executing the statement.
     */
    private final SQLException exception;
    
    /**
     * The execution time in milliseconds.
     */
    private final long executionTime;
    
    /**
     * The number of the rows affected by the statement execution.
     */
    private final int rowCount;
    
    public SQLExecutionResult(StatementInfo info, Statement statement, ResultSet resultSet, long executionTime) {
        this(info, statement, resultSet, -1, null, executionTime);
    }
    
    public SQLExecutionResult(StatementInfo info, Statement statement, int rowCount, long executionTime) {
        this(info, statement, null, rowCount, null, executionTime);
    }
    
    public SQLExecutionResult(StatementInfo info, Statement statement, SQLException exception) {
        this(info, statement, null, -1, exception, 0);
    }
    
    private SQLExecutionResult(StatementInfo info, Statement statement, ResultSet resultSet, int rowCount, SQLException exception, long executionTime) {
        this.statementInfo = info;
        this.statement = statement;
        this.resultSet = resultSet;
        this.rowCount = rowCount;
        this.exception = exception;
        this.executionTime = executionTime;
    }
    
    public StatementInfo getStatementInfo() {
        return statementInfo;
    }
    
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public SQLException getException() {
        return exception;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void close() throws SQLException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    
    public String toString() {
        return "SQLExecutionResult[resultSet=" + resultSet + ",rowCount=" + rowCount + ",exception=" + exception + ",executionTime=" + executionTime + "]";
    }
}
