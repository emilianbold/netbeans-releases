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
public enum MACINFO {
    DW_MACINFO_define(0x1),
    DW_MACINFO_undef(0x2),
    DW_MACINFO_start_file(0x3),
    DW_MACINFO_end_file(0x4),
    DW_MACINFO_vendor_ext(0xff);

    private static final HashMap<Integer, MACINFO> hashmap = new HashMap<Integer, MACINFO>();
    private final int value;
    
    static {
        for (MACINFO elem : MACINFO.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }

    MACINFO(int value) {
        this.value = value;
    }
    
    public static MACINFO get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    
    public int value() {
        return value;
        
    }
}
