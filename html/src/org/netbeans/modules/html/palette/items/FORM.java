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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class FORM implements ActiveEditorDrop {

    public static final String METHOD_GET = "GET"; // NOI18N
    public static final String METHOD_POST = "POST"; // NOI18N

    public static final String ENC_URLENC = "application/x-www-form-urlencoded"; // NOI18N
    public static final String ENC_MULTI = "multipart/form-data"; // NOI18N
    
    private static final String METHOD_DEFAULT = METHOD_GET;
    private static final String ENC_DEFAULT = ENC_URLENC;
    
    private String action = "";
    private String method = METHOD_DEFAULT;
    private String enc = ENC_DEFAULT;
    private String name = "";
    
    public FORM() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        FORMCustomizer c = new FORMCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HTMLPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strAction = "";
        if (action.length() > 0)
            strAction = " action=\"" + action + "\""; // NOI18N
        
        String strMethod = "";
        if (!method.equals(METHOD_DEFAULT))
            strMethod = " method=\"" + method + "\""; // NOI18N

        String strEnc = "";
        if (!enc.equals(ENC_DEFAULT))
            strEnc = " enctype=\"" + enc + "\""; // NOI18N

        String strName = "";
        if (name.length() > 0)
            strName = " name=\"" + name + "\""; // NOI18N

        String formBody = "<form" + strName + strAction + strMethod + strEnc + ">\n</form>"; // NOI18N
        
        return formBody;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnc() {
        return enc;
    }

    public void setEnc(String enc) {
        this.enc = enc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
        
}
