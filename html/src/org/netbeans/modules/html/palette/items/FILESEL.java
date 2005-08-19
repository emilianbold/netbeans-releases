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
public class FILESEL implements ActiveEditorDrop {

    private String name = "";
    private String width = "";
    private boolean disabled = false;
    
    public FILESEL() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        FILESELCustomizer c = new FILESELCustomizer(this);
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

        String strWidth = "";
        if (width.length() > 0)
            strWidth = " width=\"" + width + "\""; // NOI18N
        
        String strDisabled = (disabled ? " disabled" : ""); // NOI18N

        String fileselBody = "<input type=\"file\"" + strName + " value=\"\"" + strWidth + strDisabled + " />"; // NOI18N
        
        return fileselBody;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
        
}
