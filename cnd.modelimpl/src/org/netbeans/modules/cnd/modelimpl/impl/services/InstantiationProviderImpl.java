/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionEvaluator;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariadicSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;

/**
 * Service that provides template instantiations
 * 
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.class)
public final class InstantiationProviderImpl extends CsmInstantiationProvider {
    private static final Logger LOG = Logger.getLogger(InstantiationProviderImpl.class.getSimpleName());
    
    private static final int MAX_DEPTH = 20;

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params) {
        return instantiate(template, params, true);
    }
    
    public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, boolean specialize) {
        return instantiate(template, null, 0, params, specialize);
    }
    
    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, List<CsmSpecializationParameter> params, boolean specialize) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = getTemplateRelatedCache(template, specialize);
        Object key = new InstantiateListKey(params);
        boolean[] found = new boolean[] { false };
        CsmObject result = (CsmObject) CsmCacheMap.getFromCache(cache, key, found);
        boolean cached = true;
        if (result == null || found[0]) {
            cached = false;
            time = System.currentTimeMillis();
            LOG.log(Level.FINEST, "instantiate 1 {0}spec:{1};params={2}\n", new Object[]{template.getDisplayName(), specialize, params});
            result = template;
            if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template) || CsmKindUtilities.isTypeAlias(template)) {
                if (contextFile == null) {
                    contextFile = ((CsmOffsetable) template).getContainingFile();
                    contextOffset = ((CsmOffsetable) template).getStartOffset();
                }
                
                List<CsmTemplateParameter> templateParams = template.getTemplateParameters();
                Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
                Iterator<CsmSpecializationParameter> paramsIter = params.iterator();
                int i = 0;
                for (CsmTemplateParameter templateParam : templateParams) {
                    if(templateParam.isVarArgs() && i == templateParams.size() - 1 && paramsIter.hasNext()) {
                        List<CsmSpecializationParameter> args = new ArrayList<CsmSpecializationParameter>();
                        while(paramsIter.hasNext()) {
                            args.add(paramsIter.next());
                        }
                        mapping.put(templateParam, new VariadicSpecializationParameterImpl(args, ((CsmOffsetableDeclaration)template).getContainingFile(), 0, 0));
                    } else if (paramsIter.hasNext()) {
                        mapping.put(templateParam, paramsIter.next());
                    } else {
                        CsmSpecializationParameter defaultValue = getTemplateParameterDefultValue(template, templateParam, i);
                        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(defaultValue)) {
                            CsmType defaultType = ((CsmTypeBasedSpecializationParameter)defaultValue).getType();
                            defaultType = TemplateUtils.checkTemplateType(defaultType, template);
                            if (defaultType != null) {
                                mapping.put(templateParam, new TypeBasedSpecializationParameterImpl(defaultType));
                            }
                        } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(defaultValue)) {
                            mapping.put(templateParam, defaultValue);
                        }
                    }
                    i++;
                }
                result = Instantiation.create(template, mapping);
                if (specialize) {
                    if (CsmKindUtilities.isClassifier(result)) {
                        CsmClassifier specialization = specialize((CsmClassifier) result, contextFile, contextOffset);
                        if (CsmKindUtilities.isTemplate(specialization)) {
                            result = (CsmTemplate) specialization;
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            time = System.currentTimeMillis() - time;
        }
        LOG.log(Level.FINE, "Instantiate 1 took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        return result;
    }    
    
    @Override
    public CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation) {
        return instantiate(template, instantiation, true);        
    }

    public CsmObject instantiate(CsmTemplate template, CsmInstantiation instantiation, boolean specialize) {
        return instantiate(template, null, 0, instantiation, specialize);
    }
    
    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, CsmInstantiation instantiation, boolean specialize) {
        return instantiate(template, contextFile, contextOffset, instantiation.getMapping(), specialize);
    }    
    
    @Override
    public CsmObject instantiate(CsmTemplate template, CsmType type) {
        return instantiate(template, type, true);
    }
    
    public CsmObject instantiate(CsmTemplate template, CsmType type, boolean specialize) {
        return instantiate(template, type.getContainingFile(), type.getStartOffset(), type.getInstantiationParams(), specialize);
    }
    
    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return instantiate(template, contextFile, contextOffset, mapping, true);
    }
    
    public CsmObject instantiate(CsmTemplate template, CsmFile contextFile, int contextOffset, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, boolean specialize) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = getTemplateRelatedCache(template, specialize);
        Object key =  new InstantiateMapKey(mapping);
        boolean[] found = new boolean[] { false };
        CsmObject result = (CsmObject) CsmCacheMap.getFromCache(cache, key, found);
        boolean cached = true;
        if (result == null || found[0]) {
            cached = false;
            LOG.log(Level.FINEST, "instantiate 2 {0}; spec:{1};mapping={2}\n", new Object[]{template.getDisplayName(), specialize, mapping});
            result = template;
            time = System.currentTimeMillis();
            if (CsmKindUtilities.isClass(template) || CsmKindUtilities.isFunction(template) || CsmKindUtilities.isTypeAlias(template)) {
                if (contextFile == null) {
                    contextFile = ((CsmOffsetable) template).getContainingFile();
                    contextOffset = ((CsmOffsetable) template).getStartOffset();
                }
                result = Instantiation.create(template, mapping);
                if (specialize && CsmKindUtilities.isClassifier(result)) {
                    CsmClassifier specialization = specialize((CsmClassifier) result, contextFile, contextOffset);
                    if (CsmKindUtilities.isTemplate(specialization)) {
                        result = (CsmTemplate) specialization;
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            time = System.currentTimeMillis() - time;
        }
        LOG.log(Level.FINE, "Instantiate 2 took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        return result;
    }
    
    @Override
    public CsmType instantiate(CsmTemplateParameter templateParam, List<CsmInstantiation> instantiations) {
        CsmSpecializationParameter resolvedParam = Instantiation.resolveTemplateParameter(templateParam, TemplateUtils.gatherMapping(instantiations));        
        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(resolvedParam)) {
            CsmType resolvedType = createTypeInstantiationForTypeParameter((CsmTypeBasedSpecializationParameter) resolvedParam, instantiations.iterator(), 0);
            return resolvedType != null ? resolvedType : (CsmTypeBasedSpecializationParameter) resolvedParam;
        }
        return null;
    }    

    @Override
    public CharSequence getInstantiatedText(CsmType type) {
        long time = System.currentTimeMillis();
        try {
            return Instantiation.getInstantiatedText(type);
        } finally {
            time = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "getInstantiatedText took {0}ms\n", new Object[]{time}); // NOI18N
        }
    }

    @Override
    public CharSequence getTemplateSignature(CsmTemplate template) {
        long time = System.currentTimeMillis();
        LOG.log(Level.FINEST, "getTemplateSignature {0}\n", new Object[]{template});// NOI18N
        StringBuilder sb = new StringBuilder();
        if (CsmKindUtilities.isQualified(template)) {
            sb.append(((CsmQualifiedNamedElement)template).getQualifiedName());
        } else if (CsmKindUtilities.isNamedElement(template)) {
            sb.append(((CsmNamedElement)template).getName());
        } else {
            System.err.println("uknown template object " + template);
        }
        appendTemplateParamsSignature(template.getTemplateParameters(), sb);
        time = System.currentTimeMillis() - time;
        LOG.log(Level.FINE, "getTemplateSignature took {0}ms\n", new Object[]{time}); // NOI18N        
        return sb;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getSpecializations(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = CsmCacheManager.getClientCache(TemplateSpecializationsKey.class, SPECIALIZATIONS_INITIALIZER);
        Object key = new TemplateSpecializationsKey(templateDecl, contextFile, contextOffset);
        Collection<CsmOffsetableDeclaration> specs = (Collection<CsmOffsetableDeclaration>) CsmCacheMap.getFromCache(cache, key, null);
        boolean cached = true;
        if (specs == null) {
            cached = false;
            LOG.log(Level.FINEST, "getSpecializations {0}\n", new Object[]{templateDecl});
            time = System.currentTimeMillis();
            specs = Collections.emptyList();
            if (CsmKindUtilities.isTemplate(templateDecl)) {
                if (contextFile == null && CsmKindUtilities.isOffsetable(templateDecl)) {
                    contextFile = ((CsmOffsetable)templateDecl).getContainingFile();
                }
                CsmProject proj = contextFile != null ? contextFile.getProject() : null;
                if (proj instanceof ProjectBase) {
                    StringBuilder fqn = new StringBuilder(templateDecl.getUniqueName());
                    fqn.append('<'); // NOI18N
                    specs = new ArrayList<>(((ProjectBase) proj).findDeclarationsByPrefix(fqn.toString()));
                }
            } else if (CsmKindUtilities.isMethod(templateDecl)) {
                // try to find explicit specialization of method if any
                CsmClass cls = CsmBaseUtilities.getFunctionClass((CsmFunction) templateDecl);
                if (CsmKindUtilities.isTemplate(cls)) {
                    specs = new ArrayList<>();
                    CharSequence funName = templateDecl.getName();
                    Collection<CsmOffsetableDeclaration> specializations = getSpecializations(cls, contextFile, contextOffset);
                    for (CsmOffsetableDeclaration specialization : specializations) {
                        CsmTemplate spec = (CsmTemplate) specialization;
                        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers((CsmClass) spec, CsmSelect.getFilterBuilder().createNameFilter(funName, true, true, false));
                        //if (spec.isExplicitSpecialization()) {
                        while(classMembers.hasNext()) {
                            CsmMember next = classMembers.next();
                            if (CsmKindUtilities.isFunctionDeclaration(next)) {
                                CsmFunctionDefinition definition = ((CsmFunction) next).getDefinition();
                                if (definition != null && !definition.equals(next)) {
                                    specs.add(definition);
                                }
                            }
                        }
                        //}
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(specs, time));
            }
        } else {
            if (specs.isEmpty()) {
                specs = Collections.emptyList();
            } else {
                specs = new ArrayList<>(specs);
            }
            time = System.currentTimeMillis() - time;
        }
        LOG.log(Level.FINE, "getSpecializations took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        
        return specs;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getBaseTemplate(CsmDeclaration declaration) {
        long time = System.currentTimeMillis();
        CsmCacheMap cache = CsmCacheManager.getClientCache(BaseTemplateKey.class, BASE_TEMPLATE_INITIALIZER);
        Object key = new BaseTemplateKey(declaration);
        Collection<CsmOffsetableDeclaration> result = (Collection<CsmOffsetableDeclaration>) CsmCacheMap.getFromCache(cache, key, null);
        boolean cached = true;
        if (result == null) {
            cached = false;
            LOG.log(Level.FINEST, "getBaseTemplate {0}\n", new Object[]{declaration});
            time = System.currentTimeMillis();
            result = Collections.<CsmOffsetableDeclaration>emptyList();
            if (CsmKindUtilities.isSpecialization(declaration)) {
                if (CsmKindUtilities.isOffsetable(declaration) && CsmKindUtilities.isQualified(declaration)) {
                    CharSequence qualifiedName = ((CsmQualifiedNamedElement)declaration).getQualifiedName();
                    String removedSpecialization = qualifiedName.toString().replaceAll("<.*>", "");// NOI18N
                    CsmFile contextFile = ((CsmOffsetable) declaration).getContainingFile();
                    CsmProject proj = contextFile != null ? contextFile.getProject() : null;
                    Iterator<? extends CsmObject> decls = Collections.<CsmObject>emptyList().iterator();
                    if (CsmKindUtilities.isClass(declaration)) {
                        if (proj instanceof ProjectBase) {
                            decls = ((ProjectBase)proj).findClassifiers(removedSpecialization).iterator();
                        }
                    } else if (proj != null && CsmKindUtilities.isFunction(declaration)) {
                        String removedParams = removedSpecialization.replaceAll("\\(.*", "");// NOI18N
                        decls = CsmSelect.getFunctions(proj, removedParams);
                    }
                    result = new ArrayList<>();
                    while (decls.hasNext()) {
                        CsmObject decl = decls.next();
                        if (!CsmKindUtilities.isSpecialization(decl)) {
                            result.add((CsmOffsetableDeclaration) decl);
                        }
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if (cache != null) {
                cache.put(key, CsmCacheMap.toValue(result, time));
            }
        } else {
            if (result.isEmpty()) {
                result = Collections.<CsmOffsetableDeclaration>emptyList();
            } else {
                result = new ArrayList<>(result);
            }
            time = System.currentTimeMillis() - time;
        }
        LOG.log(Level.FINE, "getBaseTemplate took {0}ms ({1})\n", new Object[]{time, cached ? "CACHE HIT" : "calculated"});
        return result;
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
            TemplateUtils.addGREATERTHAN(sb);
        }
    }
    
    private CsmClassifier specialize(CsmClassifier classifier, CsmFile contextFile, int contextOffset) {
        List<CsmSpecializationParameter> params = getInstantiationParams(classifier);
        CsmClassifier specialization = null;
        if (CsmKindUtilities.isTemplate(classifier)) {
            List<CsmTemplateParameter> templateParams = ((CsmTemplate) classifier).getTemplateParameters();
            if (params.size() == templateParams.size() && CsmKindUtilities.isClass(classifier)) {
                List<ProjectBase> projects = collectProjects(contextFile);
                if (!projects.isEmpty()) {
                    // try to find full specialization of class
                    CsmClass cls = (CsmClass) classifier;
                    StringBuilder fqn = new StringBuilder(cls.getUniqueName());
                    fqn.append(Instantiation.getInstantiationCanonicalText(params));
                    
                    for (CsmProject proj : projects) {
                        CsmDeclaration decl = proj.findDeclaration(fqn.toString());
                        if(decl instanceof ClassImplSpecialization && CsmIncludeResolver.getDefault().isObjectVisible(contextFile, decl)) {
                            specialization = (CsmClassifier) decl;
                            break;
                        }
                    }
                    
                    // try to find partial specialization of class
                    if (specialization == null) {
                        Collection<CsmOffsetableDeclaration> specs = new ArrayList<CsmOffsetableDeclaration>();
                        
                        for (ProjectBase proj : projects) {
                            fqn = new StringBuilder();
                            fqn.append(Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS));
                            fqn.append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR);
                            fqn.append(cls.getQualifiedName());
                            fqn.append('<'); // NOI18N
                            specs.addAll(proj.findDeclarationsByPrefix(fqn.toString()));
                            fqn.setLength(0);
                            fqn.append(Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT));
                            fqn.append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR);
                            fqn.append(cls.getQualifiedName());
                            fqn.append('<'); // NOI18N
                            specs.addAll(proj.findDeclarationsByPrefix(fqn.toString()));
                        }
                        
                        Collection<CsmOffsetableDeclaration> visibleSpecs = new ArrayList<CsmOffsetableDeclaration>();
                        for (CsmOffsetableDeclaration spec : specs) {
                            if(CsmIncludeResolver.getDefault().isObjectVisible(contextFile, spec)) {
                                visibleSpecs.add(spec);
                            }
                        }
                        
                        specialization = findBestSpecialization(visibleSpecs, params, cls);
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
                        ClassImplSpecialization spec = (ClassImplSpecialization) decl;
//                        if(spec.getSpecializationParameters().size() >= params.size()) {
                            specialization = spec;
                            break;
//                        }
                    }
                }
            }
        }
        if(specialization != null && !classifier.equals(specialization) &&
                CsmKindUtilities.isTemplate(specialization) && CsmKindUtilities.isInstantiation(classifier)) {
            // inherit mapping
            List<CsmTemplateParameter> specParams = ((CsmTemplate)specialization).getTemplateParameters();
            List<CsmTemplateParameter> clsParams = ((CsmTemplate)classifier).getTemplateParameters();
            
            MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping = TemplateUtils.gatherMapping((CsmInstantiation) classifier);
            
            Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<>();            
            Set<CsmTemplateParameter> mapped = new HashSet<>();
            
            outer:
            for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entries()) {
                CsmTemplateParameter p = entry.getKey();
                int length = (clsParams.size() < specParams.size()) ? clsParams.size() : specParams.size();
                for (int i = 0; i < length; i++) {
                    if(p.equals(clsParams.get(i))) {
                        if (!mapped.contains(specParams.get(i))) { 
                            newMapping.put(specParams.get(i), entry.getValue());
                        }
                        mapped.add(specParams.get(i));
                        if (mapped.size() >= length) {
                            break outer;
                        } else {
                            break;
                        }
                    }
                }
            }
            
            mapped.clear();
            
            outer:
            for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entries()) {
                CsmTemplateParameter p = entry.getKey();
                int length = clsParams.size();
                for (int i = 0; i < length; i++) {
                    if(p.equals(clsParams.get(i))) {
                        for (CsmTemplateParameter p2 : specParams ) {
                            if(p2.getName().toString().equals(clsParams.get(i).getName().toString())) {
                                if (!mapped.contains(p2)) {
                                    newMapping.put(p2, entry.getValue());
                                }                                
                                mapped.add(p2);
                                if (mapped.size() >= specParams.size()) {
                                    break outer;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            mapping.pop();
            mapping.push(newMapping);
            
            CsmObject obj = specialization;
            List<Map<CsmTemplateParameter, CsmSpecializationParameter>> maps = mapping.getMaps(new MapHierarchy.NonEmptyFilter());            
            for (int i = maps.size() - 1; i >= 0; i--) {
                obj = instantiate((CsmTemplate) obj, contextFile, contextOffset, maps.get(i), false);
            }

            if(CsmKindUtilities.isClassifier(obj)) {
                specialization = (CsmClassifier) obj ;
            }
        }
        LOG.log(Level.FINE, "CLASSIFIER\n{0}\nSPECIALIZED as {1}", new Object[] {classifier, specialization});

        return specialization != null ? specialization : classifier;
    }
    
    /**
     * Collects projects to find specialization in
     * 
     * @param contextFile - start file
     * 
     * @return list of projects. First element in the list is the project which contains start file
     */
    private List<ProjectBase> collectProjects(CsmFile contextFile) {
        List<ProjectBase> projects = new ArrayList<>(4);
        
        CsmProject project = contextFile.getProject();
        
        if (project instanceof ProjectBase) {
            projects.add((ProjectBase) project);
        }
        
        if (project != null && !project.isArtificial()) {
            for (CsmProject libProject : project.getLibraries()) {
                if (libProject instanceof ProjectBase) {
                    projects.add((ProjectBase) libProject);
                }
            }
        }
        
        return projects;
    }

    private static CsmClassifier findBestSpecialization(Collection<CsmOffsetableDeclaration> specializations, List<CsmSpecializationParameter> params, final CsmClassifier cls) {
        // TODO : update

        CsmClassifier bestSpecialization = null;

        boolean variadic = false;
        
        List<CsmSpecializationParameter> params2 = new ArrayList<CsmSpecializationParameter>();
        for (CsmSpecializationParameter param : params) {
            if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                params2.addAll(((CsmVariadicSpecializationParameter)param).getArgs());
                variadic = true;
            } else {
                params2.add(param);
            }
        }
        params = params2;
        
        if (!specializations.isEmpty()) {
            int bestMatch = 0;
            int paramsSize = 0;
            for (CsmSpecializationParameter param : params) {
                if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                    paramsSize += ((CsmVariadicSpecializationParameter)param).getArgs().size();
                } else {
                    paramsSize++;
                }
            }

            List<CharSequence> paramsText = new ArrayList<CharSequence>();
            List<CsmType> paramsType = new ArrayList<CsmType>();
            for (CsmSpecializationParameter param : params) {
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                    CsmType paramType = ((CsmTypeBasedSpecializationParameter) param).getType();
                    
                    if (CsmKindUtilities.isInstantiation(cls)) {
                        // Try to instantiate type with appropriate instantiations
                        Iterator<CsmInstantiation> instIters = new Iterator() {

                            private CsmInstantiation instantiation = (CsmInstantiation) cls;

                            @Override
                            public boolean hasNext() {
                                return instantiation != null;
                            }

                            @Override
                            public Object next() {
                                CsmInstantiation res = instantiation;
                                if (CsmKindUtilities.isInstantiation(instantiation.getTemplateDeclaration())) {
                                    instantiation = (CsmInstantiation) instantiation.getTemplateDeclaration();
                                } else {
                                    instantiation = null;
                                }
                                return res;
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException("Not supported.");  // NOI18N
                            }
                        };
                        
                        CsmType instantiatedType = createTypeInstantiationForTypeParameter((CsmTypeBasedSpecializationParameter) param, instIters, 0);
//                        CsmType instantiatedType = null;
                        
                        if (instantiatedType != null) {
                            // If success then it will be parameter type
                            paramType = instantiatedType;
                        } else {
                            // If instantiation failed then create instantiation with the the full class isntantiation
                            paramType = Instantiation.createType(paramType, (CsmInstantiation) cls);
                        }
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
                    if (specParams.size() - 1  <= paramsSize) {
                        if(variadic) {
                            match += specParams.size();
                        }
                    }
                    if (specParams.size() == paramsSize) {
                        for (int i = 0; i < paramsSize - 1; i++) {
                            CsmSpecializationParameter specParam1 = specParams.get(i);
                            CsmSpecializationParameter param1 = params.get(i);
//                            for (int j = i + 1; j < paramsSize; j++) {
                            int j = i + 1;
                                CsmSpecializationParameter specParam2 = specParams.get(j);
                                CsmSpecializationParameter param2 = params.get(j);
                                if (specParam1.getText().toString().equals(specParam2.getText().toString())
                                        && param1.getText().toString().equals(param2.getText().toString())) {
                                    match += 1;
                                }
                                if(TraceFlags.EXPRESSION_EVALUATOR_EXTRA_SPEC_PARAMS_MATCHING) {
                                    // it works but does it too slow
                                    if(specParam1.getText().toString().equals(specParam2.getText().toString()) &&
                                            CsmKindUtilities.isTypeBasedSpecalizationParameter(param1) &&
                                            CsmKindUtilities.isTypeBasedSpecalizationParameter(param2)) {
                                        CsmTypeBasedSpecializationParameter tbp1 = (CsmTypeBasedSpecializationParameter) param1;
                                        CsmType type1 = tbp1.getType();
                                        if(CsmKindUtilities.isInstantiation(cls)) {
                                            type1 = Instantiation.createType(tbp1.getType(), (Instantiation)cls);
                                        }
                                        CsmClassifier tbsp1Cls = getClassifier(type1);
                                        if (tbsp1Cls != null) {
                                            CsmTypeBasedSpecializationParameter tbp2 = (CsmTypeBasedSpecializationParameter) param2;
                                            CsmType type2 = tbp2.getType();
                                            if(CsmKindUtilities.isInstantiation(cls)) {
                                                type2 = Instantiation.createType(tbp2.getType(), (Instantiation)cls);
                                            }
                                            CsmClassifier tbsp2Cls = getClassifier(type2);
                                            if(tbsp2Cls != null) {
                                                if (tbsp1Cls.getQualifiedName().toString().equals(tbsp2Cls.getQualifiedName().toString())) {
                                                    match += 1;
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                        }
                        for (int i = 0; i < paramsSize; i++) {
                            CsmSpecializationParameter specParam = specParams.get(i);
                            CsmSpecializationParameter param = params.get(i);
                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam) &&
                                    CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                                CsmTypeBasedSpecializationParameter instSpecParam = (CsmTypeBasedSpecializationParameter)param;                                
                                CsmTypeBasedSpecializationParameter declSpecParam = (CsmTypeBasedSpecializationParameter) specParam;                                
                                CsmClassifier declCls = declSpecParam.getClassifier();                                                                
                                if (declCls != null) {
                                    String declClsQualifiedName = declCls.getQualifiedName().toString();
                                    if (declClsQualifiedName.equals(paramsText.get(i).toString())) {
                                        match += 2;
                                    } else if (declCls.isValid() && declClsQualifiedName.contains(paramsText.get(i))) {
                                        CsmClassifier instCls = instSpecParam.getClassifier();
                                        if (instCls != null && instCls.getQualifiedName() != null) {
                                            if (declClsQualifiedName.equals(instCls.getQualifiedName().toString())) {
                                                match += 1;
                                            } else {
                                                match -= 1;
                                            }
                                        }
                                    } else if (declCls.isValid() && !instSpecParam.isInstantiation()) {
                                        // It is safe to get classifier from param type which is not instantiation
                                        CsmClassifier instCls = instSpecParam.getClassifier();
                                        if (CsmKindUtilities.isTypedefOrTypeAlias(instCls)) {                                            
                                            final List<String> nestedQualifiedNames = new ArrayList<String>();                                            
                                            
                                            CsmUtilities.iterateTypeChain(instSpecParam, new CsmUtilities.Predicate<CsmType>() {

                                                boolean first = true;
                                                
                                                @Override
                                                public boolean check(CsmType value) {
                                                    if (!first) {
                                                        CsmClassifier classifier = value.getClassifier();
                                                        if (classifier != null) {
                                                            nestedQualifiedNames.add(classifier.getQualifiedName().toString());
                                                        }
                                                    } else {
                                                        first = false;
                                                    }
                                                    return false;
                                                }
                                                
                                            });
                                            
                                            for (String nestedQualifiedName : nestedQualifiedNames) {
                                                if (declClsQualifiedName.equals(nestedQualifiedName)) {
                                                    match += 1;
                                                    break;
                                                }
                                                // TODO: maybe should decrement match variable if no matches found
                                            }
                                        }
                                    }
                                    if (declSpecParam.isPointer() &&
                                            isPointer(paramsType.get(i))) {
                                        match += 1;
                                    }
                                    if (declSpecParam.isReference()) {
                                        int checkReference = checkReference(paramsType.get(i));
                                        if (checkReference > 0) {
                                            match += 1;
                                            if ((checkReference == 2) == declSpecParam.isRValueReference()) {
                                                match +=1;
                                            }
                                        }
                                    }
                                }
                            } else if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(specParam)) {
                                if (paramsText.get(i).equals(((CsmExpressionBasedSpecializationParameter) specParam).getText())) {
                                    match += 2;
                                } else {
                                    // Expression evaluation
                                    if (TraceFlags.EXPRESSION_EVALUATOR) {
                                        CsmExpressionEvaluatorProvider p = CsmExpressionEvaluator.getProvider();
                                        if (CsmKindUtilities.isInstantiation(cls)) {
                                            final Object val1;
                                            final Object val2;
                                            if(p instanceof ExpressionEvaluator) {
                                                 val1 = ((ExpressionEvaluator)p).eval(((CsmTemplate) ((CsmInstantiation) cls).getTemplateDeclaration()).getTemplateParameters().get(i).getName().toString(), (CsmInstantiation) cls);
                                                 val2 = ((ExpressionEvaluator)p).eval(((CsmExpressionBasedSpecializationParameter) specParam).getText().toString());
                                            } else {
                                                 val1 = p.eval(((CsmTemplate) ((CsmInstantiation) cls).getTemplateDeclaration()).getTemplateParameters().get(i).getName().toString(), (CsmInstantiation) cls);
                                                 val2 = p.eval(((CsmExpressionBasedSpecializationParameter) specParam).getText().toString());
                                            }
                                            if (val1.equals(val2)) {
                                                match += 2;
                                            }
                                        } else {
                                            final Object val1;
                                            final Object val2;
                                            if(p instanceof ExpressionEvaluator) {
                                                val1 = ((ExpressionEvaluator)p).eval(paramsText.get(i).toString());
                                                val2 = ((ExpressionEvaluator)p).eval(((CsmExpressionBasedSpecializationParameter) specParam).getText().toString());
                                            } else {
                                                val1 = p.eval(paramsText.get(i).toString());
                                                val2 = p.eval(((CsmExpressionBasedSpecializationParameter) specParam).getText().toString());
                                            }
                                            if (val1.equals(val2)) {
                                                match += 2;
                                            }
                                        }
                                    }
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

    /**
     * Instantiates parameter type with appropriate mappings.
     * 
     * @param param - specialization parameter
     * @param instantiation - whole instantiation
     * @return instantiated type or null
     */
    public static CsmType createTypeInstantiationForTypeParameter(CsmTypeBasedSpecializationParameter param, Iterator<CsmInstantiation> instantiations, int level) {
        if (level > 10 || !instantiations.hasNext()) {
            return null;
        }
        
        CsmInstantiation instantiation = instantiations.next();
        
        Collection<CsmSpecializationParameter> parameters = instantiation.getMapping().values();
        
        for (CsmSpecializationParameter parameter : parameters) {
            if (parameter == param) {
                return param.getType(); // paramater has been found
            }
        }
        
        CsmType instantiatedType = createTypeInstantiationForTypeParameter(param, instantiations, level + 1);

        if (instantiatedType != null) { 
            // parameter found and we are instantiating it
            return Instantiation.createType((CsmType) instantiatedType, instantiation);
        }
        
        return null; // parameter not found
    }     
    
    private static boolean isPointer(CsmType type) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (type.isPointer()) {
                return true;
            }
            CsmClassifier cls = type.getClassifier();
            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return false;
    }

    private static int checkReference(CsmType type) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (type.isReference()) {
                if (type.isRValueReference()) {
                    return 2;
                }
                return 1;
            }
            CsmClassifier cls = type.getClassifier();
            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return 0;
    }
    
    private static CsmClassifier getClassifier(CsmType type) {
        int iteration = MAX_DEPTH;
        CsmClassifier cls = type.getClassifier();
//        while (cls != null && iteration != 0) {
//            if (CsmKindUtilities.isTypedef(cls)) {
//                CsmTypedef td = (CsmTypedef) cls;
//                type = td.getType();
//                if (type instanceof Resolver.SafeClassifierProvider) {
//                    cls = ((Resolver.SafeClassifierProvider) type).getClassifier(resolver);
//                } else {
//                    cls = type.getClassifier();
//                }
//            } else {
//                break;
//            }
//            iteration--;
//        }
        return cls;
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
        return ExpressionBasedSpecializationParameterImpl.create(expression, file, start, end);
    }
    
    public List<CsmSpecializationParameter> getInstantiationParams(CsmObject o) {
        if (!CsmKindUtilities.isInstantiation(o)) {
            return Collections.emptyList();
        }
        long time = System.currentTimeMillis();
        List<CsmSpecializationParameter> res = new ArrayList<CsmSpecializationParameter>();
        CsmInstantiation i = (CsmInstantiation) o;
        Map<CsmTemplateParameter, CsmSpecializationParameter> m = i.getMapping();
        CsmOffsetableDeclaration decl = i.getTemplateDeclaration();
        if(!CsmKindUtilities.isInstantiation(decl)) {
            // first inst
            if(CsmKindUtilities.isTemplate(decl)) {
                for (CsmTemplateParameter tp : ((CsmTemplate)decl).getTemplateParameters()) {
                    CsmSpecializationParameter sp = m.get(tp);
                    if(sp != null) {
                        res.add(sp);
                    }
                }
            }
        } else {
            // non first inst
            List<CsmSpecializationParameter> sps = getInstantiationParams(decl);
            for (CsmSpecializationParameter instParam : sps) {
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam) &&
                        CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instParam).getType())) {
                    CsmTemplateParameterType paramType = (CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instParam).getType();
                    CsmSpecializationParameter newTp = m.get(paramType.getParameter());
                    if (newTp != null && newTp != instParam) {
                        res.add(newTp);
                    } else {
                        res.add(instParam);
                    }
                } else {
                    res.add(instParam);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        LOG.log(Level.FINE, "getInstantiationParams took {0}ms\n", new Object[]{time});// NOI18N        
        return res;
    }    
    
    private CsmClassForwardDeclaration findCsmClassForwardDeclaration(CsmScope scope, CsmClass cls) {
        if (scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                CsmFile file = (CsmFile) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClass fwdCls = ((CsmClassForwardDeclaration) decl).getCsmClass();
                    if (fwdCls != null && fwdCls.equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClass fwdCls = ((CsmClassForwardDeclaration) decl).getCsmClass();
                    if (fwdCls != null && fwdCls.equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
        }
        return null;
    }    
    
    private CsmSpecializationParameter getTemplateParameterDefultValue(CsmTemplate declaration, CsmTemplateParameter param, int index) {
        CsmSpecializationParameter res = param.getDefaultValue();
        if (res != null) {
            return res;
        }
        if (CsmKindUtilities.isClass(declaration)) {
            CsmClass cls = (CsmClass) declaration;
            CsmClassForwardDeclaration fdecl;
            fdecl = findCsmClassForwardDeclaration(cls.getContainingFile(), cls);
            if (fdecl != null) {
                List<CsmTemplateParameter> templateParameters = ((CsmTemplate) fdecl).getTemplateParameters();
                if (templateParameters.size() > index) {
                    CsmTemplateParameter p = templateParameters.get(index);
                    if (p != null) {
                        res = p.getDefaultValue();
                        if (res != null) {
                            return res;
                        }
                    }
                }
            }
        }
        return res;
    }        
    
    private static CsmCacheMap getTemplateRelatedCache(CsmObject template, boolean specialize) {
        if (true) return null;
        return CsmCacheManager.getClientCache(new TemplateCacheKey(template, specialize), new TemplateCacheInitializer(template));
    }
    
    private static final class TemplateCacheKey {

        private final CsmObject obj;
        private final boolean specialize;

        public TemplateCacheKey(CsmObject instance, boolean specialize) {
            this.obj = instance;
            this.specialize = specialize;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + System.identityHashCode(this.obj);
            hash = 79 * hash + (this.specialize ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TemplateCacheKey other = (TemplateCacheKey) obj;
            if (this.specialize != other.specialize) {
                return false;
            }
            return this.obj == other.obj;
        }

        @Override
        public String toString() {
            return "TemplateCacheKey{" + "obj=" + obj + ", specialize=" + specialize + '}'; // NOI18N
        }
    }

    private static final class TemplateCacheInitializer implements Callable<CsmCacheMap> {

        private final CsmObject template;

        private TemplateCacheInitializer(CsmObject template) {
            this.template = template;
        }

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("Cache Template " + CsmDisplayUtilities.getTooltipText(template), 1);// NOI18N
        }
    }    

    private static final class InstantiateListKey {
        private final List<CsmSpecializationParameter> params;
        private int hashCode = 0;

        public InstantiateListKey(List<CsmSpecializationParameter> params) {
            this.params = new ArrayList<>(params);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash = 17 * hash + CndCollectionUtils.hashCode(params);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstantiateListKey other = (InstantiateListKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            return CndCollectionUtils.equals(params, other.params);
        }       

        @Override
        public String toString() {
            return "InstantiateListKey{" + params + '}';// NOI18N
        }
    }

    private static final class InstantiateMapKey {
        private final HashMap<CsmTemplateParameter, CsmSpecializationParameter> params;
        private int hashCode = 0;
        
        public InstantiateMapKey(Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            this.params = new HashMap<>(mapping);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int hash = 7;
                hash += CndCollectionUtils.hashCode(params);
                hashCode = hash;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstantiateMapKey other = (InstantiateMapKey) obj;
            if (this.hashCode != other.hashCode && (this.hashCode != 0 && other.hashCode != 0)) {
                return false;
            }
            return CndCollectionUtils.equals(params, other.params);
        }
        
        @Override
        public String toString() {
            return "InstantiateMapKey{" + params + '}';// NOI18N
        }
    }
    
    private static final Callable<CsmCacheMap> BASE_TEMPLATE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("BASE_TEMPLATE Cache", 1); // NOI18N
        }

    };    

    private static class BaseTemplateKey {
        private final CsmDeclaration declaration;

        public BaseTemplateKey(CsmDeclaration declaration) {
            this.declaration = declaration;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + Objects.hashCode(this.declaration);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BaseTemplateKey other = (BaseTemplateKey) obj;
            return Objects.equals(this.declaration, other.declaration);
        }        
    }
    
    private static final Callable<CsmCacheMap> SPECIALIZATIONS_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("SPECIALIZATIONS Cache", 1); // NOI18N
        }

    };    

    private static class TemplateSpecializationsKey {
        private final CsmDeclaration decl;
        private final CsmFile contextFile;
        private final int contextOffset;

        public TemplateSpecializationsKey(CsmDeclaration templateDecl, CsmFile contextFile, int contextOffset) {
            this.decl = templateDecl;
            this.contextFile = contextFile;
            this.contextOffset = contextOffset;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.contextOffset;
            hash = 31 * hash + Objects.hashCode(this.contextFile);
            hash = 31 * hash + Objects.hashCode(this.decl);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TemplateSpecializationsKey other = (TemplateSpecializationsKey) obj;
            if (this.contextOffset != other.contextOffset) {
                return false;
            }
            if (!Objects.equals(this.contextFile, other.contextFile)) {
                return false;
            }
            return Objects.equals(this.decl, other.decl);
        }

        
    }
}
