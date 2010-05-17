/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
