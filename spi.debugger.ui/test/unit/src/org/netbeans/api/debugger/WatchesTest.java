/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDebuggerManagerListener;

import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * Tests DebuggerManager's Watches management.
 *
 * @author Maros Sandor
 */
public class WatchesTest extends DebuggerApiTestBase {

    public WatchesTest(String s) {
        super(s);
    }

    public void testWatches() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();

        dm.addDebuggerListener(dml);

        initWatches(dm, dml);
        Watch w1 = addWatch(dm, dml);
        Watch w2 = addWatch(dm, dml);
        Watch w3 = addWatch(dm, dml);
        removeWatch(dm, w2, dml);
        removeWatch(dm, w3, dml);
        Watch w4 = addWatch(dm, dml);
        removeWatch(dm, w1, dml);
        Watch w5 = addWatch(dm, dml);
        removeWatch(dm, w5, dml);
        removeWatch(dm, w4, dml);

        dm.removeDebuggerListener(dml);
    }

    private void initWatches(DebuggerManager dm, TestDebuggerManagerListener dml) {
        dm.getWatches();    // trigger the "watchesInit" property change
        TestDebuggerManagerListener.Event event;
        List events = dml.getEvents();
        assertEquals("Wrong PCS", 1, events.size());
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watchesInit", pce.getPropertyName());
    }

    private void removeWatch(DebuggerManager dm, Watch w, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        Watch [] watches = dm.getWatches();

        dm.removeWatch(w);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchRemoved", w)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] newWatches = dm.getWatches();
        for (int i = 0; i < newWatches.length; i++) {
            assertNotSame("Watch was not removed", newWatches[i], w);
        }
        assertEquals("Wrong number of installed watches", watches.length - 1, newWatches.length);
    }

    private Watch addWatch(DebuggerManager dm, TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;

        int watchesSize = dm.getWatches().length;
        Watch newWatch = dm.createWatch("watch");
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new TestDebuggerManagerListener.Event("watchAdded", newWatch)));
        event = (TestDebuggerManagerListener.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.getName());
        PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
        assertEquals("Wrong PCE name", "watches", pce.getPropertyName());
        Watch [] watches = dm.getWatches();
        assertEquals("Wrong number of installed watches", watchesSize + 1, watches.length);
        return newWatch;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
