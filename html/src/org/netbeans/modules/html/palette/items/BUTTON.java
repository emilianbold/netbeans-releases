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
public class BUTTON implements ActiveEditorDrop {

    public static final String TYPE_SUBMIT = "submit"; // NOI18N
    public static final String TYPE_RESET = "reset"; // NOI18N
    public static final String TYPE_BUTTON = "button"; // NOI18N

    private String value = "";
    private String type = TYPE_SUBMIT;
    private boolean disabled = false;
    private String name = "";
    
    public BUTTON() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        BUTTONCustomizer c = new BUTTONCustomizer(this);
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
        
        String strType = " type=\"" + type + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N

        String strName = "";
        if (name.length() > 0)
            strName = " name=\"" + name + "\""; // NOI18N

        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String inputBody = "<input" + strType + strValue + strName + strDisabled + " />"; // NOI18N
        
        return inputBody;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
        
}
