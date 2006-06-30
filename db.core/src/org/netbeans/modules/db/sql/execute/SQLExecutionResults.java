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
 * Encapsulates the result of executing a list of SQL statements.
 *
 * @author Andrei Badea
 */
public class SQLExecutionResults {

    private final SQLExecutionResult[] results;

    public SQLExecutionResults(SQLExecutionResult[] results) {
        this.results = results;
    }

    public SQLExecutionResult[] getResults() {
        return results;
    }

    public void close() throws SQLException {
        for (int i = 0; i < results.length; i++) {
            results[i].close();
        }
    }
}
