/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;

import org.netbeans.modules.cnd.antlr.collections.AST;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.AstRendererException;
import org.netbeans.modules.cnd.modelimpl.csm.deep.*;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.util.Exceptions;

/**
 * @author Vladimir Kvasihn
 */
public class AstRenderer {

    private final FileImpl file;

    public AstRenderer(FileImpl fileImpl) {
        this.file = fileImpl;
    }

    public void render(AST root) {
//        if (file.getAbsolutePath().toString().endsWith("shared.h")) {
//            int i = 10;
//        }
        render(root, (NamespaceImpl) file.getProject().getGlobalNamespace(), file);
    }

    @SuppressWarnings("fallthrough")
    public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (tree == null) {
            return; // paranoia
        }
        for (AST token = tree.getFirstChild(); token != null; token = token.getNextSibling()) {
            int type = token.getType();
            switch (type) {
                case CPPTokenTypes.CSM_LINKAGE_SPECIFICATION:
                    render(token, currentNamespace, container);
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_DECLARATION:
                    NamespaceDefinitionImpl ns = NamespaceDefinitionImpl.findOrCreateNamespaceDefionition(container, token, currentNamespace, file);
                    render(token, (NamespaceImpl) ns.getNamespace(), ns);
                    break;
                case CPPTokenTypes.CSM_CLASS_DECLARATION:
                case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION: {
                    ClassImpl cls = TemplateUtils.isPartialClassSpecialization(token) ?
                                        ClassImplSpecialization.create(token, currentNamespace, file, !isRenderingLocalContext(), container) :
                                        ClassImpl.create(token, currentNamespace, file, !isRenderingLocalContext(), container);
                    container.addDeclaration(cls);
                    addTypedefs(renderTypedef(token, cls, currentNamespace).typedefs, currentNamespace, container, cls);
                    renderVariableInClassifier(token, cls, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_ENUM_DECLARATION: {
                    CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, file, !isRenderingLocalContext());
                    container.addDeclaration(csmEnum);
                    renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                    if (isFuncLikeVariable(token, false)) {
                        if (renderFuncLikeVariable(token, currentNamespace, container, !isFuncLikeVariable(token, true))) {
                            break;
                        }
                    }
                //nobreak!
                case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
                    try {
                        FunctionImpl fi = new FunctionImpl(token, file, currentNamespace, !isRenderingLocalContext(), !isRenderingLocalContext());
                        container.addDeclaration(fi);
                        if (NamespaceImpl.isNamespaceScope(fi)) {
                            currentNamespace.addDeclaration(fi);
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_CTOR_DEFINITION:
                case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                    try {
                        container.addDeclaration(new ConstructorDefinitionImpl(token, file, null, !isRenderingLocalContext()));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_DTOR_DEFINITION:
                case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                    try {
                        container.addDeclaration(new DestructorDefinitionImpl(token, file, !isRenderingLocalContext()));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DEFINITION:
                case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                    try {
                        if (isMemberDefinition(token)) {
                            container.addDeclaration(new FunctionDefinitionImpl(token, file, null, !isRenderingLocalContext(), !isRenderingLocalContext()));
                        } else {
                            FunctionDDImpl fddi = new FunctionDDImpl(token, file, currentNamespace, !isRenderingLocalContext());
                            //fddi.setScope(currentNamespace);
                            container.addDeclaration(fddi);
                            if (NamespaceImpl.isNamespaceScope(fddi)) {
                                currentNamespace.addDeclaration(fddi);
                            }
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                    if (isClassSpecialization(token)) {
                        ClassImpl spec = ClassImplSpecialization.create(token, currentNamespace, file, !isRenderingLocalContext(), container);
                        container.addDeclaration(spec);
                        addTypedefs(renderTypedef(token, spec, currentNamespace).typedefs, currentNamespace, container, spec);
                    } else {
                        try {
                            if (isMemberDefinition(token)) {
                                // this is a template method specialization declaration (without a definition)
                                container.addDeclaration(new FunctionImplEx(token, file, null, !isRenderingLocalContext(), !isRenderingLocalContext()));
                            } else {
                                if (renderForwardMemberDeclaration(token, currentNamespace, container, file)) {
                                    break;
                                }
                                FunctionImpl funct = new FunctionImpl(token, file, currentNamespace, !isRenderingLocalContext(), !isRenderingLocalContext());
                                container.addDeclaration(funct);
                                if (NamespaceImpl.isNamespaceScope(funct)) {
                                    currentNamespace.addDeclaration(funct);
                                }
                            }
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_CTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
                    try {
                        container.addDeclaration(new ConstructorDefinitionImpl(token, file, null, !isRenderingLocalContext()));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_DTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
                    try {
                        container.addDeclaration(new DestructorDefinitionImpl(token, file, !isRenderingLocalContext()));
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
                    try {
                        if (isMemberDefinition(token)) {
                            container.addDeclaration(new FunctionDefinitionImpl(token, file, null, !isRenderingLocalContext(), !isRenderingLocalContext()));
                        } else {
                            FunctionDDImpl fddit = new FunctionDDImpl(token, file, currentNamespace, !isRenderingLocalContext());
                            container.addDeclaration(fddit);
                            if (NamespaceImpl.isNamespaceScope(fddit)) {
                                currentNamespace.addDeclaration(fddit);
                            }
                        }
                    } catch (AstRendererException e) {
                        DiagnosticExceptoins.register(e);
                    }
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                    NamespaceAliasImpl alias = new NamespaceAliasImpl(token, file, currentNamespace, !isRenderingLocalContext());
                    container.addDeclaration(alias);
                    currentNamespace.addDeclaration(alias);
                    break;
                case CPPTokenTypes.CSM_USING_DIRECTIVE: {
                    UsingDirectiveImpl using = new UsingDirectiveImpl(token, file, !isRenderingLocalContext());
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                    break;
                }
                case CPPTokenTypes.CSM_USING_DECLARATION: {
                    UsingDeclarationImpl using = new UsingDeclarationImpl(token, file, currentNamespace, !isRenderingLocalContext(), CsmVisibility.PUBLIC);
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                    break;
                }
                case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                    if (renderForwardClassDeclaration(token, currentNamespace, container, file, isRenderingLocalContext())) {
                        break;
                    } else {
                        renderForwardMemberDeclaration(token, currentNamespace, container, file);
                    }
                    break;
                case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                    if (renderNSP(token, currentNamespace, container, file)) {
                        break;
                    }
                    if (renderVariable(token, currentNamespace, container, false)) {
                        break;
                    }
                    if (renderForwardClassDeclaration(token, currentNamespace, container, file, isRenderingLocalContext())) {
                        break;
                    }
                    if (renderLinkageSpec(token, file, currentNamespace, container)) {
                        break;
                    }
                    addTypedefs(renderTypedef(token, file, currentNamespace, container).typedefs, currentNamespace, container,null);
                    break;
                default:
                    renderNSP(token, currentNamespace, container, file);
            }
        }
    }

    protected void addTypedefs(Collection<CsmTypedef> typedefs, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, ClassImpl enclosingClassifier) {
        if (typedefs != null) {
            for (CsmTypedef typedef : typedefs) {
                // It could be important to register in project before add as member...
                if (!isRenderingLocalContext()) {
                    file.getProjectImpl(true).registerDeclaration(typedef);
                }
                if (container != null) {
                    container.addDeclaration(typedef);
                }
                if (currentNamespace != null) {
                    // Note: DeclarationStatementImpl.DSRenderer can call with null namespace
                    currentNamespace.addDeclaration(typedef);
                }
                if (enclosingClassifier != null) {
                   enclosingClassifier.addEnclosingTypedef(typedef);
                }
            }
        }
    }

    /**
     * Parser don't use a symbol table, so constructs like
     * int a(b) 
     * are parsed as if they were functions.
     * At the moment of rendering, we check whether this is a variable of a function
     * @return true if it's a variable, otherwise false (it's a function)
     */
    private boolean isFuncLikeVariable(AST ast, boolean findRefsForParams) {
        AST astParmList = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
        if (astParmList != null) {
            for (AST node = astParmList.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (!isRefToVariableOrFunction(node, findRefsForParams)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Parser don't use a symbol table, so local constructs like
     * int a(b) 
     * are parsed as if they were variables.
     * At the moment of rendering, we check whether this is a variable of a function
     * @return true if it's a function, otherwise false (it's a variable)
     */
    protected boolean isVariableLikeFunc(AST ast) {
        AST astParmList = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
        if (astParmList != null) {
            for (AST node = astParmList.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getType() != CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                    return false;
                }
                AST child = node.getFirstChild();
                if (child != null) {
                    if (child.getType() == CPPTokenTypes.LITERAL_const) {
                        return true;
                    } else if (child.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                        return true;
                    } else if (child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                        CsmType type = TypeFactory.createType(child, file, null, 0);
                        if (type != null && type.getClassifier().isValid()) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the given parameter can actually be a reference to a variable,
     * not a parameter
     * @param node an AST node that corresponds to parameter
     * @param findVariable indicates that we should find param variable or just check that it looks like id
     * @return true if might be just a reference to a variable, otherwise false
     */
    private boolean isRefToVariableOrFunction(AST node, boolean findVariableOrFunction) {

        if (node.getType() != CPPTokenTypes.CSM_PARAMETER_DECLARATION) { // paranoja
            return false;
        }

        AST child = node.getFirstChild();

        AST name = null;

        // AST structure is different for int f1(A) and int f2(*A)
        if (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
            // we know it's variable initialization => no need to look for variable
            // TODO: why we need to go deeper after * or & ? I'd prefer to return 'true'
            if (true) {
                return true;
            }
            while (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                child = child.getNextSibling();
            }
            // now it's CSM_VARIABLE_DECLARATION
            if (child != null) {
                name = child.getFirstChild();
            }
        } else if (child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
            if (!isAbstractDeclarator(child.getNextSibling())) {
                return false;
            }
            name = child.getFirstChild();
        } else if(child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
            return true;
        }

        if (name == null) {
            return false;
        }
        if (name.getType() != CPPTokenTypes.CSM_QUALIFIED_ID &&
                name.getType() != CPPTokenTypes.ID) {
            return false;
        }

        return isVariableOrFunctionName(name, findVariableOrFunction);
    }

    private boolean isVariableOrFunctionName(AST name, boolean findVariableOrFunction) {
        CsmAST csmAST = AstUtil.getFirstCsmAST(name);

        StringBuilder varName = new StringBuilder(name.getText());
        AST next = name.getNextSibling();
        while (next != null) {
            next = skipTemplateParameters(next);
            if(next == null) {
                break;
            }
            name = next;
            varName.append(name.getText());
            next = next.getNextSibling();
        }

        if (findVariableOrFunction) {
            return findVariable(varName, csmAST.getOffset()) || findFunction(varName, csmAST.getOffset());
        } else {
            next = name.getNextSibling();
            next = skipTemplateParameters(next);
            if (next != null) {
                return isScopedId(name);
            }
            return true;
        }
    }

    public static AST skipTemplateParameters(AST node) {
        int depth = 0;
        while (node != null) {
            switch (node.getType()) {
                case CPPTokenTypes.LESSTHAN:
                    depth++;
                    break;
                case CPPTokenTypes.GREATERTHAN:
                    depth--;
                    if (depth == 0) {
                        return node.getNextSibling();
                    }
                    break;
                default:
                    if(depth == 0) {
                        return node;
                    }
            }
            node = node.getNextSibling();
        }
        return null;
    }

    private boolean isAbstractDeclarator(AST node) {
        if(node == null) {
            return true;
        }
        if(node.getType() != CPPTokenTypes.LPAREN) {
            return false;
        }
        node = node.getNextSibling();
        if(node == null || node.getType() != CPPTokenTypes.RPAREN) {
            return false;
        }
        if(node.getNextSibling() !=  null) {
            return false;
        }
        return true;
    }


    /**
     * Finds variable in globals and in the current file
     */
    private boolean findVariable(CharSequence name, int offset) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE) +
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + "::" + name; // NOI18N
        if (findGlobal(file.getProject(), uname, new ArrayList<CsmProject>())) {
            return true;
        }
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION);
        return findVariable(name, CsmSelect.getDeclarations(file, filter), offset, filter);
    }

    /**
     * Finds function in globals and in the current file
     */
    private boolean findFunction(CharSequence name, int offset) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) +
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + "::" + name; // NOI18N
        if (findGlobal(file.getProject(), uname, new ArrayList<CsmProject>())) {
            return true;
        }
        uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) +
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + "::" + name; // NOI18N
        if (findGlobal(file.getProject(), uname, new ArrayList<CsmProject>())) {
            return true;
        }
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION,
                CsmDeclaration.Kind.FUNCTION_FRIEND, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
        return findFunction(name, CsmSelect.getDeclarations(file, filter), offset, filter);
    }

    private boolean findGlobal(CsmProject project, String uname, Collection<CsmProject> processedProjects) {
        if (processedProjects.contains(project)) {
            return false;
        }
        processedProjects.add(project);
        if (project.findDeclaration(uname) != null) {
            return true;
        }
        for (CsmProject lib : project.getLibraries()) {
            if (findGlobal(lib, uname, processedProjects)) {
                return true;
            }
        }
        return false;
    }

    private boolean findVariable(CharSequence name, Iterator<CsmOffsetableDeclaration> it, int offset, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() >= offset) {
                break;
            }
            switch (decl.getKind()) {
                case VARIABLE:
                    if (CharSequenceKey.Comparator.compare(name, ((CsmVariable) decl).getName()) == 0) {
                        return true;
                    }
                    break;
                case VARIABLE_DEFINITION:
                    if (CharSequenceKey.Comparator.compare(name, ((CsmVariable) decl).getQualifiedName()) == 0) {
                        return true;
                    }
                    break;
                case NAMESPACE_DEFINITION:
                    CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                    if (nd.getStartOffset() <= offset && nd.getEndOffset() >= offset) {
                        if (findVariable(name, CsmSelect.getDeclarations(nd, filter), offset, filter)) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    private boolean findFunction(CharSequence name, Iterator<CsmOffsetableDeclaration> it, int offset, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() >= offset) {
                break;
            }
            switch (decl.getKind()) {
                case FUNCTION:
                case FUNCTION_FRIEND:
                    if (CharSequenceKey.Comparator.compare(name, ((CsmFunction) decl).getName()) == 0) {
                        return true;
                    }
                    break;
                case FUNCTION_DEFINITION:
                case FUNCTION_FRIEND_DEFINITION:
                    if (CharSequenceKey.Comparator.compare(name, ((CsmFunctionDefinition) decl).getQualifiedName()) == 0) {
                        return true;
                    }
                    break;
                case NAMESPACE_DEFINITION:
                    CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                    if (nd.getStartOffset() <= offset && nd.getEndOffset() >= offset) {
                        if (findFunction(name, CsmSelect.getDeclarations(nd, filter), offset, filter)) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    protected boolean isRenderingLocalContext() {
        return false;
    }

    /**
     * In the case of the "function-like variable" - construct like
     * int a(b) 
     * renders the AST to create the variable
     */
    private boolean renderFuncLikeVariable(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, boolean fakeRegistration) {
        if (token != null) {
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
                    while (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                        next = next.getNextSibling();
                    }
                    if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        TypeImpl type;
                        if (typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                            type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, file);
                        } else {
                            type = TypeFactory.createType(typeToken, file, null, 0);
                        }
                        String name = next.getText();

                        if (!fakeRegistration) {
                            VariableImpl var = createVariable(next, file, type, name, _static, _extern, currentNamespace, container, null);
                            if (currentNamespace != null) {
                                currentNamespace.addDeclaration(var);
                            }
                            if (container != null) {
                                container.addDeclaration(var);
                            }
                            return true;
                        } else {
                            if (isScopedId(next)) {
                                try {
                                    FunctionImplEx fi = new FunctionImplEx(ast, file, currentNamespace, false, !isRenderingLocalContext());
                                    file.onFakeRegisration(fi, ast);
                                } catch (AstRendererException e) {
                                    DiagnosticExceptoins.register(e);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean renderLinkageSpec(AST ast, FileImpl file, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (ast != null) {
            AST token = ast.getFirstChild();
            if (token != null) {
                if (token.getType() == CPPTokenTypes.CSM_LINKAGE_SPECIFICATION) {
                    render(token, currentNamespace, container);
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected void renderVariableInClassifier(AST ast, CsmClassifier classifier,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2) {
        AST token = ast.getFirstChild();
        boolean unnamedStaticUnion = false;
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        boolean _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        int typeStartOffset = 0;
        if (token != null) {
            typeStartOffset = AstUtil.getFirstCsmAST(token).getOffset();
            if (token.getType() == CPPTokenTypes.LITERAL_static) {
                token = token.getNextSibling();
                if (token != null) {
                    if (token.getType() == CPPTokenTypes.LITERAL_union) {
                        token = token.getNextSibling();
                        if (token != null) {
                            if (token.getType() == CPPTokenTypes.LCURLY) {
                                unnamedStaticUnion = true;
                            }
                        }
                    }
                }
            }
        }
        for (; token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.RCURLY) {
                break;
            }
        }
        if (token != null) {
            int rcurlyOffset = AstUtil.getFirstCsmAST(token).getEndOffset();
            CsmOffsetable typeOffset = new OffsetableBase(file, typeStartOffset, rcurlyOffset);
            token = token.getNextSibling();
            boolean nothingBeforSemicolon = true;
            AST ptrOperator = null;
            for (; token != null; token = token.getNextSibling()) {
                switch (token.getType()) {
                    case CPPTokenTypes.CSM_PTR_OPERATOR:
                        nothingBeforSemicolon = false;
                        if (ptrOperator == null) {
                            ptrOperator = token;
                        }
                        break;
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION: {
                        nothingBeforSemicolon = false;
                        int arrayDepth = 0;
                        String name = null;
                        for (AST varNode = token.getFirstChild(); varNode != null; varNode = varNode.getNextSibling()) {
                            switch (varNode.getType()) {
                                case CPPTokenTypes.LSQUARE:
                                    arrayDepth++;
                                    break;
                                case CPPTokenTypes.CSM_QUALIFIED_ID:
                                case CPPTokenTypes.ID:
                                    name = varNode.getText();
                                    break;
                            }
                        }
                        if (name != null) {
                            CsmType type = TypeFactory.createType(classifier, ptrOperator, arrayDepth, token, file, typeOffset);
                            VariableImpl var = createVariable(token, file, type, name, _static, _extern, container1, container2, null);
                            if (container2 != null) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if (container1 != null) {
                                container1.addDeclaration(var);
                            }
                            ptrOperator = null;
                        }
                    }
                    case CPPTokenTypes.SEMICOLON: {
                        if (unnamedStaticUnion && nothingBeforSemicolon) {
                            nothingBeforSemicolon = false;
                            CsmType type = TypeFactory.createType(classifier, null, 0, null, file, typeOffset);
                            VariableImpl var = new VariableImpl(new OffsetableBase(file, rcurlyOffset, rcurlyOffset),
                                    file, type, "", null, true, false, !isRenderingLocalContext()); // NOI18N
                            if (container2 != null) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if (container1 != null) {
                                container1.addDeclaration(var);
                            }
                        }
                    }
                    default:
                        nothingBeforSemicolon = false;
                }
            }
        }
    }

    @SuppressWarnings("fallthrough")
    protected Pair renderTypedef(AST ast, CsmClass cls, CsmObject container) {

        Pair results = new Pair();

        AST typedefNode = ast.getFirstChild();

        if (typedefNode != null && typedefNode.getType() == CPPTokenTypes.LITERAL_typedef) {

            AST classNode = typedefNode.getNextSibling();
            if (isVolatileQualifier(classNode.getType())) {
                classNode = classNode.getNextSibling();
            }
            switch (classNode.getType()) {

                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_struct:

                    AST curr = AstUtil.findSiblingOfType(classNode, CPPTokenTypes.RCURLY);
                    if (curr == null) {
                        return results;
                    }

                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    String name = "";
                    for (curr = curr.getNextSibling(); curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = ast;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                nameToken = curr;
                                //token t = nameToken.
                                name = AstUtil.findId(nameToken);
                                //name = token.getText();
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                                break;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = TypeFactory.createType(cls, ptrOperator, arrayDepth, ast, file);
                                if (typeImpl != null) {
                                    typeImpl.setTypeOfTypedef();
                                }
                                CsmTypedef typedef = createTypedef((nameToken == null) ? ast : nameToken, file, container, typeImpl, name);
                                if (cls != null && cls.getName().length() == 0) {
                                    ((TypedefImpl) typedef).setTypeUnnamed();
                                }
                                if (typedef != null) {
                                    if (cls != null) {
                                        results.enclosing = (ClassImpl)cls;
                                    }
                                    results.typedefs.add(typedef);
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }

                    }
                    break;
                default:
                // error message??
            }
        }
        return results;
    }

    protected static class Pair {
        protected List<CsmTypedef> typedefs = new ArrayList<CsmTypedef>();
        protected ClassEnumBase enclosing;
        private Pair(){
        }
        public List<CsmTypedef> getTypesefs(){
            return typedefs;
        }
        public ClassEnumBase getEnclosingClassifier(){
            return enclosing;
        }
    }

    @SuppressWarnings("fallthrough")
    protected Pair renderTypedef(AST ast, FileImpl file, CsmScope scope, MutableDeclarationsContainer container) {
        Pair results = new Pair();
        if (ast != null) {
            AST firstChild = ast.getFirstChild();
            if (firstChild != null) {
                if (firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
                    //return createTypedef(ast, file, container);

                    AST classifier = null;
                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    String name = "";

                    CsmClassForwardDeclaration cfdi = null;

                    for (AST curr = firstChild; curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.CSM_TYPE_COMPOUND:
                            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                                classifier = curr;
                                break;
                            case CPPTokenTypes.LITERAL_enum:
                                if (AstUtil.findSiblingOfType(curr, CPPTokenTypes.RCURLY) != null) {
                                    results.enclosing = EnumImpl.create(curr, scope, file, !isRenderingLocalContext());
                                    if (results.getEnclosingClassifier() != null && scope instanceof MutableDeclarationsContainer) {
                                        ((MutableDeclarationsContainer) scope).addDeclaration(results.getEnclosingClassifier());
                                    }
                                    if (container != null && results.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(results.getEnclosingClassifier())) {
                                        container.addDeclaration(results.getEnclosingClassifier());
                                    }
                                    break;
                                }
                            // else fall through!
                            case CPPTokenTypes.LITERAL_struct:
                            case CPPTokenTypes.LITERAL_union:
                            case CPPTokenTypes.LITERAL_class:
                                AST next = curr.getNextSibling();
                                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                                    classifier = next;
                                    cfdi = createForwardClassDeclaration(ast, container, file, scope);
                                }
                                break;
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = curr;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                // now token corresponds the name, since the case "struct S" is processed before
                                nameToken = curr;
                                name = AstUtil.findId(nameToken);
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                                break;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = null;
                                if (cfdi != null) {
                                    typeImpl = TypeFactory.createType(cfdi, ptrOperator, arrayDepth, ast, file);
                                } else if (classifier != null) {
                                    typeImpl = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth, null, scope, false, true);
                                } else if (results.getEnclosingClassifier() != null) {
                                    typeImpl = TypeFactory.createType(results.getEnclosingClassifier(), ptrOperator, arrayDepth, ast, file);
                                }
                                if (typeImpl != null) {
                                    typeImpl.setTypeOfTypedef();
                                    CsmTypedef typedef = createTypedef(ast/*nameToken*/, file, scope, typeImpl, name);
                                    if (typedef != null) {
                                        if (results.getEnclosingClassifier() != null && results.getEnclosingClassifier().getName().length() == 0) {
                                            ((TypedefImpl) typedef).setTypeUnnamed();
                                        }
                                        results.typedefs.add(typedef);
                                    }
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }
                    }
                }
            }
        }
        return results;
    }

    protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
        ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file, !isRenderingLocalContext());
        if (container != null) {
            container.addDeclaration(cfdi);
        }
        cfdi.init(ast, scope, !isRenderingLocalContext());
        return cfdi;
    }

    protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
        return new TypedefImpl(ast, file, container, type, name, !isRenderingLocalContext());
    }

    public boolean renderForwardClassDeclaration(
            AST ast,
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container,
            FileImpl file,
            boolean isRenderingLocalContext) {

        AST child = ast.getFirstChild();
        if (child == null) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = child.getNextSibling();
            if (child == null) {
                return false;
            }
        }

        switch (child.getType()) {
            case CPPTokenTypes.LITERAL_class:
            case CPPTokenTypes.LITERAL_struct:
            case CPPTokenTypes.LITERAL_union:
                createForwardClassDeclaration(ast, container, file, currentNamespace);
                return true;
        }

        return false;
    }

    public boolean renderForwardMemberDeclaration(
            AST ast,
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container,
            FileImpl file) {

        AST child = ast.getFirstChild();
        while (child != null) {
            switch (child.getType()) {
                case CPPTokenTypes.LITERAL_template:
                    child = skipTemplateSibling(child);
                    continue;
                case CPPTokenTypes.LITERAL_inline:
                case CPPTokenTypes.LITERAL__inline:
                case CPPTokenTypes.LITERAL___inline:
                case CPPTokenTypes.LITERAL___inline__:
                    child = child.getNextSibling();
                    continue;
            }
            break;
        }
        if (child == null) {
            return false;
        }
        child = getFirstSiblingSkipQualifiers(child);
        if (child == null) {
            return false;
        }

        switch (child.getType()) {
            case CPPTokenTypes.CSM_TYPE_COMPOUND:
            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                child = getFirstSiblingSkipQualifiers(child.getNextSibling());
                while (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                    child = child.getNextSibling();
                }
                if (child != null) {
                    if (child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                            child.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                        //static variable definition
                        return renderVariable(ast, null, container, false);
                    } else {
                        //method forward declaratin
                        try {
                            FunctionImpl ftdecl = new FunctionImpl(ast, file, currentNamespace, !isRenderingLocalContext(), !isRenderingLocalContext());
                            if (container != null) {
                                container.addDeclaration(ftdecl);
                            }
                            if (NamespaceImpl.isNamespaceScope(ftdecl)) {
                                currentNamespace.addDeclaration(ftdecl);
                            }
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    public static CharSequence getQualifiedName(AST qid) {
        if (qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            if (qid.getFirstChild() != null) {
                StringBuilder sb = new StringBuilder();
                for (AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling()) {
                    // TODO: update this assert it should accept names like: allocator<char, typename A>
//                    if( ! ( namePart.getType() == CPPTokenTypes.ID || namePart.getType() == CPPTokenTypes.SCOPE ||
//                            namePart.getType() == CPPTokenTypes.LESSTHAN || namePart.getType() == CPPTokenTypes.GREATERTHAN ||
//                            namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
//                            namePart.getType() == CPPTokenTypes.COMMA) ) {
//			new Exception("Unexpected token type " + namePart).printStackTrace(System.err);
//		    }
                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                            namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                        AST builtInType = namePart.getFirstChild();
                        sb.append(builtInType != null ? builtInType.getText() : "");
                    } else {
                        sb.append(namePart.getText());
                    }
                }
                return TextCache.getManager().getString(sb.toString());
            }
        }
        return "";
    }

    public static CharSequence[] getNameTokens(AST qid) {
        if (qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            int templateDepth = 0;
            if (qid.getNextSibling() != null) {
                List<CharSequence> l = new ArrayList<CharSequence>();
                for (AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling()) {
                    if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID) {
                        l.add(NameCache.getManager().getString(namePart.getText()));
                    } else if (namePart.getType() == CPPTokenTypes.LESSTHAN) {
                        // the beginning of template parameters
                        templateDepth++;
                    } else if (namePart.getType() == CPPTokenTypes.GREATERTHAN) {
                        // the beginning of template parameters
                        templateDepth--;
                    } else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if (templateDepth == 0 && namePart.getType() != CPPTokenTypes.SCOPE) {
                            StringBuilder tokenText = new StringBuilder();
                            tokenText.append('[').append(namePart.getText());
                            if (namePart.getNumberOfChildren() == 0) {
                                tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                            }
                            tokenText.append(']');
                            System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                        }
                    }
                }
                return l.toArray(new CharSequence[l.size()]);
            }
        }
        return new CharSequence[0];
    }

    public static TypeImpl renderType(AST tokType, CsmFile file) {

        AST typeAST = tokType;
        tokType = getFirstSiblingSkipQualifiers(tokType);

        if (tokType != null) {
            if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                    tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                AST next = tokType.getNextSibling();
                AST ptrOperator = (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
                return TypeFactory.createType(typeAST, file, ptrOperator, 0);
            }
            if (tokType.getType() == CPPTokenTypes.LITERAL_struct ||
                    tokType.getType() == CPPTokenTypes.LITERAL_class ||
                    tokType.getType() == CPPTokenTypes.LITERAL_union) {
                AST next = tokType.getNextSibling();
                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    tokType = next;
                    next = tokType.getNextSibling();
                    AST ptrOperator = (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
                    return TypeFactory.createType(typeAST, file, ptrOperator, 0);
                }
            }
        }

        /**
        CsmClassifier classifier = null;
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
        classifier = BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
        try {
        Resolver resolver = new Resolver(file, ((CsmAST) tokType.getFirstChild()).getOffset());
        // gather name components into string array
        // for example, for std::vector new CharSequence[] { "std", "vector" }
        List l = new ArrayList();
        for( AST namePart = tokType.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
        if( namePart.getType() == CPPTokenTypes.ID ) {
        l.add(namePart.getText());
        }
        else {
        assert namePart.getType() == CPPTokenTypes.SCOPE;
        }
        }
        CsmObject o = resolver.resolve((String[]) l.toArray(new CharSequence[l.size()]));
        if( o instanceof CsmClassifier ) {
        classifier = (CsmClassifier) o;
        }
        }
        catch( Exception e ) {
        e.printStackTrace(System.err);
        }
        }

        if( classifier != null ) {
        AST next = tokType.getNextSibling();
        AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
        return TypeImpl.createType(classifier, ptrOperator, 0);
        }
        
        return null;
         */
        return null;
    }

    /**
     * Returns first sibling (or just passed ast), skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstSiblingSkipQualifiers(AST ast) {
        while (ast != null && isQualifier(ast.getType())) {
            ast = ast.getNextSibling();
        }
        return ast;
    }

    /**
     * Returns first child, skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstChildSkipQualifiers(AST ast) {
        return getFirstSiblingSkipQualifiers(ast.getFirstChild());
    }

    public static boolean isQualifier(int tokenType) {
        return isCVQualifier(tokenType) || isStorageClassSpecifier(tokenType) || (tokenType == CPPTokenTypes.LITERAL_typename);
    }

    public static boolean isCVQualifier(int tokenType) {
        return isConstQualifier(tokenType) || isVolatileQualifier(tokenType);
    }

    public static boolean isConstQualifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_const:
                return true;
            case CPPTokenTypes.LITERAL___const:
                return true;
            case CPPTokenTypes.LITERAL___const__:
                return true;
            default:
                return false;
        }
    }

    public static boolean isVolatileQualifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_volatile:
                return true;
            case CPPTokenTypes.LITERAL___volatile__:
                return true;
            case CPPTokenTypes.LITERAL___volatile:
                return true;
            default:
                return false;
        }
    }

    public static boolean isStorageClassSpecifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_auto:
                return true;
            case CPPTokenTypes.LITERAL_register:
                return true;
            case CPPTokenTypes.LITERAL_static:
                return true;
            case CPPTokenTypes.LITERAL_extern:
                return true;
            case CPPTokenTypes.LITERAL_mutable:
                return true;
            case CPPTokenTypes.LITERAL___thread:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether the given AST is a variable declaration(s), 
     * if yes, creates variable(s), adds to conteiner(s), returns true,
     * otherwise returns false;
     *
     * There might be two containers, in which the given variable should be added.
     * For example, global variables should beadded both to file and to global namespace;
     * variables, declared in some namespace definition, should be added to both this definition and correspondent namespace as well.
     *
     * On the other hand, local variables are added only to it's containing scope, so either container1 or container2 might be null.
     *
     * @param ast AST to process
     * @param container1 container to add created variable into (may be null)
     * @param container2 container to add created variable into (may be null)
     */
    public boolean renderVariable(AST ast, MutableDeclarationsContainer namespaceContainer, MutableDeclarationsContainer container2, boolean functionParameter) {
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        boolean _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        AST typeAST = ast.getFirstChild();
        AST tokType = typeAST;
        if (tokType != null && tokType.getType() == CPPTokenTypes.LITERAL_template) {
            typeAST = tokType = skipTemplateSibling(tokType);
        }
        tokType = getFirstSiblingSkipQualifiers(tokType);
        if (tokType == null) {
            return false;
        }
        boolean isThisReference = false;
        if (tokType != null &&
                (tokType.getType() == CPPTokenTypes.LITERAL_struct ||
                tokType.getType() == CPPTokenTypes.LITERAL_union ||
                tokType.getType() == CPPTokenTypes.LITERAL_enum ||
                tokType.getType() == CPPTokenTypes.LITERAL_class)) {
            // This is struct/class word for reference on containing struct/class
            tokType = tokType.getNextSibling();
            typeAST = tokType;
            if (tokType == null) {
                return false;
            }
            isThisReference = true;
        }
        if (tokType != null && isConstQualifier(tokType.getType())) {
            assert (false) : "must be skipped above";
            tokType = tokType.getNextSibling();
        }

        if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID && isThisReference) {

            AST nextToken = tokType.getNextSibling();
            while (nextToken != null &&
                    (nextToken.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ||
                    isQualifier(nextToken.getType()) ||
                    nextToken.getType() == CPPTokenTypes.LPAREN)) {
                nextToken = nextToken.getNextSibling();
            }

            if (nextToken == null ||
                    nextToken.getType() == CPPTokenTypes.LSQUARE ||
                    nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.ASSIGNEQUAL) {

                AST ptrOperator = null;
                boolean theOnly = true;
                boolean hasVariables = false;
                int inParamsLevel = 0;

                for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                    switch (token.getType()) {
                        case CPPTokenTypes.LPAREN:
                            inParamsLevel++;
                            break;
                        case CPPTokenTypes.RPAREN:
                            inParamsLevel--;
                            break;
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-st one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if (ptrOperator == null && inParamsLevel == 0) {
                                ptrOperator = token;
                            }
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            hasVariables = true;
                            if (theOnly) {
                                for (AST next = token.getNextSibling(); next != null; next = next.getNextSibling()) {
                                    int type = next.getType();
                                    if (type == CPPTokenTypes.CSM_VARIABLE_DECLARATION || type == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                                        theOnly = false;
                                    }
                                }
                            }
                            processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, namespaceContainer, container2, file, _static, _extern, false);
                            ptrOperator = null;
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                            AST inner = token.getFirstChild();
                            if (inner != null) {
                                theOnly = false;
                                TypeImpl type = null;
                                if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                                    AST typeNameToken = tokType.getFirstChild();
                                    if (typeNameToken != null) {
                                        type = TypeFactory.createBuiltinType(typeNameToken.getText(), ptrOperator, 0, tokType, file);
                                    }
                                } else {
                                    type = TypeFactory.createType(tokType, file, ptrOperator, 0);
                                }
                                if (isVariableLikeFunc(token)) {
                                    CsmScope scope = (namespaceContainer instanceof CsmNamespace) ? (CsmNamespace) namespaceContainer : null;
                                    processFunction(token, file, type, namespaceContainer, container2, scope);
                                } else {
                                    processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, namespaceContainer, container2, file, _static, _extern, false);
                                    ptrOperator = null;
                                }
                            }
                    }
                }
                if (!hasVariables && functionParameter) {
                    // unnamed parameter
                    processVariable(ast, ptrOperator, ast, typeAST/*tokType*/, namespaceContainer, container2, file, _static, _extern, false);
                }
                return true;
            }

            if (functionParameter && nextToken != null &&
                    nextToken.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                processVariable(nextToken, null, ast, typeAST/*tokType*/, namespaceContainer, container2, file, _static, _extern, true);
            }


        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected void processVariable(AST varAst, AST ptrOperator, AST offsetAst, AST classifier,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2,
            FileImpl file, boolean _static, boolean _extern, boolean inFunctionParameters) {
        int arrayDepth = 0;
        String name = "";
        AST qn = null;
        int inParamsLevel = 0;
        for (AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling()) {
            switch (token.getType()) {
                case CPPTokenTypes.LPAREN:
                    inParamsLevel++;
                    break;
                case CPPTokenTypes.RPAREN:
                    inParamsLevel--;
                    break;
                case CPPTokenTypes.LSQUARE:
                    if (inParamsLevel == 0) {
                        arrayDepth++;
                    }
                    break;
                case CPPTokenTypes.LITERAL_struct:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_enum:
                case CPPTokenTypes.LITERAL_class:
                    // skip both this and next
                    token = token.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                    if (inParamsLevel == 0) {
                        qn = token;
                    }
                // no break;
                case CPPTokenTypes.ID:
                    if (inParamsLevel == 0) {
                        name = token.getText();
                    }
                    break;
            }
        }
        CsmType type = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth, null, null, inFunctionParameters);
        if (isScopedId(qn)) {
            if (isRenderingLocalContext()) {
                System.err.println("error in rendering " + file + " offset:" + offsetAst); // NOI18N
            }
            // This is definition of global namespace variable or definition of static class variable
            // TODO What about global variable definitions:
            // extern int i; - declaration
            // int i; - definition
            VariableDefinitionImpl var = new VariableDefinitionImpl(offsetAst, file, type, name);
            var.setStatic(_static);
            if (container2 != null) {
                container2.addDeclaration(var);
            }
        } else {
            VariableImpl var = createVariable(offsetAst, file, type, name, _static, _extern, container1, container2, null);
            if (container2 != null) {
                container2.addDeclaration(var);
            }
            // TODO! don't add to namespace if....
            if (container1 != null) {
                container1.addDeclaration(var);
            }
        }
    }

    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static,  boolean _extern,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
        type = TemplateUtils.checkTemplateType(type, scope);
        VariableImpl var = new VariableImpl(offsetAst, file, type, name, scope, !isRenderingLocalContext(), !isRenderingLocalContext());
        var.setStatic(_static);
        var.setExtern(_extern);
        return var;
    }

    protected void processFunction(AST token, CsmFile file, CsmType type,
             MutableDeclarationsContainer container1,
             MutableDeclarationsContainer container2, CsmScope scope) {
        FunctionImpl fun = createFunction(token, file, type, scope);
        if (fun != null) {
            if (container2 != null) {
                container2.addDeclaration(fun);
            }
            if (container2 != null && NamespaceImpl.isNamespaceScope(fun)) {
                container1.addDeclaration(fun);
            }
        }
    }

    protected FunctionImpl createFunction(AST ast, CsmFile file, CsmType type, CsmScope scope) {
        FunctionImpl fun = null;
        try {
            fun = new FunctionImpl(ast, file, type, scope, !isRenderingLocalContext(), !isRenderingLocalContext());
        } catch (AstRendererException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fun;
    }

    public static List<CsmParameter> renderParameters(AST ast, final CsmFile file, CsmScope scope, boolean isRenderingLocalContext) {
        ArrayList<CsmParameter> parameters = new ArrayList<CsmParameter>();
        if (ast != null && (ast.getType() == CPPTokenTypes.CSM_PARMLIST ||
                ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                if (token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                    List<ParameterImpl> params = renderParameter(token, file, scope, isRenderingLocalContext);
                    if (params != null) {
                        parameters.addAll(params);
                    }
                }
            }
        }
        parameters.trimToSize();
        return parameters;
    }

    public static boolean isVoidParameter(AST ast) {
        if (ast != null && (ast.getType() == CPPTokenTypes.CSM_PARMLIST ||
                ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            AST token = ast.getFirstChild();
            if (token != null && token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                AST firstChild = token.getFirstChild();
                if (firstChild != null) {
                    if (firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null) {
                        AST grandChild = firstChild.getFirstChild();
                        if (grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static List<ParameterImpl> renderParameter(AST ast, final CsmFile file, final CsmScope scope1, final boolean isRenderingLocalContext) {

        // The only reason there might be several declarations is the K&R C style
        // we can split this function into two (for K&R and "normal" parameters)
        // if we found this ineffective; but now I vote for more clear and readable - i.e. single for both cases - code

        final List<ParameterImpl> result = new ArrayList<ParameterImpl>();
        AST firstChild = ast.getFirstChild();
        if (firstChild != null) {
            if (firstChild.getType() == CPPTokenTypes.ELLIPSIS) {
                ParameterEllipsisImpl parameter = new ParameterEllipsisImpl(ast.getFirstChild(), file, null, scope1, !isRenderingLocalContext); // NOI18N
                result.add(parameter);
                return result;
            }
            if (firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null) {
                AST grandChild = firstChild.getFirstChild();
                if (grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void) {
                    return Collections.emptyList();
                }
            }
        }
        class AstRendererEx extends AstRenderer {

            public AstRendererEx() {
                super((FileImpl) file);
            }

            @Override
            protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, boolean _extern, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope2) {
                type = TemplateUtils.checkTemplateType(type, scope1);
                ParameterImpl parameter;
                if (offsetAst.getType() == CPPTokenTypes.ELLIPSIS) {
                    parameter = new ParameterEllipsisImpl(offsetAst, file, type, scope1, !isRenderingLocalContext);
                } else {
                    parameter = new ParameterImpl(offsetAst, file, type, name, scope1, !isRenderingLocalContext);
                }
                result.add(parameter);
                return parameter;
            }
        }
        AstRendererEx renderer = new AstRendererEx();
        renderer.renderVariable(ast, null, null, true);
        return result;
    }

//    public static boolean isCsmType(AST token) {
//        if( token != null ) {
//            int type = token.getType();
//            return type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND;
//        }
//        return false;
//    }
    public static int getType(AST token) {
        return (token == null) ? -1 : token.getType();
    }

    public static int getFirstChildType(AST token) {
        AST child = token.getFirstChild();
        return (child == null) ? -1 : child.getType();
    }

//    public static int getNextSiblingType(AST token) {
//        AST sibling = token.getNextSibling();
//        return (sibling == null) ? -1 : sibling.getType();
//    }
    public boolean renderNSP(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, FileImpl file) {
        token = token.getFirstChild();
        if (token == null) {
            return false;
        }
        switch (token.getType()) {
            case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                NamespaceAliasImpl alias = new NamespaceAliasImpl(token, file, currentNamespace, !isRenderingLocalContext()); 
                container.addDeclaration(alias);
                currentNamespace.addDeclaration(alias);
                return true;
            case CPPTokenTypes.CSM_USING_DIRECTIVE: {
                UsingDirectiveImpl using = new UsingDirectiveImpl(token, file, !isRenderingLocalContext());
                container.addDeclaration(using);
                currentNamespace.addDeclaration(using);
                return true;
            }
            case CPPTokenTypes.CSM_USING_DECLARATION: {
                UsingDeclarationImpl using = new UsingDeclarationImpl(token, file, currentNamespace, !isRenderingLocalContext(), CsmVisibility.PUBLIC);
                container.addDeclaration(using);
                currentNamespace.addDeclaration(using);
                return true;
            }
        }
        return false;
    }

    private boolean isClassSpecialization(AST ast) {
        AST type = ast.getFirstChild(); // type
        if (type != null) {
            AST child = type;
            while ((child = child.getNextSibling()) != null) {
                if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                    child = child.getNextSibling();
                    if (child != null && (child.getType() == CPPTokenTypes.LITERAL_class ||
                            child.getType() == CPPTokenTypes.LITERAL_struct)) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean isMemberDefinition(AST ast) {
        if (CastUtils.isCast(ast)) {
            return CastUtils.isMemberDefinition(ast);
        }
        AST id = AstUtil.findMethodName(ast);
        return isScopedId(id);
    }

    private boolean isScopedId(AST id) {
        if (id == null) {
            return false;
        }
        if (id.getType() == CPPTokenTypes.ID) {
            AST scope = id.getNextSibling();
            scope = skipTemplateParameters(scope);
            if (scope != null && scope.getType() == CPPTokenTypes.SCOPE) {
                return true;
            }
        } else if (id.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            int i = 0;
            AST q = id.getFirstChild();
            while (q != null) {
                if (q.getType() == CPPTokenTypes.SCOPE) {
                    return true;
                }
                q = q.getNextSibling();
            }
        }
        return false;
    }

    public static CsmCompoundStatement findCompoundStatement(AST ast, CsmFile file, CsmFunction owner) {
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            switch (token.getType()) {
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                    return new CompoundStatementImpl(token, file, owner);
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
                    return new LazyCompoundStatementImpl(token, file, owner);
                case CPPTokenTypes.CSM_TRY_CATCH_STATEMENT_LAZY:
                    return new LazyTryCatchStatementImpl(token, file, owner);
            }
        }
        // prevent null bodies
        return new EmptyCompoundStatementImpl(ast, file, owner);
    }

    public static StatementBase renderStatement(AST ast, CsmFile file, CsmScope scope) {
        switch (ast.getType()) {
            case CPPTokenTypes.CSM_LABELED_STATEMENT:
                return new LabelImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CASE_STATEMENT:
                return new CaseStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_DEFAULT_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.DEFAULT, scope);
            case CPPTokenTypes.CSM_EXPRESSION_STATEMENT:
                return new ExpressionStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CLASS_DECLARATION:
            case CPPTokenTypes.CSM_ENUM_DECLARATION:
            case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
            case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                if(new AstRenderer((FileImpl) file).isExpressionLikeDeclaration(ast, scope)) {
                    return new ExpressionStatementImpl(ast, file, scope);
                } else {
                    return new DeclarationStatementImpl(ast, file, scope);
                }
            case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                return new CompoundStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_IF_STATEMENT:
                return new IfStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_SWITCH_STATEMENT:
                return new SwitchStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, false, scope);
            case CPPTokenTypes.CSM_DO_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, true, scope);
            case CPPTokenTypes.CSM_FOR_STATEMENT:
                return new ForStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_GOTO_STATEMENT:
                return new GotoStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CONTINUE_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.CONTINUE, scope);
            case CPPTokenTypes.CSM_BREAK_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.BREAK, scope);
            case CPPTokenTypes.CSM_RETURN_STATEMENT:
                return new ReturnStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_TRY_STATEMENT:
                return new TryCatchStatementImpl(ast, file, scope, false);
            case CPPTokenTypes.CSM_CATCH_CLAUSE:
                // TODO: isn't it in TryCatch ??
                return new UniversalStatement(ast, file, CsmStatement.Kind.CATCH, scope);
            case CPPTokenTypes.CSM_THROW_STATEMENT:
                // TODO: throw
                return new UniversalStatement(ast, file, CsmStatement.Kind.THROW, scope);
            case CPPTokenTypes.CSM_ASM_BLOCK:
                // just ignore
                break;
//            case CPPTokenTypes.SEMICOLON:
//            case CPPTokenTypes.LCURLY:
//            case CPPTokenTypes.RCURLY:
//                break;
//            default:
//                System.out.println("unexpected statement kind="+ast.getType());
//                break;
        }
        return null;
    }

    /**
     * Parser don't use a symbol table, so constructs like
     * a & b;
     * are parsed as if they were declarations.
     * At the moment of rendering, we check whether this is a expression or a declaration
     * @return true if it's a expression, otherwise false (it's a declaration)
     */
    private boolean isExpressionLikeDeclaration(AST ast, CsmScope scope) {
        AST type = ast.getFirstChild();
        if (type != null && type.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
            AST name = type.getFirstChild();
            if (name != null) {
                if (isVariableOrFunctionName(name, false)) {
                    if (isVariableOrFunctionName(name, true)) {
                        return true;
                    }
                    if (isLocalVariableOrFunction(name.getText(), scope)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks local statements for function or variable declaration with name
     */
    private boolean isLocalVariableOrFunction(CharSequence name, CsmScope scope) {
        while(CsmKindUtilities.isStatement(scope)) {
            scope = ((CsmStatement) scope).getScope();
        }
        if(CsmKindUtilities.isFunction(scope)) {
            CsmFunction fun = (CsmFunction) scope;
            for (CsmParameter param : fun.getParameters()) {
                if (param.getQualifiedName().toString().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public ExpressionBase renderExpression(AST ast, CsmScope scope) {
        return isExpression(ast) ? new ExpressionBase(ast, file, null, scope) : null;
    }

    public CsmCondition renderCondition(AST ast, CsmScope scope) {
        if (ast != null && ast.getType() == CPPTokenTypes.CSM_CONDITION) {
            AST first = getFirstChildSkipQualifiers(ast);
            if (first != null) {
                int type = first.getType();
                if (isExpression(type)) {
                    return new ConditionExpressionImpl(first, file, scope);
                } else if (type == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        type == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                        type == CPPTokenTypes.LITERAL_struct ||
                        type == CPPTokenTypes.LITERAL_class ||
                        type == CPPTokenTypes.LITERAL_union) {
                    return new ConditionDeclarationImpl(ast, file, scope);
                }
            }
        }
        return null;
    }

    public static List<CsmExpression> renderConstructorInitializersList(AST ast, CsmScope scope, CsmFile file) {
        ArrayList<CsmExpression> initializers = null;
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER_LIST) {
                for (AST initializerToken = token.getFirstChild(); initializerToken != null; initializerToken = initializerToken.getNextSibling()) {
                    if (initializerToken.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER) {
                        ExpressionBase initializer = new ExpressionBase(initializerToken, file, null, scope);
                        if (initializers == null) {
                            initializers = new ArrayList<CsmExpression>();
                        }
                        initializers.add(initializer);
                    }
                }
            }
        }
        if (initializers != null) {
            initializers.trimToSize();
        }
        return initializers;
    }

    public static boolean isExpression(AST ast) {
        return ast != null && isExpression(ast.getType());
    }

    public static boolean isExpression(int tokenType) {
        return CPPTokenTypes.CSM_EXPRESSIONS_START < tokenType &&
                tokenType < CPPTokenTypes.CSM_EXPRESSIONS_END;
    }

    public static boolean isStatement(AST ast) {
        return ast != null && isStatement(ast.getType());
    }

    public static boolean isStatement(int tokenType) {
        return CPPTokenTypes.CSM_STATEMENTS_START < tokenType &&
                tokenType < CPPTokenTypes.CSM_STATEMENTS_END;
    }

    public static AST skipTemplateSibling(AST template) {
        assert template.getType() == CPPTokenTypes.LITERAL_template;
        AST next = template.getNextSibling();
        if (template.getFirstChild() != null) {
            // this is template node
            return next;
        } else {
            // this is plain template literal
            int balance = 0;
            while (next != null) {
                switch (next.getType()) {
                    case CPPTokenTypes.LESSTHAN:
                        balance++;
                        break;
                    case CPPTokenTypes.GREATERTHAN:
                        --balance;
                        if (balance == 0) {
                            return next.getNextSibling();
                        } else if (balance < 0) {
                            return null;
                        }
                        break;
                }
                next = next.getNextSibling();
            }
        }
        return null;
    }
//    public ExpressionBase renderExpression(ExpressionBase parent) {
//        
//    }
}
    
