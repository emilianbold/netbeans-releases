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

import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Vladimir Kvasihn
 */
public class ClassForwardDeclarationImpl extends OffsetableDeclarationBase implements CsmClassForwardDeclaration {


    private String name = null;
    
    public ClassForwardDeclarationImpl(AST ast, FileImpl file) {
        super(ast, file);
    }

    public CsmScope getScope() {
        return getContainingFile();
    }

    public String getName() {
        return getQualifiedName();
    }

    public String getQualifiedName() {
        if( name == null ) {
            AST qid = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_QUALIFIED_ID);
            name = qid == null ? "" : AstRenderer.getQualifiedName(qid);
        }
        return name;
    }

//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION;
    }

    public CsmClass getCsmClass() {
        CsmObject o = resolve();
        return (o instanceof CsmClass) ? (CsmClass) o : (CsmClass) null;
    }
    
    private CsmObject resolve() {
        Resolver resolver = ResolverFactory.createResolver(this);
        AST qid = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_QUALIFIED_ID);
        if( qid != null ) {
            String[] nameParts = AstRenderer.getNameTokens(qid);
            return resolver.resolve(nameParts);
        }
        return resolver.resolve(new String[0]);
    }
}
