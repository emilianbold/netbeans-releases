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

import org.netbeans.modules.xml.spi.model.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/**
 * Represent element attribute name (or its part for namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class ElementResultItem extends XMLResultItem {

    public ElementResultItem(){
        foreground = Color.blue;
    }
    
    public ElementResultItem(GrammarResult res){
        super(res.getNodeName());
        foreground = Color.blue;
    }

    /**
     * Replacenment text can be cutomized to retun pairs, empty tag or
     * just name of element.
     */
    public String getReplacementText(int modifiers) {
        boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;
        
        if (shift) {
            return displayText + "></" + displayText + '>';
        } else {
            return displayText;
        }
    }
    

    /**
     * If called with <code>SHIFT_MASK</code> modified it createa a start tag and 
     * end tag pair and place caret between them.
     */
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        String replacementText = getReplacementText(modifiers);
        replaceText(c, replacementText, offset, len);

        boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;
        
        if (shift) {
            Caret caret = c.getCaret();  // it is at the end of replacement
            int dot = caret.getDot();
            int rlen = replacementText.length();            
            caret.setDot(dot  - (rlen - replacementText.indexOf('>') + 1));
        }
        
        return shift == false;
    }
    
    /**
     *
     */
    static class EndTag extends ElementResultItem {
        
    }
    
}
