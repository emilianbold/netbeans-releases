/*
 * TextTest.java
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
public class TextTest extends TestCase {
    
    public TextTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        xmlModel.sync();
        text = (Text)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getChildNodes().item(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TextTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeValue() {
        System.out.println("getNodeValue");
        
        String expResult = "Vidhya Narayanan\n    ";
        String result = text.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeType() {
        System.out.println("getNodeType");
        
        short expResult = org.w3c.dom.Node.TEXT_NODE;
        short result = text.getNodeType();
        assertEquals("getNodeType must return TEXT_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeName() {
        System.out.println("getNodeName");
        
        String expResult = "#text";
        String result = text.getNodeName();
        assertEquals("getNodeName must return #text",expResult, result);
    }

    /**
     * Test of getNamespaceURI method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNamespaceURI() {
        System.out.println("getNamespaceURI");
        
        String result = text.getNamespaceURI();
        assertNull(result);
    }

    /**
     * Test of getText method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetText() {
        System.out.println("getText");
        
        String expResult = "Vidhya Narayanan\n    ";
        String result = text.getText();
        assertEquals(expResult, result);
    }

    /**
     * Test of setText method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testSetText() {
        System.out.println("setText");
        String newText = "Another Person";
        try {
            text.setText(newText);
            assertTrue("setText must throw exception for text node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Text newTextNode = (Text)text.clone(true,false,false);
        try {
            newTextNode.setText(newText);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setText must not throw exception for text node not in tree",false);
        }
        xmlModel.modify(text,newTextNode);
        text = (Text)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getChildNodes().item(0);
        assertEquals(newText, text.getText());
    }

    private XDMModel xmlModel;
    private Text text;
    
}
