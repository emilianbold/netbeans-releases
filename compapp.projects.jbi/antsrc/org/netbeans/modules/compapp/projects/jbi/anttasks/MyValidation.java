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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * Validation clients use this interface to start validation on a model.
 * Validator implementation can use this to optimize computing validation results
 * by finding which models are already validated.
 *
 * @author Nam Nguyen
 * @author Praveen Savur
 *
 */
public class MyValidation extends Validation {
    
    private static Collection<Validator> validators;
    
    static {
        // Lookup all available providers and create a list of providers.
        lookupProviders();        
    }
    
    private List<ResultItem> validationResult;
    private List<Model> validatedModels;
    
    public MyValidation() {
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
    @Override
    public void validate(Model model, ValidationType validationType) {
        if (validatedModels.contains(model))
            return;
        
        validatedModels.add(model);
        // Call each provider and accumulate results.
        for (Validator provider: validators) {
//            System.out.println(provider.getClass().getName());
            try {
                ValidationResult result = provider.validate(model, this, validationType);
                if (result != null) {
                    // Gather validation results.
                    validationResult.addAll(result.getValidationResult());

                    // Updated validated models list.
                    validatedModels.addAll(result.getValidatedModels());
                }
            } catch (NoClassDefFoundError e) {
//                System.out.println("NoClassDefFoundException: " + e.getMessage() +
//                        " (The Validator Provider is " + provider.getClass().getName() + ")");
            }
        }
    }       
    
    /**
     *  Returns the last validationResult.
     */
    @Override
    public List<ResultItem> getValidationResult() {
        return validationResult;
    }
    
    
    /**
     * Retuns an unmodifiable list of validated models.
     */
    @Override
    public List<Model> getValidatedModels() {
        return Collections.unmodifiableList(validatedModels);
    }
    
//    
//    /**
//     *  The type of validation.
//     *  COMPLETE indicates that the model will be recursively validated.
//     *     ie., all imported models will also be validated.
//     *  PARTIAL indicated that only the model will be validated and
//     *    no imports will be validated.
//     */
//    public enum ValidationType {
//        COMPLETE, PARTIAL
//    }
//    
        
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

        if (validators != null)
            return;
        
        validators = new ArrayList<Validator>();
        
        Lookup.Result result = Lookup.getDefault().lookup(
                new Lookup.Template(Validator.class));
        
        // FIXME: For real command line build, the following is needed.
        // For command line ant task debugbing (with classpath defined 
        // on command line), this is not needed. Disabled for now. 
        // Command line build needs further investigation.
//        if (result.allInstances().size() == 0) {  // command line
//            ClassLoader classLoader = MyValidation.class.getClassLoader();
//            result = Lookups.metaInfServices(classLoader).lookup(
//                    new Lookup.Template(Validator.class));
//        }
        
        for (Object obj: result.allInstances()) {
            Validator validator = (Validator) obj;
            validators.add(validator);
        }
        //System.out.println("providers are: " + validators);
    }
}
