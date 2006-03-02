/*
 * PathFromRootVisitorTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 2:08 PM
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import junit.framework.*;
import java.util.List;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author ajit
 */
public class PathFromRootVisitorTest extends TestCase {
    
    public PathFromRootVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("visitor/test.xml");
        xmlModel.sync();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(PathFromRootVisitorTest.class);
        
        return suite;
    }

    public void testFindPath() {
        System.out.println("findPath");
        
        Document root = xmlModel.getDocument();
        PathFromRootVisitor instance = new PathFromRootVisitor();
        
        // try to find path to company
        Element company = (Element)root.getChildNodes().item(0);
        List<Node> result = instance.findPath(root, company);
        assertEquals(2,result.size());
        assertEquals(company,result.get(0));
        assertEquals(root,result.get(1));

        // try to find path to attribute
        Element employee = (Element)company.getChildNodes().item(1);
        Attribute attr = (Attribute)employee.getAttributes().item(0);
        result = instance.findPath(root, attr);
        assertEquals(4,result.size());
        assertEquals(attr,result.get(0));
        assertEquals(employee,result.get(1));
        assertEquals(company,result.get(2));
        assertEquals(root,result.get(3));
    }

    private XDMModel xmlModel = null;
}
