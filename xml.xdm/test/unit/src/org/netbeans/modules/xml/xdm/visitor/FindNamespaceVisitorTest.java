/*
 * FindNamespaceVisitorTest.java
 * JUnit based test
 *
 * Created on November 18, 2005, 10:34 AM
 */

package org.netbeans.modules.xml.xdm.visitor;

import junit.framework.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.w3c.dom.NamedNodeMap;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author ajit
 */
public class FindNamespaceVisitorTest extends TestCase {
    
    public FindNamespaceVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FindNamespaceVisitorTest.class);
        
        return suite;
    }

    /**
     * Test of findNamespace method, of class org.netbeans.modules.xml.xdm.visitor.FindNamespaceVisitor.
     */
    public void testFindNamespace() throws Exception {
        System.out.println("findNamespace");
        
        FindNamespaceVisitor instance = new FindNamespaceVisitor();
        XDMModel xdmModel = Util.loadXDMModel("diff/TravelItinerary1.xsd");
        Document root = xdmModel.getDocument();
        
        Node target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3);
        String expResult = "http://www.w3.org/2001/XMLSchema";
        long before = System.currentTimeMillis();
        String result = instance.findNamespace(root, target);
        System.out.println("time to find namespace"+(System.currentTimeMillis()-before));
        assertEquals(expResult, result);

        target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3).getAttributes().item(0);
        expResult = null;
        before = System.currentTimeMillis();
        result = instance.findNamespace(root, target);
        System.out.println("time to find namespace"+(System.currentTimeMillis()-before));
        assertEquals(expResult, result);
    }
    
}
