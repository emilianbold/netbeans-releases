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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
        Source wsdlSource = new StreamSource(wsdlSchemaInputStream);
        wsdlSource.setSystemId(WSDLSchemaValidator.class.getResource(wsdlXSDUrl).toString());
        
        //combine all possible schemas through ElementFactoryProvider mechanism
        Collection<ValidatorSchemaFactory> extSchemaFactories = ValidatorSchemaFactoryRegistry.getDefault().getAllValidatorSchemaFactories();
        
        ArrayList<Source> isList = new ArrayList<Source>();
        isList.add(wsdlSource);
        for (ValidatorSchemaFactory factory : extSchemaFactories) {
            Source is = factory.getSchemaSource();
            if(is != null) {
                isList.add(is);
            } else {
                //any validator should not return a null input stream
                Logger.getLogger(getClass().getName()).severe("getSchema: " + factory.getClass() +" returned null input stream for its schema");
            }
        }

        Schema schema = getCompiledSchema(isList.toArray(new Source[isList.size()]), new CentralLSResourceResolver(extSchemaFactories), new SchemaErrorHandler());
        
        return schema;
    }

    class SchemaErrorHandler implements ErrorHandler {
    	public void error(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void fatalError(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void warning(SAXParseException exception) throws SAXException {
    		
    	}
    }
    
    class CentralLSResourceResolver implements LSResourceResolver {
        
        private Collection<ValidatorSchemaFactory> mExtSchemaFactories;
                
        CentralLSResourceResolver(Collection<ValidatorSchemaFactory> extSchemaFactories) {
            mExtSchemaFactories = extSchemaFactories;
        }
        
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            LSInput input = null;
            
            Iterator<ValidatorSchemaFactory> it = mExtSchemaFactories.iterator();
            while(it.hasNext()) {
                ValidatorSchemaFactory fac = it.next();
                LSResourceResolver resolver = fac.getLSResourceResolver();
                if(resolver != null) {
                    input = resolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
                    if(input != null) {
                       break;
                    }
                }
            }
            
            return input;
        }
        
    }
}
