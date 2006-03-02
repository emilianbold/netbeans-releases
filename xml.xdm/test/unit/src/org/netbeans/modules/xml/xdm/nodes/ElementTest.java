/*
 * ElementTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author ajit
 */
public class ElementTest extends TestCase {
    
    public ElementTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        xmlModel.sync();
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ElementTest.class);
        
        return suite;
    }
    
    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetNodeType() {
        System.out.println("getNodeType");
        
        short expResult = org.w3c.dom.Node.ELEMENT_NODE;
        short result = elem.getNodeType();
        assertEquals("getNodeType must return ATTRIBUTE_NODE",expResult, result);
    }
    
    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetNodeName() {
        System.out.println("getNodeName");
        
        String expResult = "employee";
        String result = elem.getNodeName();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getTagName method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetTagName() {
        System.out.println("getTagName");
        
        String expResult = "employee";
        String result = elem.getTagName();
        assertEquals(expResult, result);
        
        Element instance = new Element("xs:element");
        expResult = "xs:element";
        result = instance.getTagName();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetPrefix() {
        System.out.println("getPrefix");
        
        assertNull(elem.getPrefix());
        
        Element instance = new Element("xs:element");
        String expResult = "xs";
        String result = instance.getPrefix();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of setPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testSetPrefix() {
        System.out.println("setPrefix");
        
        Element origElem = elem;
        String origPrefix = elem.getPrefix();
        String newPrefix = "xs";
        try {
            elem.setPrefix(newPrefix);
            assertTrue("setPrefix must throw exception for element node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Element newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setPrefix(newPrefix);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertEquals(newPrefix,elem.getPrefix());
        //make sure old tree is not changed
        assertEquals(origPrefix,origElem.getPrefix());
        
        // try to remove prefix
        Element modifiedElem = elem;
        newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setPrefix("");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertNull(elem.getPrefix());
        //make sure modifiedElem has prefix = xs
        assertEquals(newPrefix,modifiedElem.getPrefix());
    }
    
    /**
     * Test of getLocalName method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetLocalName() {
        System.out.println("getLocalName");
        
        String expResult = "employee";
        String result = elem.getLocalName();
        assertEquals(expResult, result);
        
        Element instance = new Element("xs:element");
        expResult = "element";
        result = instance.getLocalName();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of setLocalName method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testSetLocalName() {
        System.out.println("setLocalName");

        Element origElem = elem;
        String origName = elem.getLocalName();
        String newName = "employee1";
        try {
            elem.setLocalName(newName);
            assertTrue("setLocalName must throw exception for element node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Element newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setLocalName(newName);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setLocalName must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertEquals(newName,elem.getLocalName());
        //make sure old tree is not changed
        assertEquals(origName,origElem.getLocalName());
    }

    /**
     * Test of getAttributes method, of class org.netbeans.modules.xml.xdm.nodes.NodeImpl.
     */
    public void testGetAttributes() {
        System.out.println("getAttributes");

        NamedNodeMap attributes = elem.getAttributes();
        assertEquals(4, attributes.getLength());
        assertNotNull(attributes.getNamedItem("ssn"));
        assertSame(attributes.getNamedItem("ssn"),attributes.item(0));
        assertNotNull(attributes.getNamedItem("id"));
        assertSame(attributes.getNamedItem("id"),attributes.item(1));
        assertNotNull(attributes.getNamedItem("address"));
        assertSame(attributes.getNamedItem("address"),attributes.item(2));
        assertNotNull(attributes.getNamedItem("phone"));
        assertSame(attributes.getNamedItem("phone"),attributes.item(3));
        assertNull(attributes.getNamedItem("ssn1"));
        
        Element company = (Element)xmlModel.getDocument().getDocumentElement();
        NamedNodeMap companyAttrs = company.getAttributes();
        assertEquals(1, companyAttrs.getLength());
        assertNotNull(companyAttrs.getNamedItem("xmlns"));
    }

    /**
     * Test of getChildNodes method, of class org.netbeans.modules.xml.xdm.nodes.NodeImpl.
     */
    public void testGetChildNodes() {
        System.out.println("getChildNodes");

        Element company = (Element)xmlModel.getDocument().getDocumentElement();
        NodeList children = company.getChildNodes();
        assertEquals(7, children.getLength());
        assertTrue(children.item(0) instanceof Text);
        assertTrue(children.item(1) instanceof Element);
        assertEquals("employee",children.item(1).getNodeName());
        assertTrue(children.item(2) instanceof Text);
        assertTrue(children.item(3) instanceof Text);
        assertEquals("<!-- comment -->",children.item(3).getNodeValue());
        assertTrue(children.item(4) instanceof Text);
        assertTrue(children.item(5) instanceof Element);
        assertEquals("employee",children.item(5).getNodeName());
        assertTrue(children.item(6) instanceof Text);
    }

    /**
     * Test of getAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetAttribute() {
        System.out.println("getAttribute");

        String name = "ssn";
        String expResult = "xx-xx-xxxx";
        String result = elem.getAttribute(name);
        assertEquals(expResult, result);
        // try for non-existent attribute
        assertNull(elem.getAttribute("ssn1"));
    }

    /**
     * Test of getAttributeNode method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testGetAttributeNode() {
        System.out.println("getAttributeNode");

        String name = "ssn";
        Attribute expResult = (Attribute)elem.getAttributes().item(0);
        Attribute result = elem.getAttributeNode(name);
        assertSame(expResult, result);
        // try for non-existent attribute
        assertNull(elem.getAttributeNode("ssn1"));
    }

    /**
     * Test of hasAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Element.
     */
    public void testHasAttribute() {
        System.out.println("hasAttribute");
        
        String name="ssn";
        boolean expResult = true;
        boolean result = elem.hasAttribute(name);
        assertEquals(expResult, result);
        // try for non-existent attribute
        assertFalse(elem.hasAttribute("ssn1"));
    }
//
//    /**
//     * Test of getAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testGetAttributeNS() {
//        System.out.println("getAttributeNS");
//
//        String namespaceURI = "";
//        String localName = "";
//        Element instance = new Element();
//
//        String expResult = "";
//        String result = instance.getAttributeNS(namespaceURI, localName);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getAttributeNodeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testGetAttributeNodeNS() {
//        System.out.println("getAttributeNodeNS");
//
//        String namespaceURI = "";
//        String localName = "";
//        Element instance = new Element();
//
//        Attribute expResult = null;
//        Attribute result = instance.getAttributeNodeNS(namespaceURI, localName);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of hasAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testHasAttributeNS() {
//        System.out.println("hasAttributeNS");
//
//        String namespaceURI = "";
//        String localName = "";
//        Element instance = new Element();
//
//        boolean expResult = true;
//        boolean result = instance.hasAttributeNS(namespaceURI, localName);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of setAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testSetAttribute() {
//        System.out.println("setAttribute");
//
//        String name = "";
//        String value = "";
//        Element instance = new Element();
//
//        instance.setAttribute(name, value);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of setAttributeNode method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testSetAttributeNode() {
//        System.out.println("setAttributeNode");
//
//        org.w3c.dom.Attr newAttr = null;
//        Element instance = new Element();
//
//        Attribute expResult = null;
//        Attribute result = instance.setAttributeNode(newAttr);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of removeAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testRemoveAttribute() {
//        System.out.println("removeAttribute");
//
//        String name = "";
//        Element instance = new Element();
//
//        instance.removeAttribute(name);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of removeAttributeNode method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testRemoveAttributeNode() {
//        System.out.println("removeAttributeNode");
//
//        org.w3c.dom.Attr oldAttr = null;
//        Element instance = new Element();
//
//        Attribute expResult = null;
//        Attribute result = instance.removeAttributeNode(oldAttr);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of setAttributeNodeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testSetAttributeNodeNS() {
//        System.out.println("setAttributeNodeNS");
//
//        org.w3c.dom.Attr newAttr = null;
//        Element instance = new Element();
//
//        org.w3c.dom.Attr expResult = null;
//        org.w3c.dom.Attr result = instance.setAttributeNodeNS(newAttr);
//        assertEquals(expResult, result);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of setAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testSetAttributeNS() {
//        System.out.println("setAttributeNS");
//
//        String namespaceURI = "";
//        String qualifiedName = "";
//        String value = "";
//        Element instance = new Element();
//
//        instance.setAttributeNS(namespaceURI, qualifiedName, value);
//
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of removeAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Element.
//     */
//    public void testRemoveAttributeNS() {
//        System.out.println("removeAttributeNS");
//
//        String namespaceURI = "";
//        String localName = "";
//        Element instance = new Element();
//
//        instance.removeAttributeNS(namespaceURI, localName);
//
//        fail("The test case is empty.");
//    }
//
    private XDMModel xmlModel;
    private Element elem;
}
