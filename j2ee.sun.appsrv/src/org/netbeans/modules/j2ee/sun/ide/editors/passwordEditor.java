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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * passwordEditor.java
 *
 * Created on February 36, 2004, 11:53 PM
 */
package org.netbeans.modules.j2ee.sun.ide.editors;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.beans.PropertyEditorSupport;

import javax.swing.JPasswordField;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 *
 * @author  nityad
 */

public class passwordEditor extends PropertyEditorSupport implements EnhancedPropertyEditor{

    public static String prev = "null"; // NOI18N
    private String curValue;
        
    public passwordEditor() {
	curValue = null;
    }

    public String getAsText () {
      if((curValue==null)||(curValue.trim().equals(""))) // NOI18N
         return prev;
      else
         return curValue;
    }

    public void setAsText (String string) throws IllegalArgumentException {
      /*if((string==null)||(string.equals(""))) // NOI18N
           curValue = prev;
       else
           curValue = string;*/

       if((string!=null)&&(!string.trim().equals(""))){ // NOI18N
            curValue = string;
            prev = curValue;
       }     
       firePropertyChange();
    }
    
    public void setValue (Object v) {
        if(!(v.toString().trim().equals(""))) {// NOI18N
           prev = (String)v;
        }   
        curValue = (String)v;
    }

    public Object getValue () {
       prev = curValue;
       return curValue;
    }

    public Component getInPlaceCustomEditor () {
       JPasswordField textfield = new JPasswordField(curValue);
       textfield.setEchoChar('*');
       textfield.selectAll();
       textfield.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                JPasswordField cb = (JPasswordField)evt.getSource();
                String enteredPwd = new String(cb.getPassword());
                curValue = enteredPwd;
                // CR 5055478/6199209 cb.setText(curValue);
                firePropertyChange();
                if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                    KeyEvent esc = new KeyEvent(evt.getComponent(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED); 
                    java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchKeyEvent(esc);
                    //firePropertyChange();
                }
            }
        });
        return textfield;
    }
    
    public boolean hasInPlaceCustomEditor () {
        return true;
    }

    public boolean supportsEditingTaggedValues () {
        return false;
    }

}


  
      
  
