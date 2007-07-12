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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model;

import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;

/**
 * Common ancestor for all declarations
 * @author Vladimir Kvashin
 */

public interface CsmDeclaration<T> extends CsmQualifiedNamedElement, 
        CsmScopeElement, CsmIdentifiable<T> {

    //TODO: fill in accordance to C++ standard

    public enum Kind {

        BUILT_IN,

        CLASS,
        UNION,
        STRUCT,
        
        ENUM,
        ENUMERATOR,
        MACRO,
        
        VARIABLE,
        VARIABLE_DEFINITION,
        
        FUNCTION,
        FUNCTION_DEFINITION,
        
        TEMPLATE_SPECIALIZATION,
        TYPEDEF,
        ASM,
        TEMPLATE_DECLARATION,
        NAMESPACE_DEFINITION,
        
        NAMESPACE_ALIAS,
        USING_DIRECTIVE,
        USING_DECLARATION,
        
        CLASS_FORWARD_DECLARATION,

        CLASS_FRIEND_DECLARATION
    }
    
    Kind getKind();
    
    /**
     * Gets the name, which unequely identifies the given declaration
     * within a project.
     * For classes, enums and variables such names equals to their qualified name;
     * for functions the signature should be added
     * @see CsmProject#findDeclaration
     * @see CsmProject#findDeclarations
     */
    String getUniqueName();
}
