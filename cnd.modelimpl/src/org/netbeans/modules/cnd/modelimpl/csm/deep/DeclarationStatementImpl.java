/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 * @author Vladimir Kvashin
 */
public class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private List<CsmDeclaration> declarators;

    public DeclarationStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    public List<CsmDeclaration> getDeclarators() {
        if (declarators == null) {
            declarators = new ArrayList<CsmDeclaration>();
            render();
            //RepositoryUtils.setSelfUIDs(declarators);
        }
        return declarators;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (declarators != null) {
            Utils.disposeAll(declarators);
        }
    }

    @Override
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString() + '[' + declarators + ']'; // NOI18N
    }

    private void render() {
        AstRenderer renderer = new DSRenderer();
        renderer.render(getAst(), null, null);
    }

    private class DSRenderer extends AstRenderer {

        public DSRenderer() {
            super((FileImpl) getContainingFile());
        }

        @Override
        protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, boolean _extern, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
            VariableImpl var = super.createVariable(offsetAst, file, type, name, _static, _extern, container1, container2, getScope());
            declarators.add(var);
            return var;
        }

        protected FunctionImpl createFunction(AST ast, CsmFile file, CsmType type, CsmScope scope) {
            FunctionImpl fun = null;
            try {
                fun = new FunctionImpl(ast, file, type, getScope(), !isRenderingLocalContext(), !isRenderingLocalContext());
                declarators.add(fun);
            } catch (AstRendererException ex) {
                DiagnosticExceptoins.register(ex);
            }
            return fun;
        }

        @Override
        protected boolean isRenderingLocalContext() {
            return true;
        }

        @Override
        public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
            if (tree != null) {
                AST token = tree;
                switch (token.getType()) {
                    case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
                    case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
                        if (!renderVariable(token, currentNamespace, container, false)) {
                            AST ast = token;
                            token = token.getFirstChild();
                            if (token != null) {
                                boolean _static = false;
                                boolean _extern = false;
                                if (isQualifier(token.getType())) {
                                    _static = AstUtil.hasChildOfType(token, CPPTokenTypes.LITERAL_static);
                                    _extern = AstUtil.hasChildOfType(token, CPPTokenTypes.LITERAL_extern);
                                    token = getFirstSiblingSkipQualifiers(token);
                                }
                                if (token != null && (token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                                    AST typeToken = token;
                                    AST next = token.getNextSibling();
                                    AST ptrOperator = null;
                                    while (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                                        if (ptrOperator == null) {
                                            ptrOperator = next;
                                        }
                                        next = next.getNextSibling();
                                    }
                                    TypeImpl type = null;
                                    if (typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                                        AST typeNameToken = typeToken.getFirstChild();
                                        if (typeNameToken != null) {
                                            type = TypeFactory.createBuiltinType(typeNameToken.getText(), ptrOperator, 0, typeToken, getContainingFile());
                                        }
                                    } else {
                                        type = TypeFactory.createType(typeToken, getContainingFile(), ptrOperator, 0);
                                    }
                                    if (type != null) {
                                        while (next != null) {
                                            if (next.getType() == CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION) {
                                                AST inner = next.getFirstChild();
                                                if (inner != null) {
                                                    String name = inner.getText();
                                                    if (isVariableLikeFunc(next)) {
                                                        /*FunctionImpl fun =*/ createFunction(next, getContainingFile(), type, getScope());
                                                    } else {
                                                        /*VariableImpl var =*/ createVariable(next, getContainingFile(), type, name, _static, _extern, currentNamespace, container, getScope());
                                                    }
                                                }
                                            }
                                            if (next.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
                                                AST nameAst = next.getFirstChild();
                                                if (nameAst != null && nameAst.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                                                    /*VariableImpl var =*/ createVariable(next, getContainingFile(), type, nameAst.getText(), _static, _extern, currentNamespace, container, getScope());
                                                    next = next.getNextSibling();
                                                    if (next != null && next.getType() == CPPTokenTypes.COMMA) {
                                                        next = next.getNextSibling();
                                                    }
                                                }
                                            }
                                            next = next.getNextSibling();
                                        }
                                    }
                                }
                            }

                            render(ast.getFirstChild(), currentNamespace, container);
                        }
                        break;
                    case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                        declarators.add(new NamespaceAliasImpl(token, getContainingFile(), null, !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_USING_DIRECTIVE:
                        declarators.add(new UsingDirectiveImpl(token, getContainingFile(), !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_USING_DECLARATION:
                        declarators.add(new UsingDeclarationImpl(token, getContainingFile(), null, !isRenderingLocalContext()));
                        break;

                    case CPPTokenTypes.CSM_CLASS_DECLARATION:
                    case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION:
                    {
                        ClassImpl cls = TemplateUtils.isPartialClassSpecialization(token) ?
                                        ClassImplSpecialization.create(token, null, getContainingFile(), !isRenderingLocalContext(), null) :
                                        ClassImpl.create(token, null, getContainingFile(), !isRenderingLocalContext(), null);
                        declarators.add(cls);
                        Pair typedefs = renderTypedef(token, cls, currentNamespace);
                        if (!typedefs.getTypesefs().isEmpty()) {
                            addTypedefs(typedefs.getTypesefs(), currentNamespace, container, cls);
                            for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                declarators.add(typedef);
                                //FIXME: class do not allow register enclosing typedef that does not in repository
                                //if (cls != null) {
                                //   cls.addEnclosingTypedef(typedefs[i]);
                                //}
                            }
                        }
                        renderVariableInClassifier(token, cls, currentNamespace, container);
                        break;
                    }
                    case CPPTokenTypes.CSM_ENUM_DECLARATION:
                    {
                        CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, getContainingFile(), !isRenderingLocalContext());
                        declarators.add(csmEnum);
                        renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                        break;
                    }
                    case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                    {
                        Pair typedefs = renderTypedef(token, (FileImpl) getContainingFile(), getScope(), null);
                        if (!typedefs.getTypesefs().isEmpty()) {
                            for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                declarators.add(typedef);
                            }
                        }
                        break;
                    }
                }
            }
        }

        @Override
        protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
            // TODO : implement local forward decls support
            return null;
        }

// Never used 
//	/**
//	 * Creates a variable for declaration like int x(y);
//	 * Returns a token that follows this declaration or null
//	 */
//	private AST createVarWithCtor(AST token) {
//	    assert(token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND);
//	    AST typeToken = token;
//	    AST next = token.getNextSibling();
//	    if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//		TypeImpl type;
//		if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//		    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
//		}
//		else {
//		    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
//		}
//		String name = next.getText();
//		VariableImpl var = new VariableImpl(next, getContainingFile(), type, name, true);
//		// we ignore both currentNamespace and container
//		declarators.add(var);
//		// eat all tokens up to the comma that separates the next decl
//		next = next.getNextSibling();
//		if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
//		    next = next.getNextSibling();
//		}
//		if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
//		    next = next.getNextSibling();
//		}
//		return next;
//	    }
//	    return null;
//	}
    }
}
