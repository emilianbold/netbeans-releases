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

package org.netbeans.modules.xml.tools.actions;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.Node;

public class ValidateActionTest extends AbstractCheckTest {
    
    /** Creates new ValidateActionTest */
    public ValidateActionTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    // *** Not  well-formed ***
    
    /** Validates document with incorrectly nested tags */
    public void testIncorrectlyNestedTags() throws Exception {
        performAction("IncorrectlyNestedTags.xml", new int[] {6});
    }
    
    /** Validates document where missing closing tags */
    public void testMissingClosingTag() throws Exception {
        performAction("MissingClosingTag.xml", new int[] {7});
    }
    
    /** Validates document without root element */
    public void testMissingRootElement() throws Exception {
        performAction("MissingRootElement.xml", new int[] {-1});
    }

    // *** Not valid but well-formed (DTD) ***
    
    /** Validates document with undeclared element */
    public void testInvalidElementName() throws Exception {
        performAction("InvalidElementName.xml", new int[] {9, 11});
    }
    
    /** Validates document with inaccessble DTD */
    public void testInaccessbleDTD() throws Exception {
        performAction("InaccessbleDTD.xml", new int[] {3});
    }
    
    // *** Not valid but well-formed (Schema) ***

    /** Validates document according to schema */
    public void testInvalidElementNameSD() throws Exception {
        performAction("InvalidElementNameSD.xml", new int[] {15});
    }

    /** Validates document according to schema */
    public void testInvalidSchemaLocationSD() throws Exception {
        performAction("InvalidSchemaLocationSD.xml", new int[] {6});
    }
    
    // *** Valid (DTD) ***
    
    /** Validates document where DTD is distributed in several folders*/
    public void testDistributedDTD() throws Exception {
        performAction("DistributedDTD.xml", 0);
    }
    
    // *** Valid (Schema) ***
    
    /** Validates document according to schema */
    public void testValidSD() throws Exception {
        performAction("ValidSD.xml", 0);
    }
    
    // LIBS ////////////////////////////////////////////////////////////////////
    
    /** Check all selected nodes. */
    protected QaIOReporter performAction(Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0))
            fail("Ileegal argumet 'null'");
        
        QaIOReporter reporter = new QaIOReporter();
        for (int i = 0; i<nodes.length; i++) {
            ValidateXMLCookie cake = (ValidateXMLCookie) nodes[i].getCookie(ValidateXMLCookie.class);
            if (cake == null) fail("Cannot get 'ValidateXMLCookie'.");;
            cake.validateXML(reporter);
        }
        return reporter;
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
//    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new ValidateActionTest("testInvalidSchemaLocationSD"));
//        return suite;
//    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DEBUG = true;
        // TestRunner.run(suite());
        TestRunner.run(ValidateActionTest.class);
    }
}
