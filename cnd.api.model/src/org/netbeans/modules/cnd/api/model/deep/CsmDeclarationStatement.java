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

package org.netbeans.modules.cnd.api.model.deep;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmVariable;

/**
 * Represents declaration statement
 *
 * @author Vladimir Kvashin
 */
public interface CsmDeclarationStatement extends CsmStatement {

    /**
     * According to the standard, declaration statement is a block-declaration,
     * which, in turn, might be one of
     *      asm-definition
     *      namespace-alias definition
     *      using declaration
     *      using directive
     *      simple-declaration (i.e.
     *          [decl-specifier-seq] init_declarator_list
     *
     * So, according to the standard, declaration statement consists of the *single* declaration.
     * But our API treats each variable as a separate declaration - that's why this method returns a list.
     *      
     */
    List/*CsmDeclaration*/ getDeclarators();
}
