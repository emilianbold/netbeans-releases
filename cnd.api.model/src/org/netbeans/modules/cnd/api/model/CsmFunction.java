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

import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.List;

/**
 *
 * @author Vladimir Kvashin
 */
public interface CsmFunction<T> extends CsmOffsetableDeclaration<T> {

    /** Gets this function's declaration text */
    String getDeclarationText();

    /**
     * Gets this function definition
     * TODO: describe getDefiition==this ...
     */
    CsmFunctionDefinition getDefinition();

    /**
     * Returns true if this class is template, otherwise false.
     * If isTemplate() returns true, this class is an instance of CsmTemplate
     */
    boolean isTemplate();

    boolean isInline();

    CsmType getReturnType();
    
    List/*<CsmParameter>*/  getParameters();

    /** 
     * Gets this function signature string representation.
     * Used to identify overrides, etc.
     */
    String getSignature();
}
