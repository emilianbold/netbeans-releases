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

package org.netbeans.modules.sun.manager.jbi.editors;


import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditorSupport;

import javax.swing.JPasswordField;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 *
 * @author  nityad
 */

public class PasswordEditor extends PropertyEditorSupport
        implements EnhancedPropertyEditor /*, ExPropertyEditor*/ {

//    private PropertyEnv mEnv;
    
    private String value;

    public String getAsText() {
        if (value != null) {
            return value.replaceAll(".", "*"); // NOI18N
        } else { 
            return null;
        }
    }
    
    public void setAsText(String value) throws IllegalArgumentException {        
        if (value != null){ 
            this.value = value;
            firePropertyChange();
        }
    }
    
    public void setValue(Object value) {
        setAsText((String)value);
    }
    
    public Object getValue() {
        return value;
    }
    
    public Component getInPlaceCustomEditor() {
        JPasswordField textfield = new JPasswordField(value);
        textfield.setEchoChar('*'); // NOI18N
        textfield.selectAll();
        textfield.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                JPasswordField source = (JPasswordField)evt.getSource();
                // value = new String(source.getPassword());
                setValue(new String(source.getPassword()));
                // CR 5055478/6199209 cb.setText(curValue);
                //firePropertyChange();
                if (evt.getKeyCode() == KeyEvent.VK_ENTER){
                    KeyEvent esc = new KeyEvent(evt.getComponent(), 
                            KeyEvent.KEY_PRESSED, 0, 0, 
                            KeyEvent.VK_ESCAPE, 
                            KeyEvent.CHAR_UNDEFINED); 
                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .dispatchKeyEvent(esc);
                    //firePropertyChange();
                }
            }
        });
        return textfield;
    }
    
    public boolean hasInPlaceCustomEditor() {
        return true;
    }
    
    public boolean supportsEditingTaggedValues() {
        return false;
    }

//    /**
//     * This method is called by the IDE to pass
//     * the environment to the property editor.
//     * @param env Environment passed by the ide.
//     */
//    public void attachEnv(PropertyEnv env) {
//        mEnv = env;
//    }
}





