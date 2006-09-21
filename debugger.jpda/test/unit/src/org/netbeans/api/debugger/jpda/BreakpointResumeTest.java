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

    private String          sourceRoot = "file://"+System.getProperty ("test.dir.src");

    public BreakpointResumeTest (String s) {
        super (s);
    }

    public void testBreakpointResume () throws Exception {
        JPDASupport support = null;
        JPDASupport.removeAllBreakpoints ();
        try {
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java", 
                36
            );
            lb.addJPDABreakpointListener (new TestBreakpointListener ());
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);

            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
            DebuggerManager.getDebuggerManager ().removeBreakpoint (lb);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
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
