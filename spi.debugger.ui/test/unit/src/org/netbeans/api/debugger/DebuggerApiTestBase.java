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
import org.netbeans.api.debugger.test.TestDebuggerManagerListener;

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
            TestDebuggerManagerListener.Event event1 = (TestDebuggerManagerListener.Event) i.next();
            System.out.println("event: " + event1.getName());
            if (event1.getParam() instanceof PropertyChangeEvent) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event1.getParam();
                System.out.println("PCS name: " + pce.getPropertyName());
            }
            System.out.println(event1.getParam());
        }
    }
}
