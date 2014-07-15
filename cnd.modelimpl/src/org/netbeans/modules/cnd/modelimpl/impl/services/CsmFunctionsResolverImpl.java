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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmFunctionsResolverImplementation;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmFunctionsResolverImplementation.class)
public class CsmFunctionsResolverImpl implements CsmFunctionsResolverImplementation {
    
    private static final Logger LOG = Logger.getLogger(VariableProvider.class.getSimpleName());
    
    private final ModelImpl model = createModel();

    @Override
    public Collection<CsmFunction> resolveFunction(NativeProject project, CharSequence signature) {
        CsmProject cndProject = model.addProject(project, project.getProjectDisplayName(), true);
        return resolveFunction(cndProject, signature);
    }

    @Override
    public Collection<CsmFunction> resolveFunction(CsmProject project, CharSequence signature) {
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        
//        String signatures[] = new String[]{
//            "int xxx::foo()", 
//            "AAA<int> xxx<int>::foo()", 
//            "int xxx<int>::foo<int>()",
//            "int (*foo())(int a)"
//        };
        
        try {
            // use cached TS
            TokenStream buildTokenStream = APTTokenStreamBuilder.buildTokenStream(signature.toString(), APTLanguageSupport.GNU_CPP); // NOI18N
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
                        case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_DECLARATION: {
                            AST funName = AstUtil.findMethodName(ast);
                            if (funName != null) {
                                CharSequence qualifiedName = funName.getText();
                                Iterator<CsmFunction> funIter = CsmSelect.getFunctions(project, qualifiedName);
                                return filterFunctions(ast, funName, funIter);
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
    
    private List<CsmFunction> filterFunctions(AST funAst, AST funName, Iterator<CsmFunction> candidates) {
        if (candidates.hasNext()) {
            Collection<AST> paramsAsts = getFunctionParamsAsts(funAst, funName);    
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
    
    private List<CsmFunction> filterFunctionsByParamTypes(Collection<AST> paramsAsts, Iterable<CsmFunction> candidates) {
        List<CsmFunction> filteredByParams = new ArrayList<>();                            
        int bestMatch = 0;
        for (CsmFunction candidate : candidates) {
            int match = 0;
            Iterator<CsmParameter> paramIter = candidate.getParameters().iterator();
            Collection<CsmType> parameters = createFunctionParams(paramsAsts, candidate);
            for (CsmType paramType : parameters) {
                CsmParameter candidateParam = paramIter.next();
                if (CsmUtilities.checkTypesEqual(candidateParam.getType(), candidate.getContainingFile(), paramType, candidate.getContainingFile(), false)) {
                    ++match;
                }
            }
            if (match > bestMatch) {
                bestMatch = match;
                filteredByParams.clear();
            }
            if (match == bestMatch) {
                filteredByParams.add(candidate);
            }
        }            
        return filteredByParams;        
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
    
    private Collection<CsmType> createFunctionParams(Collection<AST> targetFunParamsAsts, CsmFunction context) {
        List<CsmType> params = new ArrayList<>();
        for (AST paramAst : targetFunParamsAsts) {
            if (paramAst != null) {
                AST paramTypeAst = AstUtil.findChildOfType(paramAst, CPPTokenTypes.CSM_TYPE_BUILTIN);
                if (paramTypeAst == null) {
                    paramTypeAst = AstUtil.findChildOfType(paramAst, CPPTokenTypes.CSM_TYPE_COMPOUND);
                    if (paramTypeAst == null) {
                        paramTypeAst = AstUtil.findChildOfType(paramAst, CPPTokenTypes.CSM_TYPE_DECLTYPE);
                    }
                }
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
    
    private static ModelImpl createModel() {
        ModelImpl m = (ModelImpl) CsmModelAccessor.getModel(); // new ModelImpl(true);
        if (m == null) {
            m = new ModelImpl();
        }
        return m;
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
