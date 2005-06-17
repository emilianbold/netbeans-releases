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
package org.netbeans.modules.j2ee.websphere6.ui.nodes.editors;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.explorer.propertysheet.editors.*;

import org.netbeans.modules.j2ee.websphere6.util.*;

/**
 * An editor for the password field that appears on the properties sheet for 
 * the instance node.
 * 
 * @author Kirill Sorokin
 */
public class WSPasswordEditor extends PropertyEditorSupport 
        implements EnhancedPropertyEditor {
    
    /**
     * The internal store for the edited object, for this editor we assume 
     * that it's a string
     */
    private String value = "";
    
    /**
     * Returns the edited object as a string.
     * 
     * @return the string representation of the edited object
     */
    public String getAsText() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "getAsText()");                 // NOI18N
        
        // return the object masquerading its real value with asterisks
        return value.replaceAll(".", "*");                             // NOI18N
    }
    
    /**
     * Sets the edited object value by supplying a string representation of
     * the new value
     * 
     * @param string the string representation of the new value
     */
    public void setAsText(String string) throws IllegalArgumentException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "setAsText(" + string + ")");   // NOI18N
        
        // if the supplied string is not null 0 update the value and notify the
        // listeners
        if (string != null) {
            value = string;
            firePropertyChange();
        }
    }
    
    /**
     * Sets the edited object value
     * 
     * @param object the new value
     */
    public void setValue(Object object) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "setValue(" + object + ")");    // NOI18N
        
        // if the supplied object is not null - update the value
        if (object != null) {
            value = object.toString();
        }
    }
    
    /** 
     * Returns the edited object's value
     * 
     * @return the edited object's value
     */
    public Object getValue() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "getValue()");                  // NOI18N
       
        // return
        return value;
    }
    
    /**
     * Returns the custom in-line editor for the object. In this case it's 
     * a password field
     * 
     * @return a swing component for editing the object
     */
    public Component getInPlaceCustomEditor() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "getInPlaceCustomEditor()");    // NOI18N
        
        // init the password field
        JPasswordField textfield = new JPasswordField(value);
        
        // set its looks
        textfield.setEchoChar('*');
        textfield.setBorder(new EmptyBorder(0, 0, 0, 0));
        textfield.setMargin(new Insets(0, 0, 0, 0));
        
        // select the component's text
        textfield.selectAll();
        
        // add a key listener
        textfield.addKeyListener(new PasswordListener());
        
        // return the component
        return textfield;
    }
    
    /**
     * Tells whether this editor support custom in-line editing
     * 
     * @return true
     */
    public boolean hasInPlaceCustomEditor() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), "hasInPlaceCustomEditor()");    // NOI18N
        
        // return
        return true;
    }
    
    /**
     * This method is not clear - but apparently there are no tagged values in
     * a password, so we return false
     * 
     * @return false
     */
    public boolean supportsEditingTaggedValues() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify(getClass(), 
                    "supportsEditingTaggedValues()");                  // NOI18N
        
        // return
        return false;
    }
    
    /**
     * This a listener that is attached to the editor field and watches
     * keystrokes appending the input characters to the value
     * 
     * @author Kirill Sorokin
     */
    private class PasswordListener extends KeyAdapter {
        /**
         * Triggered when a keyboard button is released
         * 
         * @param event the corresponding keyboard event
         */
        public void keyReleased(KeyEvent event) {
            if (WSDebug.isEnabled()) // debug output
                WSDebug.notify(getClass(), "keyReleased(" + event +    // NOI18N
                        ")");                                          // NOI18N
            
            // get the event's source (in out case it would be a password field
            JPasswordField field = (JPasswordField) event.getSource();
            
            // update the edited object with the newly entered value
            value = new String(field.getPassword());
            
            // notify the listeners
            firePropertyChange();
            
            // if the enter key was pressed simulate the pressing of the 
            // escape key so that the editor closes
            if(event.getKeyCode() == KeyEvent.VK_ENTER){
                KeyEvent escapeEvent = new KeyEvent(event.getComponent(), 
                        KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, 
                        KeyEvent.CHAR_UNDEFINED); 
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        dispatchKeyEvent(escapeEvent);
            }
        }
    }
}