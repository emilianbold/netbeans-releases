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

package org.netbeans.modules.web.core.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author Libor Kotouc
 */
public class Choose implements ActiveEditorDrop {
    
    public static final int DEFAULT_WHENS = 1;
    
    private int whens = DEFAULT_WHENS;
    private boolean otherwise = true;
    
    public Choose() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

boolean accept = true;        
//        ChooseCustomizer c = new ChooseCustomizer(this, targetComponent);
//        boolean accept = c.showDialog();
//        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
//        }
        
        return accept;
    }

    private String createBody() {
        
        String cBody = generateChooseBody();
        String body = 
                "<c:choose>\n" + // NOI18N
                cBody +
                "</c:choose>\n"; // NOI18N
        
        return body;
    }
    
    private String generateChooseBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < whens; i++)
            sb.append("<c:when test=\"\">\n</c:when>\n"); // NOI18N
        
        if (otherwise)
            sb.append("<c:otherwise>\n</c:otherwise>\n"); // NOI18N
                
        String cBody = sb.toString();
        
        return cBody;
    }
    
}
