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
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/**
 * Represent element name (or its part for namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class ElementResultItem extends XMLResultItem {

    // does it represent start element name?
    // then there is more possibilities how to complete it
    private final boolean startElement;

    private final boolean empty;

    /**
     * Create a start element result item.
     */
    public ElementResultItem(GrammarResult res){
        super(res.getNodeName());
        foreground = Color.blue;
        startElement = true;
        empty = res.isEmptyElement();
    }

    /**
     * Create an end element result item.
     */
    public ElementResultItem(String name) {
        super(name);
        foreground = Color.blue;
        startElement = false;
        empty = false;
    }
    
    /**
     * Replacenment text can be cutomized to retun pairs, empty tag or
     * just name of element.
     */
    public String getReplacementText(int modifiers) {
        boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;
        
        if (shift && startElement) {
            if (empty) {
                return displayText + "/>";
            } else {
                return displayText + "></" + displayText + '>';
            }
        } else if (startElement) {
            return displayText;
        } else {
            return displayText + '>';
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
        
        if (shift && startElement) {
            Caret caret = c.getCaret();  // it is at the end of replacement            
            int dot = caret.getDot();
            int rlen = replacementText.length();
            if (empty) {
                caret.setDot((dot  - rlen) + replacementText.indexOf('/'));
            } else {
                caret.setDot((dot  - rlen) + replacementText.indexOf('<'));
            }
        }
        
        return false;
    }
    
    /**
     * @deprecated we use startElement flag
     */
//    static class EndTag extends ElementResultItem {        
//    }
    
}
