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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.*;

import java.util.*;
import java.beans.PropertyChangeEvent;

/**
 * A test debugger manager listener implementation.
 *
 * @author Maros Sandor
 */
public class TestDebuggerManagerListener implements DebuggerManagerListener {

    public static class Event {

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

        public String getName() {
            return name;
        }

        public Object getParam() {
            return param;
        }
    }

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
