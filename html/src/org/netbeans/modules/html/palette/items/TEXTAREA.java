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
public class TEXTAREA implements ActiveEditorDrop {

    public static final String STATE_DISABLED = "disabled"; // NOI18N
    public static final String STATE_READONLY = "readonly"; // NOI18N

    public static final int ROWS_DEFAULT = 4;
    public static final int COLS_DEFAULT = 20;
    
    private String name = "";
    private String value = "";
    private boolean disabled = false;
    private boolean readonly = false;
    private int rows = ROWS_DEFAULT;
    private int cols = COLS_DEFAULT;
    
    public TEXTAREA() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        TEXTAREACustomizer c = new TEXTAREACustomizer(this);
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

        String strValue = value;
        if (value.length() > 0)
            strValue += "\n";

        String strReadOnly = (readonly ? " readonly" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled" : ""); // NOI18N

        String strRows = " rows=\"" + rows + "\""; // NOI18N
        String strCols = " cols=\"" + cols + "\""; // NOI18N
        
        String taBody = "<textarea" + strName + strRows + strCols + strReadOnly + strDisabled + ">\n" + // NOI18N
                        strValue +
                        "</textarea>"; // NOI18N
        
        return taBody;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
        
}
