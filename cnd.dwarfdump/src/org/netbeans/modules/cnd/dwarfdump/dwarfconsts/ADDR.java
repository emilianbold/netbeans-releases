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

/**
 *
 * @author ak119685
 */
public enum ADDR {
    DW_ADDR_none(0x0),
    DW_ADDR_near16(0x1),
    DW_ADDR_far16(0x2),
    DW_ADDR_huge16(0x3),
    DW_ADDR_near32(0x4),
    DW_ADDR_far32(0x5);
    
    private static final HashMap<Integer, ADDR> hashmap = new HashMap<Integer, ADDR>();
    private final int value;
    
    static {
        for (ADDR elem : ADDR.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    ADDR(int value) {
        this.value = value;
    }
    
    public static ADDR get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    public int value() {
        return value;
    }
}
