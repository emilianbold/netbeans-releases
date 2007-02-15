/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package test.dwarfclassview.kindresolver;

import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import test.dwarfclassview.consts.KIND;

public class KindResolver {
    public static KIND resolveKind(DwarfEntry entry) {
        KIND result = null;
        
        if (entry == null) {
            return null;
        }
        
        TAG dwarfKind = entry.getKind();
        
        DwarfEntry specification = entry.getSpecification();
        
        if (specification != null) {
            entry = specification;
        }
        
        DwarfEntry parent = entry.getParent();
        
        if (parent != null) {
            if (parent.getKind().equals(TAG.DW_TAG_structure_type) ||
                parent.getKind().equals(TAG.DW_TAG_class_type)) {
                result = InClassKindResolver.resolveKind(dwarfKind, entry.getName(), parent.getName());
            } 
        } else {
            result = GlobalScopeKindResolver.resolveKind(dwarfKind);
        }
        
        if (result == null) {
            result = resolveKind(dwarfKind);
        }
        
        return result;
    }

    private static KIND resolveKind(TAG dwarfKind) {
        switch (dwarfKind) {
            case DW_TAG_structure_type:
            case DW_TAG_class_type:
            case DW_TAG_SUN_struct_template:
            case DW_TAG_SUN_class_template:
                return KIND.CLASS;
            case DW_TAG_namespace:
                return KIND.NAMESPACE;
            case DW_TAG_subprogram:
            case DW_TAG_SUN_function_template:
                return KIND.FUNCTION;
            case DW_TAG_member:
                return KIND.MEMBER;
            case DW_TAG_variable:
                return KIND.VARIABLE;
            case DW_TAG_typedef:
                return KIND.TYPEDEF;
            case DW_TAG_enumeration_type:
                return KIND.ENUM;
            case DW_TAG_union_type:
                return KIND.UNION;
            case DW_TAG_enumerator:
                return KIND.ENUMITEM;
            default:
                return KIND.UNHANDLED_KIND;
        }    }

    public static boolean kindSupposeParams(KIND kind) {
        if (kind == null) {
            return true;
        }
        
        switch (kind) {
            case CONSTRUCTOR:
            case DESTRUCTOR:
            case FUNCTION:
            case METHOD:
            case OPERATOR:
                return true;
            default:
                return false;
        }
    }

}
