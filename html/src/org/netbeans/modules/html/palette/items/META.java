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
public class META implements ActiveEditorDrop {

    public static final String TYPE_HEADERS = "HEADERS"; // NOI18N
    public static final String TYPE_ENGINES = "ENGINES"; // NOI18N

    public static final String[] headers = new String[] { "Content-Type", "Content-Language", "Refresh", "Cache-Control", "Expires" }; // NOI18N
    public static final int HEADER_DEFAULT = 0;
    public static final String[] engines = new String[] { "robots", "description", "keywords" }; // NOI18N
    public static final int ENGINE_DEFAULT = 0;
    
    private String type = TYPE_HEADERS;
    private int nameIndex = 0;
    private String name = "";
    private String content = "";
    
    public META() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        METACustomizer c = new METACustomizer(this);
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
        
        if (getNameIndex() != -1) {
            if (getType().equals(TYPE_HEADERS) && getNameIndex() != -1) 
                setName(headers[getNameIndex()]);
            else if (getType().equals(TYPE_ENGINES) && getNameIndex() != -1) 
                setName(engines[getNameIndex()]);
        }
        
        String strType = "";
        if (getType().equals(TYPE_HEADERS))
            strType = " http-equiv=\"" + getName() + "\""; // NOI18N
        else
            strType = " name=\"" + getName() + "\""; // NOI18N
        
        String strContent = " content=\"\""; // NOI18N
        if (getContent().length() > 0)
            strContent = " content=\"" + getContent() + "\""; // NOI18N  
        
        
        String meta = "<meta" + strType + strContent + " />"; // NOI18N
        
        return meta;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
        
}
