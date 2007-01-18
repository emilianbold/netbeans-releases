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

package org.netbeans.modules.properties;

import java.util.Comparator;

/**
 * Comparator for comparing property keys (Strings).
 *
 * @author Petr Jiricka
 */
public final class KeyComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
        int res1 = String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
        if (res1 != 0) {
            return res1;
        }
        return o1.compareTo(o2);
    }

}
/*
* <<Log>>
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/10/99  Petr Jiricka    
* $
*/
