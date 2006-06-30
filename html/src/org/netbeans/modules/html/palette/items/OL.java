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
public class OL implements ActiveEditorDrop {

    public static final int ITEM_COUNT_DEFAULT = 2;

    public static final String DEFAULT = "default"; // NOI18N
    public static final String ARABIC_NUMBERS = "1"; // NOI18N
    public static final String LOWER_ALPHA = "a"; // NOI18N
    public static final String UPPER_ALPHA = "A"; // NOI18N
    public static final String LOWER_ROMAN = "i"; // NOI18N
    public static final String UPPER_ROMAN = "I"; // NOI18N
    
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
            sb.append("<li></li>\n"); // NOI18N
    
        String strType = "";
        if (!type.equals(DEFAULT))
            strType = " type=\"" + type + "\""; // NOI18N
        
        String oList = "<ol" + strType + ">\n" + sb.toString() + "</ol>\n"; // NOI18N
        
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
