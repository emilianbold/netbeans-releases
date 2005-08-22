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
public class Update implements ActiveEditorDrop {
    
    public static String STMT_DEFAULT = "UPDATE table_name\nSET column_name = new_value\nWHERE column_name = some_value";
    
    SQLStmt stmt = null;
    
    private String variable = "";
    private int scopeIndex = SQLStmt.SCOPE_DEFAULT;
    private String update = STMT_DEFAULT;
    
    private String displayName;
    private String stmtLabel = "";
    private String stmtACSN = "";
    private String stmtACSD = "";
    
    public Update() {
        
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-Update"); // NOI18N
        }
        catch (Exception e) {}
        
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle"); // NOI18N
        try {
            stmtLabel = bundle.getString("LBL_Update_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        try {
            stmtACSN = bundle.getString("ACSN_Update_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        try {
            stmtACSD = bundle.getString("ACSD_Update_Stmt"); // NOI18N
        }
        catch (Exception e) {}
        
        stmt = new SQLStmt(variable, scopeIndex, update);
        
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
        String strVariable = " var=\"\"";
        if (variable.length() > 0)
            strVariable = " var=\"" + variable + "\""; // NOI18N
            
        scopeIndex = stmt.getScopeIndex();
        String strScope = "";
        if (scopeIndex != SQLStmt.SCOPE_DEFAULT)
            strScope = " scope=\"" + SQLStmt.scopes[scopeIndex] + "\""; // NOI18N

        update = stmt.getStmt();
        String strUpdate = update;
        if (update.length() > 0)
            strUpdate += "\n";
        
        String queryBody =  "<sql:update" + strVariable + strScope + ">\n" + // NOI18N
                            strUpdate +
                            "</sql:update>";// NOI18N
        
        return queryBody;
    }

   
}
