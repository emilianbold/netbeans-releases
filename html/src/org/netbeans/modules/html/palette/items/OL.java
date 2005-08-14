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
public class OL implements ActiveEditorDrop {
    
    public static final int ITEM_COUNT_DEFAULT = 2;
    
    public static final String DEFAULT = "default";
    public static final String ARABIC_NUMBERS = "1";
    public static final String LOWER_ALPHA = "a";
    public static final String UPPER_ALPHA = "A";
    public static final String LOWER_ROMAN = "i";
    public static final String UPPER_ROMAN = "I";
    
    private String type = DEFAULT;
    private int count = ITEM_COUNT_DEFAULT;
    
    public OL() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        OLCustomizer c = new OLCustomizer(this);
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
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++)
            sb.append("<li></li>\n");
    
        String strType = "";
        if (!type.equals(DEFAULT))
            strType = " type=\"" + type + "\"";
        
        String oList = "<ol" + strType + ">\n" + sb.toString() + "</ol>\n";
        
        return oList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
        
}
