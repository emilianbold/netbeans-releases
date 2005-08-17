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
    
    // TODO: maybe replace this class with something which will call back to the statement
    // TODO: what did I mean by the TODO above?
    
    private ResultSet[] resultSets;
    private Statement[] statements;
    
    public SQLExecutionResult(Statement[] statements, ResultSet[] resultSets) {
        this.resultSets = resultSets;
        this.statements = statements;
    }
    
    public ResultSet[] getResultSets() {
        return resultSets;
    }
    
    public Statement[] getStatements() {
        return statements;
    }
    
    public void close() throws SQLException {
        for (int i = 0; i < resultSets.length; i++) {
            resultSets[i].close();
        }
        for (int i = 0; i < statements.length; i++) {
            statements[i].close();
        }
    }
}
