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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import org.openide.util.Utilities;

// bugfix# 9219 for attachEnv() method
import org.openide.explorer.propertysheet.ExPropertyEditor; 
import org.openide.explorer.propertysheet.PropertyEnv;
import java.beans.FeatureDescriptor;
import org.openide.nodes.Node;


/** A property editor for String class.
* @author   Ian Formanek
* @version  1.00, 18 Sep, 1998
*/
public class StringEditor extends PropertyEditorSupport implements ExPropertyEditor
{
    private static boolean useRaw = System.getProperty("netbeans.stringEditor.useRawCharacters") != null;
    
   // bugfix# 9219 added editable field and isEditable() "getter" to be used in StringCustomEditor    
    private boolean editable=true;   
    /** gets information if the text in editor should be editable or not */
    public boolean isEditable(){
        return (editable);
    }
                
    /** sets new value */
    public void setAsText(String s) {
        setValue(s);
    }

    public String getJavaInitializationString () {
        String s = (String) getValue ();
        return "\"" + toAscii(s) + "\""; // NOI18N
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new StringCustomEditor (this);
    }

    private static String toAscii(String str) {
        StringBuffer buf = new StringBuffer(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\"': buf.append("\\\""); break; // NOI18N
                //        case '\'': buf.append("\\'"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && (useRaw || c <= 0x007f))
                    buf.append(c);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
            }
        }
        return buf.toString();
    }
    
    // bugfix# 9219 added attachEnv() method checking if the user canWrite in text box 
    public void attachEnv(PropertyEnv env) {        
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
        }
    }

}
