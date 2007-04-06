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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.swingapp.actions;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * The AcceleratorKeyListener is a special KeyListener which can watch for
 * accelerator key combos and turn them into strings that can be parsed by
 * the KeyStroke class.  This is used to make Accelerator editor textfields
 * 
 * @author joshua.marinacci@sun.com
 */
public class AcceleratorKeyListener implements KeyListener {

    int currentKeyCode = 0;

    public void keyPressed(KeyEvent ke) {
        ke.consume();
        JTextField tf = (JTextField) ke.getSource();
        tf.setText(toString(ke));
    }

    public void keyReleased(KeyEvent ke) {
        ke.consume();
        JTextField tf = (JTextField) ke.getSource();
        switch(currentKeyCode) {
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_ALT_GRAPH:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_SHIFT:


                tf.setText("");


                return;
        }
    }

    public void keyTyped(KeyEvent ke) {
        ke.consume();
    }

    private String toString(KeyEvent ke) {
        KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(ke);
        int keyCode = currentKeyCode = ke.getKeyCode();
        return keyStroke.toString();
    }
}