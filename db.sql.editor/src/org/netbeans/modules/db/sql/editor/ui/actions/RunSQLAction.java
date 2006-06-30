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

import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.SQLExecuteCookie;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Action for executing SQL against a database connection
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class RunSQLAction extends CookieAction {
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(RunSQLAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/runsql.png"; // NOI18N

    /**
     * The cookie classes supported by this action
     */
    protected Class[] cookieClasses () {
        return new Class[] { SQLExecuteCookie.class };
    }

    /** 
     * All selected nodes must be SQL ones to allow this action 
     */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** 
     * Schedule the action for execution asynchronously since it might take a 
     * while to complete
     */
    protected void performAction (Node[] nodes) {
        if (nodes.length < 1) {
            return;
        }
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "Called performAction on node: " + nodes[0]); // NOI18N
        }
        EditorCookie editorCookie = (EditorCookie)nodes[0].getCookie(EditorCookie.class);
        SQLExecuteCookie sqlCookie = (SQLExecuteCookie)nodes[0].getCookie(SQLExecuteCookie.class);
        
        if (sqlCookie == null) {
            if (LOG) {
                LOGGER.log(ErrorManager.INFORMATIONAL, "Called performAction on node without SQLExecuteCookie"); // NOI18N
            }
            return;
        }
        
        DatabaseConnection dbconn = ConnectionAction.getConnectionForCookie(sqlCookie);
        if (dbconn == null) {
            if (LOG) {
                LOGGER.log(ErrorManager.INFORMATIONAL, "No database connection selected for node"); // NOI18N
            }
            return;
        }
        
        try {
            sqlCookie.executeSQL(dbconn);
        } catch (SQLException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /** 
     * Human presentable name. 
     */
    public String getName() {
        return NbBundle.getMessage(RunSQLAction.class, "LBL_RunSqlAction");
    }

    /** 
     * Find the icon resource path to use for this action
     */
    protected String iconResource () {
        return ICON_PATH;
    }

    /** 
     * Provide help context. 
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (RunSQLAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
}