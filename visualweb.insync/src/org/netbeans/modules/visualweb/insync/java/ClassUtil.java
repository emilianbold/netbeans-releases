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
}
