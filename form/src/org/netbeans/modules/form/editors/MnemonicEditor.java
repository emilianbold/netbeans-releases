/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.editors;

import java.beans.*;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 * Editor for mnemonic property
 * @author  Josef Kozak
 */
public class MnemonicEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
    
    /** 
     * Converts the char to String by either leaving
     * the single char or by creating unicode escape.
     */
    public String getAsText () {
        char value = (char)(((Integer)getValue()).intValue());
        
        if (value == 0) return "";
        
        final StringBuffer buf = new StringBuffer(6);
        switch (value) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (value >= 0x0020 && value <= 0x007f)
                    buf.append(value);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(value);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
        }         
        return buf.toString() ;
    }
    /**
     * Set the property value by parsing given String.
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() < 1) {
            setValue(new Character((char)0));
            return;
        }
        
        if (text.length() == 1 && text.charAt(0) != '\\') {
            setValue(new Character(text.charAt(0)));
            return;
        }                
                
        if (text.charAt(0) == '\\') {
            // backslash means unicode escape sequence
            char value = 0;
            char ch = text.length() >=2 ? text.charAt(1) : '\\';
            switch (ch) {
                case 'b': value = '\b'; break;
                case 't': value = '\t'; break;
                case 'n': value = '\n'; break;
                case 'f': value = '\f'; break;
                case 'r': value = '\r'; break;
                case '\\': value = '\\' ; break;
                case 'u' :
                    String num = text.substring(2,text.length());
                    if (num.length () > 4) {
                        // ignore longer strings
                        return;
                    }
                    try {
                        int intValue = Integer.parseInt(num,16);
                        value = (char) intValue;
                        break;
                    } catch (NumberFormatException nfe) {
                        // ignore non parsable strings
                        return;
                    }
                default:
                        // ignore non-chars after backslash
                        return;
                        
            }
            setValue(new Character(value));
            return;
        }
        
        try {
            setValue(new Integer(text));
            return;            
        } catch (NumberFormatException e) {
            setValue(text);
            return;
        }
        
    }
    
    /**
     * Accepts Character and String values. If the argument is
     * a String the first character is taken as the new value.
     * @param v new value
     */
    public void setValue(Object newValue) throws IllegalArgumentException {
        if  (newValue instanceof Integer) {
            super.setValue(newValue);
            return;
        }        
        if  (newValue instanceof Character) {
            super.setValue(newValue);
            return;
        }
        if (newValue instanceof String) {
            String text = (String ) newValue;
            if (text.length() >= 1) {
                super.setValue(new Character(text.charAt(0)));
                return;
            }
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    public String getJavaInitializationString() {
	return "'" + getAsText() + "'";
    }

    /**
     * We don't support in place custom editor.
     * @return custom property editor to be shown inside the property
     * sheet.
     */
    public java.awt.Component getInPlaceCustomEditor () {
        return null;
    }
    
    /**
     * We don't support in place custom editor.
     * @return true if this PropertyEditor provides a enhanced in-place custom
     * property editor, false otherwise
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }
    
    /**
     * @return true if this property editor provides tagged values and
     * a custom strings in the choice should be accepted too, false otherwise
     */
    public boolean supportsEditingTaggedValues () {
        return true;
    }
}
