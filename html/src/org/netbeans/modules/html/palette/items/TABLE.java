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
public class TABLE implements ActiveEditorDrop {
    
    private static final int ROWS_DEFAULT = 2;
    private static final int COLS_DEFAULT = 2;
    private static final int BORDER_DEFAULT = 1;
    private static final String WIDTH_DEFAULT = "";
    private static final int CSPAC_DEFAULT = 0;
    private static final int CPADD_DEFAULT = 0;
    
    private int rows = ROWS_DEFAULT;
    private int cols = COLS_DEFAULT;
    private int border = BORDER_DEFAULT;
    private String width = WIDTH_DEFAULT;
    private int cspac = CSPAC_DEFAULT;
    private int cpadd = CPADD_DEFAULT;
            
    public TABLE() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        TABLECustomizer c = new TABLECustomizer(this);
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
        
        String tHead = generateTHead();
        String tBody = generateTBody();
        
        String strBorder = " border=\"" + border + "\"";
        
        String strWidth = "";
        if (!width.equals(WIDTH_DEFAULT))
            strWidth = " width=\"" + width + "\"";

        String strCspac = "";
        if (cspac != CSPAC_DEFAULT)
            strCspac = " cellspacing=\"" + cspac + "\"";
        
        String strCpadd = "";
        if (cpadd != CPADD_DEFAULT)
            strCpadd = " cellpadding=\"" + cpadd + "\"";
        
        
        String body = 
                "<table" + strBorder + strWidth + strCspac + strCpadd + ">\n" +
                "<thead>\n" + tHead + "</thead>\n" +
                "<tbody>\n" + tBody + "</tbody>\n" +
                "</table>\n";
        
        return body;
    }
    
    private String generateTHead() {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cols; i++)
            sb.append("<th></th>\n");
    
        String thead = "<tr>\n" + sb.toString() + "</tr>\n";
        
        return thead;
    }
    
    private String generateTBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rows; i++) {
            sb.append("<tr>\n");
            for (int j = 0; j < cols; j++)
                sb.append("<td></td>\n");
            sb.append("</tr>\n");
        }
                
        String tBody = sb.toString();
        
        return tBody;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setCspac(int cspac) {
        this.cspac = cspac;
    }

    public void setCpadd(int cpadd) {
        this.cpadd = cpadd;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getBorder() {
        return border;
    }

    public String getWidth() {
        return width;
    }

    public int getCspac() {
        return cspac;
    }

    public int getCpadd() {
        return cpadd;
    }
    
}
