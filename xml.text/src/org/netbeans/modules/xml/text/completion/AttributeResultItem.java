/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;

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
    
    public AttributeResultItem(){
        selectionForeground = foreground = Color.green.darker().darker();        
    }
    
    public AttributeResultItem(GrammarResult res, boolean inline){
        super(res.getNodeName());
        selectionForeground = foreground = Color.green.darker().darker();        
        this.inline = inline;
    }
    
    public String getReplacementText(int modifiers) {
        String extend = inline ? "" : "=\"";     // NOI18N
        return super.getReplacementText(modifiers) + extend;
    }
    
}
