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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.core.api.support;

public final class Strings {

    private Strings() {
    }

    /**
     * Checks whether the given <code>str</code> is an empty string. More
     * specifically, checks whether it is null and or contains only whitespace characters.
     * 
     * @param str the string to check.
     * @return true if the given <code>str</code> was null 
     * or contained only whitespaces, false otherwise.
     */ 
    public static boolean isEmpty(String str){
        return null == str || "".equals(str.trim());
    }
    
}
