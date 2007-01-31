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

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;

/**
 * Represents condition.
 *
 * In C++, conditions are either expressions (which return bool or integer value)
 * or declaration statement
 *
 * TODO: perhaps it's worth to subclass for expression and declaraion kind
 * rather then having 2 methods,  getExpression() and getDeclaration(), 
 * one of which returns null?
 * 
 * @author Vladimir Kvashin
 */
public interface CsmCondition extends CsmOffsetable, CsmObject {
  
        class Kind extends TypeSafeEnum {
            
            private Kind(String id) {
                super(id);
            }
            
            public static final Kind EXPRESSION = new Kind("Expression"); // NOI18N
            
            public static final Kind DECLARATION = new Kind("Declaration"); // NOI18N
        }
        
        Kind getKind();
        
        /** In the case this condition kind id EXPRESSION, gets the expression, otherwise null */
        CsmExpression getExpression();
        
        /** In the case this condition kind id DECLARATION, gets the declaration statement, otherwise null */
        CsmVariable getDeclaration();
        
}
