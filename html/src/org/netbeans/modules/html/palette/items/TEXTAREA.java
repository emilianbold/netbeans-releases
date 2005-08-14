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
public class TEXTAREA implements ActiveEditorDrop {

    public static final String STATE_DISABLED = "disabled";
    public static final String STATE_READONLY = "readonly";
    
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
        
        String strName = " name=\"" + name + "\"";

        String strValue = value;
        if (value.length() > 0)
            strValue += "\n";

        String strReadOnly = (readonly ? " readonly" : "");
        String strDisabled = (disabled ? " disabled" : "");

        String strRows = " rows=\"" + rows + "\"";
        String strCols = " cols=\"" + cols + "\"";
        
        String taBody = "<textarea" + strName + strRows + strCols + strReadOnly + strDisabled + ">\n" +
                        strValue +
                        "</textarea>";
        
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
