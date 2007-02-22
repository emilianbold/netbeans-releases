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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;


/**
 * 
 * Generic abstract validator. 
 * @author radval
 *
 */
public abstract class AbstractValidator implements Validator {
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
                new ValidationResult( Collections.EMPTY_SET, 
                        Collections.EMPTY_SET);

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.spi.Validator#validate(org.netbeans.modules.xml.xam.Model, org.netbeans.modules.xml.xam.spi.Validation, org.netbeans.modules.xml.xam.spi.Validation.ValidationType)
     */
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public ValidationResult validate( Model model, Validation validation,
            ValidationType validationType)
    {
        if(!(model instanceof WSDLModel)) {
            return EMPTY_RESULT;
        }
        
        WSDLModel wsdlModel = (WSDLModel) model;
        
        if ( wsdlModel.getState() == Model.State.NOT_WELL_FORMED ){
            return EMPTY_RESULT;
        }
        
        // Initialize our result object
        Set<ResultItem> results;
        Set<Model> models = Collections.singleton( model );
        
        
        ValidationVisitor visitor = getVisitor();
        Definitions def = wsdlModel.getDefinitions();
        
        if( def != null ) {
            List<ExtensibilityElement> extensions = def.getExtensibilityElements();
            Iterator<ExtensibilityElement> it = extensions.iterator();
            while(it.hasNext()) {
                ExtensibilityElement exElement = it.next();
                if(exElement instanceof BPELExtensibilityComponent) {
                    ((BPELExtensibilityComponent)exElement).accept(visitor);
                }
            }
            
        }
        
        results = visitor.getResultItems();
        if ( results == null ){
            results = Collections.EMPTY_SET;
        }
        return new ValidationResult(results, models);
    }
    
    protected abstract ValidationVisitor getVisitor();

}
