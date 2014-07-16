/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmOverloadingResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.spi.model.services.CsmEntityResolverImplementation;
import org.openide.util.CharSequences;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmEntityResolverImplementation.class)
public class CsmEntityResolverImpl implements CsmEntityResolverImplementation {

    private static final String LT = "<"; // NOI18N
    
    private static final String GT = ">"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(VariableProvider.class.getSimpleName());

    @Override
    public Collection<CsmObject> resolveEntity(NativeProject project, CharSequence declText) {
        CsmProject cndProject = CsmModelAccessor.getModel().getProject(project);
        if (cndProject != null) {
            cndProject.waitParse();
            return resolveEntity(cndProject, declText);
        } 
        return Collections.emptyList();
    }

    @Override
    public Collection<CsmObject> resolveEntity(CsmProject project, CharSequence declText) {
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        
        try {
            TokenStream buildTokenStream = APTTokenStreamBuilder.buildTokenStream(declText.toString(), APTLanguageSupport.GNU_CPP); // NOI18N
            if (buildTokenStream != null) {
                APTLanguageFilter langFilter = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP, APTLanguageSupport.FLAVOR_UNKNOWN);
                CPPParserEx parser = CPPParserEx.getInstance("In memory parse", langFilter.getFilteredStream(buildTokenStream), flags); // NOI18N
                parser.external_declaration(); // TODO: too wide
                AST ast = parser.getAST();                
                if (ast != null) {
//                    StringBuilder sb = new StringBuilder();
//                    printAST(sb, ast, 0);    
//                    System.out.println(sb.toString());
                    switch (ast.getType()) {
                        case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                        case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                        case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_DECLARATION: {
                            AST funNameAst = AstUtil.findMethodName(ast);
                            if (funNameAst != null) {
//                                CharSequence qualifiedName = funNameAst.getText();
                                CharSequence qualifiedName[] = AstRenderer.renderQualifiedId(funNameAst, null, true);
                                
                                List<CsmObject> resolvedContext = new ArrayList<>();
                                resolveContext(project, qualifiedName, resolvedContext);

                                List<CsmFunction> candidates = new ArrayList<>();
                                CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                                         CsmSelect.getFilterBuilder().createKindFilter(
                                             CsmDeclaration.Kind.FUNCTION,
                                             CsmDeclaration.Kind.FUNCTION_DEFINITION,
                                             CsmDeclaration.Kind.FUNCTION_INSTANTIATION
                                         ),
                                         CsmSelect.getFilterBuilder().createNameFilter(
                                             hasTemplateSuffix(qualifiedName[qualifiedName.length - 1]) ? trimTemplateSuffix(qualifiedName[qualifiedName.length - 1]) : qualifiedName[qualifiedName.length - 1], 
                                             true, 
                                             true, 
                                             false
                                         )
                                );                                           
                                for (CsmObject context : resolvedContext) {
                                    if (CsmKindUtilities.isNamespace(context)) {
                                        CsmNamespace ns = (CsmNamespace) context;
                                        Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(ns, filter);
                                        fillFromDecls((List<CsmObject>) (Object) candidates, iter);
                                    } else if (CsmKindUtilities.isClass(context)) {
                                        CsmClass cls = (CsmClass) context;
                                        fillFromDecls((List<CsmObject>) (Object) candidates, CsmSelect.getClassMembers(cls, filter));
                                    }
                                }
                                //Iterator<CsmFunction> funIter = CsmSelect.getFunctions(project, concat(qualifiedName, APTUtils.SCOPE));
                                return fillFromDecls(filterFunctions(ast, funNameAst, candidates.iterator()));
                            }                            
                            break;                            
                        }
                        
                        case CPPTokenTypes.CSM_GENERIC_DECLARATION: {
                            AST qualNameNode = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_TYPE_COMPOUND);
                            if (qualNameNode != null && qualNameNode.getNextSibling() == null) {
                                CharSequence qualifiedId[] = AstRenderer.renderQualifiedId(qualNameNode, null, true);
                                
                                List<CsmObject> resolvedContext = new ArrayList<>();
                                resolveContext(project, qualifiedId, resolvedContext);
                                
                                List<CsmObject> candidates = new ArrayList<>();
                                CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                                         CsmSelect.getFilterBuilder().createKindFilter(
                                             CsmDeclaration.Kind.VARIABLE,
                                             CsmDeclaration.Kind.FUNCTION,
                                             CsmDeclaration.Kind.FUNCTION_DEFINITION,
                                             CsmDeclaration.Kind.FUNCTION_INSTANTIATION,
                                             CsmDeclaration.Kind.CLASS,
                                             CsmDeclaration.Kind.STRUCT,
                                             CsmDeclaration.Kind.TYPEDEF,
                                             CsmDeclaration.Kind.TYPEALIAS                                             
                                         ),
                                         CsmSelect.getFilterBuilder().createNameFilter(qualifiedId[qualifiedId.length - 1], true, true, false)
                                );                                
                                for (CsmObject context : resolvedContext) {
                                    if (CsmKindUtilities.isNamespace(context)) {
                                        CsmNamespace ns = (CsmNamespace) context;
                                        fillFromDecls(candidates, CsmSelect.getDeclarations(ns, filter));
                                    } else if (CsmKindUtilities.isClass(context)) {
                                        CsmClass cls = (CsmClass) context;
                                        fillFromDecls(candidates, CsmSelect.getClassMembers(cls, filter));
                                    }
                                }
                                return candidates;
                            }
                            break;
                        }                            
                    }
                }                             
            }
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        }        
        
        return Collections.emptyList();
    }
    
    private Collection<CsmFunction> filterFunctions(AST funAst, AST funNameAst, Iterator<CsmFunction> candidates) {
        if (candidates.hasNext()) {
            Collection<AST> paramsAsts = getFunctionParamsAsts(funAst, funNameAst);    
            List<CsmFunction> filteredByParamNumber = filterFunctionsByParamNumber(paramsAsts, candidates);
            return filterFunctionsByParamTypes(paramsAsts, filteredByParamNumber);
        }        
        return Collections.emptyList();
    }
    
    private List<CsmFunction> filterFunctionsByParamNumber(Collection<AST> paramsAsts, Iterator<CsmFunction> candidates) {
        List<CsmFunction> filteredByParamNumber = new ArrayList<>();
        while (candidates.hasNext()) {
            CsmFunction candidate = candidates.next();
            if (candidate.getParameters().size() == paramsAsts.size()) {
                filteredByParamNumber.add(candidate);
            }
        }        
        return filteredByParamNumber;
    }
    
    private Collection<CsmFunction> filterFunctionsByParamTypes(Collection<AST> paramsAsts, Collection<CsmFunction> candidates) {       
        Map<CsmFunction, List<CsmType>> paramsPerFunction = new IdentityHashMap<>();
        for (CsmFunction candidate : candidates) {
            List<CsmType> parameters = createFunctionParams(paramsAsts, candidate);
            paramsPerFunction.put(candidate, parameters);
        }            
        return CsmOverloadingResolver.resolveOverloading(candidates, null, paramsPerFunction);
    }
    
    private Collection<AST> getFunctionParamsAsts(AST targetFunAst, AST targetFunNameAst) {
        AST lparen = AstUtil.findSiblingOfType(targetFunNameAst, CPPTokenTypes.LPAREN);
        AST rparen = AstUtil.findSiblingOfType(lparen, CPPTokenTypes.RPAREN);
        AST params = lparen;
        while (params != rparen && !(CPPTokenTypes.CSM_PARMLIST == params.getType())) {
            params = params.getNextSibling();
        }
        if (CPPTokenTypes.CSM_PARMLIST == params.getType()) {
            List<AST> parameters = new ArrayList<>();
            AST paramAst = params.getFirstChild();
            while (paramAst != null) {
                if (CPPTokenTypes.CSM_PARAMETER_DECLARATION == paramAst.getType()) {
                    parameters.add(paramAst);
                }
                paramAst = paramAst.getNextSibling();
            }
            return parameters;
        }        
        return Collections.emptyList();
    }
    
    private List<CsmType> createFunctionParams(Collection<AST> targetFunParamsAsts, CsmFunction context) {
        List<CsmType> params = new ArrayList<>();
        for (AST paramAst : targetFunParamsAsts) {
            if (paramAst != null) {
                AST paramTypeAst = AstUtil.findTypeNode(paramAst);
                if (paramTypeAst != null) {
                    AST ptrOperator = AstUtil.findSiblingOfType(paramTypeAst, CPPTokenTypes.CSM_PTR_OPERATOR);
                    // TODO: AST has wrong offsets here!
                    CsmType type = TypeFactory.createType(paramTypeAst, context.getContainingFile(), ptrOperator, 0, context.getScope());
                    params.add(type);
                }
            }
        }
        return params;
    }
    
    private void resolveContext(CsmProject project, CharSequence qualifiedName[], Collection<CsmObject> result) {   
        CharSequence[] cnn = qualifiedName;
        if (cnn != null) {
            if (cnn.length > 1) {
                resolveContext(project.getGlobalNamespace(), qualifiedName, 0, result);
            } else if (cnn.length == 1) {
                result.add(project.getGlobalNamespace());
            }
        }
    }
    
    private void resolveContext(CsmNamespace context, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        CharSequence[] cnn = qualifiedName;
        if (current >= cnn.length - 1) {
            result.add(context);
            return;
        }        
        CsmSelect.CsmFilter filter = createNamespaceFilter(qualifiedName[current]);        
        Iterator<CsmOffsetableDeclaration> decls = CsmSelect.getDeclarations(context, filter);
        if (!decls.hasNext() && hasTemplateSuffix(qualifiedName[current])) {
            filter = createNamespaceFilter(trimTemplateSuffix(qualifiedName[current]));
            decls = CsmSelect.getDeclarations(context, filter);
        }
        
        handleNamespaceDecls(decls, cnn, current, result);
        
        if (!hasTemplateSuffix(qualifiedName[current])) {            
            Set<CsmNamespace> handledNamespaces = new HashSet<>();
            for (CsmNamespace nested : context.getNestedNamespaces()) {
                if (!handledNamespaces.contains(nested)) {
                    handledNamespaces.add(nested);
                    if (qualifiedName[current].toString().equals(nested.getName().toString())) {
                        resolveContext(nested, qualifiedName, current + 1, result);
                    }
                }
            }
        }
    }
    
    private void resolveContext(CsmClass context, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        CharSequence[] cnn = qualifiedName;
        if (current >= cnn.length - 1) {
            result.add(context);
            return;
        }
        CsmSelect.CsmFilter filter = createClassFilter(qualifiedName[current]);
        Iterator<CsmMember> decls = CsmSelect.getClassMembers(context, filter);
        if (!decls.hasNext() && hasTemplateSuffix(qualifiedName[current])) {
            filter = createClassFilter(trimTemplateSuffix(qualifiedName[current]));
            decls = CsmSelect.getClassMembers(context, filter);
        }
        handleClassDecls(decls, cnn, current, result);    
    }    
    
    private void handleNamespaceDecls(Iterator<CsmOffsetableDeclaration> decls, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        Set<CsmNamespace> handledNamespaces = new HashSet<>();
        while (decls.hasNext()) {
            CsmOffsetableDeclaration decl = decls.next();
            if (CsmKindUtilities.isNamespaceAlias(decl)) {
                CsmNamespace ns = ((CsmNamespaceAlias) decl).getReferencedNamespace();
                if (!handledNamespaces.contains(ns)) {
                    handledNamespaces.add(ns);
                    resolveContext(ns, qualifiedName, current + 1, result);
                }                
            } else if (CsmKindUtilities.isClass(decl)) {
                resolveContext((CsmClass) decl, qualifiedName, current + 1, result);
            } else if (CsmKindUtilities.isTypedefOrTypeAlias(decl)) {
                CsmTypedef typedef = (CsmTypedef) decl;
                CsmClassifier cls = CsmBaseUtilities.getOriginalClassifier(typedef, typedef.getContainingFile());
                if (CsmKindUtilities.isClass(cls)) {
                    resolveContext((CsmClass) cls, qualifiedName, current + 1, result);
                }
            }
        }
    }
    
    private void handleClassDecls(Iterator<CsmMember> decls, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        while (decls.hasNext()) {
            CsmMember member = decls.next();
            if (CsmKindUtilities.isClass(member)) {
                resolveContext((CsmClass) member, qualifiedName, current + 1, result);
            } else if (CsmKindUtilities.isTypedefOrTypeAlias(member)) {
                CsmTypedef typedef = (CsmTypedef) member;
                CsmClassifier cls = CsmBaseUtilities.getOriginalClassifier(typedef, typedef.getContainingFile());
                if (CsmKindUtilities.isClass(cls)) {
                    resolveContext((CsmClass) cls, qualifiedName, current + 1, result);
                }
            }
        }        
    }
    
    private List<CsmObject> fillFromDecls(Iterable<? extends CsmObject> decls) {
        return fillFromDecls(decls.iterator());
    }
    
    private List<CsmObject> fillFromDecls(Iterator<? extends CsmObject> decls) {
        List<CsmObject> result = new ArrayList<>();
        while (decls.hasNext()) {
            result.add(decls.next());
        }
        return result;
    }
    
    private void fillFromDecls(List<CsmObject> list, Iterable<? extends CsmObject> decls) {
        fillFromDecls(list, decls.iterator());
    }    
    
    private void fillFromDecls(List<CsmObject> list, Iterator<? extends CsmObject> decls) {
        while (decls.hasNext()) {
            list.add(decls.next());
        }
    }
    
    private static String concat(CharSequence charSequences[], CharSequence separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CharSequence cs : charSequences) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(cs);
        }
        return sb.toString();
    }
    
    private boolean hasTemplateSuffix(CharSequence qualNamePart) {
        return CharSequences.indexOf(qualNamePart, GT) > CharSequences.indexOf(qualNamePart, LT);
    }
    
    private CharSequence trimTemplateSuffix(CharSequence qualNamePart) {
        return qualNamePart.subSequence(0, CharSequences.indexOf(qualNamePart, LT));
    }
    
    private CsmSelect.CsmFilter createNamespaceFilter(CharSequence qualNamePart) {
        return CsmSelect.getFilterBuilder().createCompoundFilter(
                 CsmSelect.getFilterBuilder().createKindFilter(
                     CsmDeclaration.Kind.CLASS, 
                     CsmDeclaration.Kind.STRUCT,
                     CsmDeclaration.Kind.TYPEDEF,
                     CsmDeclaration.Kind.TYPEALIAS,
                     CsmDeclaration.Kind.NAMESPACE_ALIAS
                 ),
                 CsmSelect.getFilterBuilder().createNameFilter(qualNamePart, true, true, false)
        );
    }
    
    private CsmSelect.CsmFilter createClassFilter(CharSequence qualNamePart) {
        return CsmSelect.getFilterBuilder().createCompoundFilter(
                 CsmSelect.getFilterBuilder().createKindFilter(
                     CsmDeclaration.Kind.CLASS,
                     CsmDeclaration.Kind.STRUCT,
                     CsmDeclaration.Kind.TYPEDEF,
                     CsmDeclaration.Kind.TYPEALIAS                    
                 ),
                 CsmSelect.getFilterBuilder().createNameFilter(qualNamePart, true, true, false)
        );
    }    
    
    private static void printAST(StringBuilder sb, AST ast, int level) {
        if (ast != null) {
            repeat(sb, ' ', level * 4);
            sb.append(ast.getText()).append('\n');
            printAST(sb, ast.getFirstChild(), level + 1);
            printAST(sb, ast.getNextSibling(), level);
        }
    }
    
    private static void repeat(StringBuilder sb, char character, int times) {
        while (--times >= 0) {
            sb.append(character);
        }
    }
}
