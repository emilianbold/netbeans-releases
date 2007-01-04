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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Common base for all expression implementations
 * @author Vladimir Kvashin
 */
public class ExpressionBase extends OffsetableBase implements CsmExpression {
    
    private CsmExpression.Kind kind;
    private CsmExpression parent;
    private List/*<CsmExpressionBase>*/ operands;
    
    public ExpressionBase(AST ast, CsmFile file, CsmExpression parent) {
        super(ast, file);
        this.parent = parent;
    }

    public CsmExpression.Kind getKind() {
        return kind;
    }

    public List/*<CsmExpression>*/ getOperands() {
        if( operands == null ) {
            operands = new ArrayList/*CsmExpressionBase*/();
        }
        return operands;
    }
    
    public CsmExpression getParent() {
        return parent;
    }

//    moved to OffsetableBase
//    public String getText() {
//        // TODO: perhaps it's worth to hold text...
//        return getContainingFile().getText(getStartOffset(), getEndOffset());
//    }
 
}
