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
 */
package org.netbeans.modules.collab.provider.im;


/**
 * String Utility class for performing common JID operations
 */
class StringUtility {

    public static String getLocalPartFromAddress(String in) {
        int i = in.lastIndexOf('@');

        if (i > 0) {
            if (in.charAt(i - 1) != '\\') {
                return in.substring(0, i);
            }
        }

        return in;
    }

    /**
     * removes the resource string from the uid
     */
    public static String removeResource(String str) {
        if (str == null) {
            return null;
        }

        int index = str.indexOf('/');

        if (index != -1) {
            return str.substring(0, index);
        }

        return str;
    }

}
