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
package org.netbeans.modules.iep.model.validator;

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

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class IEPSchemaValidator extends XsdBasedValidator {
    
    static final String iepXSDUrl = "/org/netbeans/modules/iep/model/validator/resources/iep_def.xsd";
    
    public String getName() {
        return "IEPSchemaValidator"; //NO I18N
    }
    
    @Override
    protected Schema getSchema(Model model) {
        if (! (model instanceof IEPModel)) {
            return null;
        }
        
        InputStream wsdlSchemaInputStream = IEPSchemaValidator.class.getResourceAsStream(iepXSDUrl);
        Source wsdlSource = new StreamSource(wsdlSchemaInputStream);
        wsdlSource.setSystemId(IEPSchemaValidator.class.getResource(iepXSDUrl).toString());
        
        
        ArrayList<Source> isList = new ArrayList<Source>();
        isList.add(wsdlSource);
        Schema schema = getCompiledSchema(isList.toArray(new Source[isList.size()]), null, new SchemaErrorHandler());
        
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
    

}
