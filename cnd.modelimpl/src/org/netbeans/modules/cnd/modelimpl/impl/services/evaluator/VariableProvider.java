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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionEvaluator;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author nk220367
 */
public class VariableProvider {

    CsmOffsetableDeclaration decl;
    Map<CsmTemplateParameter, CsmSpecializationParameter> mapping;

    public VariableProvider() {
    }

    public VariableProvider(CsmOffsetableDeclaration decl, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        this.decl = decl;
        this.mapping = mapping;
    }

    public int getValue(String variableName) {

        if (decl != null) {
            for (CsmTemplateParameter param : mapping.keySet()) {
                if (variableName.equals(param.getQualifiedName().toString()) ||
                        (decl.getQualifiedName() + "::" + variableName).equals(param.getQualifiedName().toString())) { // NOI18N
                    CsmSpecializationParameter spec = mapping.get(param);
                    if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
                        Object eval = CsmExpressionEvaluator.eval(((CsmExpressionBasedSpecializationParameter) spec).getText().toString());
                        if (eval instanceof Integer) {
                            return (Integer) eval;
                        }
                    }
                }

            }
        }
//        CsmFile file = null;
//        Resolver3 r = new Resolver3(file, 0, null);
//
//
//        CsmObject o = r.resolve(variableName, Resolver3.ALL);

        return 0;
    }
}
