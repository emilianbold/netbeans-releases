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
package org.netbeans.modules.bpel.model.api.support;

import java.util.Collection;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;

/**
 * Provides information about BPEL extension functions for XPath. 
 * 
 * TODO: It's necessary to figure out which functions are still required.
 * Only 2 functions are described in the BPEL specification
 * 
 * @author nk160297
 */
public class BpelXpathExtFunctionResolver implements ExtensionFunctionResolver, 
        BpelXPathExtFunctionMetadata {

    private static HashMap<QName, ExtFunctionMetadata> validFunctions = 
            new HashMap<QName, ExtFunctionMetadata>();

    static {
        //
        // Standard BPEL Extensions.
        validFunctions.put(GET_VARIABLE_PROPERTY_METADATA.getName(), 
                GET_VARIABLE_PROPERTY_METADATA);
        
        validFunctions.put(DO_XSL_TRANSFORM_METADATA.getName(), 
                DO_XSL_TRANSFORM_METADATA);
        //
        // Runtime specific extensions.
        validFunctions.put(CURRENT_TIME_METADATA.getName(), 
                CURRENT_TIME_METADATA);
        validFunctions.put(CURRENT_DATE_METADATA.getName(), 
                CURRENT_DATE_METADATA);
        validFunctions.put(CURRENT_DATE_TIME_METADATA.getName(), 
                CURRENT_DATE_TIME_METADATA);
        validFunctions.put(DO_MARSHAL_METADATA.getName(), 
                DO_MARSHAL_METADATA);
        validFunctions.put(DO_UNMARSHAL_METADATA.getName(), 
                DO_UNMARSHAL_METADATA);
        //
    }
    
    public BpelXpathExtFunctionResolver() {
    }

    public ExtFunctionMetadata getFunctionMetadata(QName name) {
        return validFunctions.get(name);
    }

    public Collection<QName> getSupportedExtFunctions() {
        return validFunctions.keySet();
    }

    public XPathExtensionFunction newInstance(XPathModel model, QName name) {
        return null;
    }

    public void validateFunction(XPathExtensionFunction function, 
            XPathValidationContext context) {
        return;
    }

}
