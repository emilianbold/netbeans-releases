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
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 * @author Vladimir Kvashin
 */
public class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private List/*<CsmVariableImpl>*/ declarators;
    
    public DeclarationStatementImpl(AST ast, CsmFile file) {
            super(ast, file);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    public List getDeclarators() {
        if( declarators == null ) {
            declarators = new ArrayList/*<CsmVariableImpl>*/();
            render();
        }
        return declarators;
    }
       
    private void render() {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile()) {
            protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static) {
                VariableImpl var = new VariableImpl(offsetAst, file, type, name, true);
                var.setStatic(_static);
                declarators.add(var);
                return var;
            }
            
            public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
                if( tree != null ) {
                    AST token = tree;
                    switch( token.getType() ) {
                        case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
                        case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
                            if (!renderVariable(token, currentNamespace, container)){
                                render(token.getFirstChild(), currentNamespace, container);
                            }
                            break;
                        case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                            declarators.add(new NamespaceAliasImpl(token, getContainingFile()));
                            break;
                        case CPPTokenTypes.CSM_USING_DIRECTIVE:
                            declarators.add(new UsingDirectiveImpl(token, getContainingFile()));
                            break;
                        case CPPTokenTypes.CSM_USING_DECLARATION:
                            declarators.add(new UsingDeclarationImpl(token, getContainingFile()));
                            break;
                            
                        case CPPTokenTypes.CSM_CLASS_DECLARATION:
                        {
                            ClassImpl cls = new ClassImpl(token, null, getContainingFile());
                            declarators.add(cls);
                            addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
                            renderVariableInClassifier(token, cls, currentNamespace, container);
                            break;
                        }
                        case CPPTokenTypes.CSM_ENUM_DECLARATION:
                        {
                            CsmEnum csmEnum = new EnumImpl(token, currentNamespace, getContainingFile());
                            declarators.add(csmEnum);
                            renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                            break;
                        }
                    }
                }
            }
        };
        renderer.render(getAst(), null, null);
    }
}
