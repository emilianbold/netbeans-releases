/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options.keymap;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * KeyListener trasforming keystrokes to human-readable and displaying them
 * inside given textfield
 * @author Max Sauer
 */
public class ShortcutListener implements KeyListener {

    private JTextField textField;
    private boolean enterConfirms;

    /**
     * Creates new instance
     * @param textField target textField
     * @param enterConfirms whether ENTER keystroke should be taken as
     * confirmation or displayed in the same way as other shortcuts
     */
    public ShortcutListener(boolean enterConfirms) {
//        this.textField = textField;
        this.enterConfirms = enterConfirms;
    }

    private KeyStroke backspaceKS = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    private KeyStroke enterKS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private String key = ""; //NOI18N

    /**
     * Clears cached shortcut text representation
     */
    public void clear() {
        key = "";
    }

    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    public void keyPressed(KeyEvent e) {
        assert (e.getSource() instanceof JTextField);
        textField = (JTextField) e.getSource();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(
                e.getKeyCode(),
                e.getModifiers());

        boolean add = e.getKeyCode() != KeyEvent.VK_SHIFT &&
                e.getKeyCode() != KeyEvent.VK_CONTROL &&
                e.getKeyCode() != KeyEvent.VK_ALT &&
                e.getKeyCode() != KeyEvent.VK_META &&
                e.getKeyCode() != KeyEvent.VK_ALT_GRAPH;

        if (!(enterConfirms && keyStroke.equals(enterKS))) {
            if (keyStroke.equals(backspaceKS) && !key.equals("")) {
                // delete last key
                int i = key.lastIndexOf(' '); //NOI18N
                if (i < 0) {
                    key = ""; //NOI18N
                } else {
                    key = key.substring(0, i);
                }
                textField.setText(key);
            } else {
                // add key
                addKeyStroke(keyStroke, add);
            }

            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    private void addKeyStroke(KeyStroke keyStroke, boolean add) {
        String k = Utils.getKeyStrokeAsText(keyStroke);
        if (key.equals("")) { //NOI18N
            textField.setText(k);
            if (add)
                key = k;
        } else {
            textField.setText(key + " " + k); //NOI18N
            if (add)
                key += " " + k; //NOI18N
        }
    }
}
