package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel extends AbstractModel<TestComponent> implements Model<TestComponent> {
    TestComponent testRoot;
    TestAccess access;
    
    /** Creates a new instance of TestModel */
    public TestModel() {
        super(createModelSource());
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ModelSource createModelSource() {
        Lookup lookup = Lookups.fixed(new Object[] { } );
        return new ModelSource(lookup, true);
    }
    
    public TestComponent getRootComponent() {
        if (testRoot == null) {
            testRoot = new TestComponent(this, 0);
        }
        return testRoot;
    }

    public void addChildComponent(Component target, Component child, int index) {
        TestComponent parent = (TestComponent) target;
        TestComponent tc = (TestComponent) child;
        parent.insertAtIndex(tc.getName(), tc, index > -1 ? index : parent.getChildren().size());
    }

    public void removeChildComponent(Component child) {
        TestComponent tc = (TestComponent) child;
        tc.getParent().removeChild(tc.getName(), tc);
    }

    
    public TestAccess getAccess() {
        if (access == null) { 
            access = new TestAccess();
        }
        return access;
    }
    
    public static class TestAccess extends ModelAccess {
        PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void removeUndoableEditListener(UndoableEditListener listener) {
            //TODO
        }

        public void addUndoableEditListener(UndoableEditListener listener) {
            //TODO
        }

        public Model.State sync() throws IOException {
            return Model.State.VALID;
        }

        public void prepareForUndoRedo() {
        }

        Long lastFlushTime = null;
        public void flush() {
            Long currentTime = new Long(System.currentTimeMillis());
            pcs.firePropertyChange("flushed", lastFlushTime, currentTime);
            lastFlushTime = currentTime;
        }

        public void finishUndoRedo() {
        }
        
        public void addFlushListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removeFlushListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
    }
}
