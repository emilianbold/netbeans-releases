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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Toolkit;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class RunSQLAction extends SQLExecutionBaseAction {

    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(RunSQLAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);

    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/runsql.png"; // NOI18N

    protected String getIconBase() {
        return ICON_PATH;
    }

    protected String getDisplayName(SQLExecution sqlExecution) {
        return NbBundle.getMessage(RunSQLAction.class, "LBL_RunSqlAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RunSQLAction.class);
    }

    protected void actionPerformed(SQLExecution sqlExecution) {
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "actionPerformed for " + sqlExecution); // NOI18N
        }
        DatabaseConnection dbconn = sqlExecution.getDatabaseConnection();
        if (dbconn != null) {
            sqlExecution.execute();
        } else {
            notifyNoDatabaseConnection();
        }
    }
}
