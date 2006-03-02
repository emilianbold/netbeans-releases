/*
 * XMLModelTest.java
 * JUnit based test
 *
 * Created on August 5, 2005, 12:13 PM
 */

package org.netbeans.modules.xml.xdm;
import java.beans.PropertyChangeListener;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.nodes.*;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class XDMModelTest extends TestCase {
    
    public XDMModelTest(String testName) {
        super(testName);
    }
    
    public void testAddNegative() throws Exception {
        // verify that a node which is already in the tree cannot be added
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        try {
            model.add(company, employee, 0);
            fail("adding a node already in the tree should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
    }
    
    public void testModifyNegative() throws Exception {
        // verify that a node which is already in the tree cannot be added
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        try {
            model.modify(employee, employee);
            fail("modifying a node already in the tree should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
        
        // now try to substitute a different node
        try {
            Element e = (Element)model.getDocument().createElement("");
            model.modify(employee, e);
            fail("attempting to modify a node with a non equal node should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
    }
    
    /**
     * Test of add method, of class xml.nodes.XMLModel.
     */
    public void testAdd() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        FlushVisitor fv = new FlushVisitor();
        String originalText = fv.flushModel(original);
//		 Expected model
//		 Document
//		   Element -- company
//		      Element -- employee
//		          Attribute -- ssn xx-xx-xxxx
//		          Attribute -- id
//		          Attribute -- phone
//		        Text -- Vidhya
//		      End Element
//		   End Element
        
        // first add another child element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Element customer = (Element)model.getDocument().createElement("customer");
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.add(employee,customer,0);
        
        String modifiedText = fv.flushModel(model.getDocument());
        assertNotSame("text should have been modified to add new attribute", originalText,modifiedText);
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_ADDED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotNull("customer should now be in the tree",
                pfrv.findPath(model.getDocument(), customer));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // now verify that the new child element is added in right location
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Node)company.getChildNodes().item(1);
        Element customer2 = (Element) employee.getChildNodes().item(0);
        
        assertEquals("expected name was not set", customer2.getLocalName(), customer.getLocalName());
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        Document newD = model.getDocument();
        um.undo();
        assertSame("model not original tree", model.getDocument(), original);
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
        
        //Adding a brand new element for testing
        company = (Node)model.getDocument().getChildNodes().item(0);
        Element emp = (Element)model.getDocument().createElement("employee");
        
        tl.resetFiredEvents();
        model.add(company,emp,0);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be added", model.PROP_ADDED, tl.getLastEventName());
        
        pfrv = new PathFromRootVisitor();
        assertNotNull("new employee should now be in the tree",
                pfrv.findPath(model.getDocument(), emp));
    }
    
    /**
     * Test of append method, of class xml.nodes.XMLModel.
     */
    public void testAppend() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
//		 Expected model
//		 Document
//		   Element -- company
//		      Element -- employee
//		        Attribute -- ssn xx-xx-xxxx
//		        Attribute -- id
//		        Attribute -- phone
//		        Text -- Vidhya
//		      End Element
//		   End Element
        
        // first append another child element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Element customer = (Element)model.getDocument().createElement("customer");
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.append(employee,customer);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_ADDED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotNull("customer should now be in the tree",
                pfrv.findPath(model.getDocument(), customer));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // now verify that the new child element is added in right location (at the end)
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Node)company.getChildNodes().item(1);
        Element customer2 = (Element) employee.getChildNodes().item(employee.getChildNodes().getLength()-1);
        
        assertEquals("expected name was not set", customer2.getLocalName(), customer.getLocalName());
        
        //Appending a brand new element with attributes
        company = (Node)model.getDocument().getChildNodes().item(0);
        Element emp2 = (Element)model.getDocument().createElement("employee");
        Attribute att = (Attribute)model.getDocument().createAttribute("id2");
        att.setValue("987");
        emp2.setAttributeNode(att);
        
        tl.resetFiredEvents();
        model.append(company, emp2);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be added", model.PROP_ADDED, tl.getLastEventName());
        
        pfrv = new PathFromRootVisitor();
        assertNotNull("new employee should now be in the tree",
                pfrv.findPath(model.getDocument(), emp2));
    }
    
    /**
     * Test of delete method, of class xml.nodes.XMLTreeGenerator.
     */
    public void testDelete() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        // Expected model
        // Document
        //   Element -- company
        //      Element -- employee
        //        Attribute -- ssn xx-xx-xxxx
        //        Attribute -- id
        //        Attribute -- phone
        //        Text -- Vidhya
        //      End Element
        //   End Element
        
        // first get text element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Text txt = (Text)employee.getChildNodes().item(0);
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.delete(txt);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_DELETED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNull("txt should no longer be in the tree",
                pfrv.findPath(model.getDocument(), txt));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        Document newD = model.getDocument();
        um.undo();
        assertSame("model not original tree", model.getDocument(), original);
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
    }
    
    /**
     * Test of modify method, of class xml.nodes.XMLTreeGenerator.
     */
    public void testModify() throws Exception {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        // Expected model
        // Document
        //   Element -- company
        //      Element -- employee
        //        Attribute -- ssn xx-xx-xxxx
        //        Attribute -- id
        //        Attribute -- phone
        //        Text -- Vidhya
        //      End Element
        //   End Element
        
        // first get employee element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Element employee = (Element)company.getChildNodes().item(1);
        
        Element employee2 = (Element)employee.clone(true,false,false);
        employee2.setLocalName("employee2");
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.modify(employee,employee2);
        
        assertTrue("only one event should be fired", tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_MODIFIED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotSame("original company should not be in tree",
                company, pfrv.findPath(model.getDocument(), company).get(0));
        assertNotSame("original employee should not be tree",
                employee, pfrv.findPath(model.getDocument(), employee).get(0));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // now verify that the new employee is what we set
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Element)company.getChildNodes().item(1);
        
        assertEquals("expected name was not set", employee.getLocalName(), employee2.getLocalName());
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        // Before undo, test flush to make sure that the tree flushed has the
        // new attribute changes
        Document newD = model.getDocument();
        um.undo();
        //System.out.println(sd.getText(0,sd.getLength()));
        assertSame("model not original tree", model.getDocument(), original);
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
    }
    
    public void testFlush() throws Exception {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        String origContent = sd.getText(0,sd.getLength());
        model.flush();
        String flushContent = sd.getText(0,sd.getLength());
        assertEquals("expected same content after flush", origContent, flushContent);
        
        Document oldDoc = model.getDocument();
        assertSame("Models before and after flush are same ", original, oldDoc);
        
        //Force sync to make sure the new model is the same as the current one
        model.sync();
        
        Document newDoc = model.getDocument();
        assertSame("Models before and after flush/sync are same ", oldDoc, newDoc);
        //TODO should have a good way of testing old and new models.
    }
    
    public void testSyncAndNamespace() throws Exception {
        javax.swing.text.Document swdoc = Util.getResourceAsDocument("TestSyncNamespace.wsdl");
        XDMModel m = Util.loadXDMModel(swdoc);
        Element root = (Element) m.getCurrentDocument().getDocumentElement();
        NodeList nl = root.getChildNodes();
        Element messageE = null;
        for (int i=0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element e = (Element) nl.item(i);
                if (e.getLocalName().equals("message")) {
                    messageE = e;
                }
            }
        }
        assertNotNull(messageE);
        assertEquals("http://schemas.xmlsoap.org/wsdl/" , messageE.getNamespaceURI());
        
        Util.setDocumentContentTo(swdoc, "TestSyncNamespace_1.wsdl");
        m.sync();
        
        nl = messageE.getChildNodes();
        Element partE = null;
        for (int i=0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element e = (Element) nl.item(i);
                if (e.getLocalName().equals("part")) {
                    partE = e;
                }
            }
        }
        assertNotNull(partE);
        //FIXME
        //assertEquals("http://schemas.xmlsoap.org/wsdl/" , messageE.getNamespaceURI());
        //assertEquals("http://schemas.xmlsoap.org/wsdl/" , partE.getNamespaceURI());
    }
    
    static class TestListener implements PropertyChangeListener {
        private String eventName;
        private int count = 0;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            eventName = evt.getPropertyName();
            count++;
        }
        
        public int getEventsFired() {
            return count;
        }
        
        public String getLastEventName() {
            return eventName;
        }
        
        public void resetFiredEvents() {
            count = 0;
        }
    }
    
    protected void setUp() throws Exception {
        um = new UndoManager();
        sd = Util.getResourceAsDocument("test.xml");
        model = new XDMModel(sd);
        model.sync();
    }
    
    private javax.swing.text.Document sd;
    private XDMModel model;
    private UndoManager um;
}
