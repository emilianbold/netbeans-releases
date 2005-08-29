/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
