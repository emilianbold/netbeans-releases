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
package org.netbeans.modules.xml.wsdl.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.validation.Schema;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;

public class WSDLSchemaValidator extends XsdBasedValidator {
    
    /*
     * Uses the WSDL Basic Profile 1.1 schema from 
     * http://www.ws-i.org/Profiles/BasicProfile-1.1.html#WSDLDOCSTRUCT
     */
    static final String wsdlXSDUrl = "/org/netbeans/modules/xml/wsdl/validator/resources/wsdl-2004-08-24.xsd";
    
    public String getName() {
        return "WSDLSchemaValidator"; //NO I18N
    }
    
    @Override
    protected Schema getSchema(Model model) {
        if (! (model instanceof WSDLModel)) {
            return null;
        }
        
        InputStream wsdlSchemaInputStream = WSDLSchemaValidator.class.getResourceAsStream(wsdlXSDUrl);
        
        //combine all possible schemas through ElementFactoryProvider mechanism
        Collection<ValidatorSchemaFactory> extSchemaFactories = ValidatorSchemaFactoryRegistry.getDefault().getAllValidatorSchemaFactories();
        
        ArrayList<InputStream> isList = new ArrayList<InputStream>();
        isList.add(wsdlSchemaInputStream);
        for (ValidatorSchemaFactory factory : extSchemaFactories) {
            InputStream is = factory.getSchemaInputStream();
            isList.add(is);
        }

        Schema schema = getCompiledSchema(isList.toArray(new InputStream[isList.size()]), null);
        for (InputStream stream : isList) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                // ignore.
            }
        }
        return schema;
    }


    
}
