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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;

/**
 * Encapsulates the result of the execution of a list of SQL statements.
 *
 * @author Andrei Badea
 */
public class SQLExecutionResults {
    
    private final List<SQLExecutionResult> results;
    
    public SQLExecutionResults(List<SQLExecutionResult> results) {
        this.results = Collections.unmodifiableList(results);
    }
    
    public List<SQLExecutionResult> getResults() {
        return results;
    }
    
    public void close() {
        for (Iterator<SQLExecutionResult> i = results.iterator(); i.hasNext();) {
            try {
                i.next().close();
            } catch (SQLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    public int size() {
        return results.size();
    }
    
    public boolean hasExceptions() {
        for (SQLExecutionResult result: results) {
            if (result.getException() != null) {
                return true;
            }
        }
        return false;
    }
}
