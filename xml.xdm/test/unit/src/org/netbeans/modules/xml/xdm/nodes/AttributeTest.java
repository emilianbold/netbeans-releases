/*
 * AttributeTest.java
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
public class AttributeTest extends TestCase {
    
    public AttributeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        um = new javax.swing.undo.UndoManager();
        um.setLimit(10);
        xmlModel.addUndoableEditListener(um);
        xmlModel.sync();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AttributeTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeType() {
        System.out.println("getNodeType");
        
        short expResult = org.w3c.dom.Node.ATTRIBUTE_NODE;
        short result = attr.getNodeType();
        assertEquals("getNodeType must return ATTRIBUTE_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeName() {
        System.out.println("getNodeName");
        
        String expResult = "ssn";
        String result = attr.getNodeName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeValue() {
        System.out.println("getNodeValue");
        
        String expResult = "xx-xx-xxxx";
        String result = attr.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOwnerElement method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetOwnerElement() {
        System.out.println("getOwnerElement");
        
        Element expResult = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        
        org.w3c.dom.Element result = attr.getOwnerElement();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLocalName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetLocalName() {
        System.out.println("getLocalName");
        
        String expResult = "ssn";
        String result = attr.getLocalName();
        assertEquals(expResult, result);

        Attribute instance = new Attribute("xs:attribute");
        expResult = "attribute";
        result = instance.getLocalName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetPrefix() {
        System.out.println("getPrefix");
        
        assertNull(attr.getPrefix());

        Attribute instance = new Attribute("xs:attribute");
        String expResult = "xs";
        String result = instance.getPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetPrefix() {
        System.out.println("setPrefix");
        Attribute oldAttr = attr;
        String oldPrefix = attr.getPrefix();
        String newPrefix = "xs";
        try {
            attr.setPrefix(newPrefix);
            assertTrue("setPrefix must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setPrefix(newPrefix);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newPrefix,attr.getPrefix());

        //try undo and make sure original tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertNull(attr.getPrefix());
        um.redo();
        // try to remove prefix
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        oldAttr = attr;
        newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setPrefix("");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertNull(attr.getPrefix());

        //try undo and make sure previous tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(newPrefix,attr.getPrefix());
    }

    /**
     * Test of getName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetName() {
        System.out.println("getName");
        
        String expResult = "ssn";
        String result = attr.getName();
        assertEquals(expResult, result);

        Attribute instance = new Attribute("xs:attribute");
        expResult = "xs:attribute";
        result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetName() {
        System.out.println("setName");
        
        Attribute oldAttr = attr;
        String oldName = attr.getName();
        String newName = "ssn1";
        try {
            attr.setName(newName);
            assertTrue("setName must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setName(newName);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setName must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newName,attr.getName());

        //try undo and make sure old tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(oldName,attr.getName());
    }

    /**
     * Test of getValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetValue() {
        System.out.println("getValue");
        
        String expResult = "xx-xx-xxxx";
        String result = attr.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetValue() {
        System.out.println("setValue");
        
        Attribute oldAttr = attr;
        String oldValue = oldAttr.getValue();
        String newValue = "123-45-6789";
        try {
            attr.setValue(newValue);
            assertTrue("setValue must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setValue(newValue);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setValue must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newValue,attr.getValue());
        //try undo and make sure old tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(oldValue,attr.getValue());
    }
    
    private XDMModel xmlModel;
    private Attribute attr;
    private javax.swing.undo.UndoManager um;
}
