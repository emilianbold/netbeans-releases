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

package org.netbeans.modules.wsdlextensions.snmp.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Provides for easy string formatting
 */
public class Str {
    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param args Object[]
     */
    public static String msg(String msg, Object[] args) {
        return MessageFormat.format(msg, args);
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     */
    public static String msg(String msg) {
        return msg(msg, new Object[] {});
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param arg1 Object
     */
    public static String msg(String msg, Object arg1) {
        return msg(msg, new Object[] {arg1});
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param arg1 Object
     * @param arg2 Object
     */
    public static String msg(String msg, Object arg1, Object arg2) {
        return msg(msg, new Object[] {arg1, arg2});
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param arg1 Object
     * @param arg2 Object
     * @param arg3 Object
     */
    public static String msg(String msg, Object arg1, Object arg2, Object arg3) {
        return msg(msg, new Object[] {arg1, arg2, arg3});
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param arg1 Object
     * @param arg2 Object
     * @param arg3 Object
     * @param arg4 Object
     */
    public static String msg(String msg, Object arg1, Object arg2, Object arg3, Object arg4) {
        return msg(msg, new Object[] {arg1, arg2, arg3, arg4});
    }

    /**
     * Formats a msg
     *
     * @return formatted string
     * @param msg String
     * @param arg1 Object
     * @param arg2 Object
     * @param arg3 Object
     * @param arg4 Object
     * @param arg5 Object
     */
    public static String msg(String msg, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return msg(msg, new Object[] {arg1, arg2, arg3, arg4, arg5});
    }

    /**
     * Converts a password to a string suitable to display in log files
     *
     * @param inp password
     * @return neutralized string
     */
    public static String password(String inp) {
        if (inp == null) {
            return "null";
        } else if (inp.length() == 0) {
            return "zero-length";
        } else {
            return "###";
        }
    }

    /**
     * Returns true if the specified string is empty (null, "" or just spaces)
     *
     * @param s String
     * @return boolean true if empty
     */
    public static boolean empty(String s) {
        if (s == null || s.length() == 0) {
           return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isSpaceChar(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * isEqual
     *
     * @param a String
     * @param b String
     * @return boolean
     */
    public static boolean isEqual(String a, String b) {
        if (a == null) {
            return (b == null);
        } else {
            return a.equals(b);
        }
    }

    /**
     * hash
     *
     * @param seed int
     * @param o Object
     * @return int
     */
    public static int hash(int seed, Object o) {
        if (o == null) {
            return seed + 17;
        }
        return seed * 37 + o.hashCode();
    }

    /**
     * Hash tool
     *
     * @param seed int
     * @param o boolean
     * @return int
     */
    public static int hash(int seed, boolean o) {
        return seed * 37 + (o ? 3 : 7);
    }

    /**
     * Parses the specified properties and merges them into the
     * specified properties set.
     *
     * @param s serialized properties; may be empty
     * @param toAdd properties set to merge into
     */
    public static void deserializeProperties(String s, Properties toAdd) {
        if (empty(s)) {
            return;
        }

        try {
            // Load
            Properties p = new Properties();
            ByteArrayInputStream inp = new ByteArrayInputStream(s.getBytes("ISO-8859-1"));
            p.load(inp);

            // Copy
            for (Iterator iter = p.entrySet().iterator(); iter.hasNext();) {
                Map.Entry element = (Map.Entry) iter.next();
                toAdd.put(element.getKey(), element.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties: " + e, e);
        }
    }

    /**
     * Serializes a properties set to a String
     *
     * @param p properties to serialize
     * @return String
     */
    public static String serializeProperties(Properties p) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            p.store(out, "");
            return out.toString("ISO-8859-1");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize properties: " + e, e);
        }
    }

//    /**
//     * Serializes a properties set to a String
//     *
//     * @param p properties to serialize
//     * @return String
//     */
//    public static String propertiesToString(Properties p) {
//        StringBuffer ret = new StringBuffer();
//        for (Iterator iter = p.entrySet().iterator(); iter.hasNext();) {
//            Map.Entry x = (Map.Entry) iter.next();
//            ret.append(x.getKey()).append(" = ").append(x.getValue());
//        }
//        return ret.toString();
//    }
    
    /**
     * Concatenates string components
     * 
     * @param strs components
     * @param delim delimeter, e.g. ", "
     * @return concatenated string
     */
    public static String concat(Object[] strs, String delim) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < strs.length; i++) {
            if (i != 0) {
                ret.append(delim);
            }
            ret.append(strs[i]);
        }
        return ret.toString();
    }
    
    /**
     * Returns if a string is empty or null
     * 
     * @param s string to test
     * @return true if null or empty
     */
    public boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
