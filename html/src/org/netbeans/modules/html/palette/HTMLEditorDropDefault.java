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

package org.netbeans.modules.html.palette;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;



/**
 *
 * @author Libor Kotouc
 */
public class HTMLEditorDropDefault implements ActiveEditorDrop {
    
    String body;

    public HTMLEditorDropDefault(String body) {
        this.body = body;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        if (targetComponent == null)
            return false;

        try {
            HTMLPaletteUtilities.insert(body, (JTextComponent)targetComponent);
        }
        catch (BadLocationException ble) {
            return false;
        }
        
        return true;
    }

}
