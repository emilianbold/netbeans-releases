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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.xml.api.model.*;

/**
 * It represents attribute name (or namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class AttributeResultItem extends XMLResultItem {

    // we are requested to avoid appending extra data
    private boolean inline = false;

    public AttributeResultItem(GrammarResult res, boolean inline){
        super(res.getNodeName());
        selectionForeground = foreground = Color.green.darker().darker();        
        this.inline = inline;
    }
    
    public String getReplacementText(int modifiers) {
        String extend = inline ? "" : "=\"\"";     // NOI18N
        return super.getReplacementText(modifiers) + extend;
    }
    
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        boolean result = super.replaceText(c, getReplacementText(modifiers), offset, len);
        c.getCaret().setDot(c.getCaretPosition() - 1); //shift cursor into bracklets
        return result;
    }
    
}
