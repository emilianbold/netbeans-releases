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
import java.util.ResourceBundle;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;


/**
 *
 * @author Libor Kotouc
 */
public class Report implements ActiveEditorDrop {
    
    public static String QUERY_DEFAULT = "SELECT column_name(s) FROM table_name"; // NOI18N
    private static final String VARIABLE_DEFAULT = "result"; // NOI18N
    
    SQLStmt stmt = null;
    
    private String variable = VARIABLE_DEFAULT;
    private int scopeIndex = SQLStmt.SCOPE_DEFAULT;
    private String query = QUERY_DEFAULT;
    
    private String displayName;
    private String stmtLabel = "";
    private String stmtACSN = "";
    private String stmtACSD = "";
    
    public Report() {
        
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-Report"); // NOI18N
        }
        catch (Exception e) {}
        
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle"); // NOI18N
        try {
            stmtLabel = bundle.getString("LBL_Report_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        try {
            stmtACSN = bundle.getString("ACSN_Report_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        try {
            stmtACSD = bundle.getString("ACSD_Report_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        
        stmt = new SQLStmt(variable, scopeIndex, query);
        
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        
        boolean accept = stmt.customize(targetComponent, displayName, stmtLabel, stmtACSN, stmtACSD);
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        variable = stmt.getVariable();
        String strVariable = " var=\"\""; // NOI18N
        if (variable.length() > 0)
            strVariable = " var=\"" + variable + "\""; // NOI18N
            
        scopeIndex = stmt.getScopeIndex();
        String strScope = "";
        if (scopeIndex != SQLStmt.SCOPE_DEFAULT)
            strScope = " scope=\"" + SQLStmt.scopes[scopeIndex] + "\""; // NOI18N

        query = stmt.getStmt();
        String strQuery = query;
        if (query.length() > 0)
            strQuery += "\n"; // NOI18N

        String body =  "<sql:query" + strVariable + strScope + ">\n" + // NOI18N
                        strQuery +
                        "</sql:query>\n" + // NOI18N
                        "\n" + // NOI18N
                        "<table>\n" + // NOI18N
                        "<!-- column headers -->\n" + // NOI18N
                        "<tr>\n" + // NOI18N
                        "<c:forEach var=\"columnName\" items=\"${" + variable + ".columnNames}\">\n" + // NOI18N
                        "<th><c:out value=\"${columnName}\"/></th>\n" + // NOI18N
                        "</c:forEach>\n" + // NOI18N
                        "</tr>\n" + // NOI18N
                        "<!-- column data -->\n" + // NOI18N
                        "<c:forEach var=\"row\" items=\"${" + variable + ".rowsByIndex}\">\n" + // NOI18N
                        "<tr>\n" + // NOI18N
                        "<c:forEach var=\"column\" items=\"${row}\">\n" + // NOI18N
                        "<td><c:out value=\"${column}\"/></td>\n" + // NOI18N
                        "</c:forEach>\n" + // NOI18N
                        "</tr>\n" + // NOI18N
                        "</c:forEach>\n" + // NOI18N
                        "</table>";// NOI18N
        
        return body;
    }

   
}
