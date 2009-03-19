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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImplSpecialization;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;

/**
 * Service that provides template instantiations
 * 
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider.class)
public class InstantiationProviderImpl extends CsmInstantiationProvider {

    @Override
    public CsmObject instantiate(CsmTemplate template, List<CsmType> params, CsmFile contextFile) {
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
            if (!hasUnresolvedParams && params.size() == templateParams.size() && CsmKindUtilities.isClass(template)) {
                // try to find full specialization of class
                CsmClass decl = (CsmClass) template;
                StringBuilder fqn = new StringBuilder(decl.getQualifiedName());
                fqn.append(Instantiation.getInstantiationCanonicalText(params));
                CsmObject resolved = ResolverFactory.createResolver(contextFile, Integer.MAX_VALUE).resolve(fqn, Resolver.CLASS);
                if (resolved != null) {
                    return resolved;
                }
            }
            if (template instanceof ForwardClass) {
                // try to find specialization of class forward
                CsmClass decl = (CsmClass) template;
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(decl.getName(), true, true, false);
                Iterator<? extends CsmObject> it = getScopeObjectsIterator(filter, decl.getScope());
                while (it != null && it.hasNext()) {
                    CsmObject obj = it.next();
                    if (obj instanceof ClassImplSpecialization) {
                        return obj;
                    }
                }
            }
            return Instantiation.create(template, mapping);
        }
        return template;
    }

    private static Iterator<? extends CsmObject> getScopeObjectsIterator(CsmFilter offsetFilter, CsmScope scope) {
        Iterator<? extends CsmObject> out = Collections.<CsmObject>emptyList().iterator();
        if (CsmKindUtilities.isFile(scope)) {
            out = CsmSelect.getDeclarations((CsmFile) scope, offsetFilter);
        } else if (CsmKindUtilities.isClass(scope)) {
            out = CsmSelect.getClassMembers(((CsmClass) scope), offsetFilter);
        } else if (CsmKindUtilities.isNamespace(scope)) {
            out = CsmSelect.getDeclarations(((CsmNamespace) scope), offsetFilter);
        }
        return out;
    }
}
