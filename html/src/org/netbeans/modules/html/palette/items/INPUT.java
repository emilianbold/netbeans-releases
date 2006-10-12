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
public class INPUT implements ActiveEditorDrop {

    public static final String TYPE_TEXT = "text"; // NOI18N
    public static final String TYPE_PASS = "password"; // NOI18N
    public static final String TYPE_HIDDEN = "hidden"; // NOI18N
    public static final String STATE_DISABLED = "disabled"; // NOI18N
    public static final String STATE_READONLY = "readonly"; // NOI18N
    
    private String name = "";
    private String value = "";
    private String type = TYPE_TEXT;
    private boolean disabled = false;
    private boolean hidden = false;
    private boolean readonly = false;
    private String width = "";
    
    public INPUT() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        INPUTCustomizer c = new INPUTCustomizer(this);
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

        String strName = " name=\"" + name + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N

        String strReadOnly = (readonly ? " readonly=\"readonly\"" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String strWidth = "";
        if (width.length() > 0)
            strWidth = " size=\"" + width + "\""; // NOI18N
        
        String inputBody = "<input" + strType + strName + strValue + strWidth + strReadOnly + strDisabled + " />"; // NOI18N
        
        return inputBody;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
        
}
