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
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Andrei Badea
 */
public class RunSQLSelectionAction extends SQLExecutionBaseAction {

    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(RunSQLSelectionAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);

    protected void initialize() {
        putValue(Action.NAME, NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunSQLSelectionAction"));
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public String getDisplayName(SQLExecution sqlExecution) {
        if (sqlExecution == null || sqlExecution.isSelection()) {
            return NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunSelectionAction");
        } else {
            return NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunCurrentStatementAction");
        }
    }

    public void actionPerformed(SQLExecution sqlExecution) {
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "actionPerformed for " + sqlExecution); // NOI18N
        }
        DatabaseConnection dbconn = sqlExecution.getDatabaseConnection();
        if (dbconn != null) {
            sqlExecution.executeSelection();
        } else {
            notifyNoDatabaseConnection();
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SelectionContextAwareDelegate(this, actionContext);
    }

    private static final class SelectionContextAwareDelegate extends ContextAwareDelegate implements Presenter.Popup {

        public SelectionContextAwareDelegate(RunSQLSelectionAction parent, Lookup actionContext) {
            super(parent, actionContext);
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.MenuItem(this, false);
        }
    }
}
