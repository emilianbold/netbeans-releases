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

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import test.dwarfclassview.consts.KIND;

public class InClassKindResolver {
    static KIND resolveKind(TAG dwarfKind, String name, String className) {
        int idx = className.indexOf('<');
        
        if (idx > -1) {
            className = className.substring(0, idx);
        }
        
        if (name.equals(className)) {
            return KIND.CONSTRUCTOR;
        }
        
        if (name.equals("~" + className)) { // NOI18N
            return KIND.DESTRUCTOR;
        }
        
        if (name.matches("operator[\\*!<>+-=&| ]+.*")) { // NOI18N
            return KIND.OPERATOR;
        }

        if (dwarfKind.equals(TAG.DW_TAG_subprogram)) {
            return KIND.METHOD;
        }
        
        if (dwarfKind.equals(TAG.DW_TAG_variable)) {
            return KIND.FIELD;
        }
        
        if (dwarfKind.equals(TAG.DW_TAG_member)) {
            return KIND.FIELD;
        }
        
        return null;
    }
}
