/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
                cb.setText(curValue);
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


  
      
  
