package org.netbeans.modules.xml.xam;

import org.netbeans.modules.xml.xam.ComponentUpdater;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponentUpdater implements ComponentUpdater<TestComponent> {
    public void update(TestComponent target, TestComponent child, ComponentUpdater.Operation operation) {
        update(target, child, -1, operation);
    }

    public void update(TestComponent target, TestComponent child, int index, ComponentUpdater.Operation operation) {
        if (operation.equals(ComponentUpdater.Operation.ADD)) {
            target.insertAtIndex("ChildComponentAdded", child, index, TestComponent.class);
        } else {
            target.removeChild("ChildComponentRemoved", child);
        }
    }
}
