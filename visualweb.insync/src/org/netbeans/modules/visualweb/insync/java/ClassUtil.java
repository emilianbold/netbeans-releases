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
package org.netbeans.modules.visualweb.insync.java;

import java.lang.reflect.Array;
import java.util.HashMap;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;

/**
 * Provides utilities to obtain the class given the identifier or type name
 *
 * @author jdeva
 *
 */
public class ClassUtil {

    /**
     * Get the actual class given its type. The type may be a primitive and/or it
     * may be an array.
     *
     * @param type  The type retrieve the Class for.
     * @return The Class that represents the given type.
     * @throws ClassNotFoundException
     */
    public static Class getClass(String type, ClassLoader cl) throws ClassNotFoundException {
        if(type == null)
            return null;

        if (type.endsWith("[]")) {
            String ctype = type.substring(0, type.length()-2);
            return Array.newInstance(getClass(ctype, cl), 0).getClass();
        }

        if (type.equals("boolean") || type.equals("Z"))  //NOI18N
            return Boolean.TYPE;
        if (type.equals("byte") || type.equals("B"))  //NOI18N
            return Byte.TYPE;
        if (type.equals("char") || type.equals("C"))  //NOI18N
            return Byte.TYPE;
        if (type.equals("double") || type.equals("D"))  //NOI18N
            return Double.TYPE;
        if (type.equals("float") || type.equals("F"))  //NOI18N
            return Float.TYPE;
        if (type.equals("int") || type.equals("I"))  //NOI18N
            return Integer.TYPE;
        if (type.equals("long") || type.equals("J"))  //NOI18N
            return Long.TYPE;
        if (type.equals("short") || type.equals("S"))  //NOI18N
            return Short.TYPE;
        if (type.equals("void"))  //NOI18N
            return Void.TYPE;

        return Class.forName(type, true, cl);
    }


    /**
     * Get the actual class given its type. The type may be a primitive and/or it
     * may be an array.
     *
     * @param type  The type retrieve the Class for.
     * @return The Class that represents the given type.
     * @throws ClassNotFoundException
     */
    public static Class getClass(String type) throws ClassNotFoundException {
        //Use the project classloader to load the class
        return getClass(type, FacesContainer.getCurrentLoader(ClassUtil.class));
    }

    static HashMap arrayTypeKeyHash = new HashMap();
    static {
        arrayTypeKeyHash.put("B", "byte");   //NOI18N
        arrayTypeKeyHash.put("C", "char");   //NOI18N
        arrayTypeKeyHash.put("D", "double");   //NOI18N
        arrayTypeKeyHash.put("F", "float");   //NOI18N
        arrayTypeKeyHash.put("I", "int");   //NOI18N
        arrayTypeKeyHash.put("J", "long");   //NOI18N
        arrayTypeKeyHash.put("S", "short");   //NOI18N
        arrayTypeKeyHash.put("Z", "boolean");   //NOI18N
        arrayTypeKeyHash.put("V", "void");   //NOI18N
    }

    /*
     * Returns the simple name of the underlying class as given
     * in the source code.
     */
    public static String getSimpleName(String tn) {
        if (isArray(tn)) {
            String retStr = getArrayType(tn);
            for (int i = 0; i < getArrayDimension(tn); i++) {
                retStr += "[]";   //NOI18N
            }
            return retStr;
        }
        return tn;
    }

    /*
     * Returns array dimension given its internal form of the name
     */
    public static int getArrayDimension(String tn) {
        int dim = 0;
        while (tn.startsWith("[")) {   //NOI18N
            tn = tn.substring(1);
            dim++;
        }
        return dim;
    }

    /*
     * Checks if the given string in internal form represents an array
     */
    public static boolean isArray(String tn) {
        return tn.startsWith("[") ? true: false;
    }

    /*
     * Returns array element type given its internal form of the name
     */    
    public static String getArrayType(String tn) {
        if (isArray(tn)) {   
            while (tn.startsWith("[")) {   //NOI18N
                tn = tn.substring(1);
            }
            if (tn.startsWith("L")) {   //NOI18N
                tn = tn.substring(1);
                tn = tn.substring(0, tn.length() - 1);
            }
            else {
                char typeKey = tn.charAt(0);
                tn = (String)arrayTypeKeyHash.get("" + typeKey);   //NOI18N
            }
        }
        return tn;
    }
}
