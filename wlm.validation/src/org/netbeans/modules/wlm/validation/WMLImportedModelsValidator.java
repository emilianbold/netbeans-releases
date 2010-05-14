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

package org.netbeans.modules.wlm.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author anjeleevich
 */
public class WMLImportedModelsValidator implements Validator {

    public String getName() {
        return "Imported Models Validator"; // NOI18N
    }

    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType)
    {
        if (!(model instanceof WLMModel)) {
            return null;
        }

        if (validationType != ValidationType.COMPLETE) {
            return null;
        }

        WLMModel wlmModel = (WLMModel) model;

        TTask task = wlmModel.getTask();

        if (task == null) {
            return null;
        }

        Collection<TImport> imports = task.getImports();
        if (imports == null || imports.isEmpty()) {
            return null;
        }

        WLMValidationResultBuilder builder = new WLMValidationResultBuilder(
                wlmModel, this, validation, validationType);

        Set<Model> modelsSet = new HashSet<Model>();
        for (TImport importElement : imports) {
            WSDLModel wsdlModel = null;
            try {
                wsdlModel = importElement.getImportedWSDLModel();
            } catch (CatalogModelException ex) {
                // do nothing
            }

            if (wsdlModel == null) {
                builder.addError(importElement, getClass(), 
                        "FIX_Not_Well_Formed_Import"); // NOI18N
            } else if (modelsSet.contains(wsdlModel)) {
                builder.addError(importElement, getClass(), 
                        "FIX_DuplicateImport"); // NOI18N
            } else {
                modelsSet.add(wsdlModel);
                validation.validate(wsdlModel, validationType);
            }
        }

        return builder.createValidationResult();
    }
}
