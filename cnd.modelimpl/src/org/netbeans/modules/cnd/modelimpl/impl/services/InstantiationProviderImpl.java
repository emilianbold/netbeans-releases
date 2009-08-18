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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;

/**
 * Service that provides template instantiations
 * 
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.class)
public final class InstantiationProviderImpl extends CsmInstantiationProvider {

    private static final int MAX_DEPTH = 20;

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmFile contextFile, int contextOffset) {
        return instantiate(template, params, contextFile, contextOffset, null);
    }

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile contextFile, int contextOffset) {
        return instantiate(template, params, mapping, contextFile, contextOffset, null);
    }
    
    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmType type, CsmFile contextFile, int contextOffset) {
        return instantiate(template, params, type, contextFile, null, contextOffset);
    }

    @Override
    public CharSequence getInstantiatedText(CsmType type) {
        return Instantiation.getInstantiatedText(type);
    }

    @Override
    public CharSequence getTemplateSignature(CsmTemplate template) {
        StringBuilder sb = new StringBuilder();
        if (CsmKindUtilities.isQualified(template)) {
            sb.append(((CsmQualifiedNamedElement)template).getQualifiedName());
        } else if (CsmKindUtilities.isNamedElement(template)) {
            sb.append(((CsmNamedElement)template).getName());
        } else {
            System.err.println("uknown template object " + template);
        }
        appendTemplateParamsSignature(template.getTemplateParameters(), sb);
        return sb;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getSpecializations(CsmClassifier classifier, CsmFile contextFile, int contextOffset) {
        if (CsmKindUtilities.isTemplate(classifier) && CsmKindUtilities.isClass(classifier)) {
            CsmProject proj = contextFile.getProject();
            if (proj instanceof ProjectBase) {
                CsmClass cls = (CsmClass) classifier;
                StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                fqn.append('<'); // NOI18N
                Collection<CsmOffsetableDeclaration> specs = ((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString());
                return specs;
            }
        }
        return Collections.<CsmOffsetableDeclaration>emptyList();
    }

    private static final int PARAMETERS_LIMIT = 1000; // do not produce too long signature

    public static void appendParametersSignature(Collection<CsmParameter> params, StringBuilder sb) {
        sb.append('(');
        int limit = 0;
        for (Iterator<CsmParameter> iter = params.iterator(); iter.hasNext();) {
            if (limit >= PARAMETERS_LIMIT) {
                break;
            }
            limit++;
            CsmParameter param = iter.next();
            CsmType type = param.getType();
            if (type != null) {
                sb.append(type.getCanonicalText());
                if (iter.hasNext()) {
                    sb.append(',');
                }
            } else if (param.isVarArgs()) {
                sb.append("..."); // NOI18N
            }
        }
        sb.append(')');
    }

    public static void appendTemplateParamsSignature(List<CsmTemplateParameter> params, StringBuilder sb) {
        if (params != null && params.size() > 0) {
            sb.append('<');
            int limit = 0;
            for (Iterator<CsmTemplateParameter> iter = params.iterator(); iter.hasNext();) {
                if (limit >= PARAMETERS_LIMIT) {
                    break;
                }
                limit++;
                CsmTemplateParameter param = iter.next();
                if (CsmKindUtilities.isVariableDeclaration(param)) {
                    CsmVariable var = (CsmVariable) param;
                    CsmType type = var.getType();
                    if (type != null) {
                        sb.append(type.getCanonicalText());
                        if (iter.hasNext()) {
                            sb.append(',');
                        }
                    }
                }
                if (CsmKindUtilities.isClassifier(param)) {
                    CsmClassifier classifier = (CsmClassifier) param;
                    sb.append("class"); // NOI18N // Name of parameter does not matter
                    if (CsmKindUtilities.isTemplate(param)) {
                        appendTemplateParamsSignature(((CsmTemplate) classifier).getTemplateParameters(), sb);
                    }
                    if (iter.hasNext()) {
                        sb.append(',');
                    }
                }
            }
            sb.append('>');
        }
    }

    private CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmFile contextFile, int contextOffset, Resolver resolver) {
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            List<CsmTemplateParameter> templateParams = template.getTemplateParameters();
            // check that all params are resolved
            boolean hasUnresolvedParams = false;
            Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
            for (int i = 0; i < templateParams.size() && i < params.size(); i++) {
                CsmTemplateParameter templateParam = templateParams.get(i);
                CsmSpecializationParameter paramValue = params.get(i);
                if (templateParam != null && paramValue != null) {
                    mapping.put(templateParam, paramValue);
                } else {
                    hasUnresolvedParams = true;
                }
            }
            if (!hasUnresolvedParams) {
                if (CsmKindUtilities.isClassifier(template)) {
                    CsmClassifier specialization = specialize((CsmClassifier) template, params, contextFile, contextOffset, resolver);
                    if (CsmKindUtilities.isTemplate(specialization)) {
                        template = (CsmTemplate) specialization;
                    } else {
                        return specialization;
                    }
                }
            }
            return Instantiation.create(template, mapping);
        }
        return template;
    }

    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile contextFile, int contextOffset, Resolver resolver) {
        CsmObject result = template;
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            result = Instantiation.create(template, mapping);
            if (CsmKindUtilities.isClassifier(result)) {
                CsmClassifier specialization = specialize((CsmClassifier) result, params, contextFile, contextOffset, resolver);
                if (CsmKindUtilities.isTemplate(specialization)) {
                    result = (CsmTemplate) specialization;
                } else {
                    return result;
                }
            }
        }
        return result;
    }
  
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmType type, CsmFile contextFile, Resolver resolver, int contextOffset) {
        CsmObject result = template;
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            result = Instantiation.create(template, type);
            if (CsmKindUtilities.isClassifier(result)) {
                CsmClassifier specialization = specialize((CsmClassifier) result, params, contextFile, contextOffset, resolver);
                if (CsmKindUtilities.isTemplate(specialization)) {
                    result = (CsmTemplate) specialization;
                } else {
                    return result;
                }
            }
        }
        return result;
    }

    private CsmClassifier specialize(CsmClassifier classifier, List<CsmSpecializationParameter> params, CsmFile contextFile, int contextOffset, Resolver resolver) {
        CsmClassifier specialization = null;
        if (CsmKindUtilities.isTemplate(classifier)) {
            List<CsmTemplateParameter> templateParams = ((CsmTemplate) classifier).getTemplateParameters();
            if (params.size() == templateParams.size() && CsmKindUtilities.isClass(classifier)) {
                CsmProject proj = contextFile.getProject();
                if (proj instanceof ProjectBase) {
                    // try to find full specialization of class
                    CsmClass cls = (CsmClass) classifier;
                    StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                    fqn.append(Instantiation.getInstantiationCanonicalText(params));
                    CsmDeclaration decl = ((ProjectBase) proj).findDeclaration(fqn.toString());
                    if(decl instanceof ClassImplSpecialization) {
                        specialization = (CsmClassifier) decl;
                    }
                    // try to find partial specialization of class
                    if (specialization == null) {
                        fqn = new StringBuilder(cls.getUniqueName());
                        fqn.append('<'); // NOI18N
                        Collection<CsmOffsetableDeclaration> specs = ((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString());
                        specialization = findBestSpecialization(specs, params, cls, resolver);
                    }
                }
            }
            if (specialization == null && isClassForward(classifier)) {
                // try to find specialization of class forward
                CsmClass cls = (CsmClass) classifier;
                CsmProject proj = contextFile.getProject();
                StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                fqn.append('<'); // NOI18N
                Collection<CsmOffsetableDeclaration> specs = ((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString());
                for (CsmOffsetableDeclaration decl : specs) {
                    if (decl instanceof ClassImplSpecialization) {
                        specialization = (CsmClassifier) decl;
                        break;
                    }
                }
            }
        }
        if(specialization != null && !classifier.equals(specialization) &&
                CsmKindUtilities.isTemplate(specialization) && CsmKindUtilities.isInstantiation(classifier)) {
            // inherit mapping
            List<CsmTemplateParameter> specParams = ((CsmTemplate)specialization).getTemplateParameters();
            List<CsmTemplateParameter> clsParams = ((CsmTemplate)classifier).getTemplateParameters();
            Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = TemplateUtils.gatherMapping((CsmInstantiation) classifier);
            Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>(mapping);
            for (CsmTemplateParameter p : mapping.keySet()) {
                int length = (clsParams.size() < specParams.size()) ? clsParams.size() : specParams.size();
                for (int i = 0; i < length; i++) {
                    if(p.equals(clsParams.get(i))) {
                        newMapping.put(specParams.get(i), mapping.get(p));
                        break;
                    }
                }
            }
            CsmObject obj =  Instantiation.create((CsmTemplate) specialization,  newMapping);
            if(CsmKindUtilities.isClassifier(obj)) {
                specialization = (CsmClassifier) obj ;
            }
        }
        return specialization != null ? specialization : classifier;
    }

    private static CsmClassifier findBestSpecialization(Collection<CsmOffsetableDeclaration> specializations, List<CsmSpecializationParameter> params, CsmClassifier cls, Resolver resolver) {

        // TODO : update

        CsmClassifier bestSpecialization = null;
       
        if (!specializations.isEmpty()) {
            int bestMatch = 0;
            int paramsSize = params.size();

            List<CharSequence> paramsText = new ArrayList<CharSequence>();
            List<CsmType> paramsType = new ArrayList<CsmType>();
            for (CsmSpecializationParameter param : params) {
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                    CsmType paramType = ((CsmTypeBasedSpecializationParameter) param).getType();
                    if (CsmKindUtilities.isInstantiation(cls)) {
                        paramType = Instantiation.createType(paramType, (CsmInstantiation) cls);
                    }
                    paramsType.add(paramType);
                    paramsText.add(paramType.getCanonicalText());
                } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                    paramsType.add(null);
                    paramsText.add(((CsmExpressionBasedSpecializationParameter) param).getText());
                }
            }            
            for (CsmOffsetableDeclaration decl : specializations) {
                if (decl instanceof ClassImplSpecialization) {
                    ClassImplSpecialization specialization = (ClassImplSpecialization) decl;
                    List<CsmSpecializationParameter> specParams = specialization.getSpecializationParameters();
                    int match = 0;
                    if (specParams.size() == paramsSize) {
                        for (int i = 0; i < paramsSize; i++) {
                            CsmSpecializationParameter specParam = specParams.get(i);
                            CsmSpecializationParameter param = params.get(i);
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam) &&
                                    CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                CsmTypeBasedSpecializationParameter tbsp = (CsmTypeBasedSpecializationParameter) specParam;
                                CsmClassifier cls2;
                                if (tbsp instanceof Resolver.SafeClassifierProvider) {
                                    cls2 = ((Resolver.SafeClassifierProvider) tbsp).getClassifier(resolver);
                                } else {
                                    cls2 = tbsp.getClassifier();
                                }
                                if (cls2 != null) {
                                    if (cls2.getQualifiedName().toString().equals(paramsText.get(i).toString())) {
                                        match += 2;
                                    }
                                    if (tbsp.isPointer() && // NOI18N
                                            isPointer(paramsType.get(i), resolver)) {
                                        match += 1;
                                    }
                                    if (tbsp.isReference() && // NOI18N
                                            isReference(paramsType.get(i), resolver)) {
                                        match += 1;
                                    }
                                }
                            } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(specParam) &&
                                    CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                                if (paramsText.get(i).equals(((CsmExpressionBasedSpecializationParameter) specParam).getText())) {
                                    match += 2;
                                }
                            } else {
                                match = 0;
                                break;
                            }
                        }
                    }
                    if (match > bestMatch) {
                        bestMatch = match;
                        bestSpecialization = (CsmClassifier) decl;
                    }
                }
            }
        }
        return bestSpecialization;
    }

    private static boolean isPointer(CsmType type, Resolver resolver) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (type.isPointer()) {
                return true;
            }
            CsmClassifier cls;
            if (type instanceof Resolver.SafeClassifierProvider) {
                cls = ((Resolver.SafeClassifierProvider)type).getClassifier(resolver);
            } else {
                cls = type.getClassifier();
            }
            if (CsmKindUtilities.isTypedef(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return false;
    }

    private static boolean isReference(CsmType type, Resolver resolver) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (type.isReference()) {
                return true;
            }
            CsmClassifier cls;
            if (type instanceof Resolver.SafeClassifierProvider) {
                cls = ((Resolver.SafeClassifierProvider)type).getClassifier(resolver);
            } else {
                cls = type.getClassifier();
            }
            if (CsmKindUtilities.isTypedef(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return false;
    }

    private boolean isClassForward(CsmClassifier cls) {
        while (CsmKindUtilities.isInstantiation(cls)) {
            CsmOffsetableDeclaration decl = ((CsmInstantiation) cls).getTemplateDeclaration();
            if (CsmKindUtilities.isClassifier(cls)) {
                cls = (CsmClassifier) decl;
            } else {
                break;
            }
        }
        return ForwardClass.isForwardClass(cls);
    }

    @Override
    public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type) {
        return new TypeBasedSpecializationParameterImpl(type);
    }

    @Override
    public CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmFile file, int start, int end) {
        return new ExpressionBasedSpecializationParameterImpl(expression, file, start, end);
    }

}
