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

package org.netbeans.modules.wlm.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class WLMValidationResultBuilder {
    private List<ResultItem> resultItems = new ArrayList<ResultItem>();
    private Set<Model> modelsSet = new HashSet<Model>();

    private WLMModel model;

    private Validator validator;
    private Validation validation;
    private ValidationType validationType;

    public WLMValidationResultBuilder(WLMModel model, Validator validator,
            Validation validation, ValidationType validationType)
    {
        this.model = model;
        this.validator = validator;
        this.validation = validation;
        this.validationType = validationType;
    }

    public ValidationResult createValidationResult() {
        return new ValidationResult(new ArrayList<ResultItem>(resultItems),
                new HashSet<Model>(modelsSet));
    }

    public WLMModel getModel() {
        return model;
    }

    public Validation getValidation() {
        return validation;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public void addError(Component component, Class ruleClass, 
            String messageKey, Object... params)
    {
        if (component == null || component.getModel() == null) {
            throw new NullPointerException("Blah");
        }

        String message = createMessage(ruleClass, messageKey, params);
        resultItems.add(new ResultItem(validator, ResultType.ERROR, component,
                message));
        modelsSet.add(component.getModel());
    }

    public void addWarning(Component component, Class ruleClass,
            String messageKey, Object... params)
    {
        if (component == null || component.getModel() == null) {
            throw new NullPointerException("Blah");
        }
        
        String message = createMessage(ruleClass, messageKey, params);
        resultItems.add(new ResultItem(validator, ResultType.WARNING, component,
                message));
        modelsSet.add(component.getModel());
    }

    public void addAdvice(Component component, Class ruleClass,
            String messageKey, Object... params)
    {
        if (component == null || component.getModel() == null) {
            throw new NullPointerException("Blah");
        }
        
        String message = createMessage(ruleClass, messageKey, params);
        resultItems.add(new ResultItem(validator, ResultType.ADVICE, component,
                message));
        modelsSet.add(component.getModel());
    }

    public void addResultItem(Component component, ResultType resultType, 
            String message) 
    {
        if (component == null || component.getModel() == null) {
            throw new NullPointerException("Blah");
        }

        resultItems.add(new ResultItem(validator, resultType, component,
                message));
    }

    private static String createMessage(Class ruleClass, String messageKey,
            Object... params)
    {
        return (params == null || params.length == 0)
                ? NbBundle.getMessage(ruleClass, messageKey)
                : NbBundle.getMessage(ruleClass, messageKey, params);
    }
}
