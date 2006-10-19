/*
 * TEST.java
 *
 * Created on August 21, 2006, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.snippets;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author bohemius
 */
public class TEST implements ActiveEditorDrop {
      
    /** Creates a new instance of TEST */
    public TEST() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        String body = "//TEST";
        try {
            SnippetsPaletteUtilities.insert(body, targetComponent);
        } catch (BadLocationException ble) {
            return false;
        }
        return true;
    }
    
}
