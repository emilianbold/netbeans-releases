/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    // TODO: Include check of these call in the test suite
    public void engineAdded(DebuggerEngine engine) {
    }

    // TODO: Include check of these call in the test suite
    public void engineRemoved(DebuggerEngine engine) {
    }
}
