/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.swingapp.actions;

import java.awt.Component;
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
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_ALT_GRAPH:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_META:
            case KeyEvent.VK_SHIFT:
                return;
            default:
                setModifiers(ke);
                String s = toString(ke);
                if (!tf.getText().equals(s)) {
                    tf.setText(s);
                }
        }
    }

    public void keyReleased(KeyEvent ke) {
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
        }
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
        if (ke.isShiftDown() || ke.isControlDown() || ke.isAltDown() || ke.isMetaDown()) {
            panel.shiftCheckbox.setSelected(ke.isShiftDown());
            panel.controlCheckbox.setSelected(ke.isControlDown());
            panel.altCheckbox.setSelected(ke.isAltDown());
            panel.metaCheckbox.setSelected(ke.isMetaDown());
        }
    }
    
    
    private String toString(KeyEvent ke) {
        if (!ke.isShiftDown() && !ke.isControlDown() && !ke.isAltDown() && !ke.isMetaDown()) {
            int modifiers = panel.altCheckbox.isSelected() ? KeyEvent.ALT_MASK : 0;
            modifiers = modifiers | (panel.controlCheckbox.isSelected() ? KeyEvent.CTRL_MASK : 0);
            modifiers = modifiers | (panel.metaCheckbox.isSelected() ? KeyEvent.META_MASK : 0);
            modifiers = modifiers | (panel.shiftCheckbox.isSelected() ? KeyEvent.SHIFT_MASK : 0);
            ke = new KeyEvent((Component)ke.getSource(), ke.getID(), ke.getWhen(), modifiers, ke.getKeyCode(), ke.getKeyChar());
        }
        keyStroke = KeyStroke.getKeyStrokeForEvent(ke);
        currentKeyCode = ke.getKeyCode();
        return KeyEvent.getKeyText(currentKeyCode);
    }
    
}