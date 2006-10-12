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

package org.netbeans.modules.db.sql.execute.ui;

import java.io.IOException;
import java.sql.SQLException;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLResultPanelModel {
    
    private final ResultSetTableModel resultSetModel;
    private final String affectedRows;
    
    public static SQLResultPanelModel create(SQLExecutionResults executionResults) throws IOException, SQLException {
        ResultSetTableModel resultSetModel = null;
        String affectedRows = null;
        
        if (executionResults != null && executionResults.size() > 0) {
            SQLExecutionResult result = (SQLExecutionResult)executionResults.getResults().iterator().next();
            
            if (result.getResultSet() != null) {
                resultSetModel = ResultSetTableModel.create(result.getResultSet());
                if (resultSetModel == null) { // thread interrupted
                    return null;
                }
            } else {
                return new SQLResultPanelModel();
            }
        }
        
        return new SQLResultPanelModel(resultSetModel, affectedRows);
    }

    private SQLResultPanelModel() {
        this(null, null);
    }
    
    private SQLResultPanelModel(ResultSetTableModel resultSetModel, String affectedRows) {
        this.resultSetModel = resultSetModel;
        this.affectedRows = affectedRows;
    }
    
    public ResultSetTableModel getResultSetModel() {
        return resultSetModel;
    }
    
    public String getAffectedRows() {
        return affectedRows;
    }
    
    public boolean isEmpty() {
        return resultSetModel == null && affectedRows == null;
    }
}
