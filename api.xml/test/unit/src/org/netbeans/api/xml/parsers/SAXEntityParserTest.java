/*
 * SAXEntityParserTest.java
 * NetBeans JUnit based test
 *
 * Created on April 16, 2002, 5:09 PM
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
        
        InputSource input = new InputSource(new StringReader("<!ELEMENT x ANY>"));
        
        XMLReader peer = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
        TestDeclHandler dtdHandler = new TestDeclHandler();
        peer.setProperty("http://xml.org/sax/properties/declaration-handler", dtdHandler);
        SAXEntityParser parser = new SAXEntityParser(peer, false);
        parser.parse(input);

        // Add your test code below by replacing the default call to fail.
        assertTrue("DTD entity parser did not detected x decl!", dtdHandler.pass);

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
