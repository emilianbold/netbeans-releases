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
package com.sun.jsfcl.std.property;

/**
 * @author eric
 *
 * @deprecated
 */
public class LengthValuePropertyEditor extends AbstractPropertyEditor {

    public String getAsText() {
        String value = (String)getValue();
        if (value == null || value.length() == 0) {
            return ""; //NOI18N
        }
        return value;
    }

    public String getJavaInitializationString() {

        return stringToJavaSourceString(getAsText());
    }

    public void setAsText(String string) {
        string = string.trim();
        boolean unset = true;
        boolean isPercent = false;
        String value = null;
        if (string != null && string.length() > 0) {
            try {
                if (string.endsWith("%")) { //NOI18N
                    isPercent = true;
                    string = string.substring(0, string.length() - 1).trim();
                }
                int number = Integer.parseInt(string);
                if (number >= 0) {
                    value = String.valueOf(number);
                    if (isPercent) {
                        value = number + "%"; //NOI18N
                    }
                    unset = false;
                }
            } catch (Throwable t) {
            }
        }
        setValue(value);
        if (unset) {
            unsetProperty();
        }
    }

}
