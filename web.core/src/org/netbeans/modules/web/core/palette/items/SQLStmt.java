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
    private String stmt;
    
    public SQLStmt(String variable, int scopeIndex, String stmt) {
                   
        this.variable = variable;
        this.scopeIndex = scopeIndex;
        this.stmt = stmt;
    }

    public boolean customize(JTextComponent target, String displayName, String stmtLabel, String stmtACSN, String stmtACSD) {

        SQLStmtCustomizer c = new SQLStmtCustomizer(this, target, displayName, stmtLabel, stmtACSN, stmtACSD);
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

    
}
