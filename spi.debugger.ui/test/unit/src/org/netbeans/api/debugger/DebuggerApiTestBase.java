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

import org.netbeans.junit.NbTestCase;

import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * A base utility class for debugger unit tests.
 *
 * @author Maros Sandor
 */
public abstract class DebuggerApiTestBase extends NbTestCase {

    protected DebuggerApiTestBase(String s) {
        super(s);
    }

    protected void assertInstanceOf(String msg, Object obj, Class aClass) {
        if (obj.getClass().isAssignableFrom(aClass))
        {
            fail(msg);
        }
    }

    protected static void printEvents(List events) {
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

    class Event {

        String name;
        Object param;

        public Event(String name, Object param) {
            this.name = name;
            this.param = param;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;

            final Event event = (Event) o;

            if (!name.equals(event.name)) return false;
            if (param != null ? !param.equals(event.param) : event.param != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = name.hashCode();
            result = 29 * result + (param != null ? param.hashCode() : 0);
            return result;
        }
    }

    class TestDebuggerManagerListener extends DebuggerManagerAdapter {

        private List events = new ArrayList();

        public List getEvents() {
            List listCopy = new ArrayList(events);
            events.clear();
            return listCopy;
        }

        public void breakpointAdded(Breakpoint breakpoint) {
            events.add(new Event("breakpointAdded", breakpoint));
        }

        public void breakpointRemoved(Breakpoint breakpoint) {
            events.add(new Event("breakpointRemoved", breakpoint));
        }

        public void watchAdded(Watch watch) {
            events.add(new Event("watchAdded", watch));
        }

        public void watchRemoved(Watch watch) {
            events.add(new Event("watchRemoved", watch));
        }

        public void sessionAdded(Session session) {
            events.add(new Event("sessionAdded", session));
        }

        public void sessionRemoved(Session session) {
            events.add(new Event("sessionRemoved", session));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            events.add(new Event("propertyChange", evt));
        }

        public Breakpoint[] initBreakpoints() {
            return new Breakpoint[0];
        }

        public void initWatches() {
        }
    }

    class TestActionsManagerListener implements ActionsManagerListener {

        private List performed = new ArrayList();

        public void actionPerformed(Object action) {
            performed.add(action);
        }

        public void actionStateChanged(Object action, boolean enabled) {
        }

        public List getPerformedActions() {
            List listCopy = new ArrayList(performed);
            performed.clear();
            return listCopy;
        }
    }
}
