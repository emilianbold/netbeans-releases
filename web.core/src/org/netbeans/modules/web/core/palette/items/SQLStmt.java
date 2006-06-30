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

package org.netbeans.modules.web.core.palette.items;
import javax.swing.text.JTextComponent;



/**
 *
 * @author Libor Kotouc
 */
public class SQLStmt {

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;

    //user data
    private String variable;
    private int scopeIndex;
    private String dataSource;
    private String stmt;

    private String helpID;

    public SQLStmt(String variable, int scopeIndex, String dataSource, String stmt, String helpID) {
                   
        this.variable = variable;
        this.scopeIndex = scopeIndex;
        this.dataSource = dataSource;
        this.stmt = stmt;
        
        this.helpID = helpID;
    }

    public boolean customize(JTextComponent target, String displayName, String stmtLabel, String stmtACSN, String stmtACSD) {

        SQLStmtCustomizer c = new SQLStmtCustomizer(this, target, displayName, stmtLabel, stmtACSN, stmtACSD, helpID);
        boolean accept = c.showDialog();
        
        return accept;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public int getScopeIndex() {
        return scopeIndex;
    }

    public void setScopeIndex(int scopeIndex) {
        this.scopeIndex = scopeIndex;
    }

    public String getStmt() {
        return stmt;
    }

    public void setStmt(String query) {
        this.stmt = query;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

}
