/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.xam;

import junit.framework.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.TestComponent.A;
import org.netbeans.modules.xml.xam.TestComponent.Aa;
import org.netbeans.modules.xml.xam.TestComponent.B;
import org.netbeans.modules.xml.xam.TestComponent.C;
import org.netbeans.modules.xml.xam.TestComponent.D;
import org.netbeans.modules.xml.xam.TestComponent.TestComponentReference;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
    TestComponentListener clistener;
    
    public AbstractComponentTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void defaultSetup() throws Exception {
        model = Util.loadModel("resources/Empty.xml");
	model.startTransaction();
        p = TestComponent.class.cast(model.getRootComponent());
        assertEquals("setup", "test-1", p.getName());
        
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
        clistener = new TestComponentListener();
        model.addComponentListener(clistener);
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
    
    class TestComponentListener implements ComponentListener {
        List<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public List<ComponentEvent> getEvents() { return accu; }
    
        private void assertEvent(ComponentEvent.EventType type, DocumentComponent source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event" + type +" on source " + source, false);
        }
    }    
    
    public void testInsertAtIndex() throws Exception {
        defaultSetup();
        String propertyName = "testInsertAtIndex";
        TestComponent parent = new TestComponent(model, "test", TestComponent.NS_URI);
        B b0 = new B(model, 0);
        B b1 = new B(model, 1);
        B b2 = new B(model, 2);
        B b3 = new B(model, 3);
        parent.insertAtIndex(propertyName, b1, 0, B.class);
        assertEquals("testInsertAtIndex.res", "[b1]", parent.getChildren().toString());
        assertTrue(parent == b1.getParent());

        model.startTransaction();
        model.getRootComponent().appendChild("test-setup", parent);
        parent.insertAtIndex(propertyName, b2, 1, B.class);
        model.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b1, b2]", parent.getChildren().toString());
        assertTrue(parent == b2.getParent());
        
        try {
            parent.insertAtIndex(propertyName, b0, 0, B.class);
            assertFalse("Did not get expected IllegalStateException", true);
        } catch(IllegalStateException ex) {
            // expected
        }

        model.startTransaction();
        parent.insertAtIndex(propertyName, b0, 0, B.class);
        model.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2]", parent.getChildren().toString());
        assertTrue(parent == b0.getParent());

        model.startTransaction();
        parent.insertAtIndex(propertyName, b3, 3, B.class);
        model.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2, b3]", parent.getChildren().toString());
        assertTrue(parent == b3.getParent());
    }
    
    public void testInsertAtIndexRelative() throws Exception {
        defaultSetup();
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
        defaultSetup();
        String propertyName = "testInsertAtIndexRelative0";
	model.startTransaction();
        B b0 = new B(model, 0);
        p.insertAtIndex(propertyName, b0, 0, B.class);
        model.endTransaction();
        assertEventListener(propertyName, null, b0);
        assertTrue(p == b0.getParent());
        
        List<B> res1 = p.getChildren(B.class);
        assertEquals("testInsertAtIndexRelative0.res1", "[b0, b1]", res1.toString());
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative0.res2", "[a1, b0, b1, c1]", res2.toString());
    }

    // a1 b1 c1 -> a1 b1 c1 d1
    public void testInsertAtIndexRelative0Empty() throws Exception {
        defaultSetup();
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
        assertTrue(p == d1.getParent());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeA() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testAddBeforeA.res2", "[d1, a1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddBeforeC() throws Exception {
        defaultSetup();
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
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeC.res", "[a1, b1, d1, c1, c2]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddBeforeBC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeBC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._BC);
        assertTrue(p == d1.getParent());
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeBC.res", "[a1, d1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeAC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addBefore(propertyName, d1, TestComponent._AC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddBeforeBAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeBAC";
	model.startTransaction();
        D d1 = new D(model, 1);
	
        p.addBefore(propertyName, d1, TestComponent._BAC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddBeforeBAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddAfterA() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res2 = p.getChildren();
        assertEquals("testAddAfterA.res2", "[a1, d1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddAfterC() throws Exception {
        defaultSetup();
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
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterC.res", "[a1, b1, c1, c2, d1]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddAfterAC() throws Exception {
        defaultSetup();
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
        defaultSetup();
        String propertyName = "testAddAfterAB";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter(propertyName, d1, TestComponent._AB);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent> res = p.getChildren();
        assertEquals("testAddAfterAC.res", "[a1, b1, d1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddAfterBAC() throws Exception {
        defaultSetup();
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
        defaultSetup();
        String propertyName = "testSetA";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.setChild(D.class, propertyName, d1, TestComponent._A);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetA.res", "[a1, d1, b1, c1]", p.getChildren().toString());
        assertTrue(p == d1.getParent());
    }
    
    // a1 b1 c1 -> a1 b1 c1 d1
    public void testSetBC() throws Exception {
        defaultSetup();
        String propertyName = "testSetBC";
	model.startTransaction();
        D d1 = new D(model, 1);
        p.setChild(D.class, propertyName, d1, TestComponent._BC);
        model.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetBC.res", "[a1, b1, c1, d1]", p.getChildren().toString());
        assertTrue(p == d1.getParent());

	model.startTransaction();
        D d2 = new D(model, 2);
        p.setChild(D.class, propertyName, d2, TestComponent._BC);
        model.endTransaction();
        assertEventListener(propertyName, d1, d2);
        assertEquals("testSetBC.res", "[a1, b1, c1, d2]", p.getChildren().toString());
        assertTrue(p == d2.getParent());
    }
    
    // a1 b1 b2 c1 d1 -> a1 b1 b2 c2 d1
    public void testSetAfterAB() throws Exception {
        defaultSetup();
        String propertyName = "testSetC";
	model.startTransaction();
        B b2 = new B(model, 2);
        p.addAfter("setup", b2, TestComponent._A);
        model.endTransaction();
        assertEquals("testSetC.res", "[a1, b1, b2, c1]", p.getChildren().toString());
        assertTrue(p == b2.getParent());
	model.startTransaction();
        D d1 = new D(model, 1);
        p.addAfter("setup", d1, TestComponent._ABC);
        C c2 = new C(model, 2);
        p.setChild(C.class, propertyName, c2, TestComponent._AB);
        model.endTransaction();
        assertEventListener(propertyName, c1, c2);
        assertEquals("testSetC.res", "[a1, b1, b2, c2, d1]", p.getChildren().toString());
        assertTrue(p == c2.getParent());
    }
    
    // c1 -> a1 b1 c1
    public void testSpecificOrdering() throws Exception {
        // setup
        model = Util.loadModel("resources/Empty.xml");
	model.startTransaction();
        p = TestComponent.class.cast(model.getRootComponent());
        assertEquals("setup", "test-1", p.getName());
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
        assertTrue(p == a1.getParent());
        assertTrue(p == b1.getParent());
    }
    
    public void testGetSetAttribute() throws Exception {
        defaultSetup();
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
        defaultSetup();
	model.startTransaction();
        D myD = new D(model, -1);
        p.setChildBefore(D.class, "myD", myD, TestComponent._BC);
	model.endTransaction();
        assertEquals("testSetGetChild.order", "[a1, d-1, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild.equals", myD, p.getChild(D.class));
        assertTrue(p == myD.getParent());
        
	model.startTransaction();
        D myD2 = new D(model, -2);
        p.setChildBefore(D.class, "myD", myD2, TestComponent._BC);
	model.endTransaction();
        assertEventListener("myD", myD, myD2);
        assertEquals("testSetGetChild2.count", 1, p.getChildren(D.class).size());
        assertEquals("testSetGetChild2.order", "[a1, d-2, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild2.equals", myD2, p.getChild(D.class));
        assertTrue(p == myD2.getParent());
    }
    
    public void testRemoveChild() throws Exception {
        defaultSetup();
        model.startTransaction();
        p.removeChild(b1.getName(), b1);
        model.endTransaction();
        
        assertEventListener(b1.getName(), b1, null);
        assertNull("testRemoveChild.gone", p.getChild(B.class));
        assertEquals("testRemoveChild.count", 0, p.getChildren(B.class).size());
        assertEquals("testRemoveChild.count.all", 2, p.getChildren().size());
        assertNull(b1.getParent());
    }
    
    public void testRemoveAttribute() throws Exception {
        defaultSetup();
        model.startTransaction();
        A myA = p.getChild(A.class);
        assertEquals("testRemoveAttribute.init", "1", myA.getAttribute(TestAttribute.INDEX));
        myA.setAttribute(TestAttribute.INDEX.getName(), TestAttribute.INDEX, null);
        assertEquals("testRemoveAttribute.result", -1, myA.getIndex());
        model.endTransaction();
        
        assertEventListener(TestAttribute.INDEX.getName(), Integer.valueOf(1), null);
    }
    
    public void testGetParent() throws Exception {
        defaultSetup();
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
    
    public void testAnyAttribute() throws Exception {
        defaultSetup();
        model.startTransaction();
        A a1 = p.getChild(A.class);
        String ns = "testAnyAttribute";
        String prefix = "any";
        String attrName = "any1";
        QName attr = new QName(ns, attrName, prefix);
        String value = "any attribute test";
        a1.setAnyAttribute(attr, value);
        model.endTransaction();
        
        QName noPrefixAttr = new QName(ns, attrName);
        assertEquals(value, a1.getAnyAttribute(noPrefixAttr));
        assertEquals(prefix, a1.getPeer().lookupPrefix(ns));
        
        model = Util.dumpAndReloadModel(model);
        model.addPropertyChangeListener(listener);
        model.addComponentListener(clistener);
        a1 = model.getRootComponent().getChild(A.class);
        assertEquals(value, a1.getAnyAttribute(noPrefixAttr));
        assertEquals(prefix, a1.getPeer().lookupPrefix(ns));
        
        model.startTransaction();
        a1.setAnyAttribute(attr, null);
        model.endTransaction();
        assertNull(a1.getAnyAttribute(attr));
        
        assertEventListener(attr.getLocalPart(), value, null);
        clistener.assertEvent(ComponentEvent.EventType.VALUE_CHANGED, a1);
    }

    public void testCopyAndResetNS() throws Exception {
        defaultSetup();
        model = Util.loadModel("resources/test3.xml");
        p = model.getRootComponent();
        Aa aa1 = p.getChild(Aa.class);
        assertEquals(TestComponent.NS2_URI, aa1.getNamespaceURI());
        D d = aa1.getChild(D.class);
        assertEquals(TestComponent.NS_URI, d.getNamespaceURI());
        Aa aa2 = (Aa) aa1.copy(p);
        assertEquals(TestComponent.NS2_URI, aa2.getNamespaceURI());
        assertEquals(TestComponent.NS_URI, aa2.lookupNamespaceURI(""));
        D dCopy = aa2.getChild(D.class);
        assertEquals(TestComponent.NS_URI, dCopy.getNamespaceURI());
        assertEquals(TestComponent.NS_URI, dCopy.lookupNamespaceURI(""));
        
        model.startTransaction();
        aa2.setAttribute("testCopy.setup", TestAttribute.INDEX, 2);
        aa2.removePrefix("myNS");
        aa2.getPeer().setPrefix(null);
        p.appendChild("testCopy.setup", aa2);
        model.endTransaction();
        
        assertEquals(2, aa2.getIndex());
        assertNull(aa2.lookupNamespaceURI("myNS"));
        //Util.dumpToFile(model.getBaseDocument(), new File("C:\\temp\\testCopy_after.xml"));
    }
    
    public void testThreeAppendsThenCopy() throws Exception {
        defaultSetup();
        A compA = model.createA(a1);
        B compB = model.createB(compA);
        C compC = model.createC(compB);
        model.startTransaction();
        a1.appendChild("compA", compA);
        compA.appendChild("compB", compB);
        compB.appendChild("compC", compC);
        model.endTransaction();
        
        assertEquals(compA, a1.getChild(A.class));
        assertEquals(compB, compA.getChild(B.class));
        int length = compB.getPeer().getChildNodes().getLength();
        assertEquals("Got B children count="+length, 3, length);
        assertEquals(compC, compB.getChild(C.class));
        
        assertEquals(compA.getPeer().getChildNodes().item(1), compB.getPeer());
        
        A copyA = (A) compA.copy(b1);
        B childOfCopy = copyA.getChild(B.class);
        length = childOfCopy.getPeer().getChildNodes().getLength();
        assertEquals("Got childOfCopy children count="+length, 3, length);
        C grandChildOfCopy = childOfCopy.getChild(C.class);
        assertNotNull(grandChildOfCopy);
    }

    public void testCopyHierarchy() throws Exception {
        defaultSetup();
        model = Util.loadModel("resources/test3.xml");
        p = model.getRootComponent();
        B b= p.getChild( B.class );
        model.startTransaction();
        C c = new C(model, 0);
        b.addBefore( "c" , c , Collections.EMPTY_LIST );
              assertNotNull( b.getChild( C.class ) );
              D d = new D( model , 0 );
        c.addBefore( "d" , d , Collections.EMPTY_LIST );
              assertNotNull( c.getChild( D.class ));
              B component = (B)b.copy( p );
              assertNotNull( component.getChild( C.class ));
              c = component.getChild( C.class );
              assertNotNull( c.getChild( D.class ));

        model.endTransaction();
    }

    public void testCopyAndAppendWithReference() throws Exception {
        defaultSetup();
        model = Util.loadModel("resources/test3_reference.xml");
        p = model.getRootComponent();
        TestModel model2 = Util.loadModel("resources/test3.xml");
        TestComponent recipient = model2.getRootComponent();
        Aa aa1 = p.getChild(Aa.class);
        D aa1Child = aa1.getChild(D.class);
        B aa1GrandChild = aa1Child.getChild(B.class);
        assertEquals("tns", aa1GrandChild.lookupPrefix("myTargetNS"));
        
        Aa copy = (Aa) aa1.copy(p);
        D copyChild = copy.getChild(D.class);
        assertEquals("myTargetNS", copyChild.lookupNamespaceURI("tns"));
        B copyGrandChild = copyChild.getChild(B.class);
        assertEquals("tns", aa1GrandChild.lookupPrefix("myTargetNS"));
        TestComponentReference<TestComponent> ref = copyGrandChild.getRef(TestComponent.class);
        assertEquals("tns:a1", ref.getRefString());
        
        try {
            ref.get();
            assertFalse("ref should not be accessible in copy", true);
        } catch(IllegalStateException e) {
            //OK
        }
        assertEquals(p.getTargetNamespace(), ref.getQName().getNamespaceURI());
        
        A recipientA1 = model2.getRootComponent().getChild(A.class);
        assertEquals("a1", model2.getRootComponent().getChild(A.class).getName());
        
        recipient.getModel().startTransaction();
        recipient.insertAtIndex(copy.getPeer().getLocalName(), copy, 0);
        recipient.getModel().endTransaction();
        
        Aa inserted = recipient.getChildren(Aa.class).get(0);
        assertTrue(inserted == copy);
        
        // assert model pointers
        assertTrue(recipient.getModel() == inserted.getModel());
        D insertedChild = inserted.getChild(D.class);
        assertTrue(recipient.getModel() == insertedChild.getModel());
        B insertedGrandChild = insertedChild.getChild((B.class));
        assertTrue(recipient.getModel() == insertedGrandChild.getModel());
        assertTrue(ref.get() == recipientA1);
    }
    
    public void testReAddDeep() throws Exception {
        defaultSetup();
        model = Util.loadModel("resources/test1_deep.xml");
        p = model.getRootComponent();
        A a1 = p.getChild(A.class);
        A a1Copy = (A) a1.copy(p);
        
        model.startTransaction();
        p.removeChild("testReAddDeep", a1);
        model.endTransaction();
        assertNull(p.getChild(A.class));
        
        try {
            model.startTransaction();
            p.appendChild("testReAddDeep", a1);
            assertFalse("Failed to get IllegalStateException", true);
        } catch(IllegalStateException ex) {
            //OK
        } finally {
            model.endTransaction();
        }
        model.startTransaction();
        p.appendChild("testReAddDeep", a1Copy);
        model.endTransaction();
        
        A a1ReAdded = p.getChild(A.class);
        assertEquals(3, a1ReAdded.getChildren(B.class).size());
        assertNotSame(a1, a1ReAdded);
    }
    
    public void testAddToSelfClosingRootElement() throws Exception {
        TestModel refmod = Util.loadModel("resources/Empty_selfClosing.xml");
        assertEquals(0, refmod.getRootComponent().getPeer().getChildNodes().getLength());
        
        model = Util.loadModel("resources/Empty.xml");
        p = model.getRootComponent();
        assertEquals(1, p.getPeer().getChildNodes().getLength());
        
        Util.setDocumentContentTo(model.getBaseDocument(), "resources/Empty_selfClosing.xml");
        model.sync();

        A a = model.createA(p);
        model.startTransaction();
        p.addAfter("test", a, TestComponent._B);
        a.setValue("foo");
        model.endTransaction();
        assertEquals(3, p.getPeer().getChildNodes().getLength());
        
        File f = Util.dumpToTempFile(model.getBaseDocument());
        TestModel model2 = Util.loadModel(f);
        assertEquals(3, model2.getRootComponent().getPeer().getChildNodes().getLength());
    }
    
    public void testSetText() throws Exception {
        model = Util.loadModel("resources/test_removeChildren.xml");
        p = model.getRootComponent();
        a1 = p.getChild(A.class);
        String a1Leading = "\n function match(a,b) if (a > 0 && b < 7) <a>     \n    ";
        assertEquals(a1Leading, p.getLeadingText(a1));

        model.startTransaction();
        p.setText("test", "---a1---", a1, true);
        model.endTransaction();
        assertFalse(p.getPeer().getChildNodes().item(1) instanceof Text);

        model.startTransaction();
        p.setText("test", "---b1---", a1, false);
        model.endTransaction();
        
        assertEquals("a", p.getPeer().getChildNodes().item(1).getLocalName());
        assertEquals("---b1---", ((Text)p.getPeer().getChildNodes().item(2)).getNodeValue());
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());

        b1 = p.getChild(B.class);
        c1 = p.getChild(C.class);

        model.startTransaction();
        p.setText("test", "---c1---", c1, true);
        model.endTransaction();
        
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());
        assertEquals("---c1---", ((Text)p.getPeer().getChildNodes().item(4)).getNodeValue());
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());

        model.startTransaction();
        p.setText("test", "---(c1)---", b1, false);
        model.endTransaction();
        
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());
        assertEquals("---(c1)---", p.getLeadingText(c1));
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());

        model.startTransaction();
        p.setText("test", "---d1---", c1, false);
        model.endTransaction();
        //Util.dumpToFile(model.getBaseDocument(), new File("c:/temp/test1.xml"));
        
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());
        assertEquals("---d1---", p.getTrailingText(c1));
        assertEquals(7, p.getPeer().getChildNodes().getLength());

        model.startTransaction();
        p.setText("test", null, a1, true);
        p.setText("test", null, b1, false);
        p.setText("test", null, c1, true);
        p.setText("test", null, c1, false);
        model.endTransaction();

        assertNull(p.getLeadingText(a1));
        assertNull(p.getTrailingText(b1));
        assertNull(p.getLeadingText(c1));
        assertNull(p.getTrailingText(c1));
    }
          
    public void testGetXmlFragmentInclusiveMiddle() throws Exception {
        TestModel model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent root = model.getRootComponent();
        TestComponent.B b = model.getRootComponent().getChildren(TestComponent.B.class).get(0);
        String result = b.getXmlFragmentInclusive();
        assertTrue(result.startsWith("<b index='1'>"));
        assertTrue(result.endsWith("</b>"));
    }

    public void testGetXmlFragmentInclusiveEdgeWithCDATA() throws Exception {
        TestModel model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent root = model.getRootComponent();
        TestComponent.C c = model.getRootComponent().getChildren(TestComponent.C.class).get(1);
        String result = c.getXmlFragmentInclusive();
        assertEquals("<c index='2'/>", result);
    }

    public void testGetXmlFragmentInclusiveDeepWithComment() throws Exception {
        TestModel model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent root = model.getRootComponent();
        TestComponent.B b = model.getRootComponent().getChildren(TestComponent.B.class).get(0);
        TestComponent.B bb = b.getChildren(TestComponent.B.class).get(0);
        String result = bb.getXmlFragmentInclusive();
        assertEquals("<b index='1' value=\"c\"/>", result);
    }

    public void testGetXmlFragmentInclusiveOnRoot() throws Exception {
        TestModel model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent root = model.getRootComponent();
        String result = root.getXmlFragmentInclusive();
        assertTrue(result.startsWith("<test xmlns=\"http://www.test"));
        assertTrue(result.endsWith("</test  >"));
    }

    public void testGetXmlFragmentInclusiveNoTextNode() throws Exception {
        TestModel model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent root = model.getRootComponent();
        TestComponent.A a = model.getRootComponent().getChildren(TestComponent.A.class).get(0);
        String result = a.getXmlFragmentInclusive();
        assertEquals("<a index='1'>CharDataString</a>", result);
    }
    
    public void testAddComponentDoFixupOnChildDefaultPrefix() throws Exception {
        TestModel model = Util.loadModel("resources/test1_prefix.xml");
        TestComponent root = model.getRootComponent();
        assertEquals(root.getNamespaceURI(), root.lookupNamespaceURI("ns"));
        TestComponent.Aa aa = model.createAa(root);
        model.startTransaction();
        root.appendChild("testAddComponentDoFixupDefaultPrefix", aa);
        model.endTransaction();
        TestComponent.A a = root.getChild(TestComponent.A.class);
        assertEquals(TestComponent.NS_URI, a.getNamespaceURI());
        assertEquals(TestComponent.NS2_URI, aa.getNamespaceURI());
        assertEquals("ns1", aa.getPeer().getPrefix());
    }
    
    public void testAdd_ToStandalone_ComponentDoFixupOnChildDefaultPrefix() throws Exception {
        TestModel model = Util.loadModel("resources/test1_prefix.xml");
        TestComponent root = model.getRootComponent();
        assertEquals(root.getNamespaceURI(), root.lookupNamespaceURI("ns"));
        TestComponent.Aa aa = model.createAa(root);
        TestComponent.Aa aaChild = model.createAa(aa);
        aa.appendChild("appendToStandAloneAa", aaChild);
        model.startTransaction();
        root.appendChild("testAddComponentDoFixupDefaultPrefix", aa);
        model.endTransaction();
        TestComponent.A a = root.getChild(TestComponent.A.class);
        assertEquals(TestComponent.NS_URI, a.getNamespaceURI());
        assertEquals(TestComponent.NS2_URI, aa.getNamespaceURI());
        assertEquals(TestComponent.NS2_URI, aaChild.getNamespaceURI());
        assertEquals("ns1", aa.getPeer().getPrefix());
        assertEquals("ns1", aaChild.getPeer().getPrefix());
    }
    
    public void testSetRefOnStandAlone() throws Exception {
        model = Util.loadModel("resources/test4_reference.xml");
        p = model.getRootComponent();
        TestComponent.A aa = model.createA(p);
        TestModel model2 = Util.loadModel("resources/test4.xml");
        TestComponent root2 = model2.getRootComponent();
        A a1 = root2.getChild(A.class);
        model.startTransaction();
        aa.setRef(a1, A.class);
        p.appendChild("testSetRefOnStandAlone", aa);
        model.endTransaction();
        assertEquals(root2.getTargetNamespace(), p.lookupNamespaceURI("ns1"));
        assertEquals("myTargetNS3", p.lookupNamespaceURI("ns"));
    }
    
    public void testAddComponentDoFixupOnRefDefaultPrefix() throws Exception {
        model = Util.loadModel("resources/test4_reference.xml");
        p = model.getRootComponent();
        B b1 = p.getChild(B.class);
        TestModel model2 = Util.loadModel("resources/test4.xml");
        TestComponent root2 = model2.getRootComponent();
        A a1 = root2.getChild(A.class);
        model.startTransaction();
        b1.setRef(a1, A.class);
        model.endTransaction();
        assertEquals(root2.getTargetNamespace(), p.lookupNamespaceURI("ns1"));
        assertEquals("myTargetNS3", p.lookupNamespaceURI("ns"));
    }
    
    // TODO support PI inside normal element
    public void FIXME_testProcessingInstruction() throws Exception {
        model = Util.loadModel("resources/PI_after_prolog.xml");
        p = model.getRootComponent();
        A a1 = p.getChild(A.class);
        assertEquals(132, a1.findPosition());
        B b1 = p.getChild(B.class);
        Element peer = (Element) b1.getPeer();
        List<Token> tokens = peer.getTokens();
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(2).getType());
        assertEquals(TokenType.TOKEN_PI_NAME, tokens.get(3).getType());
        assertEquals("Siebel-Property-Set", tokens.get(4).getValue());
        assertEquals(TokenType.TOKEN_PI_VAL, tokens.get(6).getType());
        assertEquals("SkipValidation=\"true\"", tokens.get(6).getValue());
        NodeList nl = peer.getChildNodes();
        assertEquals(2, nl.getLength());    
    }
}
    
