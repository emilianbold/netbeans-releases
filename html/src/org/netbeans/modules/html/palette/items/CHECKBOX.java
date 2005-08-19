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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class CHECKBOX implements ActiveEditorDrop {

    public static final String VALUE_DEFAULT = "ON"; // NOI18N
    
    private String name = "";
    private String value = VALUE_DEFAULT;
    private boolean selected = false;
    private boolean disabled = false;
    
    public CHECKBOX() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        CHECKBOXCustomizer c = new CHECKBOXCustomizer(this);
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
        
        String strName = " name=\"" + name + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N

        String strSelected = (selected ? " checked" : ""); // NOI18N

        String strDisabled = (disabled ? " disabled" : ""); // NOI18N
        
        String inputBody = "<input type=\"checkbox\"" + strName + strValue + strSelected + strDisabled + " />"; // NOI18N
        
        return inputBody;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
        
}
