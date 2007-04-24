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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.deprecation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class BPELExtensionDeprecationValidator implements Validator {
    
    public static final String VAL_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION = "VAL_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION";
    public static final String FIX_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION = "FIX_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION";
    public static final String VAL_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION = "VAL_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION";
    public static final String FIX_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION = "FIX_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION";
   
    public static final String DEPRECATED_PLINK_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";
    public static final String DEPRECATED_PROPERTY_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";
    
    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }

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
        Set<ResultItem> results = null;
        Set<Model> models = Collections.singleton( model );
        
        
        Definitions def = wsdlModel.getDefinitions();
        boolean foundDeprecatedElements = false;

        for (GenericExtensibilityElement element : def.getExtensibilityElements(GenericExtensibilityElement.class)) {
            if (element.getQName() != null) {
                String namespaceURI = element.getQName().getNamespaceURI();
                if (namespaceURI != null && namespaceURI.equals(DEPRECATED_PLINK_NAMESPACE) || namespaceURI.equals(DEPRECATED_PROPERTY_NAMESPACE)) {
                    foundDeprecatedElements = true;
                    break;
                }
            }
        }

        
        
        if (foundDeprecatedElements) {
            Map<String, String> prefixMap = ((AbstractDocumentComponent) def).getPrefixes();
            results = new HashSet<ResultItem>();
            if (prefixMap.containsValue(DEPRECATED_PLINK_NAMESPACE)) {
                results.add(new ResultItem(this,
                        Validator.ResultType.WARNING, 
                        def,
                        NbBundle.getMessage(getClass(), VAL_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION, DEPRECATED_PLINK_NAMESPACE) + " : " +
                        NbBundle.getMessage(getClass(), FIX_DEPRECATED_PLINK_NAMESPACE_IN_DEFINITION, DEPRECATED_PLINK_NAMESPACE, BPELQName.PLNK_NS)));
            }

            if (prefixMap.containsValue(DEPRECATED_PROPERTY_NAMESPACE)) {
                results.add(new ResultItem(this,
                        Validator.ResultType.WARNING, 
                        def,  
                        NbBundle.getMessage(getClass(), VAL_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION, DEPRECATED_PROPERTY_NAMESPACE) + " : " +
                        NbBundle.getMessage(getClass(), FIX_DEPRECATED_PROPERTY_NAMESPACE_IN_DEFINITION, DEPRECATED_PROPERTY_NAMESPACE, BPELQName.VARPROP_NS)));
            }
        }        
        
        if (results == null) {
            return EMPTY_RESULT;
        }
        
        return new ValidationResult(results, models);
    }

    
}

