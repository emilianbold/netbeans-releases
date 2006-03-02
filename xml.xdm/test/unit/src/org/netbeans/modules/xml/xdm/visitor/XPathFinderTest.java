package org.netbeans.modules.xml.xdm.visitor;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author nn136682
 */
public class XPathFinderTest extends TestCase {
    
    public XPathFinderTest(String testName) {
        super(testName);
    }

    private Document doc;
    protected void setUp() throws Exception {
        System.out.println("setUp loading doc...");
        doc = Util.loadXdmDocument("visitor/address.xsd");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(XPathFinderTest.class);
        
        return suite;
    }

    public void testFindAndGetXpath() throws Exception {
        String expr = "/company/employee[1]";
        Document root = Util.loadXdmDocument("test.xml");
        Node n = new XPathFinder().findNode(root, expr);
        assertNotNull(n);
        
        String xpathString = XPathFinder.getXpath(root, n);
        System.out.println(xpathString);
        assertEquals("getXpath check round-trip", expr, xpathString);
    }
    
    public void testFindNodeWithIndex() throws Exception {
        String expr = "/schema/complexType[2]";
        Node n = new XPathFinder().findNode(doc, expr);
        assertNotNull(n);
        assertEquals("attribute name=", "US-Address", n.getAttributes().getNamedItem("name").getNodeValue());
        
        String xpathString = XPathFinder.getXpath(doc, n);
        System.out.println(xpathString);
        assertEquals("checking round-trip", expr, xpathString);
    }
    
    public void testSelectionByAttribute() throws Exception {
        String expr = "/schema/simpleType[@name='US-State']/restriction/enumeration";
        List<Node> nodes = new XPathFinder().findNodes(doc, expr);
        assertEquals("testSelectionByAttribute", 54, nodes.size());
        
        String xpathString = XPathFinder.getXpath(doc, nodes.get(50));
        System.out.println(xpathString);
        assertEquals("testSelectionByAttribute.2", "/schema/simpleType[1]/restriction[1]/enumeration[51]", xpathString);
    }

    public void testFindAttributeNode() throws Exception {
        String expr = "/schema/complexType[2]/complexContent[1]/extension[1]/@base";
        Node n = new XPathFinder().findNode(doc, expr);
        assertTrue("Attribute node", n instanceof Attribute);

        Attribute attr = (Attribute) n;
        assertEquals("Attribute value", "ipo:Address", attr.getNodeValue());
        
        String xpathString = XPathFinder.getXpath(doc, n);
        System.out.println(xpathString);
        assertEquals("checking round-trip", expr, xpathString);
    }
    
    public void testWithDefaultNamespaceAndRootElementPrefix() throws Exception {
        Document root = Util.loadXdmDocument("visitor/ipo.xml");
        String expr = "/ipo:purchaseOrder/shipTo[1]/street[1]";
        Node n = new XPathFinder().findNode(root, expr);
        assertEquals("no default namespace", "street", n.getNodeName());
        
        String xpathString = XPathFinder.getXpath(root, n);
        System.out.println(xpathString);
        assertEquals("checking round-trip", expr, xpathString);
    }
     
    public void testNonEmptyDefaultNamspacePrefix() throws Exception {
        Document root = Util.loadXdmDocument("visitor/OrgChart.xsd");
        String expr = "/xsd:schema/xsd:complexType[1]/xsd:sequence[1]/xsd:element[1]";
        Node n = new XPathFinder().findNode(root, expr);

        String xpathString = XPathFinder.getXpath(root, n);
        System.out.println(xpathString);
        assertEquals("checking round-trip", expr, xpathString);
    }

    private XDMModel xmlModel = null;
}
