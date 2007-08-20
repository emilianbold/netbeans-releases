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
public enum LNE {
    DW_LNE_end_sequence(0x01),
    DW_LNE_set_address(0x02),
    DW_LNE_define_file(0x03),
    DW_LNE_lo_user(0x80),
    DW_LNE_hi_user(0xff);
    
    private final int value;
    static private final HashMap<Integer, LNE> hashmap = new HashMap<Integer, LNE>();
    
    static {
        for (LNE elem : LNE.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    LNE(int value) {
        this.value = value;
    }
    
    public static LNE get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    public int value() {
        return value;
    }
}
