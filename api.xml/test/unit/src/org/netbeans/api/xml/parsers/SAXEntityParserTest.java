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
package org.netbeans.api.xml.parsers;

import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

/**
 * Tests SAXEntityParser as DTDParser.
 * Tests wrapping logic for relative references.
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
        input.setSystemId("StringReader");
                
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
        } 
        catch (IllegalStateException ex) {
            exceptionThrown = true;
        } 
        finally {
            assertTrue("Parser may not be reused!", exceptionThrown);
        }
        
        relativeReferenceTest();
    }        
    
    /**
     * Wrapping used to broke relative references.
     */
    private void relativeReferenceTest() throws Exception {

        final boolean pass[] = {false};

        try {
            URL url = getClass().getResource("data/RelativeTest.dtd");
            System.out.println("URL:" + url);
            InputSource input = new InputSource(url.toExternalForm());
            XMLReader peer = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
            peer.setDTDHandler(new DefaultHandler() {
                public void notationDecl(String name, String publicId, String systemId) {
                    if ("notation".equals(name)) pass[0] = true;
                }
            });
            SAXEntityParser parser = new SAXEntityParser(peer, false);
            parser.parse(input);
        }
        finally {
            assertTrue("External entity not reached!", pass[0]);
        }
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
