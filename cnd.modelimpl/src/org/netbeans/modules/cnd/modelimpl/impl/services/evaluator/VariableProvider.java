/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver3;
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
    CsmOffsetableDeclaration decl;
    Map<CsmTemplateParameter, CsmSpecializationParameter> mapping;

    public VariableProvider(int level) {
        this.level = level;
    }

    public VariableProvider(CsmOffsetableDeclaration decl, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, int level) {
        this.decl = decl;
        this.mapping = mapping;
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
                        Object eval = new ExpressionEvaluator(level+1).eval(((CsmTypeBasedSpecializationParameter) spec).getText().toString(), decl, mapping);
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }
                    }
                }
            }
            if(CsmKindUtilities.isClass(decl)) {
                final CsmClass clazz = (CsmClass) decl;
                MemberResolverImpl r = new MemberResolverImpl(null);
                final Iterator<CsmMember> classMembers = r.getDeclarations(clazz, variableName);
                if (classMembers.hasNext()) {
                    CsmMember member = classMembers.next();
                    if(member.isStatic() && CsmKindUtilities.isField(member) && member.getName().toString().equals(variableName)) {
                        if(CsmKindUtilities.isInstantiation(member)) {
                            Object eval = new ExpressionEvaluator(level+1).eval(((CsmField)member).getInitialValue().getText().toString(), member.getContainingClass(), getMapping((CsmInstantiation) member));
                            if (eval instanceof Integer) {
                                return (Integer) eval;
                            }
                        }
                    }
                }
            }
            // it works but does it too slow
//            Resolver3 r = new Resolver3(decl.getContainingFile(), decl.getStartOffset(), null);
//            CsmObject o = r.resolve(variableName.replaceAll("(.*)::.*", "$1"), Resolver3.ALL); // NOI18N
//            if (CsmKindUtilities.isClassifier(o)) {
//                CsmClassifier cls = (CsmClassifier) o;
//                CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(cls, decl.getContainingFile());
//                if(CsmKindUtilities.isInstantiation(originalClassifier)) {
//                    Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier); // NOI18N
//                    if (eval instanceof Integer) {
//                        return (Integer) eval;
//                    }
//                }
//            }
//            {
//                TokenStream buildTokenStream = APTTokenStreamBuilder.buildTokenStream(variableName.replaceAll("(.*)::.*", "$1")); // NOI18N
//
//                CPPParserEx parser = CPPParserEx.getInstance(decl.getContainingFile().getName().toString(), buildTokenStream, 0);
//                parser.type_name();
//                AST ast = parser.getAST();
//
//
//                CsmType type = TypeFactory.createType(ast, decl.getContainingFile(), null, 0, decl.getScope());
//                if(CsmKindUtilities.isInstantiation(decl)) {
//                    type = checkTemplateType(type, (Instantiation)decl);
//                }
//
//                if(CsmKindUtilities.isInstantiation(decl)) {
//                    type = Instantiation.createType(type, (Instantiation)decl);
//                }
//                CsmClassifier originalClassifier = CsmClassifierResolver.getDefault().getOriginalClassifier(type.getClassifier(), decl.getContainingFile());
//                if (CsmKindUtilities.isTemplate(originalClassifier)) {
//                    CsmObject instantiate = InstantiationProviderImpl.getDefault().instantiate((CsmTemplate) originalClassifier, Collections.<CsmSpecializationParameter>emptyList(), mapping, decl.getContainingFile(), decl.getStartOffset());
//                    if (CsmKindUtilities.isClassifier(instantiate)) {
//                        originalClassifier = (CsmClassifier) instantiate;
//                    }
//                }
//                if(CsmKindUtilities.isInstantiation(originalClassifier)) {
//                    Object eval = new ExpressionEvaluator(level+1).eval(variableName.replaceAll(".*::(.*)", "$1"), (CsmInstantiation) originalClassifier); // NOI18N
//                    if (eval instanceof Integer) {
//                        return (Integer) eval;
//                    }
//                }
//
//            }
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
