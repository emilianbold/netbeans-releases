package org.netbeans.modules.xml.xam;

import java.io.IOException;
import javax.swing.event.UndoableEditEvent;
import junit.framework.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.xml.diff.util.Debug;
import org.netbeans.modules.xml.xam.ComponentEvent.EventType;
import org.netbeans.modules.xml.xam.Model.State;

import org.netbeans.modules.xml.xam.TestComponent.A;
import org.netbeans.modules.xml.xam.TestComponent.B;
import org.netbeans.modules.xml.xam.TestComponent.C;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractModelTest extends TestCase {
    TestComponentListener listener;
    TestModel model;
    TestComponent root;
    
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
    }
    
    private void assertEvents(String assertName, List<ComponentEvent.EventType> types, List<Component> sources) {
        List<ComponentEvent> events = listener.getEvents();
        for (ComponentEvent e : events) {
            int i = events.indexOf(e);
            assertEquals(assertName, types.get(i), e.getEventType());
            assertEquals(assertName, sources.get(i), e.getSource());
        }
    }
    
    public AbstractModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        model = Util.loadModel("resources/test1.xml");
        root = TestComponent.class.cast(model.getRootComponent());
        listener = new TestComponentListener();
        model.addComponentListener(listener);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return new TestSuite(AbstractModelTest.class);
    }

    public void testTransactionOnComponentListener() throws Exception {
        assertEquals("testComponentListener.ok", State.VALID, model.getState());
        model.startTransaction();
        A a1 = root.getChild(A.class);
        a1.setValue("testComponentListener.a1");
        B b2 = new B(model, 2);
        root.addAfter(b2.getName(), b2, TestComponent._A);
        C c1 = root.getChild(C.class);
        root.removeChild("testComponentListener.remove.c1", c1);
        assertEquals("testComponentListener.noEventBeforeCommit", 0, listener.getEventCount());
        
        List<ComponentEvent.EventType> types = new ArrayList<ComponentEvent.EventType>();
        types.add(EventType.ATTRIBUTE); types.add(EventType.ATTRIBUTE);
        types.add(EventType.CHILD_ADDED);
        types.add(EventType.CHILD_REMOVED);
        List<Component> sources = new ArrayList<Component>();
        sources.add(a1); sources.add(b2); sources.add(root); sources.add(root);
        model.endTransaction();
        assertEquals("testComponentListener.noEventBeforeCommit", 4, listener.getEventCount());
        assertEvents("testComponentListener.afterCommit", types, sources);
    }
    
    public void notestStateTransition() throws Exception {
        TestModel mod = Util.loadModel("resources/test1.xml");
        assertEquals("testState.invalid", State.VALID, mod.getState());

        IOException expected = null;
        Util.setDocumentContentTo(mod.getBaseDocument(), "resources/Bad.xml");
        try {
            mod.sync();
        } catch (IOException io) {
            expected = io;
        }
        assertNotNull("Expected IOException", expected);
        assertEquals("Expected state not well-formed", State.NOT_WELL_FORMED, mod.getState());
        
        Util.setDocumentContentTo(mod.getBaseDocument(), "resources/test1.xml");
        mod.sync();
        assertEquals("testState.valid", State.VALID, mod.getState());
    }
    
    public void testUndoRedo() throws Exception {
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
		
        //setup
        model.startTransaction();
        A a1 = root.getChild(A.class);
        String v = "testComponentListener.a1";
        a1.setValue(v);
        model.endTransaction();	
        assertEquals("edit #1: initial set a1 attribute 'value'", v, a1.getAttribute(TestAttribute.VALUE));

        urListener.undo();
        String val = a1.getAttribute(TestAttribute.VALUE);
        //assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        urListener.redo();
        
        model.startTransaction();
        B b2 = new B(model, 2);
        root.addAfter(b2.getName(), b2, TestComponent._A);
        model.endTransaction();		
        assertEquals("edit #2: insert b2", 2, root.getChildren(B.class).size());
        
        model.startTransaction();
        C c1 = root.getChild(C.class);
        root.removeChild("testComponentListener.remove.c1", c1);
        model.endTransaction();		
        assertNull("edit #3: remove c1", root.getChild(C.class));
        
        urListener.undo();
        c1 = root.getChild(C.class);	
        assertEquals("undo edit #3", 1, c1.getIndex());

        urListener.redo();			
        assertNull("redo edit #3", root.getChild(C.class));
        
        urListener.undo();	
        assertEquals("undo edit #3 after redo", 1, root.getChildren(C.class).size());
        urListener.undo();		
        assertEquals("undo edit #2", 1, root.getChildren(B.class).size());
        urListener.undo();
        a1 = root.getChild(A.class);
        val = a1.getAttribute(TestAttribute.VALUE);
        
        //FIXME: 
        // seems to be problem with Attribute's have id collision.			
        //assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        
        urListener.redo();
        urListener.redo();
        assertEquals("redo edit #1 and #2", 2, root.getChildren(B.class).size());
        assertEquals("testUndo.1", 1, root.getChildren(C.class).size());
    }
    
    public void notestSourceEditSyncUndo() throws Exception {
        UndoManager urListener = new UndoManager();
        model.addUndoableEditListener(urListener);
        
        model.startTransaction();
        B b2 = new B(model, 2);
        root.addAfter(b2.getName(), b2, TestComponent._A);
        model.endTransaction();		
        assertEquals("first edit setup", 2, root.getChildren(B.class).size());
        
        Util.setDocumentContentTo(model.getBaseDocument(), "resources/test2.xml");
        model.sync();
        
        assertEquals("sync setup", 1, root.getChildren(B.class).size());
        assertEquals("sync setup", 0, root.getChildren(C.class).size());
        
        urListener.undo();
        assertEquals("undo sync", 1, root.getChildren(C.class).size());
        assertEquals("undo sync", 2, root.getChildren(B.class).size());

        urListener.undo();
        assertEquals("undo first edit before sync", 1, root.getChildren(B.class).size());

        urListener.redo();
        assertEquals("redo first edit", 1, root.getChildren(C.class).size());
        assertEquals("redo first edit", 2, root.getChildren(B.class).size());

        urListener.redo();
        assertEquals("redo to sync", 1, root.getChildren(B.class).size());
        assertEquals("redo to sync", 0, root.getChildren(C.class).size());
    }
}
