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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Implements both CsmFunction and CsmFunctionDefinition -
 * for those cases, when they coinside (i.e. implivit inlines)
 * @author Vladimir Kvasihn
 */
public class FunctionDDImpl extends FunctionImpl implements CsmFunctionDefinition {
    
    private final CsmCompoundStatement body;

    public FunctionDDImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile());
    }

    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
        String uname = CsmDeclaration.Kind.FUNCTION.toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration decl = getContainingFile().getProject().findDeclaration(uname);
        if( decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return (CsmFunction) decl;
        }
        else {
            return this;
        }
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }
    
    public List getScopeElements() {
        List l = new ArrayList();
        l.addAll(getParameters());
        l.add(getBody());
        return l;
    }
    
}

