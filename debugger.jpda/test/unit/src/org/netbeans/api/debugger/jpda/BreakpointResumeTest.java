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

package org.netbeans.api.debugger.jpda;

import org.netbeans.api.debugger.DebuggerManager;

import java.net.URL;

/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor
 */
public class BreakpointResumeTest  extends DebuggerJPDAApiTestBase {

    private JPDASupport     support;
    private JPDADebugger    debugger;
    private DebuggerManager dm;
    private String          urlString;

    public BreakpointResumeTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dm = DebuggerManager.getDebuggerManager();
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = cl.getResource("org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.class");
        urlString = url.toString();
    }

    public void testBreakpointResume() throws Exception {
        try {
            LineBreakpoint lb = LineBreakpoint.create(urlString, 30);
            TestBreakpointListener tbl = new TestBreakpointListener();
            lb.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(lb);

            support = JPDASupport.listen("org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp", false);
            debugger = support.getDebugger();

            support.waitDisconnected(5000);
            dm.removeBreakpoint(lb);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        public TestBreakpointListener() {
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            event.resume();
        }
    }
}
