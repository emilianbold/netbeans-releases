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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;

/**
 * Service that provides template instantiations
 * 
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.class)
public final class InstantiationProviderImpl extends CsmInstantiationProvider {

    private static final int MAX_DEPTH = 20;

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, CsmFile contextFile) {
        return instantiate(template, params, contextFile, null);
    }

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, Map<CsmTemplateParameter, CsmType> mapping, CsmFile contextFile) {
        return instantiate(template, params, mapping, contextFile, null);
    }
    
    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, CsmType type, CsmFile contextFile) {
        return instantiate(template, params, type, contextFile, null);
    }

    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, CsmFile contextFile, Resolver resolver) {
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            List<CsmTemplateParameter> templateParams = template.getTemplateParameters();
            // check that all params are resolved
            boolean hasUnresolvedParams = false;
            Map<CsmTemplateParameter, CsmType> mapping = new HashMap<CsmTemplateParameter, CsmType>();
            for (int i = 0; i < templateParams.size() && i < params.size(); i++) {
                CsmTemplateParameter templateParam = templateParams.get(i);
                CsmType paramValue = params.get(i);
                if (templateParam != null && paramValue != null) {
                    mapping.put(templateParam, paramValue);
                } else {
                    hasUnresolvedParams = true;
                }
            }
            if (!hasUnresolvedParams) {
                if (CsmKindUtilities.isClassifier(template)) {
                    CsmClassifier specialization = specialize((CsmClassifier) template, params, contextFile, resolver);
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

    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, Map<CsmTemplateParameter, CsmType> mapping, CsmFile contextFile, Resolver resolver) {
        CsmObject result = template;
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            result = Instantiation.create(template, mapping);
            if (CsmKindUtilities.isClassifier(result)) {
                CsmClassifier specialization = specialize((CsmClassifier) result, params, contextFile, resolver);
                if (CsmKindUtilities.isTemplate(specialization)) {
                    result = (CsmTemplate) specialization;
                } else {
                    return result;
                }
            }
        }
        return result;
    }
  
    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, CsmType type, CsmFile contextFile, Resolver resolver) {
        CsmObject result = template;
        if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template)) {
            result = Instantiation.create(template, type);
            if (CsmKindUtilities.isClassifier(result)) {
                CsmClassifier specialization = specialize((CsmClassifier) result, params, contextFile, resolver);
                if (CsmKindUtilities.isTemplate(specialization)) {
                    result = (CsmTemplate) specialization;
                } else {
                    return result;
                }
            }
        }
        return result;
    }
    
    private CsmClassifier specialize(CsmClassifier classifier, List<CsmType> params, CsmFile contextFile, Resolver resolver) {
        CsmClassifier specialization = classifier;
        if (CsmKindUtilities.isTemplate(classifier)) {
            List<CsmTemplateParameter> templateParams = ((CsmTemplate) classifier).getTemplateParameters();
            if (params.size() == templateParams.size() && CsmKindUtilities.isClass(classifier)) {
                // try to find full specialization of class
                CsmClass cls = (CsmClass) classifier;
                StringBuilder fqn = new StringBuilder(cls.getQualifiedName());
                fqn.append(Instantiation.getInstantiationCanonicalText(params));
                CsmObject resolved = ResolverFactory.createResolver(contextFile, Integer.MAX_VALUE).resolve(fqn, Resolver.CLASS);
                if (resolved != null) {
                    return (CsmClassifier) resolved;
                }
                // try to find partial specialization of class
                CsmProject proj = contextFile.getProject();
                if(proj instanceof ProjectBase) {
                    fqn = new StringBuilder(cls.getUniqueName());
                    fqn.append('<'); // NOI18N
                    Collection<CsmOffsetableDeclaration> specs = ((ProjectBase)proj).findDeclarationsByPrefix(fqn.toString());
                    int bestMatch = 0;
                    for (CsmOffsetableDeclaration decl : specs) {
                        if(decl instanceof ClassImplSpecialization) {
                            int currentMatch = match((ClassImplSpecialization) decl, params, classifier, resolver);
                            if(currentMatch > bestMatch) {
                                bestMatch = currentMatch;
                                specialization = (CsmClassifier)decl;
                            }
                        }
                    }
                }
            }
            if (classifier instanceof ForwardClass) {
                // try to find specialization of class forward
                CsmClass cls = (CsmClass) classifier;
                CsmProject proj = contextFile.getProject();
                StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                fqn.append('<'); // NOI18N
                Collection<CsmOffsetableDeclaration> specs = ((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString());
                for (CsmOffsetableDeclaration decl : specs) {
                    if (decl instanceof ClassImplSpecialization) {
                        return (CsmClassifier) decl;
                    }
                }
            }
        }
        if(!classifier.equals(specialization) && CsmKindUtilities.isTemplate(specialization) && classifier instanceof CsmInstantiation) {
            // inherit mapping
            List<CsmTemplateParameter> specParams = ((CsmTemplate)specialization).getTemplateParameters();
            List<CsmTemplateParameter> clsParams = ((CsmTemplate)classifier).getTemplateParameters();
            Map<CsmTemplateParameter, CsmType> mapping = TemplateUtils.gatherMapping((CsmInstantiation) classifier);
            Map<CsmTemplateParameter, CsmType> newMapping = new HashMap<CsmTemplateParameter, CsmType>(mapping);
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
        return specialization;
    }

    private static int match(ClassImplSpecialization specialization, List<CsmType> params, CsmClassifier cls, Resolver resolver) {

        // TODO : update

        List<CsmSpecializationParameter> specParams = specialization.getSpecializationParameters();
        int match = 0;
        if(specParams.size() == params.size()) {
            for (int i = 0; i < specParams.size(); i++) {
                CsmSpecializationParameter specParam = specParams.get(i);
                CsmType param = params.get(i);
                if(cls instanceof CsmInstantiation) {
                    param = Instantiation.createType(param, (CsmInstantiation)cls);
                }
                if(specParam instanceof CsmTypeBasedSpecializationParameter) {
                    CsmTypeBasedSpecializationParameter tbsp = (CsmTypeBasedSpecializationParameter) specParam;
                    CsmClassifier cls2;
                    if (tbsp instanceof Resolver.SafeClassifierProvider) {
                        cls2 = ((Resolver.SafeClassifierProvider) tbsp).getClassifier(resolver);
                    } else {
                        cls2 = tbsp.getClassifier();
                    }
                    if(cls2.getQualifiedName().toString().equals(param.getClassifierText())) {
                        match += 2;
                    }
                    if(tbsp.isPointer() && // NOI18N
                            isPointer(param, resolver)) {
                        match += 1;
                    }
                    if(tbsp.isReference() && // NOI18N
                            isReference(param, resolver)) {
                        match += 1;
                    }
                }
            }
        }
        return match;
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

}
