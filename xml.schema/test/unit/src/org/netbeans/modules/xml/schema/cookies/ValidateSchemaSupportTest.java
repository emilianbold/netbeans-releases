/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.cookies;

import java.io.*;
import java.net.*;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.xml.sax.*;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.junit.*;
import org.openide.loaders.DataObject;
import org.netbeans.api.xml.parsers.SAXEntityParser;

/**
 * Tries to parse severel schemas with imports, includes and errors.
 *
 * @author Petr Kuzel
 */
public class ValidateSchemaSupportTest extends TestCase {
    
    public ValidateSchemaSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ValidateSchemaSupportTest.class);
        
        return suite;
    }
    
    /** Test of createParser method, of class org.netbeans.modules.xml.schema.cookies.ValidateSchemaSupport. */
    public void testSchemaSupport() throws Exception {
        System.out.println("testSchemaSupport");
        
        URL invalid = getClass().getResource("data/Invalid.xsd");
        URL simple = getClass().getResource("data/Simple.xsd");
        URL chameleon = getClass().getResource("data/Chameleon.xsd");
        URL imports = getClass().getResource("data/Import.xsd");
        URL include = getClass().getResource("data/Include.xsd");
        
        assertTrue("Invalid.xsd must not pass!",  validate(invalid) == false);
        assertTrue("Simple.xsd was marked as invalid!", validate(simple));
        assertTrue("Chameleon.xsd was marked as invalid!", validate(chameleon));
        assertTrue("Import.xsd was marked as invalid!", validate(imports));
        assertTrue("Include.xsd was marked as invalid!", validate(include));
    }
    
    public boolean validate(URL schema) throws Exception {
        InputSource in = new InputSource(schema.toExternalForm());        
        ValidateSchemaSupport support = new ValidateSchemaSupport(in);
        return support.validateXML(new CookieObserver() {
            public void receive(CookieMessage msg) {
                System.out.println("MSG: " + msg.getMessage());
            }
        });
    }
    
}
