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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.netbeans.modules.swingapp.ActionPropertyEditorPanel;

/**
 * The AcceleratorKeyListener is a special KeyListener which can watch for
 * accelerator key combos and turn them into strings that can be parsed by
 * the KeyStroke class.  This is used to make Accelerator editor textfields
 * 
 * @author joshua.marinacci@sun.com
 */
public class AcceleratorKeyListener implements KeyListener {

    private int currentKeyCode = 0;
    private KeyStroke keyStroke = null;
    private ActionPropertyEditorPanel panel;

    public AcceleratorKeyListener(ActionPropertyEditorPanel panel) {
        this.panel = panel;
    }
    
    public void keyPressed(KeyEvent ke) {
        ke.consume();
        JTextField tf = (JTextField) ke.getSource();
        setModifiers(ke);
        String s = toString(ke);
        tf.setText(s);
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
    
    public void setCurrentKeyStroke(KeyStroke ks) {
        clearFields();
        keyStroke = ks;
        if(keyStroke != null) {
            currentKeyCode = keyStroke.getKeyCode();
            panel.acceleratorText.setText(KeyEvent.getKeyText(currentKeyCode));
            panel.shiftCheckbox.setSelected((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) > 0);
            panel.controlCheckbox.setSelected((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) > 0);
            panel.altCheckbox.setSelected((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) > 0);
            panel.metaCheckbox.setSelected((keyStroke.getModifiers() & InputEvent.META_DOWN_MASK) > 0);
        };
    }
    
    public KeyStroke getCurrentKeyStroke() {
       return keyStroke;
    }
    
    public void clearFields() {
        panel.shiftCheckbox.setSelected(false);
        panel.controlCheckbox.setSelected(false);
        panel.altCheckbox.setSelected(false);
        panel.metaCheckbox.setSelected(false);
        panel.acceleratorText.setText("");
        keyStroke = null;
    }
    
    public void setEnabled(boolean enabled) {
        panel.shiftCheckbox.setEnabled(enabled);
        panel.controlCheckbox.setEnabled(enabled);
        panel.altCheckbox.setEnabled(enabled);
        panel.metaCheckbox.setEnabled(enabled);
        panel.acceleratorText.setEnabled(enabled);
    }

    public void updateFromModifiers() {
        int mask = 0;
        if(panel.shiftCheckbox.isSelected()) { mask = mask | InputEvent.SHIFT_MASK; }
        if(panel.altCheckbox.isSelected()) { mask = mask | InputEvent.ALT_MASK; }
        if(panel.controlCheckbox.isSelected()) { mask = mask | InputEvent.CTRL_MASK; }
        if(panel.metaCheckbox.isSelected()) { mask = mask | InputEvent.META_MASK; }
        keyStroke = KeyStroke.getKeyStroke(currentKeyCode, mask);
    }
    
    private void setModifiers(KeyEvent ke) {
        panel.shiftCheckbox.setSelected(ke.isShiftDown());
        panel.controlCheckbox.setSelected(ke.isControlDown());
        panel.altCheckbox.setSelected(ke.isAltDown());
        panel.metaCheckbox.setSelected(ke.isMetaDown());
    }
    
    
    private String toString(KeyEvent ke) {
        keyStroke = KeyStroke.getKeyStrokeForEvent(ke);
        currentKeyCode = ke.getKeyCode();
        return ke.getKeyText(currentKeyCode);
    }
    
}