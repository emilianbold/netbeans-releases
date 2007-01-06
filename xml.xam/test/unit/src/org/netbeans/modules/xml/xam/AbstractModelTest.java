package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import junit.framework.*;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.WeakListeners;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractModelTest extends TestCase {
    PropertyListener plistener;
    TestComponentListener listener;
    TestModel model;
    TestComponent root;

    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertEvent(String propertyName, Object old, Object now) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
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
    
        private void assertEvent(ComponentEvent.EventType type, Component source) {
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
        model = new TestModel();
        listener = new TestComponentListener();
        plistener = new PropertyListener();
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
    }

    protected void tearDown() throws Exception {
        model.removePropertyChangeListener(plistener);
        model.removeComponentListener(listener);
    }

    public static Test suite() {
        return new TestSuite(AbstractModelTest.class);
    }
    
    public void testWeakListenerRemoval() throws Exception {
        TestModel model = new TestModel();
        TestComponent root = model.getRootComponent();
        TestComponentListener listener1 = new TestComponentListener();
        TestComponentListener listener2 = new TestComponentListener();
        TestComponentListener listener3 = new TestComponentListener();
        TestComponentListener listener4 = new TestComponentListener();
        TestComponentListener listener5 = new TestComponentListener();
        model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener1, model));
        model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener2, model));
        model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener3, model));
        model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener4, model));
        model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener5, model));
        
        model.startTransaction();
        model.addChildComponent(root, new TestComponent.B(model, 1), -1);
        model.endTransaction();
        
        listener1.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
        listener2.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
        listener3.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
        listener4.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
        listener5.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);

        listener2 = null;
        listener3 = null;
        listener4 = null;
        listener5 = null;
        System.gc();
        Thread.sleep(50);
     
        assertEquals(1, model.getComponentListenerList().getListenerCount());
    }
    
    public void testStateChangeEvent() throws Exception {
        model.startTransaction();
        model.setState(Model.State.NOT_WELL_FORMED);
        model.endTransaction();
        plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_WELL_FORMED);
    }
    
    private class FlushListener implements PropertyChangeListener {
        long flushTime = 0;
        public FlushListener() {
            model.getAccess().addFlushListener(this);
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == model.getAccess() && evt.getPropertyName().equals("flushed")) {
                flushTime = ((Long)evt.getNewValue()).longValue();
            }
        }
        public void assertFlushEvent(long since) {
            assertTrue("Expect flush event after "+since, flushTime >= since);
        }
        public void assertNoFlushEvents(long since) {
            assertTrue("Expect no flush events after "+since, flushTime < since);
        }
    }
    
    public void testReadOnlyTransactionSkipFlush() throws Exception {
        FlushListener list = new FlushListener();
        long since = System.currentTimeMillis();
        model.startTransaction();
        model.endTransaction();
        list.assertNoFlushEvents(since);
    }
    
    public void testWriteTransactionDidFlush() throws Exception {
        FlushListener list = new FlushListener();
        long since = System.currentTimeMillis();
        model.startTransaction();
        model.getRootComponent().setValue("newValue");
        model.endTransaction();
        list.assertFlushEvent(since);
    }
    
    public void testModelFactoryListener() throws Exception {
        TestModel2.factory().addPropertyChangeListener(plistener);
        TestModel2 m = TestModel2.factory().getModel(Util.createModelSource(
                "resources/test1.xml"));
        plistener.assertEvent(TestModel2.factory().MODEL_LOADED_PROPERTY, null, m);
    }
}
