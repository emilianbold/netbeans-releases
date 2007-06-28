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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.openide.ErrorManager;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 * @author David Kaspar
 */
public class ConverterUtil {

    static Boolean getBoolean (String value) {
        if (value == null)
            return null;
        return "true".equalsIgnoreCase (value); // NOI18N
    }

    static Integer getInteger (String value) {
        if (value == null)
            return null;
        try {
            return Integer.parseInt (value);
        } catch (NumberFormatException e) {
            Debug.warning (e); // TODO
            return null;
        }
    }

    static PropertyValue getStringWithUserCode (String value) {
        if (value == null)
            return null;
        if (value.startsWith ("CODE:")) // NOI18N
            return PropertyValue.createUserCode (decryptStringFromJavaCode (value.substring (5)));
        if (value.startsWith ("STRING:")) // NOI18N
            return MidpTypes.createStringValue (decryptStringFromJavaCode (value.substring (7)));
        Debug.warning ("Invalid value", value); // NOI18N
        // TODO - invalid value
        return null;
    }

//    private static String getCodeValueClass (String value) {
//        assert value.startsWith ("CODE-");
//        return value.substring (5);
//    }

    private static String decryptStringFromJavaCode (String value) {
        if (value == null)
            return null;
        final int len = value.length ();
        StringBuffer sb = new StringBuffer ();
        int i = 0;
        while (i < len) {
            char c = value.charAt (i);
            i++;
            if (c != '\\') {
                sb.append (c);
                continue;
            }
            c = value.charAt (i);
            i++;
            switch (c) {
                case 'r':
                    sb.append ('\r');
                    break;
                case 'n':
                    sb.append ('\n');
                    break;
                case 't':
                    sb.append ('\t');
                    break;
                case 'u':
                    if (i + 4 > len) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid hex number at the end: " + value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 4), 16));
                    } catch (NumberFormatException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid hex number format: " + value.substring (i, i + 4)); // NOI18N
                    }
                    i += 4;
                    break;
                case '"':
                case '\'':
                case '\\':
                    sb.append(c);
                    break;
                default:
                    if (c < '0' || c > '9') {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid character after slash: " + c); // NOI18N
                        break;
                    }
                    i--;
                    if (i + 3 > len) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid octal number at the end: " + value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 3), 8));
                    } catch (NumberFormatException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WARNING: Invalid octal number format: " + value.substring (i, i + 3)); // NOI18N
                    }
                    i += 3;
            }
        }
        return sb.toString ();
    }

    static void convertStringWithUserCode (DesignComponent component, String propertyName, String value) {
        PropertyValue propertyValue = getStringWithUserCode (value);
        if (propertyValue != null)
            component.writeProperty (propertyName, propertyValue);
    }

    public static void convertInteger (DesignComponent component, String propertyName, String value) {
        Integer integer = getInteger (value);
        if (integer != null)
            component.writeProperty (propertyName, MidpTypes.createIntegerValue (integer));
    }


    public static void convertString (DesignComponent component, String propertyName, String value) {
        if (value != null)
            component.writeProperty (propertyName, MidpTypes.createStringValue (value));
    }

}
