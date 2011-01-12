/*
 * EndTagAutocompletionResultItem.java
 *
 * Created on September 11, 2006, 4:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.text.completion;

import javax.swing.text.JTextComponent;

/**
 *
 * @author marek
 */
public class EndTagAutocompletionResultItem extends XMLResultItem {
    
    public EndTagAutocompletionResultItem(int position, String text) {
        super(position, endtagize(text));
    }

    private static String endtagize(String text) {
        return "</" + text + ">";
    }
    
    boolean replaceText( JTextComponent component, String text, int offset, int len) {
        boolean replaced = super.replaceText(component, text, offset, len);
        if(replaced) {
            //shift the cursor between tags
            component.setCaretPosition(offset);
        }
        return replaced;
    }
}
