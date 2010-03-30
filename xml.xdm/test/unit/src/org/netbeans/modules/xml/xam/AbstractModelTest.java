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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.text.Document;
import junit.framework.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.xam.ComponentEvent.EventType;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.TestComponent.A;
import org.netbeans.modules.xml.xam.TestComponent.B;
import org.netbeans.modules.xml.xam.TestComponent.C;
import org.netbeans.modules.xml.xam.TestComponent.D;
import org.netbeans.modules.xml.xam.TestComponent.E;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.diff.Change;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.nodes.Element;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractModelTest extends NbTestCase {
    PropertyListener plistener;
    TestComponentListener listener;
    TestModel model;
    Document doc;

    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertEvent(String propertyName, Object old, Object now) {
            assertEvent(propertyName, null, old, now);
        }
        
        public void assertEvent(String propertyName, Object source, Object old, Object now) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    if (source != null && source != e.getSource()) {
                        continue;
                    }
                    if (old != null && ! old.equals(e.getOldValue()) ||
                        old == null && e.getOldValue() != null) {
                        continue;
                    }
                    if (now != null && ! now.equals(e.getNewValue()) ||
                        now == null && e.getNewValue() != null) {
                        continue;
                    }
                    return; //matched
                }
            }
            assertTrue("Expect property change event on "+propertyName+" with "+old+" and "+now, false);
        }
        
        public void assertNoEvent(String propertyName) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    assertTrue("Got unexpected event "+propertyName, false);
                }
            }
        }
        
        public PropertyChangeEvent getEvent(String propertyName, Object source) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName()) && source == e.getSource()) {
                    return e;
                }
            }
            return null;
        }
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
            assertTrue("Expect component change event " + type +" on source " + source +
                    ". Instead received: " + accu, false);
        }
    }    
    
    public AbstractModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        listener = new TestComponentListener();
        plistener = new PropertyListener();
    }
    
    private void defaultSetup() throws Exception {
        doc = Util.getResourceAsDocument("resources/test1.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
    }

    protected void tearDown() throws Exception {
        if (model != null) {
            model.removePropertyChangeListener(plistener);
            model.removeComponentListener(listener);
        }
    }

    public static Test suite() {
        return new TestSuite(AbstractModelTest.class);
    }
    
    public void testTransactionOnComponentListener() throws Exception {
        defaultSetup();
        assertEquals("testComponentListener.ok", State.VALID, model.getState());
        A a1 = model.getRootComponent().getChild(A.class);
        
        try {
            a1.setValue("testComponentListener.a1");
            assertFalse("Mutate without transaction, should have thrown IllegalStateException", true);
        } catch(IllegalStateException e) {
            //OK
        }
        
        model.startTransaction();
        a1.setValue("testComponentListener.a1"); // #1
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A); // #2
        C c1 = model.getRootComponent().getChild(C.class);
        model.getRootComponent().removeChild("testComponentListener.remove.c1", c1); // #3
        assertEquals("testComponentListener.noEventBeforeCommit", 0, listener.getEventCount());
        model.endTransaction();

        assertEquals(3, listener.getEventCount());
        TestComponent root = model.getRootComponent();
        listener.assertEvent(EventType.VALUE_CHANGED, a1);
        listener.assertEvent(EventType.CHILD_ADDED, root);
        listener.assertEvent(EventType.CHILD_REMOVED, root);
    }
    
    public void testStateTransition() throws Exception {
        defaultSetup();
        assertEquals("testState.invalid", State.VALID, model.getState());

        Util.setDocumentContentTo(doc, "resources/Bad.xml");
        try {
            model.sync();
            assertFalse("not getting expected ioexception", true);
        } catch (IOException io) {
            assertEquals("Expected state not well-formed", State.NOT_WELL_FORMED, model.getState());
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_WELL_FORMED);
        }
        
        Util.setDocumentContentTo(doc, "resources/test1.xml");
        model.sync();
        assertEquals("testState.valid", State.VALID, model.getState());
        plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_WELL_FORMED, Model.State.VALID);
    }
    
    public void testSyncRemoveAttribute() throws Exception {
        defaultSetup();
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);
        
        A a1 = model.getRootComponent().getChild(TestComponent.A.class);
        assertNull("setup", a1.getValue());
        
        model.startTransaction();
        String testValue = "edit #1: testRemoveAttribute";
        a1.setValue(testValue);
        model.endTransaction();
        assertEquals(testValue, a1.getValue());

        um.undo();
        assertNull("after undo expect no attribute 'value'", a1.getValue());
        
        um.redo();
        assertEquals(testValue, a1.getValue());

        Util.setDocumentContentTo(doc, "resources/test1.xml");
        model.sync();
        assertNull("sync back to original, expect no attribute 'value'", a1.getValue());
        plistener.assertEvent("value", testValue, null);
        listener.assertEvent(ComponentEvent.EventType.VALUE_CHANGED, a1);
        
        um.undo();
        model.getAccess().flush(); // after fix for 83963 need flush after undo/redo
        
        assertEquals(testValue, a1.getValue());
        model = Util.dumpAndReloadModel(model);
        a1 = model.getRootComponent().getChild(A.class);
        assertEquals(testValue, a1.getValue());
    }
    
    public void testMultipleMutationUndoRedo() throws Exception {
        model = Util.loadModel("resources/Empty.xml");
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
		
        //setup
        model.startTransaction();
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
        String v = "testComponentListener.b2";
        b2.setValue(v);
        model.endTransaction();	
        
        b2 = model.getRootComponent().getChild(B.class);
        assertEquals(v, b2.getAttribute(TestAttribute.VALUE));
        
        urListener.undo();
        b2 = model.getRootComponent().getChild(B.class);
        assertNull(b2);

        urListener.redo();
        b2 = model.getRootComponent().getChild(B.class);
        assertEquals(v, b2.getAttribute(TestAttribute.VALUE));
    }

    public void testUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
		
        //setup
        model.startTransaction();
        A a1 = model.getRootComponent().getChild(A.class);
        String v = "testComponentListener.a1";
        a1.setValue(v);
        model.endTransaction();	
        assertEquals("edit #1: initial set a1 attribute 'value'", v, a1.getAttribute(TestAttribute.VALUE));

        urListener.undo();
        String val = a1.getAttribute(TestAttribute.VALUE);
        assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        urListener.redo();
        assertEquals(v, a1.getAttribute(TestAttribute.VALUE));
        
        model.startTransaction();
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
        b2.setValue(v);
        model.endTransaction();		
        assertEquals("edit #2: insert b2", 2, model.getRootComponent().getChildren(B.class).size());
        
        model.startTransaction();
        C c1 = model.getRootComponent().getChild(C.class);
        model.getRootComponent().removeChild("testComponentListener.remove.c1", c1);
        model.endTransaction();		
        assertNull("edit #3: remove c1", model.getRootComponent().getChild(C.class));
        
        urListener.undo();
        c1 = model.getRootComponent().getChild(C.class);	
        assertEquals("undo edit #3", 1, c1.getIndex());

        urListener.redo();			
        assertNull("redo edit #3", model.getRootComponent().getChild(C.class));
        
        urListener.undo();	
        assertEquals("undo edit #3 after redo", 1, model.getRootComponent().getChildren(C.class).size());
        assertNotNull("c should be intact", model.getRootComponent().getChild(C.class));
        
        urListener.undo();		
        assertEquals("undo edit #2", 1, model.getRootComponent().getChildren(B.class).size());
        assertNotNull("c should be intact", model.getRootComponent().getChild(C.class));
        
        urListener.undo();
        a1 = model.getRootComponent().getChild(A.class);
        val = a1.getAttribute(TestAttribute.VALUE);
        assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        assertNotNull("c should be intact", model.getRootComponent().getChild(C.class));
        
        urListener.redo();
        assertNotNull("c should be intact", model.getRootComponent().getChild(C.class));

        urListener.redo();
        assertEquals("redo edit #1 and #2", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("testUndo.1", 1, model.getRootComponent().getChildren(C.class).size());
    }
    
    public void testSyncUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
        assertEquals("setup: initial", 1, model.getRootComponent().getChildren(C.class).size());
        
        Util.setDocumentContentTo(doc, "resources/test2.xml");
        model.sync();
        assertEquals("setup: sync", 0, model.getRootComponent().getChildren(C.class).size());

        urListener.undo();
        assertEquals("undo sync", 1, model.getRootComponent().getChildren(C.class).size());

        urListener.redo();
        assertEquals("undo sync", 0, model.getRootComponent().getChildren(C.class).size());
    }
    
    public void testSourceEditSyncUndo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        Document doc = model.getBaseDocument();
        model.addUndoableEditListener(urListener);
        
        model.startTransaction();
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
        model.endTransaction();		
        assertEquals("first edit setup", 2, model.getRootComponent().getChildren(B.class).size());
        
        // see fix for issue 83963, with this fix we need coordinate edits from
        // on XDM model and on document buffer.  This reduce XDM undo/redo efficiency,
        // but is the best we can have to satisfy fine-grained text edit undo requirements.
        model.removeUndoableEditListener(urListener);
        doc.addUndoableEditListener(urListener);
        
        Util.setDocumentContentTo(doc, "resources/test2.xml");
        assertEquals("undo sync", 1, model.getRootComponent().getChildren(C.class).size());
        model.sync();
        doc.removeUndoableEditListener(urListener);
        
        assertEquals("sync setup", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("sync setup", 0, model.getRootComponent().getChildren(C.class).size());
        
        // setDocumentContentTo did delete all, then insert, hence 2 undo's'
        urListener.undo(); urListener.undo(); 
        model.sync(); // the above undo's are just on document buffer, needs sync (inefficient).
        assertEquals("undo sync", 1, model.getRootComponent().getChildren(C.class).size());
        assertEquals("undo sync", 2, model.getRootComponent().getChildren(B.class).size());

        urListener.undo();
        assertEquals("undo first edit before sync", 1, model.getRootComponent().getChildren(B.class).size());

        urListener.redo();
        assertEquals("redo first edit", 1, model.getRootComponent().getChildren(C.class).size());
        assertEquals("redo first edit", 2, model.getRootComponent().getChildren(B.class).size());

        // needs to back track the undo's, still needs sync'
        urListener.redo(); urListener.redo();
        model.sync();
        assertEquals("redo to sync", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("redo to sync", 0, model.getRootComponent().getChildren(C.class).size());
    }
	
    public void testCopyPasteUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
        
        model.startTransaction();
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
        model.endTransaction();			
        assertEquals("first edit setup", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("first edit setup", 1, model.getRootComponent().getChildren(C.class).size());		
		
		B b2Copy = (B) b2.copy(model.getRootComponent());
		
		model.startTransaction();
		model.getRootComponent().addAfter(b2Copy.getName(), b2Copy, TestComponent._A);
        model.endTransaction();
        
        assertEquals("paste", 3, model.getRootComponent().getChildren(B.class).size());
        assertEquals("paste", 1, model.getRootComponent().getChildren(C.class).size());
        
        urListener.undo();
        assertEquals("undo paste", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("undo paste", 1, model.getRootComponent().getChildren(C.class).size());		
		
        urListener.redo();
        assertEquals("redo paste", 3, model.getRootComponent().getChildren(B.class).size());
        assertEquals("redo paste", 1, model.getRootComponent().getChildren(C.class).size());		
    }	
	
    public void testCutPasteUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
        
        model.startTransaction();
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
        model.endTransaction();			
        assertEquals("first edit setup", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("first edit setup", 1, model.getRootComponent().getChildren(C.class).size());		
		
		B b2Copy = (B) b2.copy(model.getRootComponent());
		
		model.startTransaction();
		model.getRootComponent().removeChild(b2.getName(), b2);
        model.endTransaction();
        
        assertEquals("cut", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("cut", 1, model.getRootComponent().getChildren(C.class).size());
        
		model.startTransaction();
		model.getRootComponent().addAfter(b2Copy.getName(), b2Copy, TestComponent._A);
        model.endTransaction();
		
        assertEquals("paste", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("paste", 1, model.getRootComponent().getChildren(C.class).size());
		
        urListener.undo();
        assertEquals("undo paste", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("undo paste", 1, model.getRootComponent().getChildren(C.class).size());		
		
        urListener.undo();
        assertEquals("undo cut", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("undo cut", 1, model.getRootComponent().getChildren(C.class).size());	
		
        urListener.undo();
        assertEquals("undo first sync", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("undo first sync", 1, model.getRootComponent().getChildren(C.class).size());	
		
        urListener.redo();
        assertEquals("redo first sync", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("redo first sync", 1, model.getRootComponent().getChildren(C.class).size());		
		
        urListener.redo();
        assertEquals("redo cut", 1, model.getRootComponent().getChildren(B.class).size());
        assertEquals("redo cut", 1, model.getRootComponent().getChildren(C.class).size());	
		
        urListener.redo();
        assertEquals("redo paste", 2, model.getRootComponent().getChildren(B.class).size());
        assertEquals("redo paste", 1, model.getRootComponent().getChildren(C.class).size());	
    }	
    
    public void testFindComponentByPosition() throws Exception {
        TestModel model = Util.loadModel("resources/forTestFindComponentOnly.xml");
        DocumentComponent c = model.findComponent(142);
        C c11 = (C) c;
        assertEquals(11, c11.getIndex());
    }
    
    public void testStartTransactionAfterModelSyncedIntoUnparseableState() throws Exception {
        defaultSetup();
        Util.setDocumentContentTo(doc, "resources/Bad.xml");
        try {
            model.sync();
            assertFalse("Did not get expected IOException", true);
        } catch (IOException ex) {
            // OK
        }
        assertTrue(State.NOT_WELL_FORMED == model.getState());

        try {
            assertFalse("Did not get expected failure to start", model.startTransaction());
        } finally {
            model.endTransaction(); // should be OK
        }
    }
    
    // sync with ns change will cause identity change and subsequently component delete/added events.
    public void testNamespaceAttribute() throws Exception {
        doc = Util.getResourceAsDocument("resources/test3.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        TestComponent root = model.getRootComponent();
        
        Util.setDocumentContentTo(doc, "resources/test3_changedNSonA2.xml");
        model.sync();
        
        listener.assertEvent(ComponentEvent.EventType.CHILD_REMOVED, root);
        listener.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
    }

    // Dual root namespaces test model
	public class TestModel2 extends TestModel {
            public static final String NS_URI = "http://www.test.com/TestModel2";

		public TestModel2(Document doc) {
			super(doc);
		}

		public TestComponent createRootComponent(org.w3c.dom.Element root) {
			if ((NS_URI.equals(root.getNamespaceURI()) ||
				TestComponent.NS_URI.equals(root.getNamespaceURI())) &&
				"test".equals(root.getLocalName())) {
					testRoot = new TestComponent(this, root);
			} else {
				testRoot = null;
			}
			return testRoot;
		}
	}

    // sync with ns change will cause identity change and subsequently component delete/added events.
    public void testRootNSChangeOK() throws Exception {
        Document doc = Util.getResourceAsDocument("resources/test1.xml");
        assert doc != null;
        TestModel2 model = new TestModel2(doc);
        model.sync();
        model.addComponentListener(listener);
        TestComponent root = model.getRootComponent();
        assertEquals(TestComponent.NS_URI, model.getRootComponent().getNamespaceURI());
        
        Util.setDocumentContentTo(doc, "resources/test1_rootnschange.xml");
        model.sync();
        assertEquals(Model.State.VALID, model.getState());
        assertTrue(root != model.getRootComponent());
        assertEquals(TestModel2.NS_URI, model.getRootComponent().getNamespaceURI());
    }
	
    public void testRootNSChangeException() throws Exception {
        defaultSetup();
        
        Util.setDocumentContentTo(doc, "resources/test1_rootnschange.xml");
        try {
            model.sync();
            assertTrue("Should have thrown IOException", false);
        } catch(IOException ioe) {
            //OK
        }
        assertEquals(Model.State.NOT_WELL_FORMED, model.getState());
    }
	
    // sync with some non-ns attribute change in root element
    public void testRootChange() throws Exception {
        defaultSetup();
        TestComponent root = model.getRootComponent();
        Util.setDocumentContentTo(doc, "resources/test1_rootchange.xml");
        model.sync();
        
        assertEquals("root is same", root, model.getRootComponent());
    }	
	
    public void testRootDeleted() throws Exception {
        Document doc = Util.getResourceAsDocument("resources/test1.xml");
        assert doc != null;
        TestModel2 model = new TestModel2(doc);
        model.sync();

        Util.setDocumentContentTo(doc, "resources/test1_rootdeleted.xml");
        model.sync();
        assertEquals(Model.State.NOT_WELL_FORMED, model.getState());
        assertNotNull(model.getRootComponent());

        Util.setDocumentContentTo(doc, "resources/test1.xml");
        model.sync();
        assertEquals(Model.State.VALID, model.getState());
        assertNotNull(model.getRootComponent());
    }

    public void testPrettyPrint() throws Exception {
        defaultSetup();
        assertEquals("testPrettyPrint.ok", State.VALID, model.getState());
		//System.out.println("doc: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
		model.startTransaction();
        assertEquals("testPrettyPrint", 7, model.getRootComponent().getPeer().getChildNodes().getLength());		
        A a1 = model.getRootComponent().getChild(A.class);
        a1.setValue("testPrettyPrint.a1");
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
		assertEquals("testPrettyPrint", 9, model.getRootComponent().getPeer().getChildNodes().getLength());
        B b3 = new B(model, 3);
        model.getRootComponent().addAfter(b3.getName(), b3, TestComponent._B);		
		assertEquals("testPrettyPrint", 11, model.getRootComponent().getPeer().getChildNodes().getLength());
        model.endTransaction();
		//System.out.println("doc after pretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));
    }	
	
    public void testUndoPrettyPrint() throws Exception {
        defaultSetup();
        assertEquals("testUndoPrettyPrint.ok", State.VALID, model.getState());
		//System.out.println("doc: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
		model.startTransaction();
        assertEquals("testUndoPrettyPrint", 7, model.getRootComponent().getPeer().getChildNodes().getLength());		
        A a1 = model.getRootComponent().getChild(A.class);
        a1.setValue("testUndoPrettyPrint.a1");
        B b2 = new B(model, 2);
        model.getRootComponent().addAfter(b2.getName(), b2, TestComponent._A);
		assertEquals("testUndoPrettyPrint", 9, model.getRootComponent().getPeer().getChildNodes().getLength());
        B b3 = new B(model, 3);
        model.getRootComponent().addAfter(b3.getName(), b3, TestComponent._B);		
		assertEquals("testUndoPrettyPrint", 11, model.getRootComponent().getPeer().getChildNodes().getLength());
        model.endTransaction();
		//System.out.println("doc after pretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));
		
		model.startTransaction();
        assertEquals("testUndoPrettyPrint", 11, model.getRootComponent().getPeer().getChildNodes().getLength());		
        List<B> bList = model.getRootComponent().getChildren(B.class);
		b2 = bList.get(1);
        model.getRootComponent().removeChild(b2.getName(), b2);
		assertEquals("testUndoPrettyPrint", 9, model.getRootComponent().getPeer().getChildNodes().getLength());
        b3 = bList.get(2);
        model.getRootComponent().removeChild(b3.getName(), b3);
		assertEquals("testUndoPrettyPrint", 7, model.getRootComponent().getPeer().getChildNodes().getLength());
        model.endTransaction();	
		//System.out.println("doc after undopretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
     }
    
    public void testUndoRedoWithIdentity() throws Exception {
        model = Util.loadModel("resources/test1_name.xml");
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);

        E e1 = model.getRootComponent().getChild(E.class);
        assertNull(e1.getValue());
        
        model.startTransaction();
        String v = "new test value";
        e1.setValue(v);
        model.endTransaction();
        assertEquals(v, e1.getValue());

        ur.undo();
        assertNull("expect null, get "+e1.getValue(), e1.getValue());
        
        ur.redo();
        assertEquals(v, e1.getValue());
    }

    public void testUndoRedoWithoutIdentity() throws Exception {
        model = Util.loadModel("resources/test1_noname.xml");
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);

        E e1 = model.getRootComponent().getChild(E.class);
        assertNull(e1.getValue());
        
        model.startTransaction();
        String v = "new test value";
        e1.setValue(v);
        model.endTransaction();
        assertEquals(v, e1.getValue());

        ur.undo();
        assertNull("expect null, get "+e1.getValue(), e1.getValue());
        
        ur.redo();
        assertEquals(v, e1.getValue());
    }

    interface FaultInjector {
        void injectFaultAndCheck(Object actor) throws Exception;
    }
    
    private void setupSyncFault(FaultInjector injector) throws Exception {
        defaultSetup();
        C c = model.getRootComponent().getChild(C.class);
        assertEquals(1, c.getIndex());
        
        try {
            injector.injectFaultAndCheck(null);
            assertFalse("Did not see NPE", true);
        } catch(NullPointerException ex) {
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_SYNCED);
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID);
            c = model.getRootComponent().getChild(C.class);
            assertEquals("ioexception", Model.State.VALID, model.getState());
            assertNull("insynced with after tree", c);
        }
    }
    
    public void testSyncWithFaultInFindComponent() throws Exception {
        setupSyncFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception {
                model.injectFaultInFindComponent();
                Util.setDocumentContentTo(doc, "resources/test2.xml");
                model.sync();
            }
        });
    }

    public void testSyncWithFaultInSyncUpdater() throws Exception {
        setupSyncFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception {
                model.injectFaultInSyncUpdater();
                Util.setDocumentContentTo(doc, "resources/test2.xml");
                model.sync();
            }
        });
    }

    public void testSyncWithFaultInEventFiring() throws Exception {
        setupSyncFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception {
                model.injectFaultInEventFiring();
                Util.setDocumentContentTo(doc, "resources/test2.xml");
                model.sync();
            }
        });
    }

    private void setupForUndoRedoFault(FaultInjector i, boolean redo) throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        A a = model.getRootComponent().getChild(A.class);
        
        model.startTransaction();
        String v = "new test value";
        a.setValue(v);
        C c = model.getRootComponent().getChild(C.class);
        model.removeChildComponent(c);
        model.endTransaction();
        assertEquals("setup success", v, a.getValue());
        try {
            i.injectFaultAndCheck(ur);
            assertFalse("Did not see NullPointerException", true);
        } catch (NullPointerException cue) {
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_SYNCED);
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID);
            a = model.getRootComponent().getChild(A.class);
            if (redo) {
                assertEquals("no cannotredoexception, redone", v, a.getValue());
                assertNull("still redone", model.getRootComponent().getChild(C.class));
            } else {
                assertNull("no cannotundoexception, undone", a.getValue());
                assertNotNull("still undone", model.getRootComponent().getChild(C.class));
            }
        }
        //TODO findout what if calling redo here
    }
    
    public void testUndoWithFaultInFindComponent() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                model.injectFaultInFindComponent(); 
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testUndoWithFaultInSyncUpdater() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                model.injectFaultInSyncUpdater(); 
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testUndoWithFaultInEventFiring() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                model.injectFaultInEventFiring(); 
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testRedoWithFaultInFindComponent() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                model.injectFaultInFindComponent(); 
                ((UndoManager)actor).redo();
            }
        }, true);
    }

    public void testRedoWithFaultInSyncUpdater() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                model.injectFaultInSyncUpdater(); 
                ((UndoManager)actor).redo();
            }
        }, true);
    }

    public void testRedoWithFaultInEventFiring() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                model.injectFaultInEventFiring(); 
                ((UndoManager)actor).redo();
            }
        }, true);
    }
    
    public void testSyncWithPositionChangeAndAttributeChange() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_1.xml");
        
        assertEquals(diffs.toString(), 3, diffs.size());
        assertTrue(diffs.toString(), ((Change)diffs.get(2)).isAttributeChanged());

        defaultSetup();
        Util.setDocumentContentTo(doc, "resources/test1_1.xml");
        model.sync();
        assertEquals("diffs="+diffs, "foo", model.getRootComponent().getChild(B.class).getValue());
    }

    public void testSyncWithReorder() throws Exception {
        doc = Util.getResourceAsDocument("resources/testreorder.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
        
        Util.setDocumentContentTo(doc, "resources/testreorder_1.xml");
        //model.sync();
        Util.setDocumentContentTo(doc, "resources/testreorder_2.xml");
        //model.sync();
        Util.setDocumentContentTo(doc, "resources/testreorder_3.xml");
        model.sync();
        assertEquals("Expect a2 is now first", 2, model.getRootComponent().getChildren(A.class).get(0).getIndex());
    }
    
    public void testSyncWithoutProlog() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/noprolog.xml");
        assertEquals("should also include change in prolog", 1, diffs.size());
        
        defaultSetup();
        Util.setDocumentContentTo(doc, "resources/noprolog.xml");
        model.sync();
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) model.getDocument();
        assertEquals("expect resulting document has no prolog", 0, doc.getTokens().size());
    }    

    public void testSyncWithChangedProlog() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_changedProlog.xml");
        assertEquals("should also include change in prolog", 1, diffs.size());

        defaultSetup();
        org.netbeans.modules.xml.xdm.nodes.Document oldDoc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) model.getDocument();
        
        Util.setDocumentContentTo(doc, "resources/test1_changedProlog.xml");
        model.sync();
        
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) model.getDocument();
        assertEquals("expect resulting document has no prolog", 6, doc.getTokens().size());
        String tokens = doc.getTokens().toString();
        assertFalse("prolog should changes: "+tokens, oldDoc.getTokens().toString().equals(tokens));
    }    

    public void testSyncWithChangedPrologAndOthers() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_changedProlog2.xml");
        assertEquals("should also include change in prolog "+diffs, 9, diffs.size());

        defaultSetup();
        org.netbeans.modules.xml.xdm.nodes.Document oldDoc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) model.getDocument();
        
        Util.setDocumentContentTo(doc, "resources/test1_changedProlog2.xml");
        model.sync();
        
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) model.getDocument();
        assertEquals("expect resulting document has no prolog", 6, doc.getTokens().size());
        String tokens = doc.getTokens().toString();
        assertFalse("prolog should changes: "+tokens, oldDoc.getTokens().toString().equals(tokens));
        javax.xml.namespace.QName attr = new javax.xml.namespace.QName("targetNamespace");
        assertEquals("foo", model.getRootComponent().getAnyAttribute(attr));
        assertEquals("b1 should be replaced by b2", 2, model.getRootComponent().getChild(B.class).getIndex());
        assertNull("c1 should be deleted", model.getRootComponent().getChild(C.class));
    }    


    
    private static class Handler implements ComponentListener {
        public void valueChanged(ComponentEvent evt) {
        }
        public void childrenDeleted(ComponentEvent evt) {
        }
        public void childrenAdded(ComponentEvent evt) {
            if (evt.getSource().getClass().isAssignableFrom(TestComponent.class)) {
                D myD = ((TestComponent)evt.getSource()).getChild(D.class);
                myD.appendChild("test", new B(myD.getModel(), 2));
            }
        }
    }
    
    public void testMutationInComponentEventHandler() throws Exception {
        defaultSetup();
        model.addComponentListener(new Handler());
        model.startTransaction();
        model.getRootComponent().appendChild("test", new D(model, 2));
        model.endTransaction();
        model = Util.dumpAndReloadModel(model);
        D d = model.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
    
    public void testUndoRedoOnMutationFromEvent() throws Exception {
        defaultSetup();
        model.addComponentListener(new Handler());
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);

        model.startTransaction();
        model.getRootComponent().appendChild("test", new D(model, 2));
        model.endTransaction();

        um.undo();
        D d = model.getRootComponent().getChild(D.class);
        assertNull(d);
        um.redo();
        d = model.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
    
    public void testXmlContentPropertyChangeEventRemove() throws Exception {
        setUp();
        doc = Util.getResourceAsDocument("resources/testXmlContentEvent.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);

        A a = model.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(doc, "resources/testXmlContentEvent_1.xml");
        model.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(0, now.size());
        assertEquals(1, old.size());
        assertEquals("101", old.get(0).getXmlFragmentText());
        plistener.assertNoEvent(DocumentComponent.TEXT_CONTENT_PROPERTY);
    }

    public void testXmlContentPropertyChangeEventAdd() throws Exception {
        setUp();
        doc = Util.getResourceAsDocument("resources/testXmlContentEvent_1.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);

        A a = model.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(doc, "resources/testXmlContentEvent.xml");
        model.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(0, old.size());
        assertEquals(1, now.size());
        assertEquals("101", now.get(0).getXmlFragmentText());
        plistener.assertNoEvent(DocumentComponent.TEXT_CONTENT_PROPERTY);
    }

    public void testXmlContentPropertyChangeEventChange() throws Exception {
        setUp();
        doc = Util.getResourceAsDocument("resources/testXmlContentEvent.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);

        A a = model.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(doc, "resources/testXmlContentEvent_2.xml");
        model.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(1, now.size());
        assertEquals(1, old.size());
        assertEquals("101", old.get(0).getXmlFragmentText());
        assertEquals("1001", now.get(0).getXmlFragmentText());
        plistener.assertEvent(DocumentComponent.TEXT_CONTENT_PROPERTY, a, " <nondomain>101</nondomain>", " <nondomain>1001</nondomain>");
    }
    
    //////////////////////////////////////////////////////////////
    // The following two tests must be reviewed at a later time //
    //////////////////////////////////////////////////////////////
    
    public void testUndoOnMutationFromSyncEvent() throws Exception {
        defaultSetup();
        model.addComponentListener(new Handler());
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);

        Util.setDocumentContentTo(doc, "resources/test1_2.xml");
        model.sync();
        D d = model.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
        um.undo();
        model.getAccess().flush(); // after fix for 83963 need manual flush after undo/redo

        assertNull(model.getRootComponent().getChild(D.class));
        model = Util.dumpAndReloadModel(model);
        assertNull(model.getRootComponent().getChild(D.class));
    }

    public void testFlushOnMutationFromSyncEvent() throws Exception {
        defaultSetup();
        model.addComponentListener(new Handler());
        Util.setDocumentContentTo(doc, "resources/test1_2.xml");
        model.sync();
        D d = model.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
        
        model = Util.dumpAndReloadModel(model);
        d = model.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
}
