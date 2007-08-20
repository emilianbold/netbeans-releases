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

package org.netbeans.modules.cnd.dwarfdump.dwarfconsts;

import java.util.HashMap;

public enum ACCESS {
    
    DW_ACCESS_public(0x1),
    DW_ACCESS_private(0x2),
    DW_ACCESS_protected(0x3);
    
    private static final HashMap<Integer, ACCESS> hashmap = new HashMap<Integer, ACCESS>();
    private final int value;
    
    static {
        for (ACCESS elem : ACCESS.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    ACCESS(int value) {
        this.value = value;
    }
    
    public static ACCESS get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    public int value() {
        return value;
    }
    
    @Override
    public String toString() {
        switch (get(value)) {
            case DW_ACCESS_public:
                return "public"; // NOI18N
            case DW_ACCESS_protected:
                return "protected"; // NOI18N
            case DW_ACCESS_private:
                return "private"; // NOI18N
        }
        
        return ""; // NOI18N
    }
}
