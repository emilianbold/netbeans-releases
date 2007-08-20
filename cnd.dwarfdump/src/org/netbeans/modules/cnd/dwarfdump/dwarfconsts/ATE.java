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
public enum ATE {
    DW_ATE_address(0x1),
    DW_ATE_boolean(0x2),
    DW_ATE_complex_float(0x3),
    DW_ATE_float(0x4),
    DW_ATE_signed(0x5),
    DW_ATE_signed_char(0x6),
    DW_ATE_unsigned(0x7),
    DW_ATE_unsigned_char(0x8),
    DW_ATE_lo_user(0x80),
    DW_ATE_hi_user(0xff);
    
    private static final HashMap<Integer, ATE> hashmap = new HashMap<Integer, ATE>();
    private final int value;
    
    static {
        for (ATE elem : ATE.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    ATE(int value) {
        this.value = value;
    }
    
    public static ATE get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    
    public int value() {
        return value;
        
    }
}
