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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.xml.xam.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.Lookup;


/**
 * Validation clients use this interface to start validation on a model.
 * Validator implementation can use this to optimize computing validation results
 * by finding which models are already validated.
 *
 * @author Nam Nguyen
 * @author Praveen Savur
 *
 */
public class Validation {
    
    private static Collection<Validator> validators;
    
    static {
        // Lookup all available providers and create a list of providers.
        lookupProviders();        
    }
    
    private List<ResultItem> validationResult;
    private List<Model> validatedModels;
    
    public Validation() {
//        System.out.println("ValidationImpl(): In constructor");
        initialise();
    }
        
    
    
    /**
     * Validates the model.
     * Note: Clients should call this method on a Validation instance only
     * once. The same Validation instance should not be reused.
     *
     * @param model Contains the model for which validation has to be provided.
     * @param validationType Type of validation: complete or partial.
     */
    public void validate(Model model, ValidationType validationType) {
//        System.out.println("ValidationImpl(): validate()");
        
        if (validatedModels.contains(model))
            return;
        
        validatedModels.add(model);
        // Call each provider and accumulate results.
        for(Validator provider: validators) {
            ValidationResult result = provider.validate(model, this, validationType);
            if (result != null) {
                // Gather validation results.
                validationResult.addAll(result.getValidationResult());

                // Updated validated models list.
                validatedModels.addAll(result.getValidatedModels());
            }
        }
    }
    
    
    
    /**
     *  Returns the last validationResult.
     */
    public List<ResultItem> getValidationResult() {
        return validationResult;
    }
    
    
    /**
     * Retuns an unmodifiable list of validated models.
     */
    public List<Model> getValidatedModels() {
        return Collections.unmodifiableList(validatedModels);
    }
    
    
    /**
     *  The type of validation.
     *  COMPLETE indicates that the model will be recursively validated.
     *     ie., all imported models will also be validated.
     *  PARTIAL indicated that only the model will be validated and
     *    no imports will be validated.
     */
    public enum ValidationType {
        COMPLETE, PARTIAL
    }
    
    
    
    /**
     *  Initialise.
     */
    private void initialise() {
        validationResult = new ArrayList<ResultItem>();
        validatedModels = new ArrayList<Model>();
    }
    
    
    /**
     *  Get a list of all providers.
     */
    private static void lookupProviders() {

        if(validators != null)
            return;
        
        validators = new ArrayList<Validator>();
        
//        System.out.println("ValidationImpl(): lookupProviders()");
        Lookup.Result result = Lookup.getDefault().lookup(
                new Lookup.Template(Validator.class));
        
        for(Object obj: result.allInstances()) {
            Validator validator = (Validator) obj;
            validators.add(validator);
        }
//        System.out.println("providers are: " + validators);
    }
    
    

}
