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

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.SQLExecuteCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Selects the currently selected connection node in an explorer window
 * thus providing access to the db explorer functionality for it
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class SelectInExplorerAction extends CookieAction {
    
    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/showinexplorer.gif"; // NOI18N
    
    /**
     * This action only works with SQLExecuteCookies
     */
    protected Class[] cookieClasses () {
        return new Class[] { SQLExecuteCookie.class };
    }

    /** 
     * All selected nodes must be SQL objects to allow this action 
     */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** 
     * Perform the actual action
     */
    protected void performAction (Node[] nodes) {
        SQLExecuteCookie sqlCookie = (SQLExecuteCookie)nodes[0].getCookie(SQLExecuteCookie.class);
        DatabaseConnection dbconn = ConnectionAction.getConnectionForCookie(sqlCookie);
        if (dbconn != null) {
            ConnectionManager.getDefault().selectConnectionInExplorer(dbconn);
        }
    }   
    
    /**
     * Get the display name
     */
    public String getName() {
        return NbBundle.getMessage(SelectInExplorerAction.class, "LBL_SelectInExplorerAction");
    }

    protected String iconResource () {
        return ICON_PATH;
    }

    /** 
     * Get the help context
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SelectInExplorerAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
}