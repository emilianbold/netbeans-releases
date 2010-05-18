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
