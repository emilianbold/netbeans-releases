/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver3;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.ExpressionEvaluator;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;

/**
 *
 * @author nk220367
 */
public class VariableProvider {

    public static final int INFINITE_RECURSION = 10;

    private final int level;
    private CsmOffsetableDeclaration decl;
    private Map<CsmTemplateParameter, CsmSpecializationParameter> mapping;
    private CsmFile variableFile; 
    private int variableStartOffset;
    private int variableEndOffset;
    
    
    public VariableProvider(int level) {
        this.level = level;
    }

    public VariableProvider(CsmOffsetableDeclaration decl, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, int level) {
        this(decl, mapping, null, 0, 0, level);
    }
    
    public VariableProvider(CsmOffsetableDeclaration decl, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile variableFile, int variableStartOffset, int variableEndOffset, int level) {
        this.decl = decl;
        this.mapping = mapping;
        this.variableFile = variableFile != null ? variableFile : (decl != null ? decl.getContainingFile() : null);
        this.variableStartOffset = variableStartOffset;
        this.variableEndOffset = variableEndOffset;
        this.level = level;
    }

    public int getValue(String variableName) {
        if(level > INFINITE_RECURSION) {
            return 0;
        }
        if(variableName.equals("true")) { // NOI18N
            return 1;
        }
        if(variableName.equals("false")) { // NOI18N
            return 0;
        }
        if (decl != null) {
            for (CsmTemplateParameter param : mapping.keySet()) {
                if (variableName.equals(param.getQualifiedName().toString()) ||
                        (decl.getQualifiedName() + "::" + variableName).equals(param.getQualifiedName().toString())) { // NOI18N
                    CsmSpecializationParameter spec = mapping.get(param);
                    if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
                        Object eval = new ExpressionEvaluator(level+1).eval(((CsmExpressionBasedSpecializationParameter) spec).getText().toString(), decl, mapping);
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }
                    } else if (CsmKindUtilities.isTypeBasedSpecalizationParameter(spec)) {
                        CsmTypeBasedSpecializationParameter specParameter = (CsmTypeBasedSpecializationParameter) spec;
                        Object eval = new ExpressionEvaluator(level+1).eval(specParameter.getText().toString(), decl, specParameter.getContainingFile(), specParameter.getStartOffset(), specParameter.getEndOffset(), mapping);
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }
                    }
                }
            }
            if(CsmKindUtilities.isClass(decl)) {
                final CsmClass clazz = (CsmClass) decl;
                MemberResolverImpl r = new MemberResolverImpl();
                final Iterator<CsmMember> classMembers = r.getDeclarations(clazz, variableName);
                if (classMembers.hasNext()) {
                    CsmMember member = classMembers.next();
                    if(member.isStatic() && CsmKindUtilities.isField(member) && member.getName().toString().equals(variableName)) {
                        if(CsmKindUtilities.isInstantiation(member)) {
                            Object eval = new ExpressionEvaluator(level+1).eval(((CsmField)member).getInitialValue().getText().toString(), member.getContainingClass(), getMapping((CsmInstantiation) member));
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }
                        } else if (((CsmField)member).getInitialValue() != null) {
                            Object eval = new ExpressionEvaluator(level+1).eval(((CsmField)member).getInitialValue().getText().toString(), member.getContainingClass(), Collections.<CsmTemplateParameter, CsmSpecializationParameter>emptyMap());
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }                            
                        }
                    }
                }
            }
            
            boolean executeSimpleResolution = !(TraceFlags.EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER && variableName.contains("<")); // NOI18N
            
            if (executeSimpleResolution) {
                CsmObject o = null;
                Resolver aResolver = ResolverFactory.createResolver(decl);            
                try {
                    o = aResolver.resolve(Utils.splitQualifiedName(variableName.replaceAll("(.*)::.*", "$1")), Resolver3.ALL); // NOI18N
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
                if (CsmKindUtilities.isClassifier(o)) {
                    CsmClassifier cls = (CsmClassifier) o;
                    CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(cls, decl.getContainingFile());
                    if(CsmKindUtilities.isInstantiation(originalClassifier)) {
                        Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier); // NOI18N
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }
                    } else if (CsmKindUtilities.isOffsetableDeclaration(originalClassifier)) {
                        Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmOffsetableDeclaration) originalClassifier, Collections.<CsmTemplateParameter, CsmSpecializationParameter>emptyMap()); // NOI18N
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }                    
                    }
                }
            }
            
            if (TraceFlags.EXPRESSION_EVALUATOR_DEEP_VARIABLE_PROVIDER) {
                // it works but does it too slow

                int flags = CPPParserEx.CPP_CPLUSPLUS;
                flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
                try {
                    // use cached TS
                    TokenStream buildTokenStream = APTTokenStreamBuilder.buildTokenStream(variableName.replaceAll("(.*)::.*", "$1"), APTLanguageSupport.GNU_CPP); // NOI18N
                    if (buildTokenStream != null) {
                        if (variableStartOffset > 0) {
                            buildTokenStream = new ShiftedTokenStream(buildTokenStream, variableStartOffset);
                        }
                        
                        CPPParserEx parser = CPPParserEx.getInstance(variableFile, buildTokenStream, flags);
                        parser.type_name();
                        AST ast = parser.getAST();
                
                        CsmType type = TypeFactory.createType(ast, variableFile, null, 0, decl.getScope()); // TODO: decl.getScope() is a wrong scope
                        if(CsmKindUtilities.isInstantiation(decl)) {
                            type = checkTemplateType(type, (Instantiation)decl);
                        }
                        for (CsmTemplateParameter csmTemplateParameter : mapping.keySet()) {
                            type = TemplateUtils.checkTemplateType(type, csmTemplateParameter.getScope());
                        }

                        if (CsmKindUtilities.isTemplateParameterType(type)) {
                            CsmSpecializationParameter instantiatedType = mapping.get(((CsmTemplateParameterType) type).getParameter());
                            int iteration = 15;
                            while (CsmKindUtilities.isTypeBasedSpecalizationParameter(instantiatedType) &&
                                    CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instantiatedType).getType()) && iteration != 0) {
                                CsmSpecializationParameter nextInstantiatedType = mapping.get(((CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instantiatedType).getType()).getParameter());
                                if (nextInstantiatedType != null) {
                                    instantiatedType = nextInstantiatedType;
                                } else {
                                    break;
                                }
                                iteration--;
                            }
                            if (instantiatedType != null && instantiatedType instanceof CsmTypeBasedSpecializationParameter) {
                                type = ((CsmTypeBasedSpecializationParameter) instantiatedType).getType();
                            }
                        }

                        if(CsmKindUtilities.isInstantiation(decl)) {
                            type = Instantiation.createType(type, (Instantiation)decl);
                        }

                        CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(type.getClassifier(), decl.getContainingFile());
                        if (CsmKindUtilities.isTemplate(originalClassifier)) {
                            CsmObject instantiate = ((InstantiationProviderImpl)InstantiationProviderImpl.getDefault()).instantiate((CsmTemplate) originalClassifier, mapping);
                            if (CsmKindUtilities.isClassifier(instantiate)) {
                                originalClassifier = (CsmClassifier) instantiate;
                            }
                        }
                        if(CsmKindUtilities.isInstantiation(originalClassifier)) {
                            Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier); // NOI18N
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }
                        }

                    }
                } catch (Throwable ex) {
                }
                
            }
        }

        return Integer.MAX_VALUE;
    }

    private CsmType checkTemplateType(CsmType type, CsmInstantiation inst) {
        for (CsmTemplateParameter csmTemplateParameter : inst.getMapping().keySet()) {
            type = TemplateUtils.checkTemplateType(type, csmTemplateParameter.getScope());
        }
        if(CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            type = checkTemplateType(type, (Instantiation)inst.getTemplateDeclaration());
        }
        return type;
    }

    private Map<CsmTemplateParameter, CsmSpecializationParameter> getMapping(CsmInstantiation inst) {
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping2 = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
        mapping2.putAll(inst.getMapping());
        if(CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            mapping2.putAll(getMapping((CsmInstantiation) inst.getTemplateDeclaration()));
        }
        return mapping2;
    }
}
