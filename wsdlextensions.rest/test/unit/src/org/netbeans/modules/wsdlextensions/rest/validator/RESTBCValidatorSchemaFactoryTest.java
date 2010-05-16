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
 * RESTBCValidatorSchemaFactoryTest.java
 * JUnit based test
 *
 * Created on February 6, 2007, 11:21 PM
 */

package org.netbeans.modules.wsdlextensions.rest.validator;

import javax.xml.transform.Source;
import junit.framework.*;
import java.io.InputStream;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;

/**
 *
 * @author radval
 */
public class RESTBCValidatorSchemaFactoryTest extends TestCase {
    
    public RESTBCValidatorSchemaFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getNamespaceURI method, of class org.netbeans.modules.wsdlextensions.rest.validator.RESTBCValidatorSchemaFactory.
     */
    public void testGetNamespaceURI() {
        System.out.println("getNamespaceURI");
        
        RESTBCValidatorSchemaFactory instance = new RESTBCValidatorSchemaFactory();
        
        String result = instance.getNamespaceURI();
        assertNotNull(result);
       
    }

    /**
     * Test of getSchemaInputStream method, of class org.netbeans.modules.wsdlextensions.rest.validator.RESTBCValidatorSchemaFactory.
     */
    public void testGetSchemaSource() {
        System.out.println("getSchemaSource");
        
        RESTBCValidatorSchemaFactory instance = new RESTBCValidatorSchemaFactory();
        

        Source result = instance.getSchemaSource();
        assertNotNull(result);
        
       
    }
    
}
