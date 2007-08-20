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
public enum LANG {
    DW_LANG_C89(0x0001),
    DW_LANG_C(0x0002),
    DW_LANG_Ada83(0x0003),
    DW_LANG_C_plus_plus(0x0004),
    DW_LANG_Cobol74(0x0005),
    DW_LANG_Cobol85(0x0006),
    DW_LANG_Fortran77(0x0007),
    DW_LANG_Fortran90(0x0008),
    DW_LANG_Pascal83(0x0009),
    DW_LANG_Modula2(0x000a),
    DW_LANG_C99(0x000c),
    DW_LANG_lo_user(0x8000),
    DW_LANG_hi_user(0xffff);
    
/* What about dwarf 3.0 ?
See http://dwarf.freestandards.org/Dwarf3Std.php
Convenient definitions in http://www.arcknowledge.com/gmane.comp.compilers.llvm.cvs/2005-12/msg00423.html
-  DW_LANG_Java = 0x000b,
-  DW_LANG_C99 = 0x000c,
-  DW_LANG_Ada95 = 0x000d,
-  DW_LANG_Fortran95 = 0x000e,
-  DW_LANG_PLI = 0x000f,
-  DW_LANG_ObjC = 0x0010,
-  DW_LANG_ObjC_plus_plus = 0x0011,
-  DW_LANG_UPC = 0x0012,
-  DW_LANG_D = 0x0013,
 */    
    
    private final int value;
    static private final HashMap<Integer, LANG> hashmap = new HashMap<Integer, LANG>();
    
    static {
        for (LANG elem : LANG.values()) {
            hashmap.put(new Integer(elem.value), elem);
        }
    }
    
    LANG(int value) {
        this.value = value;
    }

    public static LANG get(int val) {
        return hashmap.get(new Integer(val));
    }
    
    public int value() {
        return value;
    }
    
    @Override
    public String toString() {
        switch(this) {
            case DW_LANG_C89: return "C 89"; // NOI18N
            case DW_LANG_C99: return "C 99"; // NOI18N
            case DW_LANG_C: return "C"; // NOI18N
            case DW_LANG_Ada83: return "Ada 83"; // NOI18N
            case DW_LANG_C_plus_plus: return "C++";  // NOI18N
            case DW_LANG_Cobol74: return "Cobol 74"; // NOI18N
            case DW_LANG_Cobol85: return "Cobol 85"; // NOI18N
            case DW_LANG_Fortran77: return "Fortran 77"; // NOI18N
            case DW_LANG_Fortran90: return "Fortran 90"; // NOI18N
            case DW_LANG_Pascal83: return "Fortran 83"; // NOI18N
            case DW_LANG_Modula2: return "Modula 2"; // NOI18N
            default:
                return null;
        }
    }
}
