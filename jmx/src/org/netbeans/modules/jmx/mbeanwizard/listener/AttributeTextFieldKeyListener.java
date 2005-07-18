/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import org.netbeans.modules.jmx.WizardHelpers;


/**
 *
 * @author an156382
 */
public class AttributeTextFieldKeyListener implements KeyListener{
    
    /**
     * Creates a new instance of AttributeTextFieldKeyListener 
     */
    public AttributeTextFieldKeyListener() {
    }
    
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {
        JTextField nameField = ((JTextField)e.getSource());
        String txt = nameField.getText();
        int selectionStart = nameField.getSelectionStart();
        int selectionEnd = nameField.getSelectionEnd();
        char typedKey = e.getKeyChar();
        boolean acceptedKey = false;
        if (selectionStart == 0) {
            acceptedKey = Character.isJavaIdentifierStart(typedKey);
        } else {
            acceptedKey = Character.isJavaIdentifierPart(typedKey);
        }
        if (acceptedKey) {
            if ((typedKey != KeyEvent.VK_BACK_SPACE) &&
                    (typedKey != KeyEvent.VK_DELETE)) {
                txt = txt.substring(0, selectionStart) +
                        typedKey +
                        txt.substring(selectionEnd);
            } else if (typedKey == KeyEvent.VK_DELETE) {
                txt = txt.substring(0, selectionStart) +
                        txt.substring(selectionEnd);
            } else {
                txt = txt.substring(0, selectionStart) +
                        txt.substring(selectionEnd);
            }
        } else {
            nameField.getToolkit().beep(); 
        }
        txt = WizardHelpers.capitalizeFirstLetter(txt);
        nameField.setText(txt);
        if ((typedKey == KeyEvent.VK_BACK_SPACE) ||
                (typedKey == KeyEvent.VK_DELETE))
            nameField.setCaretPosition(selectionStart);
        else {
            if (acceptedKey) {
                nameField.setCaretPosition(selectionStart + 1);
            } else {
                nameField.setCaretPosition(selectionStart);
            }
        }
        
        e.consume();
    }
}
