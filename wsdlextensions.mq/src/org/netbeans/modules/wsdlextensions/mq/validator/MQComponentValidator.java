/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.mq.validator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;


/**
 * WSDL document semantic validator for MQ extensibility elements.
 */
public class MQComponentValidator implements Validator {

    private static ValidationResult EMPTY_RESULT;
    static {
        java.util.Set<ResultItem> r = Collections.emptySet();
        java.util.Set<Model> m = Collections.emptySet();
        EMPTY_RESULT = new ValidationResult(r, m);
    }
    
    public MQComponentValidator() {}
    
    public String getName() {
        return getClass().getName();
    }

    /**
     * Validates given model.
     * @param model Document model to validate.
     * @param validation reference to the validation context.
     * @param validationType the type of validation to perform
     * @return ValidationResult.
     */
    public ValidationResult validate(Model model,
                                     Validation validation,
                                     ValidationType validationType) {
        
        if (model.getState() == State.NOT_WELL_FORMED) {
            return EMPTY_RESULT;
        } else if (!(model instanceof WSDLModel)) {
            return EMPTY_RESULT;
        } else {
            MQWsdlValidator validator = new MQWsdlValidator(this);
            validator.validate((WSDLModel) model);
            Set<ResultItem> results = validator.getValidationResults();
            Set<Model> models = new HashSet<Model>();
            models.add(model);
            return new ValidationResult(results, models);
        }
    }
}
