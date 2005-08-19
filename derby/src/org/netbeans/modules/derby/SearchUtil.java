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

package org.netbeans.modules.derby;

/**
 *
 * @author pj97932
 */
public class SearchUtil {
    
    static final int FOUND = -1;

    /** Returns the number of characters from the searchedFor string that are at the end of the buffer,
     * or FOUND if found the whole string
     **/
    static int checkForString(String searchedFor, int searchStart, char[] buf, int bufLen) {
        if (searchedFor.length() == 0)
            throw new IllegalArgumentException();
        if (searchStart > 0) {
            // already have a substring
            int res = checkPosition(searchedFor, searchStart, buf, bufLen, 0);
            if (res == FOUND)
                return FOUND;
        }
        for (int i = 0; i < bufLen; i++) {
            if (buf[i] == searchedFor.charAt(searchStart)) {
                // found the first character
                int res = checkPosition(searchedFor, 0, buf, bufLen, i);
                if (res != 0)
                    return res;
            }
        }
        return 0;
    }

    /** Checks whether the buffer contains a portion of searchedFor from searchStart, starting from buffer
     * position bufFrom.
     *  Returns the number of characters from the searchedFor string that are at the end of the buffer,
     * or FOUND if found the whole string
     */
    static int checkPosition(String searchedFor, int searchStart, char[] buf, int bufLen, int bufFrom) {
        String realSearch = searchedFor.substring(searchStart);
        for (int i = 0; i < realSearch.length(); i++) {
            int bufPos = bufFrom + i;
            if (bufPos >= bufLen) {
                return searchStart + i;
            }
            if (buf[bufPos] != realSearch.charAt(i))
                return 0;
        }
        return FOUND;
    }
        
    
}
