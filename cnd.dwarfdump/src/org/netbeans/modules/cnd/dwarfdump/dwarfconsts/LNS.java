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
public enum LNS {
    DW_LNS_copy(0x01),
    DW_LNS_advance_pc(0x02),
    DW_LNS_advance_line(0x03),
    DW_LNS_set_file(0x04),
    DW_LNS_set_column(0x05),
    DW_LNS_negate_stmt(0x06),
    DW_LNS_set_basic_block(0x07),
    DW_LNS_const_add_pc(0x08),
    DW_LNS_fixed_advance_pc(0x09),
    DW_LNS_set_prologue_end(0x0a),
    DW_LNS_set_epilogue_begin(0x0b),
    DW_LNS_set_isa(0x0c);
    
    private final int value;
    static HashMap<Integer, LNS> hashmap = new HashMap<Integer, LNS>();
    
    static {
        for (LNS elem : LNS.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    LNS(int value) {
        this.value = value;
    }
    
    public static LNS get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    public int value() {
        return value;
    }
}

