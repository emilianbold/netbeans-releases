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

package org.netbeans.core.filesystems;

import org.openide.util.Utilities;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
class Util {
    /** Forbid creating new Util */
    private Util() {
    }

    static String[] addString(String[] array, String val) {
        if (array == null) {
            return new String[] {val};
        } else {
            String[] n = new String[array.length + 1];
            System.arraycopy(array, 0, n, 0, array.length);
            n[array.length] = val;
            return n;
        }
    }

    static int indexOf(Object[] where, Object what) {                    
        if (where == null) return -1;
        for (int i = 0; i<where.length; i++) {
            if (where[i].equals(what)) return i;
        }        
        return -1;
    }

    static int indexOf(String[] where, String what, boolean caseInsensitiv) {                  
        boolean isEqual;        
        
        for (int i = 0; where != null && i < where.length; i++) {            
            if (caseInsensitiv)
                isEqual = where[i].equalsIgnoreCase (what);
            else  
                isEqual = where[i].equals(what);
            
            if (isEqual)  return i;
        }                
        return -1;
    }
        
    static boolean contains(Object[] where, Object what) {
        return indexOf(where, what) != -1;
    }
    
    static boolean contains(String[] where, String what, boolean caseInsensitiv) {                    
        return indexOf(where, what, caseInsensitiv) != -1;
    }    
}
