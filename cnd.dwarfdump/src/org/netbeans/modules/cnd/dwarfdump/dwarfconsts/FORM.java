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
public enum FORM {
    DW_FORM_addr(0x01),
    DW_FORM_block2(0x03),
    DW_FORM_block4(0x04),
    DW_FORM_data2(0x05),
    DW_FORM_data4(0x06),
    DW_FORM_data8(0x07),
    DW_FORM_string(0x08),
    DW_FORM_block(0x09),
    DW_FORM_block1(0x0a),
    DW_FORM_data1(0x0b),
    DW_FORM_flag(0x0c),
    DW_FORM_sdata(0x0d),
    DW_FORM_strp(0x0e),
    DW_FORM_udata(0x0f),
    DW_FORM_ref_addr(0x10),
    DW_FORM_ref1(0x11),
    DW_FORM_ref2(0x12),
    DW_FORM_ref4(0x13),
    DW_FORM_ref8(0x14),
    DW_FORM_ref_udata(0x15),
    DW_FORM_indirect(0x16);
    
    private final int value;
    private static final HashMap<Integer, FORM> hashmap = new HashMap<Integer, FORM>();
    
    static {
        for (FORM elem : FORM.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    FORM(int value) {
        this.value = value;
    }
    
    public static FORM get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    
    public int value() {
        return value;
    }
}
