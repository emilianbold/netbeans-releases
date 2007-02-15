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

package org.netbeans.modules.cnd.api.model;

import org.netbeans.modules.cnd.api.model.deep.CsmExpression;

/**
 * Represents a variable
 * @author Vladimir Kvashin
 */
public interface CsmVariable<T> extends CsmOffsetableDeclaration<T> {

    /** Gets this variable type */
    CsmType getType();

    //TODO: how to reflect declarations like int x(5); - 5 isn't initialValue, but rather constructor parameter
    
    /** Gets this variable initial value */
    CsmExpression getInitialValue();

    //TODO: create an interface to place getDeclarationText() in
    String getDeclarationText();

    /**
     * Gets this (static) variable definition
     */
    CsmVariableDefinition getDefinition();

    //public boolean isAuto();

    //public boolean isRegister();

    // moved to CsmMember
    //public boolean isStatic();

    //public boolean isExtern();

    //public boolean isMutable();
    
}
