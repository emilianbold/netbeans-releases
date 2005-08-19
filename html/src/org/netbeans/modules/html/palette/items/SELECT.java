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
public class SELECT implements ActiveEditorDrop {

    public static final int OPTIONS_DEFAULT = 2;
    public static final int OPTIONS_VISIBLE_DEFAULT = 1;
    
    private String name = "";
    private int options = OPTIONS_DEFAULT;
    private int optionsVisible = OPTIONS_VISIBLE_DEFAULT;
    private boolean disabled = false;
    private boolean multiple = false;
    
    
    public SELECT() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        SELECTCustomizer c = new SELECTCustomizer(this);
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
        
        String sBody = generateSBody();
        
        String strName = " name=\"" + name + "\""; // NOI18N

        String strVisibleOptions = "";
        if (optionsVisible != OPTIONS_VISIBLE_DEFAULT)
            strVisibleOptions = " size=\"" + optionsVisible + "\""; // NOI18N
        
        String strMulti = (multiple ? " multiple" : ""); // NOI18N
        String strDisabled = (disabled ? " disabled" : ""); // NOI18N

        String selBody = "<select" + strName + strVisibleOptions + strMulti + strDisabled + ">\n" + // NOI18N
                        sBody +
                        "</select>"; // NOI18N
        
        return selBody;
    }
        
    private String generateSBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < options; i++)
            sb.append("<option></option>\n"); // NOI18N
                
        String sBody = sb.toString();
        
        return sBody;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    public int getOptionsVisible() {
        return optionsVisible;
    }

    public void setOptionsVisible(int optionsVisible) {
        this.optionsVisible = optionsVisible;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
    
}
