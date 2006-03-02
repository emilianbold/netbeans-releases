/*
 * DocumentTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;

/**
 *
 * @author ajit
 */
public class DocumentTest extends TestCase {
    
    public DocumentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        xmlModel.sync();
        doc = xmlModel.getDocument();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DocumentTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetNodeType() {
        System.out.println("getNodeType");
        
        short expResult = org.w3c.dom.Node.DOCUMENT_NODE;
        short result = doc.getNodeType();
        assertEquals("getNodeType must return DOCUMENT_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetNodeName() {
        System.out.println("getNodeName");
        
        String expResult = "#document";
        String result = doc.getNodeName();
        assertEquals("getNodeName must return #document",expResult, result);
    }

    /**
     * Test of createElement method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateElement() {
        System.out.println("createElement");
        
        String tagName = "newElement";
        org.w3c.dom.Element result = doc.createElement(tagName);
        assertEquals(tagName, result.getTagName());
    }

    /**
     * Test of createAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateAttribute() {
        System.out.println("createAttribute");
        
        String name = "attrName";
        org.w3c.dom.Attr result = doc.createAttribute(name);
        assertEquals(name, result.getName());
    }

    /**
     * Test of createElementNS method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateElementNS() {
        System.out.println("createElementNS");
        
        String namespaceURI = "";
        String qualifiedName = "xs:element";
        org.w3c.dom.Element result = doc.createElementNS(namespaceURI, qualifiedName);
        assertEquals("element", result.getLocalName());
        assertEquals("xs", result.getPrefix());
    }

    /**
     * Test of createAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateAttributeNS() {
        System.out.println("createAttributeNS");
        
        String namespaceURI = "";
        String qualifiedName = "xs:attribute";
        org.w3c.dom.Attr result = doc.createAttributeNS(namespaceURI, qualifiedName);
        assertEquals("attribute", result.getLocalName());
        assertEquals("xs", result.getPrefix());
    }

    /**
     * Test of getDocumentElement method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetDocumentElement() {
        System.out.println("getDocumentElement");
        
        Element expResult = (Element)doc.getChildNodes().item(0);
        Element result = (Element)doc.getDocumentElement();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlVersion method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlVersion() {
        System.out.println("getXmlVersion");
        
        String expResult = "1.0";
        String result = doc.getXmlVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlEncoding method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlEncoding() {
        System.out.println("getXmlEncoding");
        
        String expResult = "UTF-8";
        String result = doc.getXmlEncoding();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlStandalone method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlStandalone() {
        System.out.println("getXmlStandalone");
        
        boolean expResult = false;
        boolean result = doc.getXmlStandalone();
        assertEquals(expResult, result);
    }

    private XDMModel xmlModel;
    private Document doc;
}
