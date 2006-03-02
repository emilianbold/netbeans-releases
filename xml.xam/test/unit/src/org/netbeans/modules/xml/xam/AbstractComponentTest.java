package org.netbeans.modules.xml.xam;

import junit.framework.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xam.TestComponent.A;
import org.netbeans.modules.xml.xam.TestComponent.B;
import org.netbeans.modules.xml.xam.TestComponent.C;
import org.netbeans.modules.xml.xam.TestComponent.D;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractComponentTest extends TestCase {
    
    TestModel model;
    TestComponent p;
    A a1; 
    B b1;
    C c1;
    Listener listener;
    
    public AbstractComponentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        model = Util.loadModel("resources/Empty.xml");
	model.startTransaction();
        p = TestComponent.class.cast(model.getRootComponent());
        assertEquals("setup", p.getName(), "test");
        
        a1 = new A(model, 1);
        b1 = new B(model, 1);
        c1 = new C(model, 1);
        p.appendChild("setup", a1);
        p.appendChild("setup", b1);
        p.appendChild("setup", c1);
        model.endTransaction();
        assertEquals("setup.children", "[a1, b1, c1]", p.getChildren().toString());
        
        listener = new Listener();
        model.addPropertyChangeListener(listener);
    }
    

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractComponentTest.class);
        
        return suite;
    }

    private class Listener implements PropertyChangeListener {
        private String event;
        private Object old;
        private Object now;
        
        public void propertyChange(PropertyChangeEvent evt) {
            assertNotNull(evt);
            if (evt.getPropertyName().equals("setup")) {
                return;
            }
            event = evt.getPropertyName();
            old = evt.getOldValue();
            now = evt.getNewValue();
            System.out.println("Got event "+event+" old="+old+" now="+now);
        }

        public String getEvent() { return event; }
        public Object getOld() { return old; }
        public Object getNow() { return now; }
        public void reset() { event = null; old = null; now = null; }
    }
    
    private void assertEventListener(String name, Object old, Object now) {
        assertEquals(name+".event", name, listener.getEvent());
        assertEquals(name+".old", old, listener.getOld());
        assertEquals(name+".now", now, listener.getNow());
        listener.reset();
    }
    
    public void testInsertAtIndex() throws Exception {
        String propertyName = "testInsertAtIndex";
        TestComponent parent = new TestComponent(model, "test");
	model.startTransaction();
        B b0 = new B(model, 0);
        B b1 = new B(model, 1);
        B b2 = new B(model, 2);
        B b3 = new B(model, 3);
	model.endTransaction();

        model.startTransaction();
        parent.insertAtIndex(propertyName, b1, 0, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b1);
        assertEquals("testInsertAtIndex.res", "[b1]", parent.getChildren().toString());

        model.startTransaction();
        parent.insertAtIndex(propertyName, b2, 1, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b2);
        assertEquals("testInsertAtIndex.res", "[b1, b2]", parent.getChildren().toString());
        
        model.startTransaction();
        parent.insertAtIndex(propertyName, b0, 0, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b0);
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2]", parent.getChildren().toString());

        model.startTransaction();
        parent.insertAtIndex(propertyName, b3, 3, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b3);
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2, b3]", parent.getChildren().toString());
    }
    
    public void testInsertAtIndexRelative() throws Exception {
        String propertyName = "testInsertAtIndexRelative";
	model.startTransaction();
        B b2 = new B(model, 2);
        p.insertAtIndex(propertyName, b2, 1, B.class);
	model.endTransaction();
        assertEventListener(propertyName, null, b2);
        List<B> res1 = p.getChildren(B.class);
        assertEquals("testInsertAtIndexRelative.res1", "[b1, b2]", res1.toString());
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative.res2", "[a1, b1, b2, c1]", res2.toString());
    }

    // a1 b1 c1 -> a1 b0 b1 c1
    public void testInsertAtIndexRelative0() throws Exception {
        String propertyName = "testInsertAtIndexRelative0";
	model.startTransaction();
        B b0 = new B(model, 0);
        p.insertAtIndex(propertyName, b0, 0, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b0);
        
        List<B> res1 = p.getChildren(B.class);
        assertEquals("testInsertAtIndexRelative0.res1", "[b0, b1]", res1.toString());
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative0.res2", "[a1, b0, b1, c1]", res2.toString());
    }

    // a1 b1 c1 -> a1 b1 c1 d1
    public void testInsertAtIndexRelative0Empty() throws Exception {
        String propertyName = "testInsertAtIndexRelative0Empty";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.insertAtIndex(propertyName, d1, 0, D.class);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<D> res1 = p.getChildren(D.class);
        assertEquals("testInsertAtIndexRelative0Empty.res1", "[d1]", res1.toString());
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative0Empty.res2", "[a1, b1, c1, d1]", res2.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeA() throws Exception {
        String propertyName = "testAddBeforeA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testAddBeforeA.res2", "[d1, a1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddBeforeC() throws Exception {
        String propertyName = "testAddBeforeC";
	model.startTransaction();
        C c2 = new C(model, 2);
        p.insertAtIndex("setup", c2, 3, TestComponent.class);
        model.endTransaction();
        listener.reset();
        
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._C);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeC.res", "[a1, b1, d1, c1, c2]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddBeforeBC() throws Exception {
        String propertyName = "testAddBeforeBC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._BC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeBC.res", "[a1, d1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeAC() throws Exception {
        String propertyName = "testAddBeforeAC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._AC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddBeforeBAC() throws Exception {
        String propertyName = "testAddBeforeBAC";
	model.startTransaction();
        D d1 = new D(model, 1);
	
        p.addBefore(propertyName, d1, TestComponent._BAC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeBAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddAfterA() throws Exception {
        String propertyName = "testAddAfterA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testAddAfterA.res2", "[a1, d1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddAfterC() throws Exception {
        String propertyName = "testAddAfterC";
	model.startTransaction();
        C c2 = new C(model, 2);
        p.addAfter("setup", c2, TestComponent._AB);
        model.endTransaction();
        listener.reset();
        
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._C);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterC.res", "[a1, b1, c1, c2, d1]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddAfterAC() throws Exception {
        String propertyName = "testAddAfterAC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._AC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterAC.res", "[a1, b1, c1, d1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddAfterAB() throws Exception {
        String propertyName = "testAddAfterAB";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._AB);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterAC.res", "[a1, b1, d1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddAfterBAC() throws Exception {
        String propertyName = "testAddAfterBAC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._BAC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterBAC.res", "[a1, b1, c1, d1]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testSetA() throws Exception {
        String propertyName = "testSetA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.setChild(D.class, propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetA.res", "[a1, d1, b1, c1]", p.getChildren().toString());
    }
    
    // a1 b1 c1 -> a1 b1 c1 d1
    public void testSetBC() throws Exception {
        String propertyName = "testSetBC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.setChild(D.class, propertyName, d1, TestComponent._BC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetBC.res", "[a1, b1, c1, d1]", p.getChildren().toString());

	model.startTransaction();
        D d2 = new D(model, 2);
        p.setChild(D.class, propertyName, d2, TestComponent._BC);
        model.endTransaction();
        assertEventListener(propertyName, d1, d2);
        assertEquals("testSetBC.res", "[a1, b1, c1, d2]", p.getChildren().toString());
    }
    
    // a1 b1 b2 c1 d1 -> a1 b1 b2 c2 d1
    public void testSetAfterAB() throws Exception {
        String propertyName = "testSetC";
	model.startTransaction();
        B b2 = new B(model, 2);
        p.addAfter("setup", b2, TestComponent._A);
        model.endTransaction();
        assertEquals("testSetC.res", "[a1, b1, b2, c1]", p.getChildren().toString());
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter("setup", d1, TestComponent._ABC);
        C c2 = new C(model, 2);
        p.setChild(C.class, propertyName, c2, TestComponent._AB);
        model.endTransaction();
        assertEventListener(propertyName, c1, c2);
        assertEquals("testSetC.res", "[a1, b1, b2, c2, d1]", p.getChildren().toString());
    }
    
    // c1 -> a1 b1 c1
    public void testSpecificOrdering() throws Exception {
        // setup
        model = Util.loadModel("resources/Empty.xml");
	model.startTransaction();
        p = TestComponent.class.cast(model.getRootComponent());
        assertEquals("setup", p.getName(), "test");
        c1 = new C(model, 1);
        p.appendChild("setup", c1);
        model.endTransaction();
        assertEquals("testSpecificOrdering.setup", "[c1]", p.getChildren().toString());
        
	model.startTransaction();
        a1 = new A(model, 1);
        b1 = new B(model, 1);
        p.setChildBefore(A.class, "a", a1, TestComponent._BC);
        p.setChildBefore(B.class, "b", b1, TestComponent._C);
        model.endTransaction();
        assertEquals("testSpecificOrdering.res", "[a1, b1, c1]", p.getChildren().toString());
    }
    
    public void testGetSetAttribute() throws Exception {
        String v = a1.getAttribute(TestAttribute.VALUE);
        assertNull("testAttribute.initial.value", v);
        int i = a1.getIndex();
        assertEquals("testAttribute.initial.index", 1, i);
        
        String v2 = "testSetAttribute.set.value"; 
        int i2 = 20;
	model.startTransaction();
        a1.setValue(v2);
	model.endTransaction();
        assertEventListener(TestAttribute.VALUE.getName(), v, v2);
	model.startTransaction();
        a1.setIndex(i2);
	model.endTransaction();
        assertEventListener(TestAttribute.INDEX.getName(), i, Integer.valueOf(i2));
        
        v = v2; i = i2;
        v2 = "testSetAttribute.set.value.again"; 
        i2 = 21;
	model.startTransaction();
        a1.setValue(v2); 
	model.endTransaction();
        assertEventListener(TestAttribute.VALUE.getName(), v, v2);
	model.startTransaction();
        a1.setIndex(i2);
	model.endTransaction();
        assertEventListener(TestAttribute.INDEX.getName(), Integer.valueOf(i), Integer.valueOf(i2));
    }
    
    public void testSetGetChild() throws Exception {
	model.startTransaction();
        D myD = new D(model, -1);
        p.setChildBefore(D.class, "myD", myD, TestComponent._BC);
	model.endTransaction();
        assertEquals("testSetGetChild.order", "[a1, d-1, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild.equals", myD, p.getChild(D.class));
        
	model.startTransaction();
        D myD2 = new D(model, -2);
        p.setChildBefore(D.class, "myD", myD2, TestComponent._BC);
	model.endTransaction();
        assertEventListener("myD", myD, myD2);
        assertEquals("testSetGetChild2.count", 1, p.getChildren(D.class).size());
        assertEquals("testSetGetChild2.order", "[a1, d-2, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild2.equals", myD2, p.getChild(D.class));
    }
    
    public void testRemoveChild() throws Exception {
        model.startTransaction();
        p.removeChild(b1.getName(), b1);
        model.endTransaction();
        
        assertEventListener(b1.getName(), b1, null);
        assertNull("testRemoveChild.gone", p.getChild(B.class));
        assertEquals("testRemoveChild.count", 0, p.getChildren(B.class).size());
        assertEquals("testRemoveChild.count.all", 2, p.getChildren().size());
    }
    
    public void testRemoveAttribute() throws Exception {
        model.startTransaction();
        A myA = p.getChild(A.class);
        assertEquals("testRemoveAttribute.init", "1", myA.getAttribute(TestAttribute.INDEX));
        myA.setAttribute(TestAttribute.INDEX.getName(), TestAttribute.INDEX, null);
        assertEquals("testRemoveAttribute.result", -1, myA.getIndex());
        model.endTransaction();
        
        assertEventListener(TestAttribute.INDEX.getName(), Integer.valueOf(1), null);
    }
    
    public void testGetParent() throws Exception {
        for (TestComponent tc : p.getChildren()) {
            assertTrue("parent pointer not null", tc.getParent() == p);
        }
        model.startTransaction();
        p.removeChild("testGetParent.removeChild", a1);
        assertNull("removed component should have null parent", a1.getParent());
        model.endTransaction();
        
        model = Util.loadModel("resources/test1.xml");
        A a1 = model.getRootComponent().getChild(A.class);
        assertTrue("test getParent from loaded doc", a1.getParent() == model.getRootComponent());
    }
}
    
