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
    
    private ResultSetTableModel resultSetModel;
    private String affectedRows;
    
    public SQLResultPanelModel(SQLExecutionResults executionResults) throws IOException, SQLException {
        if (executionResults != null && executionResults.getResults().length > 0) {
            SQLExecutionResult result = executionResults.getResults()[0];
            
            if (result.getResultSet() != null) {
                resultSetModel = new ResultSetTableModel(result.getResultSet());
            } else {
                int rowCount = result.getRowCount();
                if (rowCount >= 0) {
                    affectedRows = String.valueOf(rowCount);
                } else {
                    affectedRows = NbBundle.getMessage(SQLResultPanel.class, "LBL_AffectedRowsUnknown");
                }
            }
        }
    }
    
    public ResultSetTableModel getResultSetModel() {
        return resultSetModel;
    }
    
    public String getAffectedRows() {
        return affectedRows;
    }
}
