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

package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.text.Document;
import junit.framework.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.filesystems.FileStateInvalidException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.*;
import org.netbeans.api.xml.parsers.*;

/**
 * Trivial golden type support tests.
 * <p>
 * It tests class that is exposed by CheckXMLSupport 
 * or ValidateXMLSupport.
 *
 * @author Petr Kuzel
 */
public class SharedXMLSupportTest extends TestCase {
    
    public SharedXMLSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SharedXMLSupportTest.class);
        
        return suite;
    }
    
    /** Test of checkXML method, of class org.netbeans.spi.xml.cookies.SharedXMLSupport. */
    public void testCheckXML() {
        System.out.println("testCheckXML");
                
        URL dtd = getClass().getResource("data/DTD.dtd");
        URL entity = getClass().getResource("data/Entity.ent");
        URL invalidDTD = getClass().getResource("data/InvalidDTD.dtd");
        URL invalidDocument = getClass().getResource("data/InvalidDocument.xml");
        URL invalidEntity = getClass().getResource("data/InvalidEntity.ent");
        URL validDocument = getClass().getResource("data/ValidDocument.xml");
        URL wellformedDocument = getClass().getResource("data/WellformedDocument.xml");
        URL namespacesDocument = getClass().getResource("data/NamespacesDocument.xml");
        
        CheckXMLSupport support;
        support = new CheckXMLSupport(new InputSource(dtd.toExternalForm()), CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE);
        assertTrue("DTD check failed!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(entity.toExternalForm()), CheckXMLSupport.CHECK_ENTITY_MODE);
        assertTrue("Entity check failed!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(invalidDTD.toExternalForm()), CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE);
        assertTrue("Invalid DTD must not pass!",  support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(invalidDocument.toExternalForm()));
        assertTrue("Invalid document must not pass", support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(invalidEntity.toExternalForm()), CheckXMLSupport.CHECK_ENTITY_MODE);
        assertTrue("Invalid rntity must not pass!", support.checkXML(null) == false);

        support = new CheckXMLSupport(new InputSource(validDocument.toExternalForm()));
        assertTrue("Valid document must pass!", support.checkXML(null));

        support = new CheckXMLSupport(new InputSource(wellformedDocument.toExternalForm()));
        assertTrue("Wellformed document must pass", support.checkXML(null));

        Observer observer = new Observer();
        support = new CheckXMLSupport(new InputSource(namespacesDocument.toExternalForm()));
        assertTrue("Wellformed document with namespaces must pass", support.checkXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);
        
    }
    
    /** Test of validateXML method, of class org.netbeans.spi.xml.cookies.SharedXMLSupport. */
    public void testValidateXML() {
        System.out.println("testValidateXML");

        URL dtd = getClass().getResource("data/DTD.dtd");
        URL entity = getClass().getResource("data/Entity.ent");
        URL invalidDTD = getClass().getResource("data/InvalidDTD.dtd");
        URL invalidDocument = getClass().getResource("data/InvalidDocument.xml");
        URL invalidEntity = getClass().getResource("data/InvalidEntity.ent");
        URL validDocument = getClass().getResource("data/ValidDocument.xml");
        URL wellformedDocument = getClass().getResource("data/WellformedDocument.xml");
        URL validNamespacesDocument = getClass().getResource("data/ValidNamespacesDocument.xml");
        URL conformingNamespacesDocument = getClass().getResource("data/ConformingNamespacesDocument.xml");
        
        SharedXMLSupport support;
        support = new ValidateXMLSupport(new InputSource(dtd.toExternalForm()));
        assertTrue("DTD validation must fail!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(entity.toExternalForm()));
        assertTrue("Entity validation must fail!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidDTD.toExternalForm()));
        assertTrue("Invalid DTD must not pass!",  support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidDocument.toExternalForm()));
        assertTrue("Invalid document must not pass", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(invalidEntity.toExternalForm()));
        assertTrue("Invalid rntity must not pass!", support.validateXML(null) == false);

        support = new ValidateXMLSupport(new InputSource(validDocument.toExternalForm()));
        assertTrue("Valid document must pass!", support.validateXML(null));

        support = new ValidateXMLSupport(new InputSource(wellformedDocument.toExternalForm()));
        assertTrue("Wellformed document must not pass", support.validateXML(null) == false);

        Observer observer = new Observer();
        support = new ValidateXMLSupport(new InputSource(validNamespacesDocument.toExternalForm()));
        assertTrue("Valid document with namespaces must pass", support.validateXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);

        observer = new Observer();
        support = new ValidateXMLSupport(new InputSource(conformingNamespacesDocument.toExternalForm()));
        assertTrue("Conforming document must pass", support.validateXML(observer));
        assertTrue("Unexpected warnings!", observer.getWarnings() == 0);
        
    }
    
    private static class Observer implements CookieObserver {
        private int warnings;
        public void receive(CookieMessage msg) {
            if (msg.getLevel() >= msg.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }
    };
        
}
