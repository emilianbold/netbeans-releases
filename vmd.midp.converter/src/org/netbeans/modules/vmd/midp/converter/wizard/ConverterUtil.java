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

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.ArrayList;
import java.util.HashMap;

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
            Debug.warning (e);
            return null;
        }
    }

    static Float getFloat (String value) {
        if (value == null)
            return null;
        try {
            return Float.parseFloat (value);
        } catch (NumberFormatException e) {
            Debug.warning (e);
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
        Debug.warning ("Invalid string code value", value); // NOI18N
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
            if (c != '\\') { // NOI18N
                sb.append (c);
                continue;
            }
            c = value.charAt (i);
            i++;
            switch (c) {
                case 'r': // NOI18N
                    sb.append ('\r'); // NOI18N
                    break;
                case 'n': // NOI18N
                    sb.append ('\n'); // NOI18N
                    break;
                case 't': // NOI18N
                    sb.append ('\t'); // NOI18N
                    break;
                case 'u': // NOI18N
                    if (i + 4 > len) {
                        Debug.warning ("Invalid hex number at the end", value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 4), 16));
                    } catch (NumberFormatException e) {
                        Debug.warning ("Invalid hex number format", value.substring (i, i + 4)); // NOI18N
                    }
                    i += 4;
                    break;
                case '"':
                case '\'':
                case '\\':
                    sb.append(c);
                    break;
                default:
                    if (c < '0' || c > '9') { // NOI18N
                        Debug.warning ("Invalid character after slash", c); // NOI18N
                        break;
                    }
                    i--;
                    if (i + 3 > len) {
                        Debug.warning ("Invalid octal number at the end: ", value.substring (i)); // NOI18N
                        break;
                    }
                    try {
                        sb.append ((char) Integer.parseInt (value.substring (i, i + 3), 8));
                    } catch (NumberFormatException e) {
                        Debug.warning ("Invalid octal number format", value.substring (i, i + 3)); // NOI18N
                    }
                    i += 3;
            }
        }
        return sb.toString ();
    }

    static PropertyValue decryptStringArrayArray (String value, TypeID type, int dimension) {
        if (dimension <= 0)
            return MidpTypes.createStringValue (value);
        type = type.getComponentType ();

        int pos = 0;
        int number = 0;
        for (;;) {
            char c = lookCharAhead (value, pos ++);
            if (! Character.isDigit (c))
                break;
            number = (number * 10) + (c - '0'); // NOI18N
        }

        ArrayList<PropertyValue> values = new ArrayList<PropertyValue> ();
        for (int i = 0; i < number; i++) {
            String valuePart = null;
            if (Character.isDigit (lookCharAhead (value, pos))) {
                int number2 = 0;
                for (; ;) {
                    char c = lookCharAhead (value, pos ++);
                    if (! Character.isDigit (c))
                        break;
                    number2 = (number2 * 10) + (c - '0'); // NOI18N
                }
                valuePart = value.substring (pos, pos + number2);
                pos += number2 + 1;
            } else {
                pos ++;
            }
            values.add (decryptStringArrayArray (valuePart, type, dimension - 1));
        }

        return PropertyValue.createArray (type, values);
    }

    private static char lookCharAhead (String string, int pos) {
        return pos < string.length () ? string.charAt (pos) : '\0'; // NOI18N
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

    public static void convertFloat (DesignComponent component, String propertyName, String value) {
        Float fl = getFloat (value);
        if (fl != null)
            component.writeProperty (propertyName, MidpTypes.createFloatValue (fl));
    }

    public static void convertString (DesignComponent component, String propertyName, String value) {
        if (value != null)
            component.writeProperty (propertyName, MidpTypes.createStringValue (value));
    }

    public static void convertBoolean (DesignComponent component, String propertyName, String value) {
        Boolean bool = getBoolean (value);
        if (bool != null)
            component.writeProperty (propertyName, MidpTypes.createBooleanValue (bool));
    }

    public static void convertConverterItemComponent (DesignComponent component, String propertyName, HashMap<String, ConverterItem> id2item, String value) {
        DesignComponent ref = Converter.convertConverterItemComponent (id2item, value, component.getDocument ());
        if (ref != null)
            component.writeProperty (propertyName, PropertyValue.createComponentReference (ref));
    }

}
