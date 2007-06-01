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

package org.netbeans.modules.j2ee.dd.util;

/**
 * Utility methods for working with annotations.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public class AnnotationUtils {
    
    private AnnotationUtils() {
    }
    
    /**
     * Get <tt>JavaBeans</tt> property name for given <tt>setter</tt> method.
     * Return <code>null</code> in case of incorrect method name.
     * @param methodName name of the method.
     * @return <tt>JavaBeans</tt> property name or <code>null</code>.
     */
    public static String setterNameToPropertyName(String methodName) {
        // a setter name starts with "set" and
        // is longer than 3, respectively
        // i.e. "set()" is not a property setter
        if (methodName.length() > 3
                && methodName.startsWith("set")) { // NOI18N
            return toLowerCaseFirst(methodName.substring(3));
        }
        return null;
    }
    
    private static String toLowerCaseFirst(String value) {
        if (value.length() > 0) {
            // XXX incorrect wrt surrogate pairs
            char[] characters = value.toCharArray();
            // XXX locale
            characters[0] = Character.toLowerCase(characters[0]);
            return new String(characters);
        }
        return value;
    }
}
