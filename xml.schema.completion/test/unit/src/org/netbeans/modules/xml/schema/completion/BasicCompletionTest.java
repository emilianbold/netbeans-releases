/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;

/**
 *
 * @author Samaresh
 */
public class BasicCompletionTest extends AbstractTestCase {
    
    static final String PO_INSTANCE_DOCUMENT = "resources/PO.xml";
    static final String TEST_INSTANCE_DOCUMENT = "resources/Test.xml";
    
    public BasicCompletionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new BasicCompletionTest("testPurchaseOrder"));
        suite.addTest(new BasicCompletionTest("testEmptyTag"));
        suite.addTest(new BasicCompletionTest("testWildcard1"));
        suite.addTest(new BasicCompletionTest("testWildcard2"));
        suite.addTest(new BasicCompletionTest("testWildcard3"));
        suite.addTest(new BasicCompletionTest("testWildcard4"));
        suite.addTest(new BasicCompletionTest("testSchemaFromRuntimeCatalog"));
        suite.addTest(new BasicCompletionTest("testChildren1"));
        suite.addTest(new BasicCompletionTest("testChildren2"));
        return suite;
    }

    /**
     * Queries elements.
     */
    public void testPurchaseOrder() throws Exception {
        StringBuffer buffer = new StringBuffer();        
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<po:purchaseOrder xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=72
        buffer.append("  xmlns:po=\"http://xml.netbeans.org/schema/PO\"\n"); //offset=47
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/PO PO.xsd\">\n"); //offset=65
        buffer.append("  <\n");
        buffer.append("</po:purchaseOrder>\n");
        setupCompletion(PO_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(227);
        String[] expectedResult = {"po:shipTo", "po:billTo", "po:comment", "po:items"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries an empty tag.
     */
    public void testEmptyTag() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA3 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\">\n");
        buffer.append("  <A31 />\n");
        buffer.append("</A:rootA3>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(226);
        String[] expectedResult = {"attrA31", "attrA32"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\">\n");
        buffer.append("  <\n");
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(222);
        String[] expectedResult = {"A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n");
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\">\n");
        buffer.append("  <\n");
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(265);
        String[] expectedResult = {"ns1:rootB1", "ns1:rootB2", "A:rootA1", "A:rootA2",
        "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA1 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xmlns:B=\"http://xml.netbeans.org/schema/TNSB\"\n"); //offset=48
        buffer.append("  xmlns:C=\"http://xml.netbeans.org/schema/TNSC\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n"); //offset=64
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\n"); //offset=44
        buffer.append("  http://xml.netbeans.org/schema/TNSC C.xsd\">\n"); //offset=46
        buffer.append("  <\n"); //offset=04
        buffer.append("</A:rootA1>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2",
        "A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<A:rootA2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=64
        buffer.append("  xmlns:A=\"http://xml.netbeans.org/schema/TNSA\"\n"); //offset=48
        buffer.append("  xmlns:B=\"http://xml.netbeans.org/schema/TNSB\"\n"); //offset=48
        buffer.append("  xmlns:C=\"http://xml.netbeans.org/schema/TNSC\"\n"); //offset=48
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/TNSA A.xsd\n"); //offset=64
        buffer.append("  http://xml.netbeans.org/schema/TNSB B.xsd\n"); //offset=44
        buffer.append("  http://xml.netbeans.org/schema/TNSC C.xsd\">\n"); //offset=46
        buffer.append("  <\n"); //offset=04
        buffer.append("</A:rootA2>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
        assertResult(items, expectedResult);
    }
        
    public void testSchemaFromRuntimeCatalog() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<persistence xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=67
        buffer.append("  xmlns=\"http://java.sun.com/xml/ns/persistence\"\n"); //offset=49
        buffer.append("  xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd\">\n"); //offset=122
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("  <\n"); //offset=06
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("</persistence>");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(304);
//        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
//            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
//        assertResult(items, expectedResult);
    }
    
    public void testChildren1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=70
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=59
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=78
        buffer.append("  <ns0:uninstallList>\n"); //offset 23
        buffer.append("  <\n"); //offset 9
        buffer.append("  </ns0:uninstallList>\n");
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(278);
        String[] expectedResult = {"ns0:uninstallSteps"};
        assertResult(items, expectedResult);
    }

    public void testChildren2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=70
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=59
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=78
        buffer.append("  <ns0:installList/>\n"); //offset 21
        buffer.append("  <\n"); //offset 9
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(276);
        String[] expectedResult = {"ns0:installList","ns0:uninstallList"};
        assertResult(items, expectedResult);
    }
}
