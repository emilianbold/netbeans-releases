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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.propertyeditors.util.JavaInitializer;


/**
 * A basic in-line property editor for strings. This editor maps the empty
 * text string to a null value, unlike the NetBeans string editor, which
 * displays the string "null". A text string is considered empty if it has no
 * characters, or consists of only space characters.
 *
 * <p>This editor is intended to be used with properties of type
 * <code>java.lang.String</code>. Other value types may be used, but they will
 * be converted to string by calling <code>toString()</code>.
 *
 * @author gjmurphy
 */
public class StringPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.StringPropertyEditor {

    public StringPropertyEditor() {
        super.setValue("");
    }

    public void setValue(Object value) {
        super.setValue(value == null? "" : value.toString());
    }

    public Object getValue() {
        String value = (String)super.getValue();
        if (value.trim().length() == 0)
            return null;
        return value;
    }

    public String getJavaInitializationString() {
        return JavaInitializer.toJavaInitializationString((String) getValue());
    }

    public java.awt.Component getCustomEditor() {
        return new StringPropertyPanel(this);
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null)
            setValue(null);
        setValue(unescapeString(text));
    }

    public String getAsText() {
        String value = (String)getValue();
        if (value == null)
            return "";
        return escapeString(value);
    }

    static String escapeString(String str) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                buffer.append("\\n");
            } else if (chars[i] == '\t') {
                buffer.append("\\t");
            } else if (chars[i] == '\\') {
                buffer.append("\\\\");
            } else {
                buffer.append(chars[i]);
            }
        }
        if (buffer.length() == str.length())
            return str;
        return buffer.toString();
    }

    static String unescapeString(String str) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\\' && i < chars.length - 1) {
                i++;
                if (chars[i] == '\\')
                    buffer.append("\\");
                else if (chars[i] == 't')
                    buffer.append("\t");
                else if (chars[i] == 'n')
                    buffer.append("\n");
            } else {
                buffer.append(chars[i]);
            }
        }
        if (buffer.length() == str.length())
            return str;
        return buffer.toString();
    }

}
