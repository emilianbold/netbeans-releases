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
package com.sun.rave.propertyeditors.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility methods for converting objects of various simple types to Java
 * strings that contain initializer code for the object instance. This doesn't
 * work for very many types.
 */
public class JavaInitializer {

    public static String toJavaInitializationString(Object obj) {
        if (obj instanceof String)
            return toJavaInitializationString((String) obj);
        if (obj instanceof Integer)
            return toJavaInitializationString((Integer) obj);
        if (obj instanceof Double)
            return toJavaInitializationString((Double) obj);
        if (obj instanceof Long)
            return toJavaInitializationString((Long) obj);
        if (obj instanceof Short)
            return toJavaInitializationString((Short) obj);
        if (obj instanceof Character)
            return toJavaInitializationString((Character) obj);
        if (obj instanceof Boolean)
            return toJavaInitializationString((Boolean) obj);
        if (obj instanceof Date)
            return toJavaInitializationString((Date) obj);
        if (obj instanceof URL)
            return toJavaInitializationString((URL) obj);
        if (obj instanceof URI)
            return toJavaInitializationString((URI) obj);
        if (obj instanceof File)
            return toJavaInitializationString((File) obj);
        if (obj instanceof Locale)
            return toJavaInitializationString((Locale) obj);
        return "null";
    }

    public static String toJavaInitializationString(String str) {
        if (str == null)
            return "null";
        StringBuffer buf = new StringBuffer(str.length() * 2);
        char[] chars = str.toCharArray();
        buf.append('"');
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\b':
                    buf.append("\\b"); //NOI18N
                    break;
                case '\t':
                    buf.append("\\t"); //NOI18N
                    break;
                case '\n':
                    buf.append("\\n"); //NOI18N
                    break;
                case '\f':
                    buf.append("\\f"); //NOI18N
                    break;
                case '\r':
                    buf.append("\\r"); //NOI18N
                    break;
                case '\"':
                    buf.append("\\\""); //NOI18N
                    break;
                case '\\':
                    buf.append("\\\\"); //NOI18N
                    break;
                default:
                    if (c >= 0x0020 && c <= 0x007f) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N
                        String hex = Integer.toHexString(c);
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            buf.append('0');
                        }
                        buf.append(hex);
                    }
            }
        }
        buf.append('"');
        return buf.toString();
    }

    public static String toJavaInitializationString(Boolean b) {
        if (Boolean.TRUE.equals(b))
            return "Boolean.TRUE";
        return "Boolean.FALSE";
    }

    public static String toJavaInitializationString(Character c) {
        return "new Character(" + c.toString() + ")";
    }
    public static String toJavaInitializationString(Integer i) {
        return "new Integer(" + i.toString() + ")";
    }

    public static String toJavaInitializationString(Short s) {
        return "new Short(" + s.toString() + ")";
    }
    
    public static String toJavaInitializationString(Long l) {
        return "new Long(" + l.toString() + ")";
    }
    
    public static String toJavaInitializationString(Double d) {
        return "new Double(" + d.toString() + ")";
    }
    
    public static String toJavaInitializationString(Date d) {
        String formatedDate = DateFormat.getInstance().format(d);
        return "DateFormat.getInstance().parse(" + formatedDate + ")";
    }
    
    public static String toJavaInitializationString(URI uri) {
        return "new URI(" + uri.toString() + ")";
    }
    
    public static String toJavaInitializationString(URL url) {
        return "new URL(" + url.toString() + ")";
    }
    
    public static String toJavaInitializationString(File file) {
        return "new File(" + file.toString() + ")";
    }
    
    public static String toJavaInitializationString(Locale locale) {
        if (locale.getCountry() != null)
            return "new Locale(\"" + locale.getLanguage() + "\", \"" +locale.getCountry() + "\")";
        else 
            return "new Locale(\"" + locale.getLanguage() + "\")";
    }
    
}
