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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

/**
 *
 * @author an156382
 */
public class OperationTextFieldKeyListener implements KeyListener {

    /** Creates a new instance of OperationTextFieldKeyListener */
    public OperationTextFieldKeyListener() {
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
