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
public class IMG implements ActiveEditorDrop {
    
    private String location = "";
    private String width = "";
    private String height = "";
    private String alttext = "";
    
    public IMG() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        IMGCustomizer c = new IMGCustomizer(this, targetComponent);
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
        
        String strLoc = " src=\"\""; // NOI18N
        if (location.length() > 0)
            strLoc = " src=\"" + location + "\""; // NOI18N
        
        String strWidth = "";
        if (width.length() > 0)
            strWidth = " width=\"" + width + "\""; // NOI18N

        String strHeight = "";
        if (height.length() > 0)
            strHeight = " height=\"" + height + "\""; // NOI18N

        String strAlt = "";
        if (alttext.length() > 0)
            strAlt = " alt=\"" + alttext + "\""; // NOI18N

        String imgBody = "<img" + strLoc + strWidth + strHeight + strAlt + "/>\n"; // NOI18N
        
        return imgBody;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getAlttext() {
        return alttext;
    }

    public void setAlttext(String alttext) {
        this.alttext = alttext;
    }
        
}
