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

import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebuggerManagerListener;
import org.netbeans.api.debugger.test.TestLazyDebuggerManagerListener;

import java.util.*;
import java.beans.PropertyChangeEvent;

/**
 * Launches and finishes a debugger session. Tests services registration and lookup and event firing.
 *
 * @author Maros Sandor
 */
public class LaunchDebuggerTest extends DebuggerApiTestBase {

    public LaunchDebuggerTest(String s) {
        super(s);
    }

    public void testLookup () throws Exception {

        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        TestLazyDebuggerManagerListener ldml = (TestLazyDebuggerManagerListener) 
            dm.lookupFirst (null, LazyDebuggerManagerListener.class);
        assertNotNull("Lazy debugger manager listener not loaded", ldml);

        Map args = new HashMap();
        TestDICookie tdi = TestDICookie.create(args);

        Object [] services = new Object[] { tdi, this };
        DebuggerInfo di = DebuggerInfo.create(TestDICookie.ID, services);

        DebuggerEngine engines [] = dm.startDebugging(di);
        assertEquals("Wrong number of debugger engines started", engines.length, 1);

        testStartEvents(dml, engines);
        testStartEvents(ldml, engines);

        DebuggerEngine debugger = engines[0];
        DebuggerInfo dic = (DebuggerInfo) debugger.lookupFirst(null, DebuggerInfo.class);
        assertSame("Wrong debugger info in engine lookup", dic, di);
        assertTrue("Engine did not start", tdi.hasInfo(ActionsManager.ACTION_START));

        dm.getCurrentSession().kill();
        assertTrue("Engine did not finish", tdi.hasInfo(ActionsManager.ACTION_KILL));

        testKillEvents(dml);
        testKillEvents(ldml);

        dm.removeDebuggerListener(dml);
    }

    private void testKillEvents(TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionRemoved")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertNull("Bad current engine", pce.getNewValue());
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    assertNull("Bad current session", pce.getNewValue());
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }

    private void testStartEvents(TestDebuggerManagerListener dml, DebuggerEngine[] engines) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionAdded")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertSame("Bad PCE new current engine", pce.getNewValue(), engines[0]);
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }
}
