/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import org.netbeans.modules.jmx.common.WizardHelpers;


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
