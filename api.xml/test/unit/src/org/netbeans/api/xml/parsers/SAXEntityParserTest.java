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
package org.netbeans.api.xml.parsers;

import java.io.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

/**
 *
 * @author Petr Kuzel
 */
public class SAXEntityParserTest extends NbTestCase {
    
    public SAXEntityParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(SAXEntityParserTest.class);
        
        return suite;
    }
    
    /** Test of parse method, of class org.netbeans.api.xml.parsers.SAXEntityParser. */
    public void testParse() throws Exception {
        System.out.println("testParse");
        
        // DTD parser test        
        
        InputSource input = new InputSource(new StringReader("<!ELEMENT x ANY>"));
                
        XMLReader peer = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
        TestDeclHandler dtdHandler = new TestDeclHandler();
        peer.setProperty("http://xml.org/sax/properties/declaration-handler", dtdHandler);
        SAXEntityParser parser = new SAXEntityParser(peer, false);
        parser.parse(input);

        // Add your test code below by replacing the default call to fail.
        assertTrue("DTD entity parser did not detected 'x' decl!", dtdHandler.pass);

        // Reentrance test
        
        boolean exceptionThrown = false;
        try {
            parser.parse(new InputSource(new StringReader("")));
        } catch (IllegalStateException ex) {
            exceptionThrown = true;
        } finally {
            assertTrue("Parser may not be reused!", exceptionThrown);
        }
    }        
    
    /**
     * Wrapping may not broke relative references.
     */
    private void relativeReferenceTest() {
        
    }
    
    
    class TestDeclHandler implements DeclHandler {
        
        boolean pass;
        
        public void attributeDecl(String str, String str1, String str2, String str3, String str4) throws org.xml.sax.SAXException {
        }
        
        public void elementDecl(String str, String str1) throws org.xml.sax.SAXException {
            if ("x".equals(str)) pass = true;
        }
        
        public void externalEntityDecl(String str, String str1, String str2) throws org.xml.sax.SAXException {
        }
        
        public void internalEntityDecl(String str, String str1) throws org.xml.sax.SAXException {
        }
        
    }
    
}
