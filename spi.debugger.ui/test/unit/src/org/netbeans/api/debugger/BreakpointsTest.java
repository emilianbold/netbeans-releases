/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Micro//S ystems, Inc. Portions Copyright 1997-2001 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 *
 * @author Maros Sandor
 */
public class BreakpointsTest extends DebuggerApiTestBase {

    public BreakpointsTest(String s) {
        super(s);
    }

    public void testBreakpoints() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestBreakpoint tb = new TestBreakpoint();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        initBreakpoints(dm, dml);
        addBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        addBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);
        removeBreakpoint(dm, tb, dml);

        dm.removeDebuggerListener(dml);
    }

    private void initBreakpoints(DebuggerManager dm, DebuggerApiTestBase.TestDebuggerManagerListener dml) {
        dm.getBreakpoints();    // trigger the "breakpointsInit" property change
        DebuggerApiTestBase.Event event;
        List events = dml.getEvents();
        assertEquals("Wrong PCS", 1, events.size());
        event = (DebuggerApiTestBase.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.name);
        PropertyChangeEvent pce = (PropertyChangeEvent) event.param;
        assertEquals("Wrong PCE name", "breakpointsInit", pce.getPropertyName());
    }

    private void printEvents(List events) {
        System.out.println("events: " + events.size());
        for (Iterator i = events.iterator(); i.hasNext();) {
            DebuggerApiTestBase.Event event1 = (DebuggerApiTestBase.Event) i.next();
            System.out.println("event: " + event1.name);
            if (event1.param instanceof PropertyChangeEvent) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event1.param;
                System.out.println("PCS name: " + pce.getPropertyName());
            }
            System.out.println(event1.param);
        }
    }

    private void removeBreakpoint(DebuggerManager dm, TestBreakpoint tb, DebuggerApiTestBase.TestDebuggerManagerListener dml) {
        List events;
        DebuggerApiTestBase.Event event;
        Breakpoint [] bpts;

        int bptSize = dm.getBreakpoints().length;
        dm.removeBreakpoint(tb);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new DebuggerApiTestBase.Event("breakpointRemoved", tb)));
        event = (DebuggerApiTestBase.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.name);
        PropertyChangeEvent pce = (PropertyChangeEvent) event.param;
        assertEquals("Wrong PCE name", "breakpoints", pce.getPropertyName());
        bpts = dm.getBreakpoints();
        assertEquals("Wrong number of installed breakpoionts", bptSize - 1, bpts.length);
    }

    private void addBreakpoint(DebuggerManager dm, TestBreakpoint tb, DebuggerApiTestBase.TestDebuggerManagerListener dml) {
        List events;
        DebuggerApiTestBase.Event event;
        Breakpoint [] bpts;

        int bptSize = dm.getBreakpoints().length;
        dm.addBreakpoint(tb);
        events = dml.getEvents();
        assertEquals("Wrong PCS", 2, events.size());
        assertTrue("Wrong PCS", events.remove(new DebuggerApiTestBase.Event("breakpointAdded", tb)));
        event = (DebuggerApiTestBase.Event) events.get(0);
        assertEquals("Wrong PCS", "propertyChange", event.name);
        PropertyChangeEvent pce = (PropertyChangeEvent) event.param;
        assertEquals("Wrong PCE name", "breakpoints", pce.getPropertyName());
        bpts = dm.getBreakpoints();
        assertEquals("Wrong number of installed breakpoints", bptSize + 1, bpts.length);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    class TestBreakpoint extends Breakpoint
    {
        private boolean isEnabled;

        public boolean isEnabled() {
            return isEnabled;
        }

        public void disable() {
            isEnabled = false;
        }

        public void enable() {
            isEnabled = true;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
    }
}
