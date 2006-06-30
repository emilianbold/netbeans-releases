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
