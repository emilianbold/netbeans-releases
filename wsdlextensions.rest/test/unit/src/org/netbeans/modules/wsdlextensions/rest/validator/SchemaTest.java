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

/*
 * SchemaTest.java
 * JUnit based test
 *
 * Created on January 31, 2007, 6:25 PM
 */

package org.netbeans.modules.wsdlextensions.rest.validator;

import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import junit.framework.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author radval
 */
public class SchemaTest extends TestCase {
    
    private Exception mLastError;
    
    private URL schemaUrl = SchemaTest.class.getResource("/org/netbeans/modules/wsdlextensions/rest/resources/rest-ext.xsd");
    
    public SchemaTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
    public void testSchema() throws Exception {
        MyErrorHandler errorHandler = new MyErrorHandler();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setErrorHandler(errorHandler);
        RESTBCValidatorSchemaFactory fac = new RESTBCValidatorSchemaFactory();
        Source s = fac.getSchemaSource();
        Schema schema = sf.newSchema(s);
        
        assertNotNull("schema should not be null", schema);
        
        assertNull("No exception should occur in schema parsing", mLastError);
        
    }
    
    class MyErrorHandler implements ErrorHandler {
        
        public void error(SAXParseException exception) throws SAXException {
            mLastError = exception;
            exception.printStackTrace();
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            mLastError = exception;
            exception.printStackTrace();
        }

        public void warning(SAXParseException exception) throws SAXException {
            exception.printStackTrace();
        }
        


    }
}
