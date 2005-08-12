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
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;


/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class BreakpointResumeTest  extends NbTestCase {

    private String          sourceRoot = System.getProperty ("test.dir.src");

    public BreakpointResumeTest (String s) {
        super (s);
    }

    public void testBreakpointResume () throws Exception {
        JPDASupport support = null;
        JPDASupport.removeAllBreakpoints ();
        try {
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java", 
                30
            );
            lb.addJPDABreakpointListener (new TestBreakpointListener ());
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);

            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
            DebuggerManager.getDebuggerManager ().removeBreakpoint (lb);
        } finally {
            support.doFinish ();
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
