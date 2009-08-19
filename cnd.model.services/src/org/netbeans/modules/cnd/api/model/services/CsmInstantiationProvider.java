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
package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.openide.util.Lookup;

/**
 * Service that provides template instantiations
 * 
 * @author Nick Krasilnikov
 */
public abstract class CsmInstantiationProvider {
    
    /** A dummy provider that never returns any results.
     */
    private static final CsmInstantiationProvider EMPTY = new Empty();
    /** default instance */
    private static CsmInstantiationProvider defaultProvider;

    protected CsmInstantiationProvider() {
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    public static CsmInstantiationProvider getDefault() {
        /*no need for sync synchronized access*/
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(CsmInstantiationProvider.class);
        return defaultProvider == null ? EMPTY : defaultProvider;
    }

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmFile contextFile, int contextOffset);

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @param mapping - template mapping
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile contextFile, int contextOffset);

    /**
     * Returns instantiation of template
     *
     * @param template - template for instantiation
     * @param params - template parameters
     * @param type - template type
     * @return - instantiation
     */
    public abstract CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmType type, CsmFile contextFile, int contextOffset);

    /**
     * Creates specialization parameter based on type.
     *
     * @param type - type for parameter
     * @return specialization parameter
     */
    public abstract CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type);

     /**
     * Creates specialization parameter based on expression.
     *
     * @param expression - string with expression
     * @param file - containing file
     * @param start - start offset
     * @param end - end offset
     * @return specialization parameter
      */
    public abstract CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmFile file, int start, int end);
    /**
     * returns instantiated text if possible to resolve all instantiation mappings
     */
    public abstract CharSequence getInstantiatedText(CsmType type);

    /**
     * returns signature of template parameters
     */
    public abstract CharSequence getTemplateSignature(CsmTemplate template);

    /**
     * Returns class specialisations
     *
     * @param classifier - class
     * @param contextFile - file
     * @param contextOffset - offset
     * @return
     */
    public abstract Collection<CsmOffsetableDeclaration> getSpecializations(CsmClassifier classifier, CsmFile contextFile, int contextOffset);
    
    //
    // Implementation of the default provider
    //
    private static final class Empty extends CsmInstantiationProvider {

        Empty() {
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmFile contextFile, int contextOffset) {
            return template;
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, CsmFile contextFile, int contextOffset) {
            return template;
        }

        @Override
        public CsmObject instantiate(CsmTemplate template, List<CsmSpecializationParameter> params, CsmType type, CsmFile contextFile, int contextOffset) {
            return template;
        }


        @Override
        public CsmTypeBasedSpecializationParameter createTypeBasedSpecializationParameter(CsmType type) {
            return null;
        }

        @Override
        public CsmExpressionBasedSpecializationParameter createExpressionBasedSpecializationParameter(String expression, CsmFile file, int start, int end) {
            return null;
        }

        @Override
        public CharSequence getInstantiatedText(CsmType type) {
            return type.getText();
        }

        @Override
        public CharSequence getTemplateSignature(CsmTemplate template) {
            return ""; // NOI18N
        }

        @Override
        public Collection<CsmOffsetableDeclaration> getSpecializations(CsmClassifier classifier, CsmFile contextFile, int contextOffset) {
            return Collections.<CsmOffsetableDeclaration>emptyList();
        }

    }
}
