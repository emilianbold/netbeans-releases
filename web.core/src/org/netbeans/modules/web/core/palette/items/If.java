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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class If implements ActiveEditorDrop {

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;

    private String condition = "";
    private String variable = "";
    private int scopeIndex = SCOPE_DEFAULT;
    
    public If() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        IfCustomizer c = new IfCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
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
        
        String strCondition = " test=\"" + condition + "\""; // NOI18N
        
        String strVariable = "";
        if (variable.length() > 0)
            strVariable = " var=\"" + variable + "\""; // NOI18N
            
        String strScope = "";
        if (scopeIndex != SCOPE_DEFAULT)
            strScope = " scope=\"" + scopes[scopeIndex] + "\""; // NOI18N

        String ifBody =  "<c:if" + strCondition + strVariable + strScope + ">\n" + // NOI18N
                         "</c:if>";// NOI18N
        
        return ifBody;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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
        
   
}
